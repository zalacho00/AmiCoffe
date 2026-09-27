package com.example.amicoffe_app.data.repository

import com.example.amicoffe_app.data.model.Producto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ProductoRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observarProductos(): Flow<List<Producto>> = callbackFlow {
        val listener = db.collection("productos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val productos = snapshot?.documents?.map { doc ->
                    Producto(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        categoria = doc.getString("categoria") ?: "",
                        precio = doc.getDouble("precio") ?: 0.0,
                        stock = (doc.getLong("stock") ?: 0L).toInt(),
                        estado = doc.getString("estado") ?: ""
                    )
                } ?: emptyList()
                trySend(productos)
            }
        awaitClose { listener.remove() }
    }
}