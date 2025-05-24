package nl.merijntieltjes.btserial.navigation.routes

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import nl.merijntieltjes.btserial.feature.bondeddevice.BluetoothDevicesScreen
import nl.merijntieltjes.btserial.feature.bondeddevice.BluetoothViewModel

@Serializable
data object DevicesRoute

fun NavController.navigateToDevicesRoute() {
    navigate(DevicesRoute)
}

fun NavGraphBuilder.devicesRoute(
    onDeviceSelected: (BluetoothDevice) -> Unit
) {
    composable<DevicesRoute> {
        val viewModel = remember { BluetoothViewModel() }
        val devices by viewModel.devices.collectAsStateWithLifecycle()

        BluetoothDevicesScreen(devices, onDeviceSelected)
    }
}