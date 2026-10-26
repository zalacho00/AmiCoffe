package com.example.amicoffe_app.data.model

data class CartItem(
    val producto: Producto,
    var cantidad: Int = 1,
    var personalizacion: String = ""
) {
    val precioSubtotal: Double
        get() = producto.precio * cantidad
}
