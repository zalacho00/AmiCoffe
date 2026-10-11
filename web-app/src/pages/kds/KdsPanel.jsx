import React, { useState, useEffect, useMemo } from 'react';
import {
  collection,
  query,
  where,
  orderBy,
  onSnapshot,
  doc,
  runTransaction,
  Timestamp,
} from 'firebase/firestore';
import { db, auth } from '../../firebase/config';
import './KdsPanel.css';

const SIGUIENTE_ESTADO = {
  en_espera: 'en_preparacion',
  en_preparacion: 'listo',
  listo: 'entregado',
};

const BOTON_LABEL = {
  en_espera: 'Iniciar preparación',
  en_preparacion: 'Marcar listo',
  listo: 'Marcar entregado',
};

const COLUMNAS = [
  { estado: 'en_espera', titulo: 'En espera', badgeClass: 'en-espera' },
  { estado: 'en_preparacion', titulo: 'En preparación', badgeClass: 'en-preparacion' },
  { estado: 'listo', titulo: 'Listo para retirar', badgeClass: 'listo-retiro' },
];

function formatHora(ts) {
  if (!ts) return '—';
  const date = ts.toDate ? ts.toDate() : new Date(ts);
  return date.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' });
}

export default function KdsPanel() {
  const [pedidos, setPedidos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');
  const [avisoMsg, setAvisoMsg] = useState('');

  // ── Cola en tiempo real (HU-05): excluye solo entregado/cancelado ──
  useEffect(() => {
    const q = query(
      collection(db, 'pedidos'),
      where('estado', 'in', ['en_espera', 'en_preparacion', 'listo']),
      orderBy('fecha_creacion', 'asc')
    );

    const unsubscribe = onSnapshot(
      q,
      (snapshot) => {
        setPedidos(snapshot.docs.map((d) => ({ id: d.id, ...d.data() })));
        setLoading(false);
        setErrorMsg('');
      },
      (err) => {
        console.error('Error leyendo pedidos:', err);
        setErrorMsg(
          'No se pudo cargar la cola. Si la consola del navegador menciona un índice faltante, usa el link que Firestore ofrece ahí para crearlo (tarda ~1 min).'
        );
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, []);

  const pedidosPorColumna = useMemo(() => {
    const agrupado = { en_espera: [], en_preparacion: [], listo: [] };
    pedidos.forEach((p) => {
      if (agrupado[p.estado]) agrupado[p.estado].push(p);
    });
    return agrupado;
  }, [pedidos]);

  // ── Avanzar estado (HU-06), con transacción atómica para concurrencia ──
  const avanzarEstado = async (pedido) => {
    const nuevoEstado = SIGUIENTE_ESTADO[pedido.estado];
    if (!nuevoEstado) return;

    const pedidoRef = doc(db, 'pedidos', pedido.id);
    const quien = auth.currentUser?.email || 'desconocido';

    try {
      await runTransaction(db, async (transaction) => {
        const snap = await transaction.get(pedidoRef);
        if (!snap.exists()) throw new Error('inexistente');

        const data = snap.data();
        const siguienteReal = SIGUIENTE_ESTADO[data.estado];

        if (siguienteReal !== nuevoEstado) {
          // Otro miembro de cocina ya avanzó este pedido primero.
          throw new Error('ya_actualizado');
        }

        const historial = [
          ...(data.historial_estados || []),
          { estado: nuevoEstado, fecha: Timestamp.now() }, // ⚠️ no se puede usar serverTimestamp() dentro de un array
        ];

        transaction.update(pedidoRef, {
          estado: nuevoEstado,
          historial_estados: historial,
          actualizado_por: quien,
        });
      });
    } catch (err) {
      if (err.message === 'ya_actualizado') {
        setAvisoMsg(`El pedido ${pedido.codigo_retiro} ya fue actualizado por otro compañero.`);
        setTimeout(() => setAvisoMsg(''), 3500);
      } else if (err.message === 'inexistente') {
        setAvisoMsg(`El pedido ${pedido.codigo_retiro} ya no existe.`);
        setTimeout(() => setAvisoMsg(''), 3500);
      } else {
        console.error(err);
        alert('❌ No se pudo actualizar el pedido. Intenta de nuevo.');
      }
    }
  };

  if (loading) {
    return <div style={{ padding: '32px', textAlign: 'center' }}>Cargando cola de pedidos…</div>;
  }

  return (
    <div>
      {errorMsg && <div className="auth-error-alert" style={{ marginBottom: 16 }}>{errorMsg}</div>}
      {avisoMsg && <div className="kds-aviso">{avisoMsg}</div>}

      <div className="kds-board">
        {COLUMNAS.map((col) => {
          const lista = pedidosPorColumna[col.estado];
          return (
            <div key={col.estado} className="kds-column">
              <div className="kds-column-header">
                <span className={`status-badge ${col.badgeClass}`}>{col.titulo}</span>
                <span className="kds-column-count">{lista.length}</span>
              </div>

              <div className="kds-column-body">
                {lista.length === 0 && (
                  <div className="kds-empty-column">Sin pedidos aquí.</div>
                )}

                {lista.map((pedido) => (
                  <div key={pedido.id} className="kds-order-card">
                    <div className="kds-order-header">
                      <span className="kds-order-code">{pedido.codigo_retiro}</span>
                      <span className="kds-order-time">{formatHora(pedido.fecha_creacion)}</span>
                    </div>

                    <div className="kds-order-student">{pedido.nombre_estudiante}</div>

                    <ul className="kds-items-list">
                      {(pedido.items || []).map((item, idx) => (
                        <li key={idx} className="kds-item-row">
                          <span className="kds-item-qty">{item.cantidad}×</span>
                          <span className="kds-item-name">{item.nombre_producto}</span>
                        </li>
                      ))}
                    </ul>

                    {BOTON_LABEL[pedido.estado] && (
                      <button
                        className="btn-primary kds-advance-btn"
                        onClick={() => avanzarEstado(pedido)}
                      >
                        {BOTON_LABEL[pedido.estado]} →
                      </button>
                    )}
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
