package com.example.myapplication.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Maps : Screen("maps")
    object Placeholder : Screen("placeholder")
    object Nearby : Screen("nearby")
    object Login : Screen("login")
}
