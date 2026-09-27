package com.example.amicoffe_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.amicoffe_app.navigation.NavGraph
import com.example.amicoffe_app.navigation.Screen
import com.example.amicoffe_app.ui.theme.*
import androidx.compose.foundation.clickable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmiCoffeappTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = AmiScreenBg) {
                    AmiCoffeApp()
                }
            }
        }
    }
}

@Composable
fun AmiCoffeApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { AmiBottomBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            NavGraph(navController = navController)
        }
    }
}

@Composable
fun AmiBottomBar(navController: androidx.navigation.NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Triple(Screen.Menu.route, "Menú", Icons.Default.LocalCafe),
        Triple(Screen.MisPedidos.route, "Mis Pedidos", Icons.Default.Receipt),
    )

    Surface(color = androidx.compose.ui.graphics.Color.White, tonalElevation = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (route, label, icon) ->
                val selected = currentDestination?.hierarchy?.any { it.route == route } == true
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickableNoRipple {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) AmiGreenDark else AmiTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                        color = if (selected) AmiGreenDark else AmiTextSecondary
                    )
                }
            }
        }
    }
}

// Helper simple para click sin efecto ripple en la barra
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(
        interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource(),
        indication = null,
        onClick = onClick
    )
)