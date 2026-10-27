package com.example.amicoffe_app.data.repository

import android.util.Log
import com.example.amicoffe_app.data.model.ItemPedido
import com.example.amicoffe_app.data.model.Pedido
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

/** Error con un mensaje listo para mostrarle al estudiante. */
class PedidoException(mensaje: String) : Exception(mensaje)

/** El código de 4 dígitos ya existe hoy: se reintenta con otro. */
private class CodigoOcupadoException : Exception("codigo_ocupado")

class PedidoRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    // ───────────────────────── Auth anónimo ─────────────────────────

    private fun asegurarSesion(onOk: (String) -> Unit, onFail: (Exception) -> Unit) {
        val actual = auth.currentUser
        if (actual != null) {
            onOk(actual.uid)
            return
        }
        auth.signInAnonymously()
            .addOnSuccessListener { resultado -> onOk(resultado.user!!.uid) }
            .addOnFailureListener { onFail(it) }
    }

    // ───────────────────────── HU-03: crear pedido ─────────────────────────

    /**
     * Una sola transacción: descuenta stock, reserva el código de 4 dígitos del día,
     * suma 1 a la franja horaria y crea el pedido. Si algo falla, no se escribe nada.
     */
    fun crearPedidoRemoto(
        cartState: CartState,
        onSuccess: (Pedido) -> Unit,
        onError: (Exception) -> Unit
    ) {
        asegurarSesion(
            onOk = { uid -> intentarCrear(uid, cartState, 1, onSuccess, onError) },
            onFail = { onError(PedidoException("No se pudo iniciar la sesión. Revisa tu conexión.")) }
        )
    }

    private fun intentarCrear(
        uid: String,
        cartState: CartState,
        intento: Int,
        onSuccess: (Pedido) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val (dia, minutosDelDia) = ahoraColombia()
        val codigo = Random.nextInt(1000, 10000).toString()

        val pedidoRef = db.collection("pedidos").document()
        val codigoRef = db.collection("codigos_retiro").document("${dia}_$codigo")
        val publicoRef = db.collection("configuracion_cupos").document("publico")

        db.runTransaction { tx ->
            // ───── 1) TODAS las lecturas primero ─────
            val publico = tx.get(publicoRef)
            val cupo = (publico.getLong("cupo_maximo") ?: 15L).toInt()
            val duracion = (publico.getLong("duracion_franja_minutos") ?: 10L).toInt()

            val inicio = (minutosDelDia / duracion) * duracion
            val fin = inicio + duracion
            val franjaTexto = "${hhmm(inicio)} - ${hhmm(fin)}"
            val franjaId = "${dia}_${hhmm(inicio).replace(":", "")}-${hhmm(fin).replace(":", "")}"
            val franjaRef = db.collection("metricas_horas_pico").document(franjaId)

            val franja = tx.get(franjaRef)
            if (tx.get(codigoRef).exists()) throw CodigoOcupadoException()

            val productos = cartState.items.map { item ->
                item to tx.get(db.collection("productos").document(item.producto.id))
            }

            // ───── 2) Validaciones ─────
            val totalActual = (franja.getLong("total_pedidos") ?: 0L).toInt()
            if (totalActual >= cupo) {
                throw PedidoException("La franja $franjaTexto ya está llena. Intenta en unos minutos.")
            }

            productos.forEach { (item, snap) ->
                if (!snap.exists()) {
                    throw PedidoException("${item.producto.nombre} ya no está disponible.")
                }
                val stock = (snap.getLong("stock") ?: 0L).toInt()
                if (snap.getString("estado") != "disponible" || stock < item.cantidad) {
                    throw PedidoException("No hay suficiente stock de ${item.producto.nombre} (quedan $stock).")
                }
            }

            // ───── 3) Escrituras ─────
            // Precio siempre desde Firestore (no confiamos en el precio que tenía el carrito)
            var total = 0.0
            val itemsMap = productos.map { (item, snap) ->
                val precio = snap.getDouble("precio") ?: item.producto.precio
                total += precio * item.cantidad

                val nuevoStock = (snap.getLong("stock") ?: 0L).toInt() - item.cantidad
                tx.update(
                    snap.reference,
                    mapOf(
                        "stock" to nuevoStock,
                        "estado" to if (nuevoStock == 0) "agotado" else "disponible"
                    )
                )
                tx.set(
                    db.collection("auditoria_inventario").document(),
                    mapOf(
                        "producto_id" to item.producto.id,
                        "nombre_producto" to item.producto.nombre,
                        "tipo_cambio" to "venta",
                        "stock_anterior" to nuevoStock + item.cantidad,
                        "stock_nuevo" to nuevoStock,
                        "administrador" to "Pedido $codigo (app)",
                        "pedido_id" to pedidoRef.id,
                        "fecha" to FieldValue.serverTimestamp()
                    )
                )

                mapOf(
                    "producto_id" to item.producto.id,
                    "nombre_producto" to item.producto.nombre,
                    "cantidad" to item.cantidad,
                    "precio_unitario" to precio,
                    "notas" to item.personalizacion
                )
            }

            val nuevoTotal = totalActual + 1
            val saturada = nuevoTotal >= cupo
            if (franja.exists()) {
                tx.update(franjaRef, mapOf("total_pedidos" to nuevoTotal, "saturado" to saturada))
            } else {
                tx.set(
                    franjaRef,
                    mapOf(
                        "dia" to dia,
                        "franja_horaria" to franjaTexto,
                        "total_pedidos" to nuevoTotal,
                        "saturado" to saturada
                    )
                )
            }

            tx.set(codigoRef, mapOf("dia" to dia, "codigo" to codigo, "estudiante_uid" to uid))

            tx.set(
                pedidoRef,
                mapOf(
                    "estudiante_uid" to uid,
                    "nombre_estudiante" to cartState.nombreEstudiante,
                    "codigo_retiro" to codigo,
                    "items" to itemsMap,
                    "total" to total,
                    "sede" to cartState.sedeSeleccionada,
                    "instrucciones_especiales" to cartState.instruccionesEspeciales,
                    "dia" to dia,
                    "franja_horaria" to franjaTexto,
                    "estado" to "en_espera",
                    "historial_estados" to listOf(
                        mapOf("estado" to "en_espera", "fecha" to Timestamp.now())
                    ),
                    "actualizado_por" to "",
                    "fecha_creacion" to FieldValue.serverTimestamp()
                )
            )

            Pedido(
                id = pedidoRef.id,
                estudianteUid = uid,
                estudianteNombre = cartState.nombreEstudiante,
                items = cartState.items.map {
                    ItemPedido(
                        productoId = it.producto.id,
                        nombreProducto = it.producto.nombre,
                        cantidad = it.cantidad,
                        precioUnitario = it.producto.precio,
                        personalizacion = it.personalizacion
                    )
                },
                total = total,
                sede = cartState.sedeSeleccionada,
                instruccionesEspeciales = cartState.instruccionesEspeciales,
                estado = "en_espera",
                codigoRetiro = codigo,
                dia = dia,
                franjaHoraria = franjaTexto,
                fechaTimestamp = System.currentTimeMillis()
            )
        }
            .addOnSuccessListener { pedido -> onSuccess(pedido) }
            .addOnFailureListener { e ->
                Log.e("PedidoRepository", "Error creando pedido", e)
                val raiz = raizDe(e)
                when {
                    raiz is CodigoOcupadoException && intento < 5 ->
                        intentarCrear(uid, cartState, intento + 1, onSuccess, onError)

                    raiz is PedidoException -> onError(raiz)

                    raiz is FirebaseFirestoreException &&
                            raiz.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                        onError(PedidoException("Sin permiso para crear el pedido. Revisa que las reglas estén publicadas y el acceso anónimo activado."))

                    raiz is FirebaseFirestoreException &&
                            raiz.code == FirebaseFirestoreException.Code.UNAVAILABLE ->
                        onError(PedidoException("Sin conexión. Revisa tu internet e inténtalo de nuevo."))

                    else -> onError(PedidoException("No se pudo crear el pedido. Intenta de nuevo."))
                }
            }
    }

    // ───────────────────────── Lectura en tiempo real ─────────────────────────

    /** Observa un pedido por su ID (solo funciona para pedidos del propio uid). */
    fun observarPedidoPorId(pedidoId: String): Flow<Pedido?> = callbackFlow {
        var listener: ListenerRegistration? = null
        asegurarSesion(
            onOk = {
                listener = db.collection("pedidos").document(pedidoId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("PedidoRepository", "observarPedidoPorId", error)
                            trySend(null)
                            return@addSnapshotListener
                        }
                        trySend(if (snapshot != null && snapshot.exists()) snapshot.toPedido() else null)
                    }
            },
            onFail = { trySend(null) }
        )
        awaitClose { listener?.remove() }
    }

    /** 'Mis Pedidos': solo los pedidos del uid anónimo de este teléfono. */
    fun observarMisPedidos(): Flow<List<Pedido>> = callbackFlow {
        var listener: ListenerRegistration? = null
        asegurarSesion(
            onOk = { uid ->
                listener = db.collection("pedidos")
                    .whereEqualTo("estudiante_uid", uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("PedidoRepository", "observarMisPedidos", error)
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val lista = snapshot?.documents
                            ?.map { it.toPedido() }
                            ?.filter { !it.oculto }
                            ?.sortedByDescending { it.fechaTimestamp }
                            ?: emptyList()
                    }
            },
            onFail = { trySend(emptyList()) }
        )
        awaitClose { listener?.remove() }
    }


    /** "Borrar historial": oculta los pedidos finalizados del estudiante (el documento se conserva). */
    fun ocultarPedidos(ids: List<String>, onDone: (Boolean) -> Unit = {}) {
        if (ids.isEmpty()) {
            onDone(true)
            return
        }
        val batch = db.batch()
        ids.forEach { id ->
            batch.update(db.collection("pedidos").document(id), "oculto_estudiante", true)
        }
        batch.commit()
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener {
                Log.e("PedidoRepository", "ocultarPedidos", it)
                onDone(false)
            }
    }

    // ───────────────────────── Helpers ─────────────────────────

    private fun raizDe(e: Throwable): Throwable =
        generateSequence<Throwable>(e) { it.cause }
            .firstOrNull {
                it is PedidoException || it is CodigoOcupadoException || it is FirebaseFirestoreException
            } ?: e

    /** Fecha y minutos del día en hora de Colombia (no la zona del teléfono). */
    private fun ahoraColombia(): Pair<String, Int> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("America/Bogota"))
        val dia = String.format(
            Locale.US, "%04d-%02d-%02d",
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
        )
        return dia to (cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE))
    }

    private fun hhmm(minutos: Int): String =
        String.format(Locale.US, "%02d:%02d", (minutos / 60) % 24, minutos % 60)
}

/** Convierte un documento de Firestore (snake_case) al modelo de la app. */
private fun DocumentSnapshot.toPedido(): Pedido {
    val items = (get("items") as? List<*>)
        ?.mapNotNull { it as? Map<*, *> }
        ?.map { m ->
            ItemPedido(
                productoId = m["producto_id"] as? String ?: "",
                nombreProducto = m["nombre_producto"] as? String ?: "",
                cantidad = (m["cantidad"] as? Number)?.toInt() ?: 1,
                precioUnitario = (m["precio_unitario"] as? Number)?.toDouble() ?: 0.0,
                personalizacion = m["notas"] as? String ?: ""
            )
        } ?: emptyList()

    return Pedido(
        id = id,
        estudianteUid = getString("estudiante_uid") ?: "",
        estudianteNombre = getString("nombre_estudiante") ?: "",
        items = items,
        total = getDouble("total") ?: 0.0,
        sede = getString("sede") ?: "Campus Central",
        instruccionesEspeciales = getString("instrucciones_especiales") ?: "",
        estado = getString("estado") ?: "en_espera",
        codigoRetiro = getString("codigo_retiro") ?: "",
        dia = getString("dia") ?: "",
        franjaHoraria = getString("franja_horaria") ?: "",
        oculto = getBoolean("oculto_estudiante") ?: false,
        fechaTimestamp = getTimestamp("fecha_creacion", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?.toDate()?.time ?: System.currentTimeMillis()
    )
}