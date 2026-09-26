package com.example.amicoffe_app.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.amicoffe_app.ui.screens.estado.EstadoScreen
import com.example.amicoffe_app.ui.screens.menu.AmiCoffeMainScreen
import com.example.amicoffe_app.ui.screens.pedido.PedidoScreen

data class ProductItem(
    val id: String,
    val title: String,
    val description: String,
    val price: Int,
    val priceFormatted: String,
    val tag: String? = null,
    val categoryId: String,
    val subcategory: String = "Todos",
    val customization: String = ""
)

data class Category(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val iconBg: Color,
    val iconTint: Color,
    val icon: ImageVector,
    val subcategories: List<String> = listOf("Todos")
)

data class CartEntry(
    val product: ProductItem,
    val quantity: Int,
    val customization: String = ""
)

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf("MENU") } // "MENU", "PEDIDO", "ESTADO"
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    val cartItems = remember { mutableStateMapOf<String, CartEntry>() }

    fun addToCart(product: ProductItem) {
        val existing = cartItems[product.id]
        if (existing != null) {
            cartItems[product.id] = existing.copy(quantity = existing.quantity + 1)
        } else {
            cartItems[product.id] = CartEntry(
                product = product,
                quantity = 1,
                customization = if (product.customization.isNotEmpty()) product.customization else "Estándar"
            )
        }
    }

    fun updateQuantity(productId: String, delta: Int) {
        val existing = cartItems[productId] ?: return
        val newQty = existing.quantity + delta
        if (newQty <= 0) {
            cartItems.remove(productId)
        } else {
            cartItems[productId] = existing.copy(quantity = newQty)
        }
    }

    fun removeFromCart(productId: String) {
        cartItems.remove(productId)
    }

    fun clearCart() {
        cartItems.clear()
    }

    when (currentScreen) {
        "MENU" -> {
            AmiCoffeMainScreen(
                cartItems = cartItems.values.toList(),
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it },
                onAddToCart = { addToCart(it) },
                onNavigateToCart = { currentScreen = "PEDIDO" },
                onNavigateToMenu = { currentScreen = "MENU" }
            )
        }
        "PEDIDO" -> {
            PedidoScreen(
                cartItems = cartItems.values.toList(),
                onUpdateQuantity = { id, delta -> updateQuantity(id, delta) },
                onRemoveItem = { removeFromCart(it) },
                onClearCart = { clearCart() },
                onNavigateToMenu = { currentScreen = "MENU" },
                onConfirmOrder = { currentScreen = "ESTADO" }
            )
        }
        "ESTADO" -> {
            EstadoScreen(
                onBackToMenu = {
                    clearCart()
                    currentScreen = "MENU"
                }
            )
        }
    }
}
