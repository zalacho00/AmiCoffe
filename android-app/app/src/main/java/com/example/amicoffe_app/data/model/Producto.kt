package com.example.amicoffe_app.data.model

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "",   // "bebidas" | "snacks" | "almuerzos"
    val precio: Double = 0.0,
    val stock: Int = 0,
    val estado: String = ""       // "disponible" | "agotado"
)