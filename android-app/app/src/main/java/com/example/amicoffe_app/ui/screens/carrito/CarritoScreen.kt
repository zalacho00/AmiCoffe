package com.example.amicoffe_app.ui.screens.carrito

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.amicoffe_app.data.model.CartItem
import com.example.amicoffe_app.data.model.Pedido
import com.example.amicoffe_app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    onBack: () -> Unit,
    onPedidoConfirmado: (Pedido) -> Unit,
    viewModel: CarritoViewModel = viewModel()
) {
    val cartState by viewModel.cartState.collectAsState()
    val context = LocalContext.current

    var menuSedeExpandido by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is CarritoUiEvent.PedidoExitoso -> {
                    onPedidoConfirmado(event.pedido)
                }
                is CarritoUiEvent.Error -> {
                    Toast.makeText(context, event.mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AmiScreenBg)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón atrás circular
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = AmiTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Título con Badge verde de cantidad
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Mi Carrito",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmiTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(Color(0xFFA7F3D0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cartState.totalItemsCount.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmiGreenDark
                        )
                    }
                }

                // Botón papelera para vaciar carrito
                Surface(
                    onClick = { viewModel.vaciarCarrito() },
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Vaciar carrito",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        containerColor = AmiScreenBg
    ) { innerPadding ->
        if (cartState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(AmiGreenLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = AmiGreenDark,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Tu carrito está vacío ☕",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmiTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Añade deliciosas bebidas o snacks desde el menú para realizar tu pedido remoto sin filas.",
                        fontSize = 14.sp,
                        color = AmiTextSecondary,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = AmiGreenDark),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Explorar Menú", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(2.dp)) }

                // Banner "Entrega rápida en barra"
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = AmiGreenDark,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Entrega rápida en barra",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AmiTextPrimary
                                    )
                                    Text(
                                        text = "Tiempo estimado: ~5 min de preparación",
                                        fontSize = 12.sp,
                                        color = AmiTextSecondary
                                    )
                                }
                            }

                            // Selector de sede pill con flecha desplegable
                            Box {
                                Surface(
                                    onClick = { menuSedeExpandido = true },
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White,
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cartState.sedeSeleccionada,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AmiTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Cambiar sede",
                                            tint = AmiTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = menuSedeExpandido,
                                    onDismissRequest = { menuSedeExpandido = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Campus Central") },
                                        onClick = {
                                            viewModel.actualizarSede("Campus Central")
                                            menuSedeExpandido = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Sede Poblado") },
                                        onClick = {
                                            viewModel.actualizarSede("Sede Poblado")
                                            menuSedeExpandido = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Sede Fraternidad") },
                                        onClick = {
                                            viewModel.actualizarSede("Sede Fraternidad")
                                            menuSedeExpandido = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Sección: Artículos en tu orden
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Artículos en tu orden",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmiTextPrimary
                        )
                        Text(
                            text = "${cartState.totalItemsCount} items",
                            fontSize = 13.sp,
                            color = AmiTextSecondary
                        )
                    }
                }

                // Lista de productos
                items(cartState.items, key = { it.producto.id }) { cartItem ->
                    CartItemRow(
                        cartItem = cartItem,
                        onIncrement = { viewModel.incrementarCantidad(cartItem.producto.id) },
                        onDecrement = { viewModel.decrementarCantidad(cartItem.producto.id) },
                        onRemove = { viewModel.removerItem(cartItem.producto.id) }
                    )
                }

                // Sección: Instrucciones especiales
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Notes,
                                    contentDescription = null,
                                    tint = AmiGreenDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Instrucciones especiales",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmiTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "¿Alguna instrucción especial para tu pedido? Ej: sin azúcar, leche deslactosada",
                                fontSize = 12.sp,
                                color = AmiTextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = cartState.instruccionesEspeciales,
                                onValueChange = { viewModel.actualizarInstrucciones(it) },
                                placeholder = {
                                    Text(
                                        "Escribe aquí tus notas para la barra...",
                                        fontSize = 13.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedBorderColor = AmiGreenDark,
                                    focusedContainerColor = Color(0xFFFAFAFA),
                                    unfocusedContainerColor = Color(0xFFFAFAFA)
                                ),
                                maxLines = 2
                            )
                        }
                    }
                }

                // Sección: Resumen de pago
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Resumen de pago",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmiTextPrimary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Subtotal (${cartState.totalItemsCount} productos)",
                                    fontSize = 14.sp,
                                    color = AmiTextSecondary
                                )
                                Text(
                                    text = "$${"%,.0f".format(cartState.subtotal)} COP",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmiTextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total a pagar",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmiTextPrimary
                                )
                                Text(
                                    text = "$${"%,.0f".format(cartState.total)} COP",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AmiGreenDark
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Nota de pago en efectivo
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AmiGreenLight.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = AmiGreenDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pago en efectivo al retirar tu pedido en barra con el código QR.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AmiGreenDark
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Botón principal: Pedir y Recoger en Barra (Genera el QR de retiro)
                            Button(
                                onClick = { viewModel.realizarPedidoRemoto() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(27.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AmiGreenDark)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pedir y Recoger en Barra - $${"%,.0f".format(cartState.total)} COP",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    cartItem: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Fila superior: Imagen/Icono, Nombre, Subtítulo, Precio, Botón 'X'
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Miniatura
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            when (cartItem.producto.categoria.lowercase()) {
                                "bebidas" -> AmiGreenLight
                                "snacks" -> Color(0xFFFFEDD5)
                                else -> Color(0xFFFEE2E2)
                            },
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (cartItem.producto.categoria.lowercase()) {
                            "bebidas" -> Icons.Default.LocalCafe
                            "snacks" -> Icons.Default.Fastfood
                            else -> Icons.Default.RestaurantMenu
                        },
                        contentDescription = null,
                        tint = when (cartItem.producto.categoria.lowercase()) {
                            "bebidas" -> AmiGreenDark
                            "snacks" -> Color(0xFFEA580C)
                            else -> Color(0xFFDC2626)
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cartItem.producto.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AmiTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (cartItem.producto.categoria.lowercase()) {
                            "bebidas" -> "Espresso doble, leche de avena"
                            "snacks" -> "Hojaldre de mantequilla tostado"
                            else -> "Porción de almuerzo especial"
                        },
                        fontSize = 12.sp,
                        color = AmiTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${"%,.0f".format(cartItem.producto.precio)} COP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmiGreenDark
                    )
                }

                // Botón cerrar 'X'
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Eliminar",
                        tint = AmiTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fila inferior: Personalización a la izquierda, Selector de cantidad a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = cartItem.personalizacion,
                    fontSize = 12.sp,
                    color = AmiTextSecondary,
                    modifier = Modifier.weight(1f)
                )

                // Selector - 1 +
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón decremento (-)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            .clickable { onDecrement() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Disminuir",
                            tint = AmiTextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = cartItem.cantidad.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmiTextPrimary
                    )

                    // Botón incremento (+)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(AmiGreenDark, CircleShape)
                            .clickable { onIncrement() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
