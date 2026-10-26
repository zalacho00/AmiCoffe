package com.example.amicoffe_app.data.model

data class ItemPedido(
    val productoId: String = "",
    val nombreProducto: String = "",
    val cantidad: Int = 1,
    val precioUnitario: Double = 0.0,
    val personalizacion: String = ""
)

data class Pedido(
    val id: String = "",
    val estudianteNombre: String = "Estudiante Amigoniano",
    val items: List<ItemPedido> = emptyList(),
    val total: Double = 0.0,
    val sede: String = "Campus Central",
    val instruccionesEspeciales: String = "",
    val estado: String = "en_preparacion", // "en_preparacion", "listo", "entregado"
    val codigoRetiro: String = "", // Código de 4 dígitos para retiro en barra (ej: "4829")
    val tiempoEstimadoMin: Int = 5,
    val fechaTimestamp: Long = System.currentTimeMillis()
)
