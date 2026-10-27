package com.example.amicoffe_app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ConfigRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /** Rango de espera que el admin define en "Cupos y Espera" (ej: "4-6"), en tiempo real. */
    fun observarTiempoEspera(): Flow<String> = callbackFlow {
        val listener = db.collection("configuracion_cupos").document("publico")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener   // conserva el último valor
                trySend(snapshot?.getString("tiempo_espera_estimado") ?: "4-6")
            }
        awaitClose { listener.remove() }
    }
}

