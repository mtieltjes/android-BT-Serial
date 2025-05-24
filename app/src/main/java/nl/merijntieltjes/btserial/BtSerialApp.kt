package nl.merijntieltjes.btserial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import nl.merijntieltjes.btserial.navigation.AppNavGraph
import nl.merijntieltjes.btserial.core.ui.theme.BluetoothSerialSampleTheme

@Composable
fun BtSerialApp(): Unit = BluetoothSerialSampleTheme {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        val navController = rememberNavController()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            AppNavGraph(navController = navController)
        }
    }
}