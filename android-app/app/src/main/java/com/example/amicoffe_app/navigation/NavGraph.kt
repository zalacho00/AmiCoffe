package com.example.amicoffe_app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.amicoffe_app.ui.screens.carrito.CarritoScreen
import com.example.amicoffe_app.ui.screens.estado.EstadoScreen
import com.example.amicoffe_app.ui.screens.menu.MenuScreen
import com.example.amicoffe_app.ui.screens.pedidos.MisPedidosScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Menu.route) {
        composable(Screen.Menu.route) {
            MenuScreen(
                onNavigateToCarrito = {
                    navController.navigate(Screen.Carrito.route)
                }
            )
        }

        composable(Screen.Carrito.route) {
            CarritoScreen(
                onBack = {
                    navController.popBackStack()
                },
                onPedidoConfirmado = { pedido ->
                    navController.navigate(Screen.Estado.createRoute(pedido.id)) {
                        popUpTo(Screen.Menu.route)
                    }
                }
            )
        }

        composable(
            route = Screen.Estado.route,
            arguments = listOf(navArgument("pedidoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: ""
            EstadoScreen(
                pedidoId = pedidoId,
                onBackToMenu = {
                    navController.navigate(Screen.Menu.route) {
                        popUpTo(Screen.Menu.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.MisPedidos.route) {
            MisPedidosScreen()
        }
    }
}
