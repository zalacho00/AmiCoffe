package com.example.amicoffe_app.data.repository

import com.example.amicoffe_app.data.model.CartItem
import com.example.amicoffe_app.data.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CartState(
    val items: List<CartItem> = emptyList(),
    val sedeSeleccionada: String = "Campus Central",
    val instruccionesEspeciales: String = "",
    val nombreEstudiante: String = ""
) {
    val totalItemsCount: Int
        get() = items.sumOf { it.cantidad }

    val subtotal: Double
        get() = items.sumOf { it.precioSubtotal }

    val total: Double
        get() = subtotal
}

object CartManager {

    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    fun agregarProducto(producto: Producto, personalizacion: String = "") {
        _cartState.update { currentState ->
            val listaExistente = currentState.items.toMutableList()
            val index = listaExistente.indexOfFirst { it.producto.id == producto.id }

            if (index >= 0) {
                val itemActual = listaExistente[index]
                listaExistente[index] = itemActual.copy(cantidad = itemActual.cantidad + 1)
            } else {
                val notaPersonalizada = when (personalizacion) {
                    "" -> defaultPersonalizacion(producto)
                    else -> personalizacion
                }
                listaExistente.add(CartItem(producto = producto, cantidad = 1, personalizacion = notaPersonalizada))
            }

            currentState.copy(items = listaExistente)
        }
    }

    private fun defaultPersonalizacion(producto: Producto): String {
        return when (producto.categoria.lowercase()) {
            "bebidas" -> "Personalizado: Con hielo y azúcar"
            "snacks" -> "Tostado al grill"
            "almuerzos" -> "Recién servido"
            else -> "Preparación estándar"
        }
    }

    fun incrementarCantidad(productoId: String) {
        _cartState.update { currentState ->
            val listaNueva = currentState.items.map { item ->
                if (item.producto.id == productoId) {
                    item.copy(cantidad = item.cantidad + 1)
                } else item
            }
            currentState.copy(items = listaNueva)
        }
    }

    fun decrementarCantidad(productoId: String) {
        _cartState.update { currentState ->
            val listaNueva = currentState.items.mapNotNull { item ->
                if (item.producto.id == productoId) {
                    if (item.cantidad > 1) item.copy(cantidad = item.cantidad - 1) else null
                } else item
            }
            currentState.copy(items = listaNueva)
        }
    }

    fun removerItem(productoId: String) {
        _cartState.update { currentState ->
            val listaNueva = currentState.items.filterNot { it.producto.id == productoId }
            currentState.copy(items = listaNueva)
        }
    }

    fun vaciarCarrito() {
        _cartState.update {
            CartState(
                sedeSeleccionada = it.sedeSeleccionada,
                nombreEstudiante = it.nombreEstudiante
            )
        }
    }

    fun actualizarNotas(notas: String) {
        _cartState.update {
            it.copy(instruccionesEspeciales = notas)
        }
    }

    fun actualizarSede(sede: String) {
        _cartState.update {
            it.copy(sedeSeleccionada = sede)
        }
    }

    fun actualizarNombre(nombre: String) {
        _cartState.update {
            it.copy(nombreEstudiante = nombre)
        }
    }
}
