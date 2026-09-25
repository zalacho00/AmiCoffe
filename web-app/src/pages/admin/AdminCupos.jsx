import React, { useState } from 'react';

export default function AdminCupos() {
  const [prepCapacity, setPrepCapacity] = useState(15); // slots por franja de 15 min
  const [estimatedTime, setEstimatedTime] = useState('4-6');
  const [autoBatching, setAutoBatching] = useState(true);

  const timeSlots = [
    { slot: '08:00 AM - 08:15 AM', max: 15, current: 14, status: 'Lleno' },
    { slot: '08:15 AM - 08:30 AM', max: 15, current: 15, status: 'Agotado' },
    { slot: '08:30 AM - 08:45 AM', max: 15, current: 9, status: 'Disponible' },
    { slot: '08:45 AM - 09:00 AM', max: 15, current: 4, status: 'Disponible' },
    { slot: '09:00 AM - 09:15 AM', max: 15, current: 12, status: 'Casi Lleno' },
  ];

  return (
    <div className="admin-page">
      <div className="page-header-title">
        <div>
          <h2>Control de Cupos y Tiempos de Espera</h2>
          <p className="subtext">Ajusta la capacidad operativa para evitar colas en la cafetería del Campus</p>
        </div>
      </div>

      {/* Banner de Estado en Vivo (Mirror del Banner Móvil) */}
      <div className="live-status-hero">
        <div className="live-status-main">
          <span className="live-badge-glow">🔴 EN VIVO</span>
          <h3>Tiempo de Espera Mostrado en App: <strong>{estimatedTime} min</strong></h3>
          <p>Los estudiantes ven esta estimación en tiempo real antes de añadir items al carrito.</p>
        </div>
        <div className="live-status-actions">
          <label>Cambiar estimación rápida:</label>
          <div className="quick-time-buttons">
            {['2-4', '4-6', '7-10', '12-15'].map(t => (
              <button
                key={t}
                className={`time-btn ${estimatedTime === t ? 'active' : ''}`}
                onClick={() => setEstimatedTime(t)}
              >
                ⏱️ {t} min
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="metrics-layout">
        {/* Configuración de Capacidad */}
        <div className="metrics-column main-col">
          <div className="settings-card">
            <h3>⚙️ Parámetros de Operación</h3>
            
            <div className="setting-row">
              <div>
                <strong>Límite de Pedidos por Intervalo (15 min)</strong>
                <p className="subtext">Si se supera este número, la app notificará mayor tiempo de espera</p>
              </div>
              <div className="number-stepper">
                <button onClick={() => setPrepCapacity(Math.max(5, prepCapacity - 1))}>-</button>
                <span>{prepCapacity} ped</span>
                <button onClick={() => setPrepCapacity(prepCapacity + 1)}>+</button>
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
                  checked={autoBatching}
                  onChange={(e) => setAutoBatching(e.target.checked)}
                />
                <span className="slider round"></span>
              </label>
            </div>
          </div>

          {/* Slots de Cupos del día */}
          <div className="section-header" style={{ marginTop: '24px' }}>
            <h3>📅 Ocupación por Franjas Horarias</h3>
          </div>
          <div className="slots-grid">
            {timeSlots.map((slot, idx) => (
              <div key={idx} className="slot-card">
                <div className="slot-header">
                  <span className="slot-time">{slot.slot}</span>
                  <span className={`status-badge ${slot.status.toLowerCase().replace(' ', '-')}`}>
                    {slot.status}
                  </span>
                </div>
                <div className="slot-progress">
                  <div className="slot-bar-bg">
                    <div
                      className="slot-bar-fill"
                      style={{
                        width: `${(slot.current / slot.max) * 100}%`,
                        backgroundColor: slot.current >= 14 ? '#ef4444' : slot.current >= 10 ? '#f59e0b' : '#10b981'
                      }}
                    ></div>
                  </div>
                  <span className="slot-count">{slot.current} / {slot.max} pedidos</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Panel lateral con consejos */}
        <div className="metrics-column side-col">
          <div className="info-box-green">
            <h4>💡 Algoritmo Campus Vibe</h4>
            <p>
              AmiCoffee ajusta automáticamente los sugeridos si la fila presencial incrementa. Puedes forzar un tiempo mayor en horas de pico masivo de clases (ej. 10:00 AM).
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
