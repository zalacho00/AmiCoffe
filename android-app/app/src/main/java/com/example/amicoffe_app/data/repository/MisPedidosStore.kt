package com.example.amicoffe_app.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Guarda en el teléfono los IDs de los pedidos del estudiante (no hay login). */
object MisPedidosStore {
    private const val PREFS = "amicoffe_prefs"
    private const val KEY = "mis_pedidos_ids"
    private const val MAX_PEDIDOS = 50

    private var prefs: SharedPreferences? = null
    private val _ids = MutableStateFlow<List<String>>(emptyList())
    val ids: StateFlow<List<String>> = _ids.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p
        _ids.value = p.getString(KEY, "")?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    fun agregar(id: String) {
        if (id in _ids.value) return
        guardar(listOf(id) + _ids.value)
    }

    fun quitar(idsAQuitar: Collection<String>) {
        guardar(_ids.value - idsAQuitar.toSet())
    }

    private fun guardar(lista: List<String>) {
        val recortada = lista.take(MAX_PEDIDOS)
        _ids.value = recortada
        prefs?.edit()?.putString(KEY, recortada.joinToString(","))?.apply()
    }
}

