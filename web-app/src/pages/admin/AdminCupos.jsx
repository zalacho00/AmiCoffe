import React, { useState, useEffect, useRef } from 'react';
import {
  collection,
  query,
  where,
  onSnapshot,
  doc,
  setDoc,
  serverTimestamp,
} from 'firebase/firestore';
import { db, auth } from '../../firebase/config';

// configuracion_cupos/general  → parámetros privados (solo admin)
// configuracion_cupos/publico  → dato que verá el estudiante (tiempo de espera)
const GENERAL_REF = doc(db, 'configuracion_cupos', 'general');
const PUBLICO_REF = doc(db, 'configuracion_cupos', 'publico');

// Valores por defecto documentados (HU-08): se usan si aún no existe configuración.
const DEFAULTS = {
  cupo_maximo: 15,
  duracion_franja_minutos: 10,
  agrupamiento_automatico: true,
  tiempo_espera_estimado: '4-6',
};
const DURACIONES = [5, 10, 15];
const TIEMPOS_ESPERA = ['2-4', '4-6', '7-10', '12-15'];

// Fecha LOCAL "YYYY-MM-DD" (no UTC): en Colombia, después de las 7 PM
// toISOString() ya devolvería el día siguiente.
function fechaLocalISO(d = new Date()) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${dia}`;
}

function formatFecha(ts) {
  if (!ts) return '';
  const date = ts.toDate ? ts.toDate() : new Date(ts);
  return date.toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' });
}

export default function AdminCupos() {
  const [config, setConfig] = useState(null); // null = cargando
  const [configExiste, setConfigExiste] = useState(true);
  const [draft, setDraft] = useState({
    cupo_maximo: DEFAULTS.cupo_maximo,
    duracion_franja_minutos: DEFAULTS.duracion_franja_minutos,
    agrupamiento_automatico: DEFAULTS.agrupamiento_automatico,
  });
  const [franjas, setFranjas] = useState([]);
  const [hoy] = useState(() => fechaLocalISO());
  const [errorMsg, setErrorMsg] = useState('');
  const [errorFranjas, setErrorFranjas] = useState('');
  const [aviso, setAviso] = useState('');
  const [guardando, setGuardando] = useState(false);
  const primeraCarga = useRef(true);

  // ── Configuración (general + publico) en tiempo real ──
  useEffect(() => {
    const unsubscribe = onSnapshot(
      collection(db, 'configuracion_cupos'),
      (snap) => {
        const docs = {};
        snap.docs.forEach((d) => { docs[d.id] = d.data(); });
        const general = docs.general || {};
        const publico = docs.publico || {};

        const normalizada = {
          cupo_maximo:
            Number.isInteger(general.cupo_maximo) && general.cupo_maximo > 0
              ? general.cupo_maximo
              : DEFAULTS.cupo_maximo,
          duracion_franja_minutos: DURACIONES.includes(general.duracion_franja_minutos)
            ? general.duracion_franja_minutos
            : DEFAULTS.duracion_franja_minutos,
          agrupamiento_automatico:
            typeof general.agrupamiento_automatico === 'boolean'
              ? general.agrupamiento_automatico
              : DEFAULTS.agrupamiento_automatico,
          tiempo_espera_estimado: TIEMPOS_ESPERA.includes(publico.tiempo_espera_estimado)
            ? publico.tiempo_espera_estimado
            : DEFAULTS.tiempo_espera_estimado,
          actualizado_por: general.actualizado_por || null,
          fecha_actualizacion: general.fecha_actualizacion || null,
        };

        setConfigExiste(Boolean(docs.general));
        setConfig(normalizada);
        setErrorMsg('');

        // El borrador se inicializa solo en la primera carga, para no
        // pisar lo que el admin esté editando.
        if (primeraCarga.current) {
          primeraCarga.current = false;
          setDraft({
            cupo_maximo: normalizada.cupo_maximo,
            duracion_franja_minutos: normalizada.duracion_franja_minutos,
            agrupamiento_automatico: normalizada.agrupamiento_automatico,
          });
        }
      },
      (err) => {
        console.error('Error leyendo configuracion_cupos:', err);
        setErrorMsg('No se pudo cargar la configuración de cupos.');
      }
    );
    return () => unsubscribe();
  }, []);

  // ── Ocupación de franjas de HOY en tiempo real ──
  useEffect(() => {
    const q = query(collection(db, 'metricas_horas_pico'), where('dia', '==', hoy));
    const unsubscribe = onSnapshot(
      q,
      (snap) => {
        const lista = snap.docs
          .map((d) => d.data())
          .sort((a, b) => String(a.franja_horaria).localeCompare(String(b.franja_horaria)));
        setFranjas(lista);
        setErrorFranjas('');
      },
      (err) => {
        console.error('Error leyendo metricas_horas_pico:', err);
        setErrorFranjas('No se pudo cargar la ocupación de franjas.');
      }
    );
    return () => unsubscribe();
  }, [hoy]);

  const mostrarAviso = (texto) => {
    setAviso(texto);
    setTimeout(() => setAviso(''), 3000);
  };

  // ── Guardar parámetros (cupo, duración, batching) ──
  const guardarParametros = async () => {
    const cupo = Number(draft.cupo_maximo);
    if (!Number.isInteger(cupo) || cupo <= 0) {
      setErrorMsg('El cupo máximo debe ser un número entero mayor a 0.');
      return;
    }
    if (!DURACIONES.includes(draft.duracion_franja_minutos)) {
      setErrorMsg('La duración de la franja debe ser 5, 10 o 15 minutos.');
      return;
    }

    setErrorMsg('');
    setGuardando(true);
    try {
      await setDoc(
        GENERAL_REF,
        {
          cupo_maximo: cupo,
          duracion_franja_minutos: draft.duracion_franja_minutos,
          agrupamiento_automatico: draft.agrupamiento_automatico,
          actualizado_por: auth.currentUser?.email || 'desconocido',
          fecha_actualizacion: serverTimestamp(),
        },
        { merge: true }
      );
      mostrarAviso('✅ Parámetros guardados. Aplican a los pedidos nuevos.');
    } catch (err) {
      console.error(err);
      setErrorMsg('No se pudieron guardar los parámetros. Intenta de nuevo.');
    } finally {
      setGuardando(false);
    }
  };

  const descartarCambios = () => {
    setErrorMsg('');
    setDraft({
      cupo_maximo: config.cupo_maximo,
      duracion_franja_minutos: config.duracion_franja_minutos,
      agrupamiento_automatico: config.agrupamiento_automatico,
    });
  };

  // ── Estimación rápida de espera: se aplica en vivo ──
  const cambiarEspera = async (tiempo) => {
    try {
      await setDoc(
        PUBLICO_REF,
        { tiempo_espera_estimado: tiempo, fecha_actualizacion: serverTimestamp() },
        { merge: true }
      );
    } catch (err) {
      console.error(err);
      setErrorMsg('No se pudo actualizar el tiempo de espera.');
    }
  };

  if (config === null) {
    return (
      <div className="admin-page">
        {errorMsg ? <div className="auth-error-alert">{errorMsg}</div> : 'Cargando configuración…'}
      </div>
    );
  }

  const dirty =
    Number(draft.cupo_maximo) !== config.cupo_maximo ||
    draft.duracion_franja_minutos !== config.duracion_franja_minutos ||
    draft.agrupamiento_automatico !== config.agrupamiento_automatico;

  // Franjas con estado calculado contra el cupo VIGENTE (el guardado, no el borrador)
  const franjasUI = franjas.map((f) => {
    const total = f.total_pedidos || 0;
    const max = config.cupo_maximo;
    const llena = f.saturado === true || total >= max;
    const casiLlena = !llena && total >= Math.ceil(max * 0.75);
    return {
      franja: f.franja_horaria,
      total,
      max,
      status: llena ? 'Lleno' : casiLlena ? 'Casi Lleno' : 'Disponible',
      pct: Math.min(100, (total / max) * 100),
      color: llena ? '#ef4444' : casiLlena ? '#f59e0b' : '#10b981',
    };
  });

  const ultimaAct = formatFecha(config.fecha_actualizacion);

  return (
    <div className="admin-page">
      <div className="page-header-title">
        <div>
          <h2>Control de Cupos y Tiempos de Espera</h2>
          <p className="subtext">Ajusta la capacidad operativa para evitar colas en la cafetería del Campus</p>
        </div>
      </div>

      {errorMsg && <div className="auth-error-alert">{errorMsg}</div>}

      {aviso && (
        <div
          style={{
            background: '#dcfce7',
            border: '1px solid #bbf7d0',
            color: '#166534',
            padding: '10px 14px',
            borderRadius: 'var(--radius-sm)',
            fontSize: 13,
            fontWeight: 600,
            marginBottom: 16,
          }}
        >
          {aviso}
        </div>
      )}

      {!configExiste && (
        <div
          style={{
            background: '#fff7ed',
            border: '1px solid #fdba74',
            color: '#9a3412',
            padding: '10px 14px',
            borderRadius: 'var(--radius-sm)',
            fontSize: 13,
            fontWeight: 600,
            marginBottom: 16,
          }}
        >
          ⚠️ Aún no hay configuración guardada: se muestran valores por defecto (cupo{' '}
          {DEFAULTS.cupo_maximo}, franjas de {DEFAULTS.duracion_franja_minutos} min). Guarda los
          parámetros para crearla.
        </div>
      )}

      {/* Banner de Estado en Vivo (lo que verá el estudiante) */}
      <div className="live-status-hero">
        <div className="live-status-main">
          <span className="live-badge-glow">🔴 EN VIVO</span>
          <h3>Tiempo de Espera Mostrado en App: <strong>{config.tiempo_espera_estimado} min</strong></h3>
          <p>Los estudiantes ven esta estimación en tiempo real antes de añadir items al carrito.</p>
        </div>
        <div className="live-status-actions">
          <label>Cambiar estimación rápida:</label>
          <div className="quick-time-buttons">
            {TIEMPOS_ESPERA.map((t) => (
              <button
                key={t}
                className={`time-btn ${config.tiempo_espera_estimado === t ? 'active' : ''}`}
                onClick={() => cambiarEspera(t)}
              >
                ⏱️ {t} min
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="metrics-layout">
        <div className="metrics-column main-col">
          {/* Configuración de Capacidad */}
          <div className="settings-card">
            <h3>⚙️ Parámetros de Operación</h3>
            {config.actualizado_por && (
              <p className="subtext" style={{ marginTop: -8, marginBottom: 6 }}>
                Última actualización: {ultimaAct && `${ultimaAct} · `}{config.actualizado_por}
              </p>
            )}

            <div className="setting-row">
              <div>
                <strong>Límite de Pedidos por Franja ({draft.duracion_franja_minutos} min)</strong>
                <p className="subtext">Al alcanzarse, esa franja se marca como llena y se rechazan pedidos nuevos para ella</p>
              </div>
              <div className="number-stepper">
                <button
                  onClick={() =>
                    setDraft((d) => ({ ...d, cupo_maximo: Math.max(1, (Number(d.cupo_maximo) || 0) - 1) }))
                  }
                >
                  -
                </button>
                <input
                  type="text"
                  inputMode="numeric"
                  value={draft.cupo_maximo}
                  onChange={(e) => {
                    const v = e.target.value.replace(/\D/g, '');
                    setDraft((d) => ({ ...d, cupo_maximo: v === '' ? '' : Number(v) }));
                  }}
                  style={{
                    width: 44,
                    textAlign: 'center',
                    border: 'none',
                    background: 'transparent',
                    fontWeight: 700,
                    fontSize: 13,
                    outline: 'none',
                  }}
                />
                <span>ped</span>
                <button
                  onClick={() =>
                    setDraft((d) => ({ ...d, cupo_maximo: (Number(d.cupo_maximo) || 0) + 1 }))
                  }
                >
                  +
                </button>
              </div>
            </div>

            <div className="setting-row">
              <div>
                <strong>Duración de cada franja</strong>
                <p className="subtext">Los pedidos se agrupan por franjas de este tamaño</p>
              </div>
              <div style={{ display: 'flex', gap: 8 }}>
                {DURACIONES.map((m) => (
                  <button
                    key={m}
                    className={`category-pill ${draft.duracion_franja_minutos === m ? 'active' : ''}`}
                    onClick={() => setDraft((d) => ({ ...d, duracion_franja_minutos: m }))}
                  >
                    {m} min
                  </button>
                ))}
              </div>
            </div>

            <div className="setting-row">
              <div>
                <strong>Agrupamiento Automático (Batching Barista)</strong>
                <p className="subtext">Agrupa cafés iguales para optimizar tiempo de máquina de espresso</p>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={draft.agrupamiento_automatico}
                  onChange={(e) =>
                    setDraft((d) => ({ ...d, agrupamiento_automatico: e.target.checked }))
                  }
                />
                <span className="slider round"></span>
              </label>
            </div>

            {dirty && (
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, paddingTop: 14 }}>
                <button className="btn-audit" onClick={descartarCambios} disabled={guardando}>
                  Descartar
                </button>
                <button className="btn-primary" onClick={guardarParametros} disabled={guardando}>
                  {guardando ? 'Guardando…' : 'Guardar cambios'}
                </button>
              </div>
            )}
          </div>

          {/* Franjas del día */}
          <div className="section-header" style={{ marginTop: '24px' }}>
            <h3>📅 Ocupación por Franjas Horarias</h3>
            <span className="subtext">Hoy · se actualiza en vivo</span>
          </div>

          {errorFranjas && <div className="auth-error-alert">{errorFranjas}</div>}

          {franjasUI.length === 0 && !errorFranjas && (
            <p className="subtext">
              Sin pedidos registrados hoy todavía. Las franjas aparecen aquí automáticamente a
              medida que entran pedidos.
            </p>
          )}

          <div className="slots-grid">
            {franjasUI.map((slot) => (
              <div key={slot.franja} className="slot-card">
                <div className="slot-header">
                  <span className="slot-time">{slot.franja}</span>
                  <span className={`status-badge ${slot.status.toLowerCase().replace(' ', '-')}`}>
                    {slot.status}
                  </span>
                </div>
                <div className="slot-progress">
                  <div className="slot-bar-bg">
                    <div
                      className="slot-bar-fill"
                      style={{ width: `${slot.pct}%`, backgroundColor: slot.color }}
                    ></div>
                  </div>
                  <span className="slot-count">{slot.total} / {slot.max} pedidos</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Panel lateral */}
        <div className="metrics-column side-col">
          <div className="info-box-green">
            <h4>💡 Cómo funciona el cupo</h4>
            <p>
              Los parámetros se guardan en Firestore y la ocupación de franjas se lee en vivo. El
              bloqueo real de pedidos al llenarse una franja se activa con la creación de pedidos
              (HU-03). Los cambios aplican solo a pedidos nuevos: no reabren franjas ya llenas.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}