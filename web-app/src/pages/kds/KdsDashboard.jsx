import React, { useState, useEffect } from 'react';
import KdsPanel from './KdsPanel';
import AuthModal from '../admin/AuthModal';
import '../admin/AdminDashboard.css';
import { onAuthStateChanged, signOut } from 'firebase/auth';
import { auth } from '../../firebase/config';

const IconClipboard = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2" />
    <rect x="8" y="2" width="8" height="4" rx="1" />
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

const IconMapPin = () => (
  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
    <circle cx="12" cy="10" r="3" />
  </svg>
);

export default function KdsDashboard() {
  const [currentUser, setCurrentUser] = useState(null);
  const [checkingAuth, setCheckingAuth] = useState(true);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (user) => {
      if (user) {
        const tokenResult = await user.getIdTokenResult();
        const role = tokenResult.claims.role;
        if (role === 'cocina' || role === 'administrador') {
          setCurrentUser({ name: user.email.split('@')[0], email: user.email, role });
        } else {
          setCurrentUser(null);
        }
      } else {
        setCurrentUser(null);
      }
      setCheckingAuth(false);
    });
    return () => unsubscribe();
  }, []);

  const handleLogout = async () => {
    await signOut(auth);
    setCurrentUser(null);
  };

  if (checkingAuth) {
    return <div className="admin-root" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>Cargando KDS…</div>;
  }

  return (
    <div className="admin-root">
      {!currentUser && (
        <AuthModal
          onLoginSuccess={(user) => setCurrentUser(user)}
          allowedRoles={['cocina', 'administrador']}
          subtitle="KDS • COCINA"
          wrongRoleMessage="Esta cuenta no tiene permisos de cocina."
        />
      )}

      <div className="admin-container">
        {/* ── M3 Navigation Drawer ── */}
        <aside className="admin-sidebar">
          <div className="sidebar-brand">
            <img src="/amicoffe-logo.png" alt="AmiCoffe Logo" width="40" height="40" className="brand-logo-img" />
            <div className="brand-titles">
              <h1 className="brand-name">AmiCoffe</h1>
              <span className="brand-tag">KDS COCINA</span>
            </div>
          </div>

          <p className="sidebar-nav-label">MENÚ PRINCIPAL</p>

          <nav className="sidebar-nav">
            <button className="nav-item active">
              <span className="nav-icon-svg"><IconClipboard /></span>
              <span className="nav-text">
                <span className="nav-label">Cola de Pedidos</span>
                <span className="nav-sub">Orden cronológico</span>
              </span>
              <span className="nav-active-dot" />
            </button>
            <a href="/" className="nav-item" style={{ textDecoration: 'none' }}>
              <span className="nav-icon-svg"><IconMapPin /></span>
              <span className="nav-text">
                <span className="nav-label">Panel Administración</span>
                <span className="nav-sub">Inventario y métricas</span>
              </span>
            </a>
          </nav>

          <div className="sidebar-user-footer">
            <div className="user-avatar-circle">
              <IconUser />
            </div>
            <div className="user-details">
              <span className="user-name">{currentUser ? currentUser.name : 'Sin sesión'}</span>
              <span className="user-role">{currentUser ? currentUser.role : '—'}</span>
            </div>
            {currentUser && (
              <button className="logout-btn" onClick={handleLogout} title="Cerrar Sesión" aria-label="Cerrar Sesión">
                <IconLogOut />
              </button>
            )}
          </div>
        </aside>

        {/* ── M3 Main Content ── */}
        <main className="admin-main">
          <header className="admin-topbar">
            <div className="topbar-title-section">
              <span className="location-pill">
                <IconMapPin />
                Campus Funlam
              </span>
              <h2 className="header-view-title">Cola de Pedidos — Tiempo Real · HU-05</h2>
            </div>
          </header>

          <div className="admin-content-body">
            <KdsPanel />
          </div>
        </main>
      </div>
    </div>
  );
}
