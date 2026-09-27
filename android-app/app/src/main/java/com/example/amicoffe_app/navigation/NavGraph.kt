package com.example.amicoffe_app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.amicoffe_app.ui.screens.menu.MenuScreen
import com.example.amicoffe_app.ui.screens.pedidos.MisPedidosScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Menu.route) {
        composable(Screen.Menu.route) { MenuScreen() }
        composable(Screen.MisPedidos.route) { MisPedidosScreen() }
    }
}