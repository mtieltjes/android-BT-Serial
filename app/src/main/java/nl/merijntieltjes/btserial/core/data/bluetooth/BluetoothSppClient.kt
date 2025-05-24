package nl.merijntieltjes.btserial.core.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nl.merijntieltjes.btserial.core.domain.model.ConnectionStatus
import java.io.IOException
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.coroutineContext

@SuppressLint("MissingPermission")
class BluetoothSppClient(
    private val device: BluetoothDevice,
    private val onMessageReceived: (String) -> Unit,
    private val onStatusChanged: (ConnectionStatus) -> Unit,
    private val onError: (Throwable) -> Unit,
    parentScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate)
) {

    companion object {
        private val SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    // Create a scope that auto-cancels when the parent scope is cancelled
    private val scope = parentScope + SupervisorJob()
    private val io = Dispatchers.IO

    @Volatile
    private var socket: BluetoothSocket? = null

    private val isClosed = AtomicBoolean(false)
    private val connectCalled = AtomicBoolean(false)

    private var readerJob: Job? = null

    // Create a lock that serializes the connect/disconnect
    private val mutex = Mutex()

    /**
     * Establishes the RFCOMM/SPP connection once. Do not reuse
     */
    suspend fun connect(): Unit = mutex.withLock {
        if (connectCalled.getAndSet(true)) {
            onError(IllegalStateException("connect() may only be called once per instance"))
            return
        }

        onStatusChanged(ConnectionStatus.CONNECTING)

        try {
            socket = withContext(io) {
                // Reflection because createInsecureRfcommSocketToServiceRecord()
                // still causes pairing dialogs on older devices.
                /*
                val m = device.javaClass.getMethod(
                    "createInsecureRfcommSocket",
                    Int::class.javaPrimitiveType
                )
                (m.invoke(device, 1) as BluetoothSocket).apply { connect() }
                 */

                device.createRfcommSocketToServiceRecord(SPP_UUID).apply { connect() }
            }

            onStatusChanged(ConnectionStatus.CONNECTED)

            // Begin async reads
            readerJob = scope.launch(io) { readLoop(socket!!) }
        } catch (e: IOException) {
            if (!isClosed.getAndSet(true)) {
                onError(e)
                internalClose()
            }
        }
    }

    /**
     * Writes a message on the RFCOMM link.
     * Fire-and-forget: errors are pushed to onError.
     */
    suspend fun sendMessage(message: String) {
        if (isClosed.get()) return

        withContext(io) {
            onMessageReceived("Out: $message")
            try {
                socket?.outputStream?.write(message.toByteArray())
            } catch (e: IOException) {
                onError(e)
            }
        }
    }

    /**
     * Graceful, deliberate shutdown. Safe to call multiple times.
     */
    suspend fun close(): Unit = mutex.withLock {
        // Skip if already closed
        if (isClosed.getAndSet(true)) return
        onStatusChanged(ConnectionStatus.DISCONNECTING)
        withContext(io) {
            internalClose()
        }
    }

    private suspend fun readLoop(sock: BluetoothSocket) {
        val buffer = ByteArray(1024)

        try {
            val input = sock.inputStream
            while (coroutineContext.isActive && !isClosed.get()) {
                // Read on the IO dispatcher
                val read = withContext(io) {
                    input.read(buffer)
                }

                if (read > 0) {
                    onMessageReceived(String(buffer, 0, read))
                }
            }
        } catch (e: IOException) {
            if (!isClosed.getAndSet(true)) { // Unexpected closure of the socket
                onError(e)
                internalClose()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun internalClose() {
        readerJob = null

        withContext(io) {
            try {
                socket?.close()
            } catch (e: IOException) {
                // Notify when closing failed
                onError(e)
            } finally {
                socket = null
            }
        }

        onStatusChanged(ConnectionStatus.DISCONNECTED)
    }
}