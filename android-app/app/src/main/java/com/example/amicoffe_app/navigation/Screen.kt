package com.example.amicoffe_app.navigation

sealed class Screen(val route: String) {
    object Menu : Screen("menu")
    object MisPedidos : Screen("mis_pedidos")
}