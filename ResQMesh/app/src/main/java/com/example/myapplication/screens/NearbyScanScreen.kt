package com.example.myapplication.screens

import android.Manifest
import android.bluetooth.*
import android.content.*
import android.net.wifi.p2p.*
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission

sealed class NearbyDevice {
    data class Bluetooth(val name: String, val address: String) : NearbyDevice()
    data class Wifi(val name: String, val address: String) : NearbyDevice()
    data class LoRa(val id: String) : NearbyDevice()
}

private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScanScreen() {
    val context = LocalContext.current
    val allDevices = remember { mutableStateListOf<NearbyDevice>() }
    var statusText by remember { mutableStateOf("Requesting permissions...") }

    val bluetoothAdapter: BluetoothAdapter? = remember {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    val wifiManager = remember {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }
    val wifiChannel = remember {
        wifiManager.initialize(context, context.mainLooper, null)
    }

    // Permissions
    val requiredPermissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_ADMIN)
        }
    }.toTypedArray()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            Log.d("NEARBY_SCAN", "All permissions granted.")
            startBluetoothScan(bluetoothAdapter, context, allDevices, requiredPermissions) { statusText = it }
            startWifiScan(wifiManager, wifiChannel, context, allDevices)
            addMockLoRa(allDevices)
        } else {
            statusText = "Permissions denied. Cannot scan."
        }
    }

    // Receiver for Bluetooth
    val bluetoothReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    else
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    if (device?.name != null && allDevices.none { it is NearbyDevice.Bluetooth && it.address == device.address }) {
                        allDevices.add(NearbyDevice.Bluetooth(device.name, device.address))
                        Log.d("BT_SCAN", "Found BT: ${device.name}")
                    }
                }
            }
        }
    }

    // Receiver for Wi-Fi P2P
    val wifiReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION) {
                    wifiManager.requestPeers(wifiChannel) { peerList ->
                        peerList.deviceList.forEach { device ->
                            if (allDevices.none { it is NearbyDevice.Wifi && it.address == device.deviceAddress }) {
                                allDevices.add(NearbyDevice.Wifi(device.deviceName ?: "Unnamed", device.deviceAddress))
                                Log.d("WIFI_SCAN", "Found WiFi: ${device.deviceName}")
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val btFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val wifiFilter = IntentFilter(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        context.registerReceiver(bluetoothReceiver, btFilter)
        context.registerReceiver(wifiReceiver, wifiFilter)
        onDispose {
            context.unregisterReceiver(bluetoothReceiver)
            context.unregisterReceiver(wifiReceiver)
        }
    }

    LaunchedEffect(Unit) {
        if (hasPermissions(context, *requiredPermissions)) {
            startBluetoothScan(bluetoothAdapter, context, allDevices, requiredPermissions) { statusText = it }
            startWifiScan(wifiManager, wifiChannel, context, allDevices)
            addMockLoRa(allDevices)
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nearby Devices") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(statusText, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            if (statusText.contains("Scanning", true)) {
                CircularProgressIndicator()
            }

            LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                items(allDevices.distinctBy {
                    when (it) {
                        is NearbyDevice.Bluetooth -> it.address
                        is NearbyDevice.Wifi -> it.address
                        is NearbyDevice.LoRa -> it.id
                    }
                }) { device ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val label = when (device) {
                                is NearbyDevice.Bluetooth -> "🔵 Bluetooth"
                                is NearbyDevice.Wifi -> "📶 Wi-Fi Direct"
                                is NearbyDevice.LoRa -> "📡 LoRa"
                            }
                            val title = when (device) {
                                is NearbyDevice.Bluetooth -> device.name
                                is NearbyDevice.Wifi -> device.name
                                is NearbyDevice.LoRa -> device.id
                            }
                            val sub = when (device) {
                                is NearbyDevice.Bluetooth -> device.address
                                is NearbyDevice.Wifi -> device.address
                                is NearbyDevice.LoRa -> "Simulated"
                            }
                            Text("$label: $title", style = MaterialTheme.typography.titleMedium)
                            Text(sub, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// --- Bluetooth Scanner ---
private fun startBluetoothScan(
    bluetoothAdapter: BluetoothAdapter?,
    context: Context,
    devices: MutableList<NearbyDevice>,
    permissions: Array<String>,
    onStatusUpdate: (String) -> Unit
) {
    if (!hasPermissions(context, *permissions)) {
        onStatusUpdate("Missing permissions for Bluetooth.")
        return
    }
    if (bluetoothAdapter?.isEnabled != true) {
        onStatusUpdate("Bluetooth is OFF.")
        return
    }
    try {
        if (bluetoothAdapter.isDiscovering) bluetoothAdapter.cancelDiscovery()
        val started = bluetoothAdapter.startDiscovery()
        onStatusUpdate(if (started) "Scanning for Bluetooth..." else "BT scan failed to start.")
    } catch (e: Exception) {
        Log.e("BT_SCAN", "Discovery failed", e)
        onStatusUpdate("BT discovery error.")
    }
}

// --- Wi-Fi Direct Scanner ---
@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
private fun startWifiScan(
    manager: WifiP2pManager,
    channel: WifiP2pManager.Channel,
    context: Context,
    devices: MutableList<NearbyDevice>
) {
    manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
            Log.d("WIFI_SCAN", "Wi-Fi P2P discovery started")
        }

        override fun onFailure(reason: Int) {
            Log.e("WIFI_SCAN", "Wi-Fi discovery failed: $reason")
        }
    })
}

// --- Simulate LoRa ---
private fun addMockLoRa(devices: MutableList<NearbyDevice>) {
    devices.add(NearbyDevice.LoRa("LoRa_Node_01"))
    devices.add(NearbyDevice.LoRa("LoRa_Node_02"))
}