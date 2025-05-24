package nl.merijntieltjes.btserial.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import nl.merijntieltjes.btserial.navigation.routes.PermissionGateRoute
import nl.merijntieltjes.btserial.navigation.routes.devicesRoute
import nl.merijntieltjes.btserial.navigation.routes.permissionGateRoute
import nl.merijntieltjes.btserial.navigation.routes.navigateToDevicesRoute
import nl.merijntieltjes.btserial.navigation.routes.navigateToSppMonitorRoute
import nl.merijntieltjes.btserial.navigation.routes.sppMonitorRoute

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = PermissionGateRoute) {
        permissionGateRoute {
            navController.navigateToDevicesRoute()
        }

        devicesRoute { device ->
            navController.navigateToSppMonitorRoute(device.address)
        }

        sppMonitorRoute()
    }
}