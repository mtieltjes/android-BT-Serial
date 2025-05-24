package nl.merijntieltjes.btserial.navigation.routes

import android.bluetooth.BluetoothManager
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import nl.merijntieltjes.btserial.feature.monitor.SppMonitorScreen
import nl.merijntieltjes.btserial.feature.monitor.SppMonitorViewModel

@Serializable
data class SppMonitorRoute(val deviceAddress: String)

fun NavController.navigateToSppMonitorRoute(deviceAddress: String) {
    navigate(SppMonitorRoute(deviceAddress))
}

fun NavGraphBuilder.sppMonitorRoute() {
    composable<SppMonitorRoute> { backStackEntry ->
        val route: SppMonitorRoute = backStackEntry.toRoute()
        val context = LocalContext.current
        val device =
            (context.getSystemService(BluetoothManager::class.java) as BluetoothManager).adapter.getRemoteDevice(
                route.deviceAddress
            )

        val viewModel: SppMonitorViewModel = viewModel(
            backStackEntry,
            factory = SppMonitorViewModel.Factory(device)
        )
        SppMonitorScreen(viewModel)
    }
}