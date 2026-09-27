package com.example.amicoffe_app.ui.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amicoffe_app.data.model.Producto
import com.example.amicoffe_app.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MenuViewModel(
    private val repository: ProductoRepository = ProductoRepository()
) : ViewModel() {

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observarProductos().collect { lista ->
                _productos.value = lista
                _cargando.value = false
            }
        }
    }

    fun productosPorCategoria(categoria: String): List<Producto> =
        _productos.value.filter { it.categoria == categoria && it.estado == "disponible" }
}