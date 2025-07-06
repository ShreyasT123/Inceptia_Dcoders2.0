package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.screens.LoginScreen
import com.example.myapplication.screens.MainScreen
import com.example.myapplication.screens.MapsScreen
import com.example.myapplication.screens.NearbyScanScreen
import com.example.myapplication.screens.Placeholder

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) { // ✅ Login first
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        composable(Screen.Maps.route) {
            MapsScreen()
        }
        composable(Screen.Placeholder.route) {
            Placeholder(navController = navController)
        }
        composable(Screen.Nearby.route) {
            NearbyScanScreen()
        }
    }
}

