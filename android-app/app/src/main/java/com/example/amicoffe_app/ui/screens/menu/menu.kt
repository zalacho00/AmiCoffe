package com.example.amicoffe_app.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amicoffe_app.navigation.CartEntry
import com.example.amicoffe_app.navigation.Category
import com.example.amicoffe_app.navigation.ProductItem
import com.example.amicoffe_app.ui.theme.*

// Mock Data
val categoriesList = listOf(
    Category(
        id = "cafes_calientes",
        title = "Cafés Calientes",
        subtitle = "Espresso, Latte, Capuchino, Mocha",
        badge = "8 opciones",
        iconBg = AmiGreenLight,
        iconTint = AmiGreenDark,
        icon = Icons.Default.LocalCafe,
        subcategories = listOf("Todos", "Espresso Base", "Con Leche", "Especiales")
    ),
    Category(
        id = "cold_brew",
        title = "Cold Brew & Iced Coffee",
        subtitle = "Nitro, Cold brew clásica, Shakerato",
        badge = "6 opciones",
        iconBg = Color(0xFFFEE2E2),
        iconTint = Color(0xFFDC2626),
        icon = Icons.Default.AcUnit,
        subcategories = listOf("Todos", "Cold Brew", "Bebidas Frías", "Matcha")
    ),
    Category(
        id = "panaderia",
        title = "Panadería & Brunch",
        subtitle = "Croissants, Panini de pavo, Waffles",
        badge = "10 opciones",
        iconBg = Color(0xFFFFEDD5),
        iconTint = Color(0xFFEA580C),
        icon = Icons.Default.Fastfood,
        subcategories = listOf("Todos", "Panadería", "Sandwiches & Paninis", "Postres")
    )
)

val productsList = listOf(
    // Cafés Calientes
    ProductItem(
        id = "p1",
        title = "Americano Especial",
        description = "Espresso doble con agua filtrada y vibrantes notas a cacao.",
        price = 6500,
        priceFormatted = "$6.500 COP",
        tag = "CLÁSICO",
        categoryId = "cafes_calientes",
        subcategory = "Espresso Base"
    ),
    ProductItem(
        id = "p2",
        title = "Cappuccino Italiano",
        description = "Espresso balanceado, leche vaporizada y capa sedosa de espuma.",
        price = 9500,
        priceFormatted = "$9.500 COP",
        tag = "FAVORITO",
        categoryId = "cafes_calientes",
        subcategory = "Con Leche"
    ),
    ProductItem(
        id = "p3",
        title = "Caramel Macchiato",
        description = "Vainilla artesanal, leche texturizada, espresso doble y caramelo.",
        price = 11500,
        priceFormatted = "$11.500 COP",
        tag = "TOP CAMPUS 🔥",
        categoryId = "cafes_calientes",
        subcategory = "Especiales",
        customization = "Personalizado: Leche de avena"
    ),
    ProductItem(
        id = "p4",
        title = "Latte Vainilla",
        description = "Espresso suave con leche cremosidad perfecta y toque de vainilla.",
        price = 10500,
        priceFormatted = "$10.500 COP",
        categoryId = "cafes_calientes",
        subcategory = "Con Leche"
    ),

    // Cold Brew
    ProductItem(
        id = "p5",
        title = "Nitro Cold Brew",
        description = "Macerado 20h con nitrógeno y crema suave sin lácteos.",
        price = 12500,
        priceFormatted = "$12.500 COP",
        tag = "#2 Popular",
        categoryId = "cold_brew",
        subcategory = "Cold Brew"
    ),
    ProductItem(
        id = "p6",
        title = "Iced Matcha Latte",
        description = "Matcha ceremonial frío mezclado con leche de almendras.",
        price = 13000,
        priceFormatted = "$13.000 COP",
        tag = "FAVORITO",
        categoryId = "cold_brew",
        subcategory = "Matcha",
        customization = "Frío con hielo"
    ),

    // Panadería
    ProductItem(
        id = "p7",
        title = "Croissant de Almendras",
        description = "Hojaldre de mantequilla tostado relleno de crema de almendras.",
        price = 8500,
        priceFormatted = "$8.500 COP",
        tag = "FAVORITO",
        categoryId = "panaderia",
        subcategory = "Panadería",
        customization = "Tostado al grill"
    ),
    ProductItem(
        id = "p8",
        title = "Panini de Pavo & Queso",
        description = "Pan ciabatta tostado con pavo ahumado y queso holandés melted.",
        price = 14000,
        priceFormatted = "$14.000 COP",
        categoryId = "panaderia",
        subcategory = "Sandwiches & Paninis"
    )
)

@Composable
fun AmiCoffeMainScreen(
    cartItems: List<CartEntry>,
    selectedCategory: Category?,
    onCategorySelect: (Category?) -> Unit,
    onAddToCart: (ProductItem) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToMenu: () -> Unit
) {
    val totalCount = cartItems.sumOf { it.quantity }
    val totalPriceInt = cartItems.sumOf { it.product.price * it.quantity }
    val totalPriceFormatted = "$${String.format("%,d", totalPriceInt).replace(',', '.')} COP"

    Box(modifier = Modifier.fillMaxSize().background(AmiScreenBg)) {
        if (selectedCategory == null) {
            MainMenuView(
                cartCount = totalCount,
                onCategoryClick = { onCategorySelect(it) },
                onAddToCart = onAddToCart,
                onNavigateToCart = onNavigateToCart
            )
        } else {
            CategoryDetailView(
                category = selectedCategory,
                onBack = { onCategorySelect(null) },
                onAddToCart = onAddToCart
            )
        }

        // Floating Cart Bar (if items in cart)
        if (totalCount > 0) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 70.dp)
                    .fillMaxWidth()
                    .clickable { onNavigateToCart() },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = AmiGreenDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ver Carrito ($totalCount)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "AmiCoffee",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = totalPriceFormatted,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
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
                        tint = AmiGreenDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Menú",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmiGreenDark
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onNavigateToCart() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Mis Pedidos",
                        tint = AmiTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Mis Pedidos",
                        fontSize = 12.sp,
                        color = AmiTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun MainMenuView(
    cartCount: Int,
    onCategoryClick: (Category) -> Unit,
    onAddToCart: (ProductItem) -> Unit,
    onNavigateToCart: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(36.dp)) }

        // Top Bar
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
                        Icon(
                            imageVector = Icons.Default.LocalCafe,
                            contentDescription = null,
                            tint = AmiGreenDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AmiCoffee",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = AmiGreenDark
                        )
                        Text(
                            text = "CAMPUS VIBE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFC86D51),
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Cart Icon with Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, Color(0xFF86E3CE), CircleShape)
                        .background(Color.White, CircleShape)
                        .clickable { onNavigateToCart() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart",
                        tint = AmiGreenDark,
                        modifier = Modifier.size(22.dp)
                    )
                    if (cartCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(AmiAccentOrange, CircleShape)
                                .align(Alignment.TopEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$cartCount",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Greeting Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AmiHeaderBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Espera: 4–6 min",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFB45309)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "¡Hola, Amigoniano! 👋",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmiTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "¿Qué te apetece hoy?",
                        fontSize = 15.sp,
                        color = AmiTextSecondary
                    )
                }
            }
        }

        // Section 1: Más Populares en Campus
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔥", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Más Populares en\nCampus",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmiTextPrimary,
                        lineHeight = 22.sp
                    )
                }
                Text(
                    text = "TOP DE LA\nSEMANA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D9488),
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Horizontal Product Cards
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                val popularProducts = productsList.take(2)
                items(popularProducts) { product ->
                    ProductCard(
                        product = product,
                        onAddToCart = { onAddToCart(product) }
                    )
                }
            }
        }

        // Section 2: Explorar por Categorías
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Explorar por Categorías",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmiTextPrimary
                )
                Text(
                    text = "${categoriesList.size} categorías",
                    fontSize = 13.sp,
                    color = AmiTextSecondary
                )
            }
        }

        // Categories List
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                categoriesList.forEach { category ->
                    CategoryItem(
                        category = category,
                        onClick = { onCategoryClick(category) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(130.dp)) }
    }
}

@Composable
fun CategoryDetailView(
    category: Category,
    onBack: () -> Unit,
    onAddToCart: (ProductItem) -> Unit
) {
    var selectedSubcategory by remember { mutableStateOf("Todos") }
    val categoryProducts = productsList.filter {
        it.categoryId == category.id && (selectedSubcategory == "Todos" || it.subcategory == selectedSubcategory)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(36.dp)) }

        // Category Top Bar
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
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = AmiGreenDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFEDD5), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔥", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "100% Granos de Origen",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9A3412)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFF1F5F9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = AmiTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Category Title & Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmiTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "☕", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "8 variedades artesanales preparadas al instante con granos colombianos",
                    fontSize = 13.sp,
                    color = AmiTextSecondary
                )
            }
        }

        // Subcategories Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(category.subcategories) { subcat ->
                    val isSelected = subcat == selectedSubcategory
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) AmiGreenDark else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedSubcategory = subcat }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = subcat,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else AmiTextPrimary
                        )
                    }
                }
            }
        }

        // Promo Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDD5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFEA580C), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⚡", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Energía para parciales ⚡",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF9A3412)
                            )
                            Text(
                                text = "Pide con leche de avena por solo +$1.500 COP",
                                fontSize = 11.sp,
                                color = Color(0xFFC2410C)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF9A3412),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Products List
        items(categoryProducts, key = { it.id }) { product ->
            CategoryProductCard(
                product = product,
                onAddToCart = { onAddToCart(product) }
            )
        }

        item { Spacer(modifier = Modifier.height(130.dp)) }
    }
}

@Composable
fun CategoryProductCard(
    product: ProductItem,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.TopStart
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalCafe,
                        contentDescription = null,
                        tint = AmiGreenDark,
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center)
                    )

                    if (!product.tag.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .background(Color(0xFFFEE2E2), RoundedCornerShape(6.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = product.tag,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = product.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AmiTextPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = product.description,
                        fontSize = 12.sp,
                        color = AmiTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = product.priceFormatted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AmiGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AmiGreenDark, CircleShape)
                    .clickable { onAddToCart() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductItem,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.TopStart
            ) {
                if (!product.tag.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = product.tag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = product.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AmiTextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = product.description,
                fontSize = 12.sp,
                color = AmiTextSecondary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.priceFormatted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AmiGreenDark
                )

                Button(
                    onClick = onAddToCart,
                    colors = ButtonDefaults.buttonColors(containerColor = AmiGreenDark),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "+ Añadir", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(category.iconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AmiTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.subtitle,
                            fontSize = 12.sp,
                            color = AmiTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = category.badge,
                                fontSize = 10.sp,
                                color = AmiTextSecondary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AmiTextSecondary
            )
        }
    }
}
