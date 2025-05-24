package nl.merijntieltjes.btserial.navigation.routes

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import nl.merijntieltjes.btserial.feature.permissiongate.LandingScreen

@Serializable
data object PermissionGateRoute

fun NavGraphBuilder.permissionGateRoute(
    onPermissionGranted: () -> Unit
) {
    composable<PermissionGateRoute> {
        LandingScreen(onPermissionGranted)
    }
}