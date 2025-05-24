package nl.merijntieltjes.btserial.feature.monitor

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.merijntieltjes.btserial.core.data.bluetooth.BluetoothSppClient
import nl.merijntieltjes.btserial.core.domain.model.ConnectionStatus

class SppMonitorViewModel(
    private val device: BluetoothDevice,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<SppMonitorMessage>>(emptyList())
    val messages: StateFlow<List<SppMonitorMessage>> = _messages

    private val _connectionStatus= MutableStateFlow<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus
        .distinctUntilChanged { old, new -> old == new }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConnectionStatus.DISCONNECTED)

    @SuppressLint("MissingPermission")
    private val _deviceName = MutableStateFlow(device.name ?: "Unknown")
    val deviceName: StateFlow<String> = _deviceName

    private var client: BluetoothSppClient? = null

    fun connect() {
        viewModelScope.launch {
            if (client != null) {
                client?.close()
                client = null
            }

            client = BluetoothSppClient(
                device,
                onMessageReceived = { msg ->
                    _messages.update {
                        it + SppMonitorMessage.Message(
                            msg,
                            outgoing = false
                        )
                    }
                },
                onStatusChanged = { s ->
                    _connectionStatus.value = s
                    _messages.update {
                        val message = when(s) {
                            ConnectionStatus.CONNECTED -> "Connected"
                            ConnectionStatus.CONNECTING -> "Connecting..."
                            ConnectionStatus.DISCONNECTED -> "Disconnected"
                            ConnectionStatus.DISCONNECTING -> "Disconnecting..."
                        }
                        it + SppMonitorMessage.ConnectionUpdate(message)
                    }
                },
                onError = { e ->
                    _messages.update {
                        it + SppMonitorMessage.Error(
                            e.message ?: "Unknown error", e
                        )
                    }
                },
                parentScope = viewModelScope
            )

            client?.connect()
        }
    }

    fun send(text: String) {
        viewModelScope.launch {
            _messages.update { it + SppMonitorMessage.Message(text, outgoing = true) }
            client?.sendMessage(text)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            client?.close()
            client = null
        }
    }

    fun clearMessages() {
        _messages.value = emptyList<SppMonitorMessage>()
    }

    override fun onCleared() {
        // Auto-cancels everything because parentScope was viewModelScope,
        // but we call close() anyway for deterministic shutdown
        disconnect()
    }

    companion object {
        val Factory: (BluetoothDevice) -> ViewModelProvider.Factory = { device ->
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return SppMonitorViewModel(device) as T
                }
            }
        }
    }
}

sealed interface SppMonitorMessage {
    val text: String

    data class Message(
        override val text: String,
        val outgoing: Boolean,
    ) : SppMonitorMessage

    data class Error(
        override val text: String,
        val throwable: Throwable
    ) : SppMonitorMessage

    data class ConnectionUpdate(
        override val text: String
    ) : SppMonitorMessage
}