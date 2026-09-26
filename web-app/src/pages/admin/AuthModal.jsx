import React, { useState } from 'react';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from '../../firebase/config';
import './AuthModal.css';

export default function AuthModal({ onLoginSuccess }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!email || !password) {
      setError('Por favor completa todos los campos');
      return;
    }

    setIsLoading(true);
    try {
      const credential = await signInWithEmailAndPassword(auth, email, password);
      const tokenResult = await credential.user.getIdTokenResult();
      const role = tokenResult.claims.role; // "administrador" | "cocina" | undefined

      if (role !== 'administrador') {
        setError('Esta cuenta no tiene permisos de administrador.');
        await auth.signOut();
        setIsLoading(false);
        return;
      }

      onLoginSuccess({
        name: credential.user.email.split('@')[0],
        email: credential.user.email,
        role,
      });
    } catch (err) {
      console.error(err);
      if (err.code === 'auth/invalid-credential' || err.code === 'auth/wrong-password' || err.code === 'auth/user-not-found') {
        setError('Correo o contraseña incorrectos.');
      } else {
        setError('Error al iniciar sesión. Intenta de nuevo.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-overlay">
      <div className="auth-card">
        <div className="auth-header">
          <div className="auth-logo-badge">
            <img src="/amicoffe-logo.png" alt="AmiCoffe Logo" width="44" height="44" style={{ borderRadius: '50%', objectFit: 'contain' }} />
          </div>
          <h2 className="auth-title">AmiCoffee</h2>
          <span className="auth-subtitle">CAMPUS VIBE • PANEL DE CONTROL</span>
        </div>

        <div className="auth-body">
          {error && <div className="auth-error-alert">{error}</div>}

          <form onSubmit={handleSubmit} className="auth-form">
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

            <button type="submit" className="auth-submit-btn" disabled={isLoading}>
              {isLoading ? (
                <span className="spinner">⏳ Conectando...</span>
              ) : (
                <>
                  Ingresar al Dashboard
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