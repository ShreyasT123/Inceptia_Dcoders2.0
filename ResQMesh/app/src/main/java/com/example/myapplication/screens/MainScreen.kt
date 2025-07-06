package com.example.myapplication.screens

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, sosViewModel: SosViewModel = viewModel()) {
    val context = LocalContext.current
    val isSosActive by sosViewModel.isSosActive.collectAsState()

    LaunchedEffect(key1 = true) {
        sosViewModel.checkInitialSosState(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            Log.d("SOS_DEBUG", "Permission result received.")
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                Log.d("SOS_DEBUG", "Permission GRANTED. Starting SOS.")
                sosViewModel.startSos(context)
            } else {
                Log.e("SOS_DEBUG", "Permission DENIED.")
            }
        }
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("Main Page") }) },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isSosActive) {
                CancelSosButton { sosViewModel.stopSos(context) }
            } else {
                StartSosButton {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StartSosButton(onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("In case of emergency, press the SOS button.")
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.size(150.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, "SOS", modifier = Modifier.size(60.dp), tint = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("SOS", color = Color.White, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
fun CancelSosButton(onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("SOS Mode is Active!", color = Color.Red, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
            modifier = Modifier.size(150.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Check, "I am Safe", modifier = Modifier.size(60.dp), tint = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("I'M SAFE", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavigationItem.Main,
        NavigationItem.Maps,
        NavigationItem.Placeholder,
        NavigationItem.Nearby // 👈 Added Nearby tab
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = navController.currentBackStackEntry?.destination?.route == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

sealed class NavigationItem(val route: String, val icon: ImageVector, val title: String) {
    object Main : NavigationItem(Screen.Main.route, Icons.Default.Home, "Main")
    object Maps : NavigationItem(Screen.Maps.route, Icons.Default.LocationOn, "Maps")
    object Placeholder : NavigationItem(Screen.Placeholder.route, Icons.Default.Settings, "Settings")
    object Nearby : NavigationItem(Screen.Nearby.route, Icons.Default.Bluetooth, "Nearby") // 👈 NEW
}
