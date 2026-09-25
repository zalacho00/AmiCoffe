import React, { useState } from 'react';
import './AuthModal.css';

export default function AuthModal({ onLoginSuccess }) {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [role, setRole] = useState('admin');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');
    
    if (!email || !password || (!isLogin && !name)) {
      setError('Por favor completa todos los campos requeridos');
      return;
    }

    setIsLoading(true);

    // Simulación de respuesta rápida para frontend (listo para conectar con Firebase Auth / Backend)
    setTimeout(() => {
      setIsLoading(false);
      const mockUser = {
        name: isLogin ? (email.split('@')[0] || 'Administrador') : name,
        email,
        role: role,
        avatar: '☕'
      };
      onLoginSuccess(mockUser);
    }, 600);
  };

  return (
    <div className="auth-overlay">
      <div className="auth-card">
        {/* Banner Decorativo con Vibes AmiCoffee */}
        <div className="auth-header">
          <div className="auth-logo-badge">
            <img src="/amicoffe-logo.png" alt="AmiCoffe Logo" width="44" height="44" style={{ borderRadius: '50%', objectFit: 'contain' }} />
          </div>
          <h2 className="auth-title">AmiCoffee</h2>
          <span className="auth-subtitle">CAMPUS VIBE • PANEL DE CONTROL</span>
        </div>

        <div className="auth-body">
          <div className="auth-tabs">
            <button 
              className={`auth-tab ${isLogin ? 'active' : ''}`}
              onClick={() => { setIsLogin(true); setError(''); }}
            >
              Iniciar Sesión
            </button>
            <button 
              className={`auth-tab ${!isLogin ? 'active' : ''}`}
              onClick={() => { setIsLogin(false); setError(''); }}
            >
              Nuevo Admin
            </button>
          </div>

          {error && <div className="auth-error-alert">{error}</div>}

          <form onSubmit={handleSubmit} className="auth-form">
            {!isLogin && (
              <div className="form-group">
                <label htmlFor="name">Nombre Completo</label>
                <input
                  id="name"
                  type="text"
                  placeholder="Ej. Admin Camilo"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                />
              </div>
            )}

            <div className="form-group">
              <label htmlFor="email">Correo Institucional / Admin</label>
              <input
                id="email"
                type="email"
                placeholder="admin@amicoffee.campus"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="password">Contraseña</label>
              <input
                id="password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            {!isLogin && (
              <div className="form-group">
                <label htmlFor="role">Rol en Sistema</label>
                <select id="role" value={role} onChange={(e) => setRole(e.target.value)}>
                  <option value="admin">Administrador General</option>
                  <option value="barista_lead">Líder Barista / KDS</option>
                  <option value="inventario">Encargado de Inventario</option>
                </select>
              </div>
            )}

            <button type="submit" className="auth-submit-btn" disabled={isLoading}>
              {isLoading ? (
                <span className="spinner">⏳ Conectando...</span>
              ) : (
                <>
                  {isLogin ? 'Ingresar al Dashboard' : 'Crear Cuenta Admin'}
                  <span className="btn-arrow">→</span>
                </>
              )}
            </button>
          </form>

          <div className="auth-footer-note">
            <span>🔒 Acceso seguro para personal autorizado</span>
          </div>
        </div>
      </div>
    </div>
  );
}
