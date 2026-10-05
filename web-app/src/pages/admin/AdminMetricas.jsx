import React, { useState, useEffect, useMemo } from 'react';
import {
  collection,
  query,
  where,
  getDocs,
  Timestamp,
} from 'firebase/firestore';
import { db } from '../../firebase/config';

// ── Helpers de rango de fechas ──
function getRangeStart(timeRange) {
  const now = new Date();
  const start = new Date(now);
  if (timeRange === 'hoy') {
    start.setHours(0, 0, 0, 0);
  } else if (timeRange === 'semana') {
    const dia = start.getDay(); // 0=domingo
    const diff = dia === 0 ? 6 : dia - 1; // lunes como inicio de semana
    start.setDate(start.getDate() - diff);
    start.setHours(0, 0, 0, 0);
  } else if (timeRange === 'mes') {
    start.setDate(1);
    start.setHours(0, 0, 0, 0);
  }
  return start;
}

function formatCOP(valor) {
  return `$${Math.round(valor).toLocaleString('es-CO')} COP`;
}

function tiempoEsperaMinutos(pedido) {
  const historial = pedido.historial_estados || [];
  const inicio = historial.find((h) => h.estado === 'en_espera')?.fecha;
  const listo = historial.find((h) => h.estado === 'listo')?.fecha;
  if (!inicio || !listo) return null;
  const msInicio = inicio.toDate ? inicio.toDate().getTime() : new Date(inicio).getTime();
  const msListo = listo.toDate ? listo.toDate().getTime() : new Date(listo).getTime();
  return (msListo - msInicio) / 60000;
}

export default function AdminMetricas() {
  const [timeRange, setTimeRange] = useState('hoy');
  const [pedidos, setPedidos] = useState([]);
  const [franjasHoy, setFranjasHoy] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  // ── Cargar pedidos entregados del rango seleccionado ──
  useEffect(() => {
    let activo = true;
    setLoading(true);
    setErrorMsg('');

    const start = getRangeStart(timeRange);

    async function cargar() {
      try {
        const q = query(
          collection(db, 'pedidos'),
          where('estado', '==', 'entregado'),
          where('fecha_creacion', '>=', Timestamp.fromDate(start))
        );
        const snap = await getDocs(q);
        if (!activo) return;
        setPedidos(snap.docs.map((d) => ({ id: d.id, ...d.data() })));
      } catch (err) {
        console.error(err);
        if (!activo) return;
        // Suele pasar la primera vez: falta el índice compuesto (Firestore
        // te da en la consola del navegador el link directo para crearlo con 1 click).
        setErrorMsg(
          'No se pudieron cargar las métricas. Si la consola del navegador menciona un índice faltante, usa el link que Firestore ofrece ahí para crearlo (tarda ~1 min en quedar listo).'
        );
      } finally {
        if (activo) setLoading(false);
      }
    }

    cargar();
    return () => { activo = false; };
  }, [timeRange]);

  // ── Cargar horas pico del día actual ──
  useEffect(() => {
    async function cargarFranjas() {
      try {
        const hoyStr = new Date().toISOString().slice(0, 10); // "YYYY-MM-DD"
        const q = query(collection(db, 'metricas_horas_pico'), where('dia', '==', hoyStr));
        const snap = await getDocs(q);
        setFranjasHoy(snap.docs.map((d) => d.data()));
      } catch (err) {
        console.error('Error cargando horas pico:', err);
      }
    }
    cargarFranjas();
  }, []);

  // ── KPIs calculados a partir de pedidos reales ──
  const kpis = useMemo(() => {
    const totalVentas = pedidos.reduce((sum, p) => sum + (p.total || 0), 0);
    const totalPedidos = pedidos.length;
    const ticketPromedio = totalPedidos > 0 ? totalVentas / totalPedidos : 0;

    const tiempos = pedidos
      .map(tiempoEsperaMinutos)
      .filter((t) => t !== null);
    const tiempoPromEspera =
      tiempos.length > 0 ? tiempos.reduce((a, b) => a + b, 0) / tiempos.length : null;

    return { totalVentas, totalPedidos, ticketPromedio, tiempoPromEspera };
  }, [pedidos]);

  // ── Top productos agregados en el cliente (sin $group de Mongo) ──
  const topProductos = useMemo(() => {
    const mapa = new Map();
    pedidos.forEach((pedido) => {
      (pedido.items || []).forEach((item) => {
        const key = item.producto_id || item.nombre_producto;
        const prev = mapa.get(key) || {
          nombre: item.nombre_producto,
          cantidad: 0,
          ingresos: 0,
        };
        prev.cantidad += item.cantidad || 0;
        prev.ingresos += (item.cantidad || 0) * (item.precio_unitario || 0);
        mapa.set(key, prev);
      });
    });
    return Array.from(mapa.values())
      .sort((a, b) => b.cantidad - a.cantidad)
      .slice(0, 5);
  }, [pedidos]);

  // ── Horas pico normalizadas para las barras ──
  const horasPico = useMemo(() => {
    const max = Math.max(1, ...franjasHoy.map((f) => f.total_pedidos || 0));
    return franjasHoy
      .slice()
      .sort((a, b) => (a.franja_horaria > b.franja_horaria ? 1 : -1))
      .map((f) => ({
        franja: f.franja_horaria,
        total: f.total_pedidos || 0,
        widthPct: Math.round(((f.total_pedidos || 0) / max) * 100),
        saturado: f.saturado,
      }));
  }, [franjasHoy]);

  const stats = [
    {
      title: 'Ventas Totales',
      value: formatCOP(kpis.totalVentas),
      icon: '💰',
    },
    {
      title: 'Pedidos Completados',
      value: String(kpis.totalPedidos),
      icon: '☕',
    },
    {
      title: 'Tiempo Prom. Espera',
      value: kpis.tiempoPromEspera !== null ? `${kpis.tiempoPromEspera.toFixed(1)} min` : '—',
      icon: '⏱️',
    },
    {
      title: 'Ticket Promedio',
      value: kpis.totalPedidos > 0 ? formatCOP(kpis.ticketPromedio) : '—',
      icon: '📊',
    },
  ];

  return (
    <div className="admin-page">
      <div className="campus-vibe-banner">
        <div className="banner-info">
          <div className="status-pill">
            <span className="live-dot"></span> Métricas en vivo — HU-09
          </div>
          <h2>¡Hola, Administrador! 👋</h2>
          <p>Aquí tienes el pulso en vivo del Campus Vibe de AmiCoffee.</p>
        </div>
        <div className="banner-controls">
          <select value={timeRange} onChange={(e) => setTimeRange(e.target.value)} className="select-input">
            <option value="hoy">Hoy (Campus Vibe)</option>
            <option value="semana">Esta Semana</option>
            <option value="mes">Este Mes</option>
          </select>
        </div>
      </div>

      {errorMsg && <div className="auth-error-alert">{errorMsg}</div>}

      {loading ? (
        <div style={{ padding: '32px', textAlign: 'center' }}>Cargando métricas…</div>
      ) : (
        <>
          <div className="kpi-grid">
            {stats.map((stat, i) => (
              <div key={i} className="kpi-card">
                <div className="kpi-icon-wrapper">{stat.icon}</div>
                <div className="kpi-details">
                  <span className="kpi-title">{stat.title}</span>
                  <h3 className="kpi-value">{stat.value}</h3>
                </div>
              </div>
            ))}
          </div>

          <div className="metrics-layout">
            <div className="metrics-column main-col">
              <div className="section-header">
                <h3>🔥 Más Populares en Campus</h3>
                <span className="subtext">Top productos más vendidos en el periodo</span>
              </div>

              <div className="products-list">
                {topProductos.length === 0 && (
                  <p className="subtext" style={{ padding: '16px' }}>
                    Sin pedidos entregados todavía en este periodo. En cuanto existan pedidos reales
                    (HU-03/HU-06), este ranking se llena solo.
                  </p>
                )}
                {topProductos.map((p, idx) => (
                  <div key={idx} className="product-rank-card">
                    <div className="rank-tag">#{idx + 1}</div>
                    <div className="product-rank-info">
                      <div className="product-title-row">
                        <h4>{p.nombre}</h4>
                      </div>
                    </div>
                    <div className="product-rank-stats">
                      <span className="stat-sales">{p.cantidad} unidades</span>
                      <span className="stat-revenue">{formatCOP(p.ingresos)}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="metrics-column side-col">
              <div className="section-header">
                <h3>⏰ Horas Pico de Pedidos (hoy)</h3>
              </div>

              <div className="peak-hours-card">
                {horasPico.length === 0 && (
                  <p className="subtext">
                    Sin franjas registradas hoy todavía — se llena automáticamente con cada pedido
                    (HU-08).
                  </p>
                )}
                {horasPico.map((h, i) => (
                  <div key={i} className="hour-bar-item">
                    <span className="hour-label">
                      {h.franja} {h.saturado && '🔴'}
                    </span>
                    <div className="bar-wrapper">
                      <div className="bar-fill" style={{ width: `${h.widthPct}%` }}></div>
                    </div>
                    <span className="hour-count">{h.total} ped</span>
                  </div>
                ))}
              </div>

              <div className="notice-card">
                <div className="notice-icon">💡</div>
                <div>
                  <strong className="notice-title">Estado del módulo</strong>
                  <p className="notice-desc">
                    Estas métricas ya leen datos reales de Firestore. Mientras no existan pedidos de
                    estudiantes (HU-03), es normal y correcto que todo muestre "—" o "sin datos".
                  </p>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}