const { onCall, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

/**
 * HU-07: Cloud Function Callable para actualizar el inventario en tiempo real
 * Protegida por custom claim 'administrador'
 */
exports.actualizarStock = onCall(async (request) => {
  // 1. Verificación de autenticación y Custom Claim 'administrador'
  if (!request.auth) {
    throw new HttpsError(
      "unauthenticated",
      "Debe iniciar sesión para realizar esta operación."
    );
  }

  const token = request.auth.token;
  if (!token.administrador && token.role !== "admin") {
    throw new HttpsError(
      "permission-denied",
      "Se requieren permisos de Administrador para modificar el inventario."
    );
  }

  const { productoId, nuevoStock, tipoCambio = "ajuste_manual" } = request.data;

  // Validaciones básicas de entrada
  if (!productoId || typeof productoId !== "string") {
    throw new HttpsError(
      "invalid-argument",
      "El parámetro 'productoId' es obligatorio."
    );
  }

  if (typeof nuevoStock !== "number" || isNaN(nuevoStock)) {
    throw new HttpsError(
      "invalid-argument",
      "El parámetro 'nuevoStock' debe ser un número válido."
    );
  }

  // 🟢 Criterio de Aceptación 3: Si el nuevo stock es negativo, rechaza la operación antes de escribir nada.
  if (nuevoStock < 0) {
    throw new HttpsError(
      "invalid-argument",
      "Operación rechazada: El stock no puede ser negativo."
    );
  }

  const productoRef = db.collection("productos").doc(productoId);
  const auditoriaRef = db.collection("auditoria_inventario").doc();

  try {
    // 🟢 Criterio de Aceptación 2: Ejecución atómica en una runTransaction
    const resultado = await db.runTransaction(async (transaction) => {
      const productoDoc = await transaction.get(productoRef);

      if (!productoDoc.exists) {
        throw new HttpsError(
          "not-found",
          `El producto con ID '${productoId}' no existe.`
        );
      }

      const productoData = productoDoc.data();
      const stockAnterior = productoData.stock ?? 0;

      // Recalcular estado y visibilidad en la misma escritura
      let nuevoEstado = "Disponible";
      let disponible = true;

      if (nuevoStock === 0) {
        nuevoEstado = "Agotado";
        disponible = false; // Se oculta automáticamente en la app cliente vía query/onSnapshot
      } else if (nuevoStock <= 15) {
        nuevoEstado = "Bajo Stock";
        disponible = true;
      }

      // Actualizar documento de producto
      transaction.update(productoRef, {
        stock: nuevoStock,
        estado: nuevoEstado,
        disponible: disponible,
        actualizadoEn: admin.firestore.FieldValue.serverTimestamp(),
      });

      // Crear documento en auditoria_inventario
      transaction.set(auditoriaRef, {
        producto_id: productoId,
        nombre_producto: productoData.nombre || productoId,
        tipo_cambio: tipoCambio,
        stock_anterior: stockAnterior,
        stock_nuevo: nuevoStock,
        administrador: token.email || token.uid || "admin",
        fecha: admin.firestore.FieldValue.serverTimestamp(),
      });

      return { stockAnterior, stockNuevo: nuevoStock, nuevoEstado, disponible };
    });

    return {
      success: true,
      mensaje: "Inventario y auditoría actualizados correctamente.",
      data: resultado,
    };
  } catch (error) {
    if (error instanceof HttpsError) {
      throw error;
    }
    console.error("Error en actualizarStock transaction:", error);
    throw new HttpsError(
      "internal",
      `Error al procesar la actualización de inventario: ${error.message}`
    );
  }
});
