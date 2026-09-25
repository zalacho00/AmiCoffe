import React, { useState, useEffect } from 'react';
import AdminMetricas from './AdminMetricas';
import AdminInventario from './AdminInventario';
import AdminCupos from './AdminCupos';
import AuthModal from './AuthModal';
import './AdminDashboard.css';

const SESSION_KEY = 'amicoffee_admin_session';

/* ── SVG Icon Components (Lucide-style) ── */
const IconBox = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
    <polyline points="3.27 6.96 12 12.01 20.73 6.96" />
    <line x1="12" y1="22.08" x2="12" y2="12" />
  </svg>
);

const IconBarChart = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <line x1="18" y1="20" x2="18" y2="10" />
    <line x1="12" y1="20" x2="12" y2="4" />
    <line x1="6" y1="20" x2="6" y2="14" />
  </svg>
);

const IconClock = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10" />
    <polyline points="12 6 12 12 16 14" />
  </svg>
);

const IconLogOut = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
    <polyline points="16 17 21 12 16 7" />
    <line x1="21" y1="12" x2="9" y2="12" />
  </svg>
);

const IconUser = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
    <circle cx="12" cy="7" r="4" />
  </svg>
);

const IconShoppingBag = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
    <line x1="3" y1="6" x2="21" y2="6" />
    <path d="M16 10a4 4 0 0 1-8 0" />
  </svg>
);

const IconMapPin = () => (
  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
    <circle cx="12" cy="10" r="3" />
  </svg>
);

/* ──────────────────────────── */

export default function AdminDashboard() {
  const [currentUser, setCurrentUser] = useState(() => {
    try {
      const saved = localStorage.getItem(SESSION_KEY);
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });
  const [activeTab, setActiveTab] = useState('inventario');

  useEffect(() => {
    if (currentUser) {
      localStorage.setItem(SESSION_KEY, JSON.stringify(currentUser));
    } else {
      localStorage.removeItem(SESSION_KEY);
    }
  }, [currentUser]);

  const handleLogout = () => {
    localStorage.removeItem(SESSION_KEY);
    setCurrentUser(null);
  };

  const navItems = [
    { id: 'inventario', label: 'Inventario', sub: 'Stock en tiempo real', Icon: IconBox },
    { id: 'metricas', label: 'Métricas', sub: 'Resumen operativo', Icon: IconBarChart },
    { id: 'cupos', label: 'Cupos & Espera', sub: 'Tiempos de servicio', Icon: IconClock },
  ];

  const viewTitles = {
    inventario: 'Gestión de Inventario — Tiempo Real',
    metricas: 'Panel de Métricas — Resumen General',
    cupos: 'Control de Cupos y Tiempos de Espera',
  };

  return (
    <div className="admin-root">
      {!currentUser && (
        <AuthModal onLoginSuccess={(user) => setCurrentUser(user)} />
      )}

      <div className="admin-container">
        {/* ── Sidebar ── */}
        <aside className="admin-sidebar">
          {/* Brand */}
          <div className="sidebar-brand">
            <img src="/amicoffe-logo.png" alt="AmiCoffe Logo" width="40" height="40" className="brand-logo-img" />
            <div className="brand-titles">
              <h1 className="brand-name">AmiCoffe</h1>
              <span className="brand-tag">ADMIN PANEL</span>
            </div>
          </div>

          {/* Nav label */}
          <p className="sidebar-nav-label">MENÚ PRINCIPAL</p>

          {/* Navigation */}
          <nav className="sidebar-nav">
            {navItems.map(({ id, label, sub, Icon }) => (
              <button
                key={id}
                className={`nav-item ${activeTab === id ? 'active' : ''}`}
                onClick={() => setActiveTab(id)}
              >
                <span className="nav-icon-svg"><Icon /></span>
                <span className="nav-text">
                  <span className="nav-label">{label}</span>
                  <span className="nav-sub">{sub}</span>
                </span>
                {activeTab === id && <span className="nav-active-dot" />}
              </button>
            ))}
          </nav>

          {/* User footer */}
          <div className="sidebar-user-footer">
            <div className="user-avatar-circle">
              <IconUser />
            </div>
            <div className="user-details">
              <span className="user-name">{currentUser ? currentUser.name : 'Sin sesión'}</span>
              <span className="user-role">{currentUser ? currentUser.role : '—'}</span>
            </div>
            {currentUser && (
              <button className="logout-btn" onClick={handleLogout} title="Cerrar Sesión">
                <IconLogOut />
              </button>
            )}
          </div>
        </aside>

        {/* ── Main content ── */}
        <main className="admin-main">
          {/* Topbar */}
          <header className="admin-topbar">
            <div className="topbar-title-section">
              <span className="location-pill">
                <IconMapPin />
                Campus Funlam
              </span>
              <h2 className="header-view-title">{viewTitles[activeTab]}</h2>
            </div>

            <div className="topbar-actions">
              {!currentUser && (
                <button className="btn-primary" onClick={() => setCurrentUser({ name: 'Admin Funlam', role: 'admin' })}>
                  Ingresar
                </button>
              )}
            </div>
          </header>

          {/* Content */}
          <div className="admin-content-body">
            {activeTab === 'inventario' && <AdminInventario />}
            {activeTab === 'metricas' && <AdminMetricas />}
            {activeTab === 'cupos' && <AdminCupos />}
          </div>
        </main>
      </div>
    </div>
  );
}
