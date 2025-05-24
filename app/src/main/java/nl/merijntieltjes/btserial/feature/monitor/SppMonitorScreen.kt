package nl.merijntieltjes.btserial.feature.monitor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.merijntieltjes.btserial.core.domain.model.ConnectionStatus
import nl.merijntieltjes.btserial.core.ui.theme.BluetoothSerialSampleTheme

@Composable
fun SppMonitorScreen(
    viewModel: SppMonitorViewModel
) {
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val deviceName by viewModel.deviceName.collectAsStateWithLifecycle()

    BluetoothSerialSampleTheme {
        SppMonitorScreen(
            deviceName = deviceName,
            connectionStatus = connectionStatus,
            messages = messages,
            onSendMessage = viewModel::send,
            onConnect = viewModel::connect,
            onDisconnect = viewModel::disconnect,
            onClearMessages = viewModel::clearMessages
        )
    }
}

@Composable
private fun SppMonitorScreen(
    deviceName: String,
    connectionStatus: ConnectionStatus,
    messages: List<SppMonitorMessage>,
    onSendMessage: (String) -> Unit,
    onDisconnect: () -> Unit,
    onConnect: () -> Unit,
    onClearMessages: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp)
    ) {
        ConnectionHeader(
            deviceName = deviceName,
            btConnected = true,
            serialConnectionStatus = connectionStatus,
            onConnectClick = onConnect,
            onDisconnectClick = onDisconnect,
        )

        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
                .weight(1f)
                .border(width = 1.dp, color = MaterialTheme.colorScheme.secondary)
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    when (message) {
                        is SppMonitorMessage.ConnectionUpdate -> ConnectionMessage(
                            message.text,
                            connectionStatus
                        )

                        is SppMonitorMessage.Error -> ErrorMessage("${message.throwable.javaClass.name}: ${message.text}")
                        is SppMonitorMessage.Message -> TextMessage(
                            message.text,
                            outgoing = message.outgoing
                        )
                    }
                }
            }
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                onClick = onClearMessages,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear messages"
                )
            }
        }

        var input by remember { mutableStateOf("") }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                placeholder = { Text(text = "Type a message...") },
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f)
            )

            Button(
                enabled = input.isNotBlank(),
                onClick = {
                    onSendMessage(input)
                    input = ""
                }) {
                Text("Send")
            }
        }
    }
}

@Composable
private fun TextMessage(
    text: String,
    outgoing: Boolean,
) = Row(
    modifier = Modifier
        .fillMaxWidth(),
    horizontalArrangement = if (outgoing) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.Top,
) {
    Text(text = text)
}

@Composable
private fun ErrorMessage(
    text: String
) = Row(
    modifier = Modifier
        .fillMaxWidth(),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.Top,
) {
    Text(
        text = text,
        color = Color.Red
    )
}

@Composable
private fun ConnectionMessage(
    text: String,
    connectionStatus: ConnectionStatus,
) = Row(
    modifier = Modifier
        .fillMaxWidth(),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.Top,
) {
    Column {
        Text(
            text = text,
            color = Color.Blue
        )
        if (text == "Disconnected") {
            Text(
                text = "Make sure the device is connected  before attempting a new serial connection",
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ConnectionHeader(
    deviceName: String,
    btConnected: Boolean,
    serialConnectionStatus: ConnectionStatus,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    val btnLabel = when (serialConnectionStatus) {
        ConnectionStatus.CONNECTED -> "Disconnect"
        ConnectionStatus.CONNECTING -> "Connecting"
        ConnectionStatus.DISCONNECTED -> "Connect"
        ConnectionStatus.DISCONNECTING -> "Disconnecting"
    }
    val btnAction: () -> Unit = when (serialConnectionStatus) {
        ConnectionStatus.CONNECTED -> onDisconnectClick
        ConnectionStatus.DISCONNECTED -> onConnectClick
        else -> {
            {}
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {

            /* Bluetooth connection */
            Text(
                text = deviceName,
                style = MaterialTheme.typography.bodySmall
            )
//            StatusLine(
//                label = deviceName,
//                connectionStatus = if (btConnected) ConnectionStatus.CONNECTED else ConnectionStatus.DISCONNECTED
//            )

            Spacer(Modifier.height(4.dp))

            /* Serial connection */
            StatusLine(
                label = "Serial connection: ${serialConnectionStatus.name}",
                connectionStatus = serialConnectionStatus
            )
        }

        /*  Connect / Disconnect  */
        Button(
            onClick = btnAction,
            modifier = Modifier.padding(start = 8.dp),
            enabled = serialConnectionStatus == ConnectionStatus.CONNECTED || serialConnectionStatus == ConnectionStatus.DISCONNECTED,
        ) {
            Text(btnLabel)
        }
    }
}

@Composable
private fun StatusLine(label: String, connectionStatus: ConnectionStatus) {
    val dotColor = when (connectionStatus) {
        ConnectionStatus.CONNECTED -> Color(0xFF1EB980)
        ConnectionStatus.DISCONNECTED -> Color(0xFFF44336)
        else -> Color(0xFFFF9800)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = dotColor,
            shape = CircleShape,
            modifier = Modifier
                .size(12.dp)                // diameter of the status dot
        ) {}

        Spacer(Modifier.width(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview
@Composable
fun PreviewSppMonitorScreen() {
    BluetoothSerialSampleTheme {
        SppMonitorScreen(
            deviceName = "My Bluetooth Device",
            connectionStatus = ConnectionStatus.DISCONNECTED,
            messages = listOf(
                SppMonitorMessage.ConnectionUpdate("Connecting"),
                SppMonitorMessage.ConnectionUpdate("Connected"),
                SppMonitorMessage.Message("+PTT=P", false),
                SppMonitorMessage.Message("+PTT=R", false),
                SppMonitorMessage.Message("AT+STATUS", true),
                SppMonitorMessage.Message("+PTT=P", false),
                SppMonitorMessage.Message("+PTT=R", false),
                SppMonitorMessage.Error("An unknown error occured", Exception("Unknown error")),
                SppMonitorMessage.ConnectionUpdate("Disconnected"),
            ),
            onSendMessage = {},
            onConnect = {},
            onDisconnect = {},
            onClearMessages = {},
        )
    }
}
