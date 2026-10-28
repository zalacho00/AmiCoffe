package com.example.amicoffe_app.ui.screens.pedidos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amicoffe_app.data.model.Pedido
import com.example.amicoffe_app.data.repository.PedidoRepository
import com.example.amicoffe_app.ui.theme.*
import com.example.amicoffe_app.data.repository.MisPedidosStore

@Composable
fun MisPedidosScreen() {
    val repository = remember { PedidoRepository() }
    val ids by MisPedidosStore.ids.collectAsState()
    val pedidos by remember(ids) { repository.observarMisPedidos(ids) }
        .collectAsState(initial = emptyList())

    var pedidoSeleccionadoParaCodigo by remember { mutableStateOf<Pedido?>(null) }
    var mostrarConfirmarBorrado by remember { mutableStateOf(false) }

    val finalizados = pedidos.filter { it.estado == "entregado" || it.estado == "cancelado" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AmiScreenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Header Superior de Mis Pedidos
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = AmiGreenLight,
                    modifier = Modifier.size(44.dp),
                    tonalElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = AmiGreenDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mis Pedidos",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmiTextPrimary
                    )
                    Text(
                        text = "Seguimiento y códigos de retiro",
                        fontSize = 12.sp,
                        color = AmiTextSecondary
                    )
                }

                // Botón accesible de Borrar historial para pedidos finalizados
                if (finalizados.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { mostrarConfirmarBorrado = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFFEE2E2),
                            contentColor = Color(0xFFDC2626)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Limpiar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (pedidos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(88.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shadowElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = AmiTextSecondary,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Aún no tienes pedidos registrados",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmiTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Realiza tu orden desde el menú y recógela en barra mostrando tu código de 4 dígitos.",
                            fontSize = 13.sp,
                            color = AmiTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pedidos, key = { it.id }) { pedido ->
                        val activo = pedido.estado == "en_espera" ||
                                pedido.estado == "en_preparacion" ||
                                pedido.estado == "listo"

                        val digitos = if (pedido.codigoRetiro.length == 4) {
                            pedido.codigoRetiro
                        } else {
                            pedido.id.takeLast(4).padStart(4, '0')
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(
                                width = if (activo) 1.5.dp else 1.dp,
                                color = if (activo) AmiGreenDark.copy(alpha = 0.3f) else Color(0xFFF1F5F9)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (activo) 2.dp else 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Fila superior: ID de orden y Chip de Estado con Icono
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Orden #${pedido.id.takeLast(6)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = AmiTextPrimary
                                        )
                                    }

                                    // Badge temático de estado
                                    val estadoInfo = when (pedido.estado) {
                                        "en_espera" -> EstadoUIInfo(
                                            Color(0xFFFEF3C7),
                                            Color(0xFFD97706),
                                            Icons.Default.HourglassTop,
                                            "En espera"
                                        )
                                        "en_preparacion" -> EstadoUIInfo(
                                            Color(0xFFFFF7ED),
                                            Color(0xFFEA580C),
                                            Icons.Default.LocalCafe,
                                            "En preparación"
                                        )
                                        "listo" -> EstadoUIInfo(
                                            Color(0xFFDCFCE7),
                                            AmiGreenDark,
                                            Icons.Default.CheckCircle,
                                            "¡Listo para retirar!"
                                        )
                                        "cancelado" -> EstadoUIInfo(
                                            Color(0xFFFEE2E2),
                                            Color(0xFFDC2626),
                                            Icons.Default.Cancel,
                                            "Cancelado"
                                        )
                                        else -> EstadoUIInfo(
                                            Color(0xFFF1F5F9),
                                            AmiTextSecondary,
                                            Icons.Default.TaskAlt,
                                            "Entregado"
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = estadoInfo.bg
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Icon(
                                                imageVector = estadoInfo.icon,
                                                contentDescription = null,
                                                tint = estadoInfo.textColor,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = estadoInfo.label,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = estadoInfo.textColor
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Sede y Código de retiro destacado
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = AmiTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = pedido.sede,
                                        fontSize = 13.sp,
                                        color = AmiTextSecondary
                                    )

                                    if (activo) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = AmiGreenLight,
                                            border = BorderStroke(1.dp, AmiGreenDark.copy(alpha = 0.2f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Pin,
                                                    contentDescription = null,
                                                    tint = AmiGreenDark,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "CÓDIGO: $digitos",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = AmiGreenDark
                                                )
                                            }
                                        }
                                    }
                                }

                                // Lista resumida de productos
                                if (pedido.items.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = AmiScreenBg,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = pedido.items.joinToString(" • ") { "${it.cantidad}x ${it.nombreProducto}" },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AmiTextPrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(10.dp))

                                // Fila inferior: Total a pagar y Botón de acción con tamaño táctil adecuado
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Total orden:",
                                            fontSize = 11.sp,
                                            color = AmiTextSecondary
                                        )
                                        Text(
                                            text = "$${"%,.0f".format(pedido.total)} COP",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = AmiGreenDark
                                        )
                                    }

                                    // Botón para ver código con tamaño accesible y prominente si está activo
                                    if (activo) {
                                        Button(
                                            onClick = { pedidoSeleccionadoParaCodigo = pedido },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = AmiGreenDark,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                            modifier = Modifier.height(42.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Pin,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Ver Código",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(90.dp)) }
                }
            }
        }

        // Modal de Material 3 para ver el código de retiro de 4 dígitos
        pedidoSeleccionadoParaCodigo?.let { pedido ->
            val digitos = if (pedido.codigoRetiro.length == 4) {
                pedido.codigoRetiro
            } else {
                pedido.id.takeLast(4).padStart(4, '0')
            }

            AlertDialog(
                onDismissRequest = { pedidoSeleccionadoParaCodigo = null },
                confirmButton = {
                    Button(
                        onClick = { pedidoSeleccionadoParaCodigo = null },
                        colors = ButtonDefaults.buttonColors(containerColor = AmiGreenDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Entendido, ya lo tengo", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AmiGreenLight,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Pin,
                                    contentDescription = null,
                                    tint = AmiGreenDark,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Código de Retiro",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = AmiTextPrimary
                        )
                        Text(
                            text = "Orden #${pedido.id.takeLast(6)}",
                            fontSize = 12.sp,
                            color = AmiTextSecondary
                        )
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Muestra o dicta este código de 4 dígitos en la barra de ${pedido.sede} para reclamar tu pedido.",
                            fontSize = 13.sp,
                            color = AmiTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Renderizado de las 4 casillas numéricas con diseño Material 3
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            digitos.forEach { char ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = AmiScreenBg,
                                    border = BorderStroke(2.dp, AmiGreenDark),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = char.toString(),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = AmiGreenDark
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AmiGreenLight.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = AmiGreenDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pago en efectivo en barra al retirar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AmiGreenDark
                                )
                            }
                        }
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }

        // Diálogo de confirmación para borrar historial (CORREGIDO: ahora vive fuera de pedidoSeleccionadoParaCodigo)
        if (mostrarConfirmarBorrado) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmarBorrado = false },
                title = {
                    Text(
                        text = "Borrar historial",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "Se quitarán de tu lista los pedidos ya entregados y cancelados. Los pedidos activos en preparación no se tocan.",
                        fontSize = 13.sp,
                        color = AmiTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            repository.ocultarPedidos(finalizados.map { it.id })
                            mostrarConfirmarBorrado = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Borrar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfirmarBorrado = false }) {
                        Text("Cancelar", color = AmiTextPrimary)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
    }
}

private data class EstadoUIInfo(
    val bg: Color,
    val textColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
