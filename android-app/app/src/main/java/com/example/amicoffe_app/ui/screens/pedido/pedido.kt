package com.example.amicoffe_app.ui.screens.pedido

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amicoffe_app.navigation.CartEntry
import com.example.amicoffe_app.ui.theme.*

@Composable
fun PedidoScreen(
    cartItems: List<CartEntry>,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onConfirmOrder: () -> Unit
) {
    var specialInstructions by remember { mutableStateOf("") }
    val totalCount = cartItems.sumOf { it.quantity }
    val totalPriceInt = cartItems.sumOf { it.product.price * it.quantity }
    val totalPriceFormatted = "$${String.format("%,d", totalPriceInt).replace(',', '.')} COP"

    Box(modifier = Modifier.fillMaxSize().background(AmiScreenBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(36.dp)) }

            // Top Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .border(1.dp, Color(0xFF93C5FD), CircleShape)
                            .background(Color.White, CircleShape)
                            .clickable { onNavigateToMenu() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = AmiGreenDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

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
                                .size(24.dp)
                                .background(Color(0xFFA7F3D0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$totalCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmiGreenDark
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                            .clickable { onClearCart() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Vaciar Carrito",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Delivery Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = AmiGreenDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Entrega rápida en barra",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AmiGreenDark
                                )
                                Text(
                                    text = "Tiempo estimado: ~5 min de preparación",
                                    fontSize = 12.sp,
                                    color = Color(0xFF047857)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Campus\nCentral",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmiGreenDark,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Artículos en tu orden",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmiTextPrimary
                    )
                    Text(
                        text = "$totalCount items",
                        fontSize = 13.sp,
                        color = AmiTextSecondary
                    )
                }
            }

            // Cart Items List
            if (cartItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = AmiTextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tu carrito está vacío",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmiTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Añade algunos deliciosos cafés o comidas desde el menú.",
                                fontSize = 13.sp,
                                color = AmiTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(cartItems, key = { it.product.id }) { cartEntry ->
                    CartItemCard(
                        cartEntry = cartEntry,
                        onUpdateQuantity = { delta -> onUpdateQuantity(cartEntry.product.id, delta) },
                        onRemove = { onRemoveItem(cartEntry.product.id) }
                    )
                }
            }

            // Special Instructions Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notes,
                                contentDescription = null,
                                tint = AmiGreenDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instrucciones especiales",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
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
                            value = specialInstructions,
                            onValueChange = { specialInstructions = it },
                            placeholder = {
                                Text(
                                    text = "Escribe aquí tus notas para la barra...",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedBorderColor = AmiGreenDark,
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )
                    }
                }
            }

            // Checkout Summary Button
            if (cartItems.isNotEmpty()) {
                item {
                    Button(
                        onClick = onConfirmOrder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmiGreenDark)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Confirmar Pedido ($totalCount)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = totalPriceFormatted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Bottom Navigation Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onNavigateToMenu() }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalCafe,
                        contentDescription = "Menú",
                        tint = AmiTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Menú",
                        fontSize = 12.sp,
                        color = AmiTextSecondary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Mis Pedidos",
                        tint = AmiGreenDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Mis Pedidos",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmiGreenDark
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    cartEntry: CartEntry,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(AmiGreenLight, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalCafe,
                            contentDescription = null,
                            tint = AmiGreenDark,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = cartEntry.product.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AmiTextPrimary
                        )
                        Text(
                            text = cartEntry.product.description,
                            fontSize = 12.sp,
                            color = AmiTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cartEntry.product.priceFormatted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AmiGreenDark
                        )
                    }
                }

                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Eliminar",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (cartEntry.customization.isNotEmpty()) cartEntry.customization else "Personalizado: Estándar",
                    fontSize = 12.sp,
                    color = AmiTextSecondary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                            .background(Color.White, CircleShape)
                            .clickable { onUpdateQuantity(-1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "-",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmiTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "${cartEntry.quantity}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmiTextPrimary
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(AmiGreenDark, CircleShape)
                            .clickable { onUpdateQuantity(1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
