package com.example.amicoffe_app.data.repository

import com.example.amicoffe_app.data.model.ItemPedido
import com.example.amicoffe_app.data.model.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.random.Random

class PedidoRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        // Almacenamiento local persistente durante la sesión para asegurar disponibilidad del código de retiro
        private val _pedidosLocales = MutableStateFlow<List<Pedido>>(emptyList())
    }

    /**
     * Crea un pedido remoto en Firestore (Colección 'pedidos') para la HU-03.
     * Genera un código único de 4 dígitos para retiro en barra.
     */
    fun crearPedidoRemoto(
        cartState: CartState,
        onSuccess: (Pedido) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val codigo4Digitos = Random.nextInt(1000, 10000).toString()
        val orderId = "AC-$codigo4Digitos"

        val itemsPedido = cartState.items.map { item ->
            ItemPedido(
                productoId = item.producto.id,
                nombreProducto = item.producto.nombre,
                cantidad = item.cantidad,
                precioUnitario = item.producto.precio,
                personalizacion = item.personalizacion
            )
        }

        val nuevoPedido = Pedido(
            id = orderId,
            estudianteNombre = "Estudiante Amigoniano",
            items = itemsPedido,
            total = cartState.total,
            sede = cartState.sedeSeleccionada,
            instruccionesEspeciales = cartState.instruccionesEspeciales,
            estado = "en_preparacion",
            codigoRetiro = codigo4Digitos,
            tiempoEstimadoMin = 5,
            fechaTimestamp = System.currentTimeMillis()
        )

        // Guardar localmente de inmediato para consultar código de retiro
        _pedidosLocales.value = listOf(nuevoPedido) + _pedidosLocales.value.filter { it.id != nuevoPedido.id }

        // Intenta guardar en Firestore
        db.collection("pedidos")
            .document(nuevoPedido.id)
            .set(nuevoPedido)
            .addOnSuccessListener {
                onSuccess(nuevoPedido)
            }
            .addOnFailureListener {
                // Si falla por red u offline, permite continuar devolviendo la orden localmente
                onSuccess(nuevoPedido)
            }
    }

    /**
     * Observa en tiempo real el estado de un pedido específico por su ID.
     */
    fun observarPedidoPorId(pedidoId: String): Flow<Pedido?> = callbackFlow {
        // Buscar local primero
        val local = _pedidosLocales.value.find { it.id == pedidoId }
        if (local != null) {
            trySend(local)
        }

        val listener = db.collection("pedidos").document(pedidoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(local)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val pedido = snapshot.toObject(Pedido::class.java)
                    trySend(pedido)
                } else {
                    trySend(local)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Observa todos los pedidos activos para la pestaña 'Mis Pedidos', combinando
     * Firestore y el almacenamiento local para garantía de visualización del código de retiro.
     */
    fun observarMisPedidos(): Flow<List<Pedido>> = callbackFlow {
        // Enviar locales de inicio
        trySend(_pedidosLocales.value)

        val listener = db.collection("pedidos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(_pedidosLocales.value)
                    return@addSnapshotListener
                }
                val remotas = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Pedido::class.java)
                } ?: emptyList()

                // Fusionar remotas y locales
                val combinadas = (remotas + _pedidosLocales.value)
                    .distinctBy { it.id }
                    .sortedByDescending { it.fechaTimestamp }

                trySend(combinadas)
            }
        awaitClose { listener.remove() }
    }
}
