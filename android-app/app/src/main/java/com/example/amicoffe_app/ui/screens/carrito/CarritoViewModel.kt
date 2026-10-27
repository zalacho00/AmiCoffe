package com.example.amicoffe_app.ui.screens.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amicoffe_app.data.model.Pedido
import com.example.amicoffe_app.data.repository.CartManager
import com.example.amicoffe_app.data.repository.CartState
import com.example.amicoffe_app.data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.amicoffe_app.data.repository.ConfigRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import com.example.amicoffe_app.data.repository.MisPedidosStore

sealed class CarritoUiEvent {
    data class PedidoExitoso(val pedido: Pedido) : CarritoUiEvent()
    data class Error(val mensaje: String) : CarritoUiEvent()
}

class CarritoViewModel(
    private val pedidoRepository: PedidoRepository = PedidoRepository(),
    configRepository: ConfigRepository = ConfigRepository()
) : ViewModel() {

    val tiempoEspera: StateFlow<String> = configRepository.observarTiempoEspera()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "4-6")

    val cartState: StateFlow<CartState> = CartManager.cartState

    private val _eventFlow = MutableSharedFlow<CarritoUiEvent>()
    val eventFlow: SharedFlow<CarritoUiEvent> = _eventFlow.asSharedFlow()

    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando.asStateFlow()

    fun incrementarCantidad(productoId: String) {
        CartManager.incrementarCantidad(productoId)
    }

    fun decrementarCantidad(productoId: String) {
        CartManager.decrementarCantidad(productoId)
    }

    fun removerItem(productoId: String) {
        CartManager.removerItem(productoId)
    }

    fun vaciarCarrito() {
        CartManager.vaciarCarrito()
    }

    fun actualizarInstrucciones(notas: String) {
        CartManager.actualizarNotas(notas)
    }

    fun actualizarSede(sede: String) {
        CartManager.actualizarSede(sede)
    }

    fun actualizarNombre(nombre: String) {
        CartManager.actualizarNombre(nombre.take(40))
    }

    fun realizarPedidoRemoto() {
        if (_enviando.value) return   // evita crear dos pedidos con doble toque

        val currentState = cartState.value
        if (currentState.items.isEmpty()) {
            emitirError("El carrito está vacío")
            return
        }
        val nombre = currentState.nombreEstudiante.trim()
        if (nombre.isEmpty()) {
            emitirError("Escribe tu nombre para que la barra sepa a quién entregar el pedido")
            return
        }

        _enviando.value = true
        pedidoRepository.crearPedidoRemoto(
            cartState = currentState.copy(nombreEstudiante = nombre),
            onSuccess = { pedidoCreado ->
                _enviando.value = false
                MisPedidosStore.agregar(pedidoCreado.id)
                CartManager.vaciarCarrito()
                viewModelScope.launch {
                    _eventFlow.emit(CarritoUiEvent.PedidoExitoso(pedidoCreado))
                }
            },
            onError = { error ->
                _enviando.value = false
                emitirError(error.message ?: "No se pudo crear el pedido")
            }
        )
    }

    private fun emitirError(mensaje: String) {
        viewModelScope.launch {
            _eventFlow.emit(CarritoUiEvent.Error(mensaje))
        }
    }
}