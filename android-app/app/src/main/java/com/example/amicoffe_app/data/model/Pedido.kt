package com.example.amicoffe_app.data.model

data class ItemPedido(
    val productoId: String = "",
    val nombreProducto: String = "",
    val cantidad: Int = 1,
    val precioUnitario: Double = 0.0,
    val personalizacion: String = ""      // en Firestore se guarda como "notas"
)

data class Pedido(
    val id: String = "",                  // ID automático del documento en Firestore
    val estudianteUid: String = "",       // uid de Auth anónimo (dueño del pedido)
    val estudianteNombre: String = "",    // en Firestore: nombre_estudiante
    val items: List<ItemPedido> = emptyList(),
    val total: Double = 0.0,
    val sede: String = "Campus Central",
    val instruccionesEspeciales: String = "",
    // "en_espera" | "en_preparacion" | "listo" | "entregado" | "cancelado"
    val estado: String = "en_espera",
    val codigoRetiro: String = "",        // 4 dígitos, único por día (ej: "4829")
    val dia: String = "",                 // "2026-10-26" en hora Colombia
    val franjaHoraria: String = "",       // "12:00 - 12:10"
    val tiempoEstimadoMin: Int = 5,
    val fechaTimestamp: Long = System.currentTimeMillis()  // fecha_creacion en milisegundos
)