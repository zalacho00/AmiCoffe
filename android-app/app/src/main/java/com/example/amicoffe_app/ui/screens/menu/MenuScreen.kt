package com.example.amicoffe_app.ui.screens.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onNavigateToCarrito: () -> Unit = {},
    viewModel: MenuViewModel = viewModel()
) {
    val productos by viewModel.productos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val cartState by CartManager.cartState.collectAsState()
    var categoriaFiltro by remember { mutableStateOf("todos") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AmiScreenBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Header Superior de Marca y Acceso Rápido al Carrito
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AmiGreenLight,
                            modifier = Modifier.size(46.dp),
                            tonalElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalCafe,
                                    contentDescription = "Logo AmiCoffee",
                                    tint = AmiGreenDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AmiCoffee",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = AmiGreenDark
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AmiHeaderBg
                            ) {
                                Text(
                                    text = "CAMPUS VIBE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC86D51),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Botón rápido de Carrito con BadgedBox de Material 3
                    Surface(
                        onClick = onNavigateToCarrito,
                        shape = RoundedCornerShape(22.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BadgedBox(
                                badge = {
                                    if (cartState.totalItemsCount > 0) {
                                        Badge(
                                            containerColor = AmiGreenDark,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = "${cartState.totalItemsCount}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Carrito",
                                    tint = AmiGreenDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            if (cartState.totalItemsCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$${"%,.0f".format(cartState.total)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmiGreenDark
                                )
                            }
                        }
                    }
                }
            }

            // Saludo de bienvenida
            item {
                Column {
                    Text(
                        text = "¡Hola, Amigoniano! 👋",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmiTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pide desde tu móvil y retira en barra sin filas ☕",
                        fontSize = 13.sp,
                        color = AmiTextSecondary
                    )
                }
            }

            // Chips de filtrado de categorías estilo Material 3
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = categoriaFiltro == "todos",
                            onClick = { categoriaFiltro = "todos" },
                            label = { Text("Todos", fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmiGreenDark,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = Color.White,
                                labelColor = AmiTextPrimary
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (categoriaFiltro == "todos") AmiGreenDark else Color(0xFFE2E8F0)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    items(CATEGORIAS) { cat ->
                        val isSelected = categoriaFiltro == cat.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                categoriaFiltro = if (isSelected) "todos" else cat.id
                            },
                            label = { Text(cat.label, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmiGreenDark,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = Color.White,
                                labelColor = AmiTextPrimary
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) AmiGreenDark else Color(0xFFE2E8F0)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            if (cargando) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AmiGreenDark,
                            trackColor = AmiGreenLight
                        )
                    }
                }
            } else {
                val categoriasAMostrar = if (categoriaFiltro == "todos") {
                    CATEGORIAS
                } else {
                    CATEGORIAS.filter { it.id == categoriaFiltro }
                }

                categoriasAMostrar.forEach { cat ->
                    val productosCategoria = productos.filter { it.categoria == cat.id && it.estado == "disponible" }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = cat.bg,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(cat.icon, null, tint = cat.tint, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = cat.label,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmiTextPrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AmiScreenBg,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Text(
                                    text = "${productosCategoria.size} opciones",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AmiTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    if (productosCategoria.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sin productos disponibles por el momento.",
                                        fontSize = 13.sp,
                                        color = AmiTextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        items(productosCategoria, key = { it.id }) { producto ->
                            val itemEnCarrito = cartState.items.find { it.producto.id == producto.id }
                            val cantidadEnCarrito = itemEnCarrito?.cantidad ?: 0

                            ProductoRow(
                                producto = producto,
                                categoria = cat,
                                cantidadEnCarrito = cantidadEnCarrito,
                                onAdd = { CartManager.agregarProducto(producto) }
                            )
                        }
                    }
                }
            }

            // Espaciador final para no ser cubierto por la barra flotante ni el bottom bar
            item { Spacer(modifier = Modifier.height(110.dp)) }
        }

        // Barra flotante inferior de Material 3 cuando hay elementos en el carrito
        AnimatedVisibility(
            visible = cartState.totalItemsCount > 0,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Surface(
                onClick = onNavigateToCarrito,
                shape = RoundedCornerShape(28.dp),
                color = AmiGreenDark,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${cartState.totalItemsCount}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmiGreenDark
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Ver Mi Carrito",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$${"%,.0f".format(cartState.total)} COP",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductoRow(
    producto: Producto,
    categoria: CategoriaUI,
    cantidadEnCarrito: Int,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono / Miniatura temática de categoría
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = categoria.bg,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = categoria.icon,
                        contentDescription = null,
                        tint = categoria.tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del producto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AmiTextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${"%,.0f".format(producto.precio)} COP",
                        fontSize = 14.sp,
                        color = AmiGreenDark,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (cantidadEnCarrito > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmiGreenLight
                        ) {
                            Text(
                                text = "En carrito: $cantidadEnCarrito",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmiGreenDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Botón interactivo de añadir accesible (>=40dp con ripple y diseño M3)
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmiGreenDark,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (cantidadEnCarrito > 0) "+1" else "Añadir",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
