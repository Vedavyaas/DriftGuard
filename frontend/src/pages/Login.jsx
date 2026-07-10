import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../services/api';
import { Lock, User, ShieldCheck, BarChart2, AlertCircle } from 'lucide-react';

export default function Login() {
  const [username, setUsername] = useState('CGManager');
  const [password, setPassword] = useState('123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const response = await login({ username, password });
      localStorage.setItem('token', response.token);
      navigate(response.role === 'ADMIN' ? '/admin' : '/manager');
    } catch {
      setError('Invalid credentials or account is disabled.');
    } finally {
      setLoading(false);
    }
  };

  return (
    /* Fixed overlay — always fills the viewport exactly, never scrolls */
    <div style={{
      position: 'fixed',
      inset: 0,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1,
      padding: '1.5rem',
    }}>
      <div style={{ width: '100%', maxWidth: 390 }}>

        {/* Brand */}
        <div className="fade-up" style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
          <div style={{
            width: 56, height: 56,
            background: 'linear-gradient(135deg,#7c3aed,#4f46e5 45%,#0891b2)',
            borderRadius: 17, margin: '0 auto 1rem',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            boxShadow: '0 12px 36px rgba(79,70,229,0.32), 0 0 0 1px rgba(255,255,255,0.5) inset'
          }}>
            <ShieldCheck size={26} color="#fff" strokeWidth={1.8} />
          </div>
          <h1 style={{ fontSize: '1.65rem', fontWeight: 800, letterSpacing: '-0.8px', color: 'var(--text-1)' }}>
            DriftGuard
          </h1>
          <p style={{ color: 'var(--text-2)', fontSize: '0.9rem', marginTop: 4 }}>
            Sign in to your workspace
          </p>
        </div>

        {/* Card */}
        <div className="g-card fade-up-2">
          <div className="mac-bar">Authentication</div>

          <div style={{ padding: '1.5rem' }}>

            {/* Quick-fill demo buttons */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', marginBottom: '1.25rem' }}>
              {[
                { label: 'Admin', sub: 'CGAdmin', Icon: ShieldCheck, color: 'var(--indigo)', set: () => { setUsername('CGAdmin'); setPassword('123'); } },
                { label: 'Manager', sub: 'CGManager', Icon: BarChart2, color: 'var(--cyan)', set: () => { setUsername('CGManager'); setPassword('123'); } },
              ].map(({ label, sub, Icon, color, set }) => (
                <button key={label} type="button" onClick={set} style={{
                  display: 'flex', alignItems: 'center', gap: 9,
                  padding: '0.55rem 0.75rem',
                  background: 'rgba(255,255,255,0.5)',
                  border: '1px solid rgba(255,255,255,0.85)',
                  borderRadius: 9, cursor: 'pointer',
                  transition: 'background 0.15s',
                  boxShadow: 'var(--shadow-xs)',
                }}>
                  <div style={{ width: 28, height: 28, borderRadius: 7, background: `${color}18`, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                    <Icon size={13} color={color} />
                  </div>
                  <div style={{ textAlign: 'left' }}>
                    <div style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-1)', lineHeight: 1.2 }}>{label}</div>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-3)', lineHeight: 1.2 }}>{sub} / 123</div>
                  </div>
                </button>
              ))}
            </div>

            {/* Divider */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.1rem' }}>
              <div style={{ flex: 1, height: 1, background: 'rgba(0,0,0,0.07)' }} />
              <span style={{ fontSize: '0.72rem', color: 'var(--text-3)', fontWeight: 600 }}>or sign in manually</span>
              <div style={{ flex: 1, height: 1, background: 'rgba(0,0,0,0.07)' }} />
            </div>

            {/* Form */}
            <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <div className="field">
                <label>Username</label>
                <div className="input-wrap">
                  <User size={15} />
                  <input className="inp" type="text" placeholder="Enter username"
                    value={username} onChange={e => setUsername(e.target.value)} required />
                </div>
              </div>

              <div className="field">
                <label>Password</label>
                <div className="input-wrap">
                  <Lock size={15} />
                  <input className="inp" type="password" placeholder="Enter password"
                    value={password} onChange={e => setPassword(e.target.value)} required />
                </div>
              </div>

              {/* Fixed-height error slot so layout never jumps */}
              <div style={{ minHeight: 30 }}>
                {error && (
                  <div className="feedback feedback-err" style={{ padding: '0.4rem 0.7rem' }}>
                    <AlertCircle size={13} /> {error}
                  </div>
                )}
              </div>

              <button type="submit" className="btn btn-vibrancy btn-full"
                style={{ padding: '0.8rem', fontSize: '0.97rem', borderRadius: 10 }}
                disabled={loading}>
                {loading ? 'Signing in…' : 'Continue →'}
              </button>
            </form>
          </div>
        </div>

        <p className="fade-up-3" style={{ textAlign: 'center', marginTop: '1rem', fontSize: '0.75rem', color: 'var(--text-3)' }}>
          Protected by JWT · DriftGuard v1.0
        </p>
      </div>
    </div>
  );
}
