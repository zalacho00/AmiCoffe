package com.example.amicoffe_app.navigation

sealed class Screen(val route: String) {
    object Menu : Screen("menu")
    object Carrito : Screen("carrito")
    object MisPedidos : Screen("mis_pedidos")
    object Estado : Screen("estado/{pedidoId}") {
        fun createRoute(pedidoId: String) = "estado/$pedidoId"
    }
}
