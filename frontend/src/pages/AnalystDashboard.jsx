import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSelfInfo, changeDetails } from '../services/api';
import {
  LayoutDashboard, Settings, LogOut,
  BarChart2, CheckCircle2, AlertCircle, User
} from 'lucide-react';

const NAV = [
  { id: 'overview', label: 'Overview', icon: LayoutDashboard },
  { id: 'profile',  label: 'My Profile', icon: Settings     },
];

function Field({ label, children }) {
  return (
    <div className="field">
      <label>{label}</label>
      {children}
    </div>
  );
}

function Msg({ msg }) {
  if (!msg) return <div style={{ minHeight: 32 }} />;
  return (
    <div className={`feedback ${msg.ok ? 'feedback-ok' : 'feedback-err'}`} style={{ marginBottom: 4 }}>
      {msg.ok ? <CheckCircle2 size={13} /> : <AlertCircle size={13} />} {msg.text}
    </div>
  );
}

/* ── Overview ── */
function Overview({ self }) {
  if (!self) return <div style={{ color: 'var(--text-3)', padding: '2rem' }}>Loading…</div>;

  const fmt = (ts) => ts
    ? new Date(ts).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })
    : '—';

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem', maxWidth: 700 }}>

      {/* Hero */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <div style={{
          width: 56, height: 56, borderRadius: 16, flexShrink: 0,
          background: 'linear-gradient(135deg,#0891b2,#059669)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: '0 8px 24px rgba(8,145,178,0.28)',
        }}>
          <span style={{ color: '#fff', fontSize: '1.2rem', fontWeight: 800 }}>
            {self.username?.[0]?.toUpperCase()}
          </span>
        </div>
        <div>
          <h2 style={{ fontSize: '1.3rem', fontWeight: 800, letterSpacing: '-0.5px' }}>
            Welcome back, {self.username} 👋
          </h2>
          <p style={{ color: 'var(--text-2)', marginTop: 2, fontSize: '0.9rem' }}>
            Here's your account overview.
          </p>
        </div>
      </div>

      {/* Stat cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem' }}>
        {[
          { label: 'User ID',  value: `#${self.id}`,  color: 'var(--text-3)', mono: true },
          { label: 'Username', value: self.username,   color: 'var(--cyan)'              },
          { label: 'Email',    value: self.email,      color: 'var(--indigo)'            },
        ].map(({ label, value, color, mono }) => (
          <div key={label} className="g-panel" style={{ padding: '0.9rem 1rem' }}>
            <div style={{ fontSize: '0.65rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.6px', color: 'var(--text-3)', marginBottom: 4 }}>
              {label}
            </div>
            <div style={{ fontSize: '0.9rem', fontWeight: 700, color, fontFamily: mono ? 'monospace' : 'inherit', wordBreak: 'break-all' }}>
              {value || '—'}
            </div>
          </div>
        ))}
      </div>

      {/* Account Details */}
      <div className="g-card" style={{ overflow: 'hidden' }}>
        <div className="mac-bar" style={{ padding: '6px 12px', fontSize: '0.7rem' }}>Account Details</div>
        <div style={{ padding: '1rem', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
          <div>
            <div style={{ fontSize: '0.65rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.5px', color: 'var(--text-3)', marginBottom: 6 }}>
              Account Status
            </div>
            <span className={`pill ${self.isEnabled ? 'pill-active' : 'pill-disabled'}`} style={{ fontSize: '0.75rem', padding: '4px 10px' }}>
              {self.isEnabled ? 'Active' : 'Disabled'}
            </span>
          </div>
          <div>
            <div style={{ fontSize: '0.65rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.5px', color: 'var(--text-3)', marginBottom: 6 }}>
              Member Since
            </div>
            <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-1)' }}>
              {fmt(self.createdAt)}
            </div>
          </div>
          <div>
            <div style={{ fontSize: '0.65rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.5px', color: 'var(--text-3)', marginBottom: 6 }}>
              Last Updated
            </div>
            <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-1)' }}>
              {fmt(self.lastUpdatedAt)}
            </div>
          </div>
        </div>
      </div>

    </div>
  );
}

/* ── Profile ── */
function Profile({ self }) {
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [msg, setMsg] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!self?.id) return;
    setLoading(true);
    try {
      await changeDetails(self.id, form);
      setMsg({ ok: true, text: 'Details updated successfully.' });
      setForm({ name: '', email: '', password: '' });
    } catch {
      setMsg({ ok: false, text: 'Failed to update details.' });
    } finally { setLoading(false); }
  };

  return (
    <div style={{ maxWidth: 420 }}>
      <div style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.3rem', fontWeight: 800, letterSpacing: '-0.5px' }}>My Profile</h2>
        <p style={{ color: 'var(--text-2)', marginTop: 4, fontSize: '0.85rem' }}>Update your name, email, or password.</p>
      </div>

      {self && (
        <div className="g-panel" style={{ padding: '0.8rem 1rem', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{
            width: 36, height: 36, borderRadius: 10,
            background: 'linear-gradient(135deg,#0891b2,#059669)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0
          }}>
            <User size={16} color="#fff" />
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: '0.85rem' }}>{self.username}</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-3)' }}>{self.email}</div>
          </div>
          <span className="pill pill-active" style={{ marginLeft: 'auto', fontSize: '0.7rem' }}>Analyst</span>
        </div>
      )}

      <div className="g-card">
        <div className="mac-bar">Change Details</div>
        <div style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
          <Field label="New Name (optional)">
            <input className="inp no-icon" placeholder="Leave blank to keep current" value={form.name}
              onChange={e => setForm({ ...form, name: e.target.value })} />
          </Field>
          <Field label="New Email (optional)">
            <input className="inp no-icon" type="email" placeholder="Leave blank to keep current" value={form.email}
              onChange={e => setForm({ ...form, email: e.target.value })} />
          </Field>
          <Field label="New Password (optional)">
            <input className="inp no-icon" type="password" placeholder="Leave blank to keep current" value={form.password}
              onChange={e => setForm({ ...form, password: e.target.value })} />
          </Field>

          <Msg msg={msg} />

          <button className="btn btn-primary btn-full" onClick={handleSubmit} disabled={loading}
            style={{ borderRadius: 10, padding: '0.75rem', background: 'linear-gradient(135deg,#0891b2,#059669)', boxShadow: '0 6px 18px rgba(8,145,178,0.25)' }}>
            <Settings size={15} /> {loading ? 'Saving…' : 'Save Changes'}
          </button>
        </div>
      </div>
    </div>
  );
}

/* ── Main ── */
export default function AnalystDashboard() {
  const [active, setActive] = useState('overview');
  const [self, setSelf] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    getSelfInfo().then(setSelf).catch(() => {});
  }, []);

  const handleLogout = () => { localStorage.removeItem('token'); navigate('/login'); };

  const renderPanel = () => {
    switch (active) {
      case 'overview': return <Overview self={self} />;
      case 'profile':  return <Profile self={self} />;
      default:         return null;
    }
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', position: 'relative', zIndex: 1 }}>

      {/* Sidebar */}
      <aside style={{
        width: 220, flexShrink: 0,
        display: 'flex', flexDirection: 'column',
        padding: '1.5rem 1rem',
        background: 'rgba(255,255,255,0.45)',
        backdropFilter: 'blur(28px) saturate(180%)',
        WebkitBackdropFilter: 'blur(28px) saturate(180%)',
        borderRight: '1px solid rgba(255,255,255,0.65)',
        position: 'sticky', top: 0, height: '100vh',
        boxShadow: '2px 0 24px rgba(30,40,90,0.06)',
      }}>
        {/* Brand */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 9, marginBottom: '2.5rem', paddingLeft: 4 }}>
          <div style={{
            width: 34, height: 34, borderRadius: 10,
            background: 'linear-gradient(135deg,#0891b2,#059669)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            boxShadow: '0 4px 12px rgba(8,145,178,0.3)', flexShrink: 0
          }}>
            <BarChart2 size={17} color="#fff" strokeWidth={2} />
          </div>
          <div>
            <div style={{ fontSize: '0.95rem', fontWeight: 800, letterSpacing: '-0.4px', color: 'var(--text-1)' }}>DriftGuard</div>
            <div style={{ fontSize: '0.68rem', fontWeight: 600, color: 'var(--cyan)', letterSpacing: '0.4px', marginTop: -1 }}>ANALYST</div>
          </div>
        </div>

        {/* Nav */}
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', flex: 1 }}>
          {NAV.map(({ id, label, icon: Icon }) => {
            const isActive = active === id;
            return (
              <button key={id} onClick={() => setActive(id)} style={{
                display: 'flex', alignItems: 'center', gap: 10,
                padding: '0.6rem 0.9rem', borderRadius: 9, border: 'none',
                background: isActive ? 'rgba(8,145,178,0.12)' : 'transparent',
                color: isActive ? 'var(--cyan)' : 'var(--text-2)',
                fontWeight: isActive ? 700 : 500,
                fontSize: '0.88rem', cursor: 'pointer',
                transition: 'all 0.15s', textAlign: 'left', width: '100%',
                boxShadow: isActive ? '0 0 0 1px rgba(8,145,178,0.2)' : 'none',
              }}>
                <Icon size={16} strokeWidth={isActive ? 2.2 : 1.8} />
                {label}
              </button>
            );
          })}
        </nav>

        {/* User + Logout */}
        <div style={{ borderTop: '1px solid rgba(255,255,255,0.5)', paddingTop: '1rem', marginTop: '0.5rem' }}>
          {self && (
            <div style={{ padding: '0.5rem 0.75rem', marginBottom: '0.5rem' }}>
              <div style={{ fontSize: '0.82rem', fontWeight: 700, color: 'var(--text-1)' }}>{self.username}</div>
              <div style={{ fontSize: '0.72rem', color: 'var(--text-3)' }}>{self.email}</div>
            </div>
          )}
          <button className="btn btn-danger btn-full btn-sm" onClick={handleLogout} style={{ borderRadius: 8 }}>
            <LogOut size={14} /> Sign Out
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main style={{ flex: 1, padding: '2.5rem', overflowY: 'auto', maxHeight: '100vh' }}>
        {renderPanel()}
      </main>
    </div>
  );
}
