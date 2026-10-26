package com.example.amicoffe_app.ui.screens.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.amicoffe_app.data.model.Producto
import com.example.amicoffe_app.data.repository.CartManager
import com.example.amicoffe_app.ui.theme.*

private data class CategoriaUI(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val bg: Color,
    val tint: Color
)

private val CATEGORIAS = listOf(
    CategoriaUI("bebidas", "Bebidas", Icons.Default.LocalCafe, AmiGreenLight, AmiGreenDark),
    CategoriaUI("snacks", "Snacks", Icons.Default.Fastfood, Color(0xFFFFEDD5), Color(0xFFEA580C)),
    CategoriaUI("almuerzos", "Almuerzos", Icons.Default.RestaurantMenu, Color(0xFFFEE2E2), Color(0xFFDC2626)),
)

@Composable
fun MenuScreen(
    onNavigateToCarrito: () -> Unit = {},
    viewModel: MenuViewModel = viewModel()
) {
    val productos by viewModel.productos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val cartState by CartManager.cartState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(36.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(AmiGreenLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalCafe, null, tint = AmiGreenDark, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("AmiCoffee", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AmiGreenDark)
                            Text("CAMPUS VIBE", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFC86D51))
                        }
                    }

                    // Botón rápido de Carrito en la esquina superior
                    Surface(
                        onClick = onNavigateToCarrito,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Carrito",
                                tint = AmiGreenDark,
                                modifier = Modifier.size(20.dp)
                            )
                            if (cartState.totalItemsCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(AmiGreenDark, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${cartState.totalItemsCount}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("¡Hola, Amigoniano! 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AmiTextPrimary)
            }

            if (cargando) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AmiGreenDark)
                    }
                }
            } else {
                CATEGORIAS.forEach { cat ->
                    val productosCategoria = productos.filter { it.categoria == cat.id && it.estado == "disponible" }

                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(36.dp).background(cat.bg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(cat.icon, null, tint = cat.tint, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(cat.label, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AmiTextPrimary)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("${productosCategoria.size} opciones", fontSize = 12.sp, color = AmiTextSecondary)
                        }
                    }

                    if (productosCategoria.isEmpty()) {
                        item {
                            Text("Sin productos disponibles ahora mismo.", fontSize = 13.sp, color = AmiTextSecondary)
                        }
                    } else {
                        items(productosCategoria) { producto ->
                            ProductoRow(
                                producto = producto,
                                onAdd = { CartManager.agregarProducto(producto) }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(110.dp)) }
        }

        // Barra flotante inferior cuando hay elementos en el carrito
        AnimatedVisibility(
            visible = cartState.totalItemsCount > 0,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onNavigateToCarrito,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmiGreenDark),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${cartState.totalItemsCount}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmiGreenDark
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Ver Mi Carrito",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "$${"%,.0f".format(cartState.total)} COP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductoRow(
    producto: Producto,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AmiTextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("$${"%,.0f".format(producto.precio)} COP", fontSize = 13.sp, color = AmiGreenDark, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = AmiGreenDark),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
