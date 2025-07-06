package com.example.myapplication.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.DetectedUser
import com.example.myapplication.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(viewModel: MapsViewModel = viewModel()) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var detectionRange by remember { mutableFloatStateOf(5.0f) }
    var showNotification by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<DetectedUser?>(null) }

    val currentLocation = viewModel.currentLocation.value
    val detectedUsers = viewModel.users.value

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.fetchCurrentLocation(context) // ✅ FIXED here
            }
        }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            // ✅ update location every 10 sec
            while (true) {
                viewModel.fetchCurrentLocation(context) // ✅ FIXED here
                delay(10_000)
            }

        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val usersInRange = remember(detectedUsers, currentLocation, detectionRange) {
        detectedUsers.filter { user ->
            calculateDistance(currentLocation, user.position) <= detectionRange
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Response") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD32F2F),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showNotification = true }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    detectionRange = if (detectionRange == 5.0f) 10.0f else 5.0f
                },
                containerColor = Color(0xFFD32F2F)
            ) {
                Icon(Icons.Default.Radar, contentDescription = "Toggle Range", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            StatusBar(survivorCount = usersInRange.size, detectionRange = detectionRange)
            if (currentLocation.latitude != 0.0 && currentLocation.longitude != 0.0) {
                GoogleMapView(
                    modifier = Modifier.weight(1f),
                    currentLocation = currentLocation,
                    detectedUsers = usersInRange,
                    detectionRange = detectionRange,
                    onUserSelected = { selectedUser = it }
                )
            } else {
                // Optional: Show loading or placeholder
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            BottomUserList(users = usersInRange, onUserClick = { selectedUser = it })
        }
    }

    if (showNotification) {
        LaunchedEffect(showNotification) {
            kotlinx.coroutines.delay(3000)
            showNotification = false
        }
    }

    selectedUser?.let { user ->
        UserDetailsBottomSheet(user = user, onDismiss = { selectedUser = null }, currentLocation = currentLocation)
    }
}

@Composable
fun StatusBar(survivorCount: Int, detectionRange: Float) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Survivors Detected: $survivorCount", fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Range: ${detectionRange}km", fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    currentLocation: LatLng,
    detectedUsers: List<DetectedUser>,
    detectionRange: Float,
    onUserSelected: (DetectedUser) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 14f)
    }

    GoogleMap(modifier = modifier, cameraPositionState = cameraPositionState) {
       // Marker(
           // state = MarkerState(position = currentLocation),
           // title = "Your Location",
           // snippet = "Emergency Command Center"
        //)
        Circle(
            center = currentLocation,
            radius = (detectionRange * 1000).toDouble(),
            strokeColor = Color.Blue,
            strokeWidth = 2f,
            fillColor = Color.Blue.copy(alpha = 0.1f)
        )
        detectedUsers.forEach { user ->
            Marker(
                state = MarkerState(position = user.position),
                title = user.name,
                snippet = "${user.status.name} - Battery: ${user.batteryLevel}%",
                onClick = {
                    onUserSelected(user)
                    true
                }
            )
        }
    }
}

@Composable
fun BottomUserList(users: List<DetectedUser>, onUserClick: (DetectedUser) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().height(120.dp).padding(8.dp)) {
        Text("Detected Users:", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(users) { user -> UserCard(user = user, onClick = { onUserClick(user) }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCard(user: DetectedUser, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(150.dp).fillMaxHeight(),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = getStatusIcon(user.status),
                    contentDescription = null,
                    tint = getStatusColor(user.status),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(user.status.name, color = getStatusColor(user.status), fontSize = 12.sp)
            }
            Text("${user.batteryLevel}% battery", fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsBottomSheet(user: DetectedUser, onDismiss: () -> Unit, currentLocation: LatLng) {
    val bottomSheetState = rememberModalBottomSheetState()
    val context = LocalContext.current // Needed to launch the map intent

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = bottomSheetState) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {

            Text(user.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = getStatusIcon(user.status),
                    contentDescription = null,
                    tint = getStatusColor(user.status)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Status: ${user.status.name}", color = getStatusColor(user.status), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Last Seen: ${formatTime(user.lastSeen)}")
            Text("Battery Level: ${user.batteryLevel}%")
            Text("Distance: ${calculateDistance(currentLocation, user.position).format(2)} km")

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { onDismiss() }) {
                    Icon(Icons.Default.Message, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send Message")
                }

                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Icon(Icons.Default.LocalHospital, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send Help")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Get Directions button to open Google Maps
            Button(
                onClick = {
                    val gmmIntentUri = Uri.parse("google.navigation:q=${user.position.latitude},${user.position.longitude}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")

                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Icon(Icons.Default.Radar, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Get Directions")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
