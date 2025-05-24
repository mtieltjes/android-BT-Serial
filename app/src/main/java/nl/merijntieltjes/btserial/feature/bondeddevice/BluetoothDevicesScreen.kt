package nl.merijntieltjes.btserial.feature.bondeddevice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@SuppressLint("MissingPermission")
@Composable
fun BluetoothDevicesScreen(
    devices: List<BluetoothDevice>,
    onDeviceSelected: (BluetoothDevice) -> Unit,
) {
    LazyColumn {
        items(devices) { device ->
            Text(
                text = device.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDeviceSelected(device) }
                    .padding(16.dp)
            )
        }
    }
}
