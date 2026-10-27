import React, { useState, useEffect, useCallback } from 'react';
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

/* ─── HU-05: solo estados activos en cocina ─────────────────── */
const ESTADOS_ACTIVOS = ['en_espera', 'en_preparacion', 'listo'];

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

const COL_META = {
  en_espera: {
    titulo: 'En Espera',
    emoji: '⏳',
    colorClass: 'col-espera',
    badgeClass: 'badge-espera',
  },
  en_preparacion: {
    titulo: 'En Preparación',
    emoji: '🔥',
    colorClass: 'col-prep',
    badgeClass: 'badge-prep',
  },
  listo: {
    titulo: 'Listo para Retirar',
    emoji: '✅',
    colorClass: 'col-listo',
    badgeClass: 'badge-listo',
  },
};

/* ─── Helpers ──────────────────────────────────────────────── */
function formatHora(ts) {
  if (!ts) return 'Recién creado';
  const date = ts.toDate ? ts.toDate() : new Date(ts);
  return date.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' });
}

/** Hook: tiempo transcurrido en vivo (actualiza cada segundo) */
function useElapsed(ts) {
  const [elapsed, setElapsed] = useState('');

  useEffect(() => {
    if (!ts) {
      setElapsed('Recién entrante');
      return;
    }
    const getDate = () => (ts.toDate ? ts.toDate() : new Date(ts));
    const calc = () => {
      const diff = Math.floor((Date.now() - getDate().getTime()) / 1000);
      if (diff < 60) return `${diff}s`;
      const m = Math.floor(diff / 60);
      const s = diff % 60;
      return `${m}m ${s < 10 ? '0' : ''}${s}s`;
    };
    setElapsed(calc());
    const id = setInterval(() => setElapsed(calc()), 1000);
    return () => clearInterval(id);
  }, [ts]);

  return elapsed;
}

function elapsedMinutes(ts) {
  if (!ts) return 0;
  const date = ts.toDate ? ts.toDate() : new Date(ts);
  return (Date.now() - date.getTime()) / 60000;
}

/* ─── Tarjeta individual ────────────────────────────────────── */
function OrderCard({ pedido, onAdvance, onCancel, advancing }) {
  const elapsed = useElapsed(pedido.fecha_creacion);
  const mins = elapsedMinutes(pedido.fecha_creacion);
  const enCurso = pedido.estado !== 'listo';
  const urgente = enCurso && mins > 10;
  const advertencia = enCurso && mins > 5 && mins <= 10;
  const items = Array.isArray(pedido.items)
    ? pedido.items
    : pedido.items && typeof pedido.items === 'object'
      ? Object.values(pedido.items)
      : [];

  return (
    <div
      className={`kds-card${urgente ? ' kds-card--urgente' : advertencia ? ' kds-card--advertencia' : ''}`}
    >
      <div className="kds-card-header">
        <span className="kds-card-code">{pedido.codigo_retiro ?? '—'}</span>
        <span className={`kds-card-timer${urgente ? ' timer-urgente' : advertencia ? ' timer-advertencia' : ''}`}>
          {elapsed}
        </span>
      </div>

      <div className="kds-card-meta">
        <span className="kds-card-hora">🕐 {formatHora(pedido.fecha_creacion)}</span>
        <span className="kds-card-student">{pedido.nombre_estudiante ?? 'Cliente'}</span>
      </div>

      <ul className="kds-items-list">
        {items.map((item, idx) => (
          <li key={idx} className="kds-item-row">
            <span className="kds-item-qty">{item.cantidad}×</span>
            <span className="kds-item-name">{item.nombre_producto}</span>
            {item.notas && <span className="kds-item-nota">({item.notas})</span>}
          </li>
        ))}
      </ul>

      {BOTON_LABEL[pedido.estado] && (
        <button
          id={`advance-btn-${pedido.id}`}
          className={`kds-btn-advance${
            pedido.estado === 'en_espera' ? ' btn-start' : pedido.estado === 'listo' ? ' btn-deliver' : ' btn-ready'
          }`}
          onClick={() => onAdvance(pedido)}
          disabled={advancing === pedido.id}
        >
          {advancing === pedido.id ? (
            <span className="btn-spinner" />
          ) : (
            <>{BOTON_LABEL[pedido.estado]} <span className="btn-arrow">→</span></>
          )}
        </button>
      )}
        <button
          id={`cancel-btn-${pedido.id}`}
          className="kds-btn-cancel"
          onClick={() => onCancel(pedido)}
          disabled={advancing === pedido.id}
      >
        Cancelar pedido
      </button>
    </div>
  );
}

/* ─── Panel principal ──────────────────────────────────────── */
export default function KdsPanel() {
  const [pedidos, setPedidos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');
  const [indexUrl, setIndexUrl] = useState('');
  const [avisoMsg, setAvisoMsg] = useState('');
  const [advancing, setAdvancing] = useState(null);
  const [liveOk, setLiveOk] = useState(false);
  const [lastUpdate, setLastUpdate] = useState(null);

  /* ── HU-05: onSnapshot — query exacto según criterios de aceptación ── */
  useEffect(() => {
    const q = query(
      collection(db, 'pedidos'),
      where('estado', 'in', ESTADOS_ACTIVOS),  // ["en_espera", "en_preparacion"]
      orderBy('fecha_creacion', 'asc')          // orden cronológico estable (fecha nunca se modifica)
    );

    const unsub = onSnapshot(
      q,
      (snapshot) => {
        // Firestore mantiene la suscripción viva y notifica cambios automáticamente
        setPedidos(snapshot.docs.map((d) => ({ id: d.id, ...d.data() })));
        setLoading(false);
        setErrorMsg('');
        setIndexUrl('');
        setLiveOk(true);
        setLastUpdate(new Date());
      },
      (err) => {
        console.error('KDS onSnapshot error:', err);
        setLiveOk(false);
        const match = err.message ? err.message.match(/https:\/\/console\.firebase\.google\.com[^\s]+/) : null;
        if (match) {
          setIndexUrl(match[0]);
        }
        setErrorMsg(
          err.code === 'failed-precondition'
            ? 'Falta un índice compuesto en Firestore para este query.'
            : `Error al conectar con Firestore: ${err.message}`
        );
        setLoading(false);
      }
    );

    return () => unsub();
  }, []);

    /* ── HU-06: cambiar estado con transacción atómica (avanzar o cancelar) ── */
  const cambiarEstado = useCallback(
    async (pedido, nuevoEstado) => {
      if (!nuevoEstado || advancing) return;

      setAdvancing(pedido.id);
      const pedidoRef = doc(db, 'pedidos', pedido.id);
      const quien = auth.currentUser?.email ?? 'desconocido';

      try {
        await runTransaction(db, async (tx) => {
          const snap = await tx.get(pedidoRef);
          if (!snap.exists()) throw new Error('inexistente');

          const data = snap.data();
          // Otro cocinero ya movió este pedido → no pisar su cambio
          if (data.estado !== pedido.estado) throw new Error('ya_actualizado');
          const valido =
            nuevoEstado === 'cancelado'
              ? ESTADOS_ACTIVOS.includes(data.estado)
              : SIGUIENTE_ESTADO[data.estado] === nuevoEstado;
          if (!valido) throw new Error('ya_actualizado');

          const historial = [
            ...(data.historial_estados ?? []),
            { estado: nuevoEstado, fecha: Timestamp.now() },
          ];

          tx.update(pedidoRef, {
            estado: nuevoEstado,
            historial_estados: historial,
            actualizado_por: quien,
          });
        });
      } catch (err) {
        if (err.message === 'ya_actualizado') {
          setAvisoMsg(`ℹ️ El pedido ${pedido.codigo_retiro} ya fue actualizado por otro compañero.`);
        } else if (err.message === 'inexistente') {
          setAvisoMsg(`ℹ️ El pedido ${pedido.codigo_retiro} ya no existe.`);
        } else {
          console.error(err);
          setAvisoMsg('❌ No se pudo actualizar el pedido. Intenta de nuevo.');
        }
        setTimeout(() => setAvisoMsg(''), 4000);
      } finally {
        setAdvancing(null);
      }
    },
    [advancing]
  );

  const avanzarEstado = useCallback(
    (pedido) => cambiarEstado(pedido, SIGUIENTE_ESTADO[pedido.estado]),
    [cambiarEstado]
  );

  const cancelarPedido = useCallback(
    (pedido) => {
      if (window.confirm(`¿Cancelar el pedido ${pedido.codigo_retiro ?? ''}? Esta acción no se puede deshacer.`)) {
        cambiarEstado(pedido, 'cancelado');
      }
    },
    [cambiarEstado]
  );

  const grupos = {
    en_espera: pedidos.filter((p) => p.estado === 'en_espera'),
    en_preparacion: pedidos.filter((p) => p.estado === 'en_preparacion'),
    listo: pedidos.filter((p) => p.estado === 'listo'),
  };

  if (loading) {
    return (
      <div className="kds-loader">
        <div className="kds-spinner" />
        <p>Conectando con Firestore…</p>
      </div>
    );
  }

  return (
    <div className="kds-root">
      {/* Barra de estado live */}
      <div className="kds-statusbar">
        <span className={`kds-live-dot ${liveOk ? 'live-ok' : 'live-err'}`} />
        <span className="kds-live-label">
          {liveOk ? 'Tiempo real · Firestore onSnapshot activo' : 'Sin conexión'}
        </span>
        {lastUpdate && (
          <span className="kds-last-update">
            Actualizado: {lastUpdate.toLocaleTimeString('es-CO')}
          </span>
        )}
        <span className="kds-total-badge">
          {pedidos.length} pedido{pedidos.length !== 1 ? 's' : ''} activo{pedidos.length !== 1 ? 's' : ''}
        </span>
      </div>

      {errorMsg && (
        <div className="kds-alert kds-alert--error" role="alert">
          <div>⚠️ {errorMsg}</div>
          {indexUrl && (
            <div style={{ marginTop: '10px' }}>
              <a
                href={indexUrl}
                target="_blank"
                rel="noopener noreferrer"
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '6px',
                  padding: '7px 14px',
                  background: '#991b1b',
                  color: '#ffffff',
                  borderRadius: '6px',
                  textDecoration: 'none',
                  fontWeight: '700',
                  fontSize: '12.5px',
                }}
              >
                Crear índice compuesto en Firebase Console ↗
              </a>
            </div>
          )}
        </div>
      )}
      {avisoMsg && (
        <div className="kds-alert kds-alert--info" role="status">{avisoMsg}</div>
      )}

      {/* Tablero kanban HU-05 */}
      <div className="kds-board">
        {Object.entries(COL_META).map(([estado, meta]) => {
          const lista = grupos[estado] ?? [];
          return (
            <section
              key={estado}
              className={`kds-column ${meta.colorClass}`}
              aria-label={`Columna ${meta.titulo}`}
            >
              <div className="kds-col-header">
                <div className="kds-col-title">
                  <span className="kds-col-emoji">{meta.emoji}</span>
                  <h3 className="kds-col-name">{meta.titulo}</h3>
                </div>
                <span className={`kds-col-count ${meta.badgeClass}`}>{lista.length}</span>
              </div>

              <div className="kds-col-body">
                {lista.length === 0 ? (
                  <div className="kds-empty">
                    <span className="kds-empty-icon">✅</span>
                    <p>Sin pedidos</p>
                  </div>
                ) : (
                  lista.map((pedido) => (
                    <OrderCard
                      key={pedido.id}
                      pedido={pedido}
                      onAdvance={avanzarEstado}
                      onCancel={cancelarPedido}
                      advancing={advancing}
                    />
                  ))
                )}
              </div>
            </section>
          );
        })}
      </div>

      {/* Referencia técnica HU-05 */}
      <p className="kds-footnote">
        ★ HU-05 · Query:{' '}
        <code>where("estado","in",["en_espera","en_preparacion","listo"]) · orderBy("fecha_creacion","asc")</code>
        {' '}— <code>fecha_creacion</code> se fija con <code>serverTimestamp()</code> al crear el pedido y nunca se modifica.
      </p>
    </div>
  );
}