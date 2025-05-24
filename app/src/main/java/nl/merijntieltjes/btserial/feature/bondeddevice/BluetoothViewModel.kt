package nl.merijntieltjes.btserial.feature.bondeddevice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class BluetoothViewModel : ViewModel() {
    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices

    init {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        viewModelScope.launch(Dispatchers.Default) {
            _devices.value = adapter?.bondedDevices?.toList() ?: emptyList()
        }
    }
}