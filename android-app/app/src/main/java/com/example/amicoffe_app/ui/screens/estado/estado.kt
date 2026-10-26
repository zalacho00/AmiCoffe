package com.example.amicoffe_app.ui.screens.estado

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amicoffe_app.ui.theme.*

@Composable
fun EstadoScreen(
    pedidoId: String = "AC-4829",
    codigoRetiro: String = "4829",
    sede: String = "Campus Central",
    onBackToMenu: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Extraer los 4 dígitos o usar los últimos 4 caracteres del id
    val digitos = if (codigoRetiro.length == 4) {
        codigoRetiro
    } else {
        pedidoId.takeLast(4).padStart(4, '0')
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AmiScreenBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Icono de confirmación
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFDCFCE7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AmiGreenDark,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "¡Pedido Confirmado!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AmiTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tu orden #$pedidoId está lista para ser preparada en $sede.",
                fontSize = 13.sp,
                color = AmiTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta de CÓDIGO DE RETIRO DE 4 DÍGITOS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(AmiGreenLight, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pin,
                            contentDescription = null,
                            tint = AmiGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Código de Retiro",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmiGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Muestra los 4 dígitos en casillas individuales
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        digitos.forEach { char ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(AmiScreenBg, RoundedCornerShape(14.dp))
                                    .border(2.dp, AmiGreenDark, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AmiGreenDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Muestra o dicta este código de 4 dígitos en la barra de atención para reclamar tu pedido sin hacer fila física.",
                        fontSize = 12.sp,
                        color = AmiTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de Estado y Tiempo estimado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = AmiAccentOrange,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tiempo estimado: ~5 min",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmiTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { 0.6f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = AmiGreenDark,
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pasos de estado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1. Recibido ✓", fontSize = 11.sp, color = AmiGreenDark, fontWeight = FontWeight.Bold)
                        Text("2. En preparación ☕", fontSize = 11.sp, color = AmiGreenDark, fontWeight = FontWeight.Bold)
                        Text("3. Listo 📦", fontSize = 11.sp, color = AmiTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onBackToMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmiGreenDark)
            ) {
                Text(
                    text = "Volver al Menú Principal",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
