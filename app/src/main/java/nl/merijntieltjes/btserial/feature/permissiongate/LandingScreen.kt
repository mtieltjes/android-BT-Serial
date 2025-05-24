@file:OptIn(ExperimentalPermissionsApi::class)

package nl.merijntieltjes.btserial.feature.permissiongate

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Composable
fun LandingScreen(
    /* Call when everything is granted so the app can move on */
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    val bluetoothPermission: String = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_CONNECT
        } else {
            Manifest.permission.BLUETOOTH
        }
    }
    val permissionState = rememberMultiplePermissionsState(listOf(bluetoothPermission))

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) onPermissionGranted()
    }

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    when {
        permissionState.allPermissionsGranted -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        permissionState.shouldShowRationale -> PermissionRationale { permissionState.launchMultiplePermissionRequest() }
        else -> PermissionPermanentlyDenied {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    setData(Uri.fromParts("package", context.packageName, null))
                }
            )
        }
    }
}

@Composable
private fun PermissionRationale(onRequest: () -> Unit) = Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        "For this app to set up a serial bluetooth connection, access to bluetooth devices is required.",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge
    )
    Spacer(Modifier.height(16.dp))
    Button(onClick = onRequest) { Text("Grant permission") }
}

@Composable
private fun PermissionPermanentlyDenied(onOpenSettings: () -> Unit) = Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        "This app requires bluetooth permission to work. Allow bluetooth access through the settings.",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge
    )
    Spacer(Modifier.height(16.dp))
    Button(onClick = onOpenSettings) { Text("Open settings") }
}