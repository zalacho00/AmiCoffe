package com.example.amicoffe_app.ui.screens.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amicoffe_app.data.model.Pedido
import com.example.amicoffe_app.data.repository.CartManager
import com.example.amicoffe_app.data.repository.CartState
import com.example.amicoffe_app.data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class CarritoUiEvent {
    data class PedidoExitoso(val pedido: Pedido) : CarritoUiEvent()
    data class Error(val mensaje: String) : CarritoUiEvent()
}

class CarritoViewModel(
    private val pedidoRepository: PedidoRepository = PedidoRepository()
) : ViewModel() {

    val cartState: StateFlow<CartState> = CartManager.cartState

    private val _eventFlow = MutableSharedFlow<CarritoUiEvent>()
    val eventFlow: SharedFlow<CarritoUiEvent> = _eventFlow.asSharedFlow()

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

    fun realizarPedidoRemoto() {
        val currentState = cartState.value
        if (currentState.items.isEmpty()) {
            viewModelScope.launch {
                _eventFlow.emit(CarritoUiEvent.Error("El carrito está vacío"))
            }
            return
        }

        pedidoRepository.crearPedidoRemoto(
            cartState = currentState,
            onSuccess = { pedidoCreado ->
                CartManager.vaciarCarrito()
                viewModelScope.launch {
                    _eventFlow.emit(CarritoUiEvent.PedidoExitoso(pedidoCreado))
                }
            },
            onError = { error ->
                viewModelScope.launch {
                    _eventFlow.emit(CarritoUiEvent.Error("Error al procesar pedido: ${error.localizedMessage}"))
                }
            }
        )
    }
}
