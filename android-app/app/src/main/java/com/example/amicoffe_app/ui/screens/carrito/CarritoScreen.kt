package com.example.amicoffe_app.ui.screens.carrito

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
    val enviando by viewModel.enviando.collectAsState()
    val tiempoEspera by viewModel.tiempoEspera.collectAsState()
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("amicoffe_prefs", android.content.Context.MODE_PRIVATE)
    }

    var menuSedeExpandido by remember { mutableStateOf(false) }
    var mostrarConfirmarVaciar by remember { mutableStateOf(false) }

    // Recupera el nombre guardado la primera vez que se abre el carrito
    LaunchedEffect(Unit) {
        if (cartState.nombreEstudiante.isBlank()) {
            viewModel.actualizarNombre(prefs.getString("nombre_estudiante", "") ?: "")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is CarritoUiEvent.PedidoExitoso -> {
                    prefs.edit().putString("nombre_estudiante", event.pedido.estudianteNombre).apply()
                    onPedidoConfirmado(event.pedido)
                }
                is CarritoUiEvent.Error -> {
                    Toast.makeText(context, event.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = AmiScreenBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Botón atrás accesible con ripple
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(42.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.White,
                            contentColor = AmiTextPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Título con Badge de cantidad en Material 3
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mi Carrito",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmiTextPrimary
                        )
                        if (cartState.totalItemsCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = AmiGreenLight,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = cartState.totalItemsCount.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmiGreenDark
                                    )
                                }
                            }
                        }
                    }

                    // Botón para vaciar carrito (con confirmación)
                    if (cartState.items.isNotEmpty()) {
                        IconButton(
                            onClick = { mostrarConfirmarVaciar = true },
                            modifier = Modifier.size(42.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF64748B)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Vaciar carrito",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(42.dp))
                    }
                }
            }
        },
        containerColor = AmiScreenBg
    ) { innerPadding ->

        // Diálogo de confirmación para vaciar carrito
        if (mostrarConfirmarVaciar) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmarVaciar = false },
                title = {
                    Text(
                        text = "¿Vaciar carrito?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "Se eliminarán todos los productos de tu orden actual.",
                        fontSize = 14.sp,
                        color = AmiTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.vaciarCarrito()
                            mostrarConfirmarVaciar = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Vaciar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfirmarVaciar = false }) {
                        Text("Cancelar", color = AmiTextPrimary)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

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
                    Surface(
                        shape = CircleShape,
                        color = AmiGreenLight,
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = AmiGreenDark,
                                modifier = Modifier.size(48.dp)
                            )
                        }
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
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Banner "Entrega rápida en barra" + Selector de Sede
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = BorderStroke(1.dp, Color(0xFFC8E6C9)),
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
                                Surface(
                                    shape = CircleShape,
                                    color = AmiGreenLight,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = AmiGreenDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Entrega rápida en barra",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AmiTextPrimary
                                    )
                                    Text(
                                        text = "Tiempo est.: ~$tiempoEspera min de preparación",
                                        fontSize = 12.sp,
                                        color = AmiTextSecondary
                                    )
                                }
                            }

                            // Selector de sede con menú desplegable
                            Box {
                                Surface(
                                    onClick = { menuSedeExpandido = true },
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = AmiGreenDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
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
                                    onDismissRequest = { menuSedeExpandido = false },
                                    shape = RoundedCornerShape(16.dp),
                                    containerColor = Color.White
                                ) {
                                    listOf("Campus Central", "Sede Poblado", "Sede Fraternidad").forEach { sede ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = sede,
                                                    fontWeight = if (sede == cartState.sedeSeleccionada) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (sede == cartState.sedeSeleccionada) AmiGreenDark else AmiTextPrimary
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Place,
                                                    contentDescription = null,
                                                    tint = if (sede == cartState.sedeSeleccionada) AmiGreenDark else AmiTextSecondary
                                                )
                                            },
                                            onClick = {
                                                viewModel.actualizarSede(sede)
                                                menuSedeExpandido = false
                                            }
                                        )
                                    }
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
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AmiScreenBg,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = "${cartState.totalItemsCount} productos",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = AmiTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Lista de productos con controles accesibles y diseño M3
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = AmiGreenLight,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Notes,
                                            contentDescription = null,
                                            tint = AmiGreenDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Instrucciones especiales",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmiTextPrimary
                                    )
                                    Text(
                                        text = "Ej: sin azúcar, leche deslactosada, salsa aparte",
                                        fontSize = 12.sp,
                                        color = AmiTextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
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
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AmiGreenDark,
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFFAFAFA),
                                    unfocusedContainerColor = Color(0xFFFAFAFA)
                                ),
                                maxLines = 2
                            )
                        }
                    }
                }

                // Sección: Nombre del estudiante
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = AmiGreenLight,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = AmiGreenDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "¿A nombre de quién?",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmiTextPrimary
                                    )
                                    Text(
                                        text = "La barra te llamará con este nombre y tu código de retiro",
                                        fontSize = 12.sp,
                                        color = AmiTextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = cartState.nombreEstudiante,
                                onValueChange = { viewModel.actualizarNombre(it) },
                                placeholder = {
                                    Text("Tu nombre o apodo", fontSize = 13.sp, color = Color(0xFF94A3B8))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = AmiTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AmiGreenDark,
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFFAFAFA),
                                    unfocusedContainerColor = Color(0xFFFAFAFA)
                                ),
                                singleLine = true
                            )
                        }
                    }
                }

                // Sección: Resumen de pago
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
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

                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

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

                            // Franja de tiempo de espera
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFF7ED),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = AmiAccentOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Tiempo estimado de espera: $tiempoEspera min",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AmiTextPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Franja de nota de pago en efectivo
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AmiGreenLight.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
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
                                        text = "Pago en efectivo al retirar tu pedido en barra con tu código de 4 dígitos.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AmiGreenDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Botón principal: Pedir y Recoger en Barra
                            Button(
                                onClick = { viewModel.realizarPedidoRemoto() },
                                enabled = !enviando,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(27.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmiGreenDark,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                if (enviando) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Procesando pedido...",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Fila superior: Imagen/Icono, Nombre, Subtítulo, Precio, Botón Eliminar con tamaño accesible
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Miniatura temática
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = when (cartItem.producto.categoria.lowercase()) {
                        "bebidas" -> AmiGreenLight
                        "snacks" -> Color(0xFFFFEDD5)
                        else -> Color(0xFFFEE2E2)
                    },
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
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
                            "bebidas" -> "Bebida preparada al momento"
                            "snacks" -> "Snack fresco de cafetería"
                            else -> "Porción de almuerzo especial"
                        },
                        fontSize = 12.sp,
                        color = AmiTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${"%,.0f".format(cartItem.producto.precio)} COP c/u",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmiGreenDark
                    )
                }

                // Botón de eliminar con tamaño táctil adecuado (40dp) y ripple
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Eliminar producto",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Nota de personalización
            if (cartItem.personalizacion.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmiScreenBg,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = AmiTextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cartItem.personalizacion,
                            fontSize = 11.sp,
                            color = AmiTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Fila inferior: Subtotal del producto a la izquierda, Selector de cantidad accesible a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Subtotal:",
                        fontSize = 11.sp,
                        color = AmiTextSecondary
                    )
                    Text(
                        text = "$${"%,.0f".format(cartItem.precioSubtotal)} COP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmiTextPrimary
                    )
                }

                // Selector stepper de cantidad rediseñado con botones táctiles cómodos de 36dp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Botón decremento (-)
                    Surface(
                        onClick = onDecrement,
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (cartItem.cantidad == 1) Icons.Default.DeleteOutline else Icons.Default.Remove,
                                contentDescription = if (cartItem.cantidad == 1) "Eliminar" else "Disminuir",
                                tint = if (cartItem.cantidad == 1) Color(0xFFDC2626) else AmiTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = cartItem.cantidad.toString(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmiTextPrimary,
                        modifier = Modifier.widthIn(min = 20.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Botón incremento (+)
                    Surface(
                        onClick = onIncrement,
                        shape = CircleShape,
                        color = AmiGreenDark,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Aumentar",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
