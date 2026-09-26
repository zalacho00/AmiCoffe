import React, { useState } from 'react';

export default function AdminMetricas() {
  const [timeRange, setTimeRange] = useState('hoy');

  const stats = [
    { title: 'Ventas Totales', value: '$1.485.000 COP', change: '+18.5%', positive: true, icon: '💰' },
    { title: 'Pedidos Completados', value: '142', change: '+12 pedidos', positive: true, icon: '☕' },
    { title: 'Tiempo Prom. Espera', value: '4.5 min', change: '-1.2 min', positive: true, icon: '⏱️' },
    { title: 'Ticket Promedio', value: '$10.450 COP', change: '-2%', positive: false, icon: '📊' },
  ];

  const topProducts = [
    { rank: '#1', name: 'Caramel Macchiato', desc: 'Espresso doble, leche de avena y jarabe', sales: '84 pedidos', revenue: '$966.000 COP', badge: 'Popular' },
    { rank: '#2', name: 'Nitro Cold Brew & Vanilla', desc: 'Macerado 20h con crema suave', sales: '46 pedidos', revenue: '$575.000 COP', badge: 'Top Semanal' },
    { rank: '#3', name: 'Capuchino Tradicional', desc: 'Espresso 100% arábigo con espuma densa', sales: '32 pedidos', revenue: '$288.000 COP', badge: '' },
    { rank: '#4', name: 'Croissant Mantequilla Brunch', desc: 'Hojaldrado artesanal horneado hoy', sales: '29 pedidos', revenue: '$203.000 COP', badge: '' },
  ];

  return (
    <div className="admin-page">
      {/* Dynamic Header Banner mirroring image top banner */}
      <div className="campus-vibe-banner">
        <div className="banner-info">
          <div className="status-pill">
            <span className="live-dot"></span> Espera promedio actual: <strong>4-6 min</strong>
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

      {/* KPI Cards Grid */}
      <div className="kpi-grid">
        {stats.map((stat, i) => (
          <div key={i} className="kpi-card">
            <div className="kpi-icon-wrapper">{stat.icon}</div>
            <div className="kpi-details">
              <span className="kpi-title">{stat.title}</span>
              <h3 className="kpi-value">{stat.value}</h3>
              <span className={`kpi-change ${stat.positive ? 'positive' : 'negative'}`}>
                {stat.change} vs periodo anterior
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Main Content Layout */}
      <div className="metrics-layout">
        {/* Left Column: Más Populares en Campus */}
        <div className="metrics-column main-col">
          <div className="section-header">
            <h3>🔥 Más Populares en Campus</h3>
            <span className="subtext">Top productos más vendidos hoy</span>
          </div>

          <div className="products-list">
            {topProducts.map((p, idx) => (
              <div key={idx} className="product-rank-card">
                <div className="rank-tag">{p.rank}</div>
                <div className="product-rank-info">
                  <div className="product-title-row">
                    <h4>{p.name}</h4>
                    {p.badge && <span className="product-badge">{p.badge}</span>}
                  </div>
                  <p className="product-desc">{p.desc}</p>
                </div>
                <div className="product-rank-stats">
                  <span className="stat-sales">{p.sales}</span>
                  <span className="stat-revenue">{p.revenue}</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Right Column: Actividad y Rendimiento por Hora */}
        <div className="metrics-column side-col">
          <div className="section-header">
            <h3>⏰ Horas Pico de Pedidos</h3>
          </div>
          
          <div className="peak-hours-card">
            <div className="hour-bar-item">
              <span className="hour-label">07:00 AM - 09:00 AM</span>
              <div className="bar-wrapper">
                <div className="bar-fill" style={{ width: '85%' }}></div>
              </div>
              <span className="hour-count">48 ped</span>
            </div>

            <div className="hour-bar-item">
              <span className="hour-label">09:00 AM - 11:00 AM</span>
              <div className="bar-wrapper">
                <div className="bar-fill" style={{ width: '100%' }}></div>
              </div>
              <span className="hour-count">62 ped</span>
            </div>

            <div className="hour-bar-item">
              <span className="hour-label">11:00 AM - 02:00 PM</span>
              <div className="bar-wrapper">
                <div className="bar-fill" style={{ width: '45%' }}></div>
              </div>
              <span className="hour-count">28 ped</span>
            </div>

            <div className="hour-bar-item">
              <span className="hour-label">02:00 PM - 05:00 PM</span>
              <div className="bar-wrapper">
                <div className="bar-fill" style={{ width: '60%' }}></div>
              </div>
              <span className="hour-count">36 ped</span>
            </div>
          </div>

          {/* Quick Notice Card */}
          <div className="notice-card">
            <div className="notice-icon">💡</div>
            <div>
              <strong className="notice-title">Tip de Gestión Campus</strong>
              <p className="notice-desc">
                El 68% de las compras se realizan mediante el carrito web previo a clase. Mantener el cupo de preparación actualizado previene cuellos de botella.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
