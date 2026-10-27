package com.example.amicoffe_app.ui.screens.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Receipt
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
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(AmiGreenLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = AmiGreenDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Mis Pedidos Remotos",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmiTextPrimary
                )
                if (finalizados.isNotEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { mostrarConfirmarBorrado = true }) {
                        Text(
                            "Borrar historial",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = AmiTextSecondary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aún no tienes pedidos registrados",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmiTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Realiza tu pedido desde el menú y recíbelo con tu código de 4 dígitos en la barra.",
                            fontSize = 13.sp,
                            color = AmiTextSecondary
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
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Orden #${pedido.id}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmiTextPrimary
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = when (pedido.estado) {
                                            "en_espera", "en_preparacion" -> Color(0xFFFEF3C7)
                                            "listo" -> Color(0xFFDCFCE7)
                                            "cancelado" -> Color(0xFFFEE2E2)
                                            else -> Color(0xFFF1F5F9)
                                        }
                                    ) {
                                        Text(
                                            text = when (pedido.estado) {
                                                "en_espera" -> "En espera"
                                                "en_preparacion" -> "En preparación"
                                                "listo" -> "¡Listo para retirar!"
                                                "cancelado" -> "Cancelado"
                                                else -> "Entregado"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (pedido.estado) {
                                                "en_espera", "en_preparacion" -> Color(0xFFD97706)
                                                "listo" -> AmiGreenDark
                                                "cancelado" -> Color(0xFFDC2626)
                                                else -> AmiTextSecondary
                                            },
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Sede: ${pedido.sede}",
                                        fontSize = 13.sp,
                                        color = AmiTextSecondary
                                    )
                                    if (activo) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(AmiGreenLight, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Código: $digitos",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AmiGreenDark
                                            )
                                        }
                                    }
                                }

                                if (pedido.items.isNotEmpty()) {
                                    Text(
                                        text = pedido.items.joinToString(", ") { "${it.cantidad}x ${it.nombreProducto}" },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AmiTextPrimary,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Total: $${"%,.0f".format(pedido.total)} COP",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmiGreenDark
                                    )
                                    if (activo) {
                                        Button(
                                            onClick = { pedidoSeleccionadoParaCodigo = pedido },
                                            colors = ButtonDefaults.buttonColors(containerColor = AmiGreenDark),
                                            shape = RoundedCornerShape(14.dp),
                                            contentPadding = PaddingValues(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            )
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Pin,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    "Ver Código",
                                                    fontSize = 12.sp,
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

        // Modal para ver el código de retiro de 4 dígitos
        pedidoSeleccionadoParaCodigo?.let { pedido ->
            val digitos = if (pedido.codigoRetiro.length == 4) {
                pedido.codigoRetiro
            } else {
                pedido.id.takeLast(4).padStart(4, '0')
            }

            AlertDialog(
                onDismissRequest = { pedidoSeleccionadoParaCodigo = null },
                confirmButton = {
                    TextButton(onClick = { pedidoSeleccionadoParaCodigo = null }) {
                        Text("Cerrar", color = AmiGreenDark, fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Text(
                        text = "Código de Retiro - #${pedido.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
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

                        // Renderizado de las 4 casillas numéricas
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            digitos.forEach { char ->
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(AmiScreenBg, RoundedCornerShape(12.dp))
                                        .border(2.dp, AmiGreenDark, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char.toString(),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AmiGreenDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Pago en efectivo al retirar en barra",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AmiGreenDark
                        )
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )

            if (mostrarConfirmarBorrado) {
                AlertDialog(
                    onDismissRequest = { mostrarConfirmarBorrado = false },
                    title = { Text("Borrar historial") },
                    text = { Text("Se quitarán de tu lista los pedidos entregados y cancelados. Los pedidos en curso no se tocan.") },
                    confirmButton = {
                        TextButton(onClick = {
                            repository.ocultarPedidos(finalizados.map { it.id })
                            mostrarConfirmarBorrado = false
                        }) { Text("Borrar", color = Color(0xFFDC2626)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarConfirmarBorrado = false }) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}


