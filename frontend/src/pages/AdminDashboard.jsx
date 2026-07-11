import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  getSelfInfo, getProjectManagersInfo, createProjectManager,
  changeProjectManagerValidity, changeDetails, getAdminManagerStats
} from '../services/api';
import {
  ShieldCheck, Users, UserPlus, Settings, LogOut,
  Search, CheckCircle2, XCircle, AlertCircle,
  LayoutDashboard, RefreshCw, User
} from 'lucide-react';

/* ─────────────────────────────────────────
   Sidebar nav items
───────────────────────────────────────── */
const NAV = [
  { id: 'overview',       label: 'Overview',        icon: LayoutDashboard },
  { id: 'create',         label: 'Create Manager',  icon: UserPlus        },
  { id: 'managers',       label: 'Project Managers', icon: Users           },
  { id: 'profile',        label: 'My Profile',       icon: Settings        },
];

/* ─────────────────────────────────────────
   Small reusable components
───────────────────────────────────────── */
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

/* ─────────────────────────────────────────
   Manager Comparison Visuals
───────────────────────────────────────── */
function ManagerComparisonChart({ managerStats }) {
  if (!managerStats || managerStats.length === 0) return null;
  // Sort by lowest health first (needs attention)
  const sorted = [...managerStats].sort((a, b) => a.averageHealth - b.averageHealth);
  
  return (
    <div className="g-card" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
      <div style={{ fontSize: '0.8rem', fontWeight: 800, textTransform: 'uppercase', color: 'var(--text-3)' }}>Average Health by Manager</div>
      {sorted.map(m => {
        const h = m.averageHealth;
        const color = h < 50 ? '#ef4444' : h < 80 ? '#f59e0b' : '#10b981';
        return (
          <div key={m.managerName} style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ width: 120, fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-1)' }}>{m.managerName}</div>
            <div style={{ flex: 1, height: 16, background: 'var(--surface-3)', borderRadius: 8, overflow: 'hidden' }}>
              <div style={{ width: `${h}%`, height: '100%', background: color, transition: 'width 0.3s ease' }} />
            </div>
            <div style={{ width: 40, textAlign: 'right', fontSize: '0.8rem', fontWeight: 700, color }}>{Math.round(h)}%</div>
          </div>
        );
      })}
    </div>
  );
}

function ProjectHealthList({ projectsHealth }) {
  if (!projectsHealth || projectsHealth.length === 0) return null;
  
  return (
    <div className="g-card" style={{ overflow: 'hidden' }}>
      <div className="mac-bar">Project Health Status</div>
      <table className="tbl">
        <thead>
          <tr>
            <th>Project</th>
            <th>Manager</th>
            <th>Health Score</th>
          </tr>
        </thead>
        <tbody>
          {projectsHealth.map((p, i) => {
             const h = p.healthScore;
             const color = h < 50 ? '#ef4444' : h < 80 ? '#f59e0b' : '#10b981';
             return (
              <tr key={i}>
                <td style={{ fontWeight: 600 }}>{p.projectName}</td>
                <td style={{ color: 'var(--text-2)' }}>{p.managerName}</td>
                <td style={{ fontWeight: 700, color }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <div style={{ width: 40 }}>{Math.round(h)}%</div>
                    <div style={{ width: 60, height: 6, background: 'var(--surface-3)', borderRadius: 3 }}>
                      <div style={{ width: `${h}%`, height: '100%', background: color, borderRadius: 3 }} />
                    </div>
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

function Overview({ self }) {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    getAdminManagerStats().then(setStats).catch(() => {});
  }, []);

  if (!self) return <div style={{ color: 'var(--text-3)', padding: '2rem' }}>Loading…</div>;

  const fmt = (ts) => ts
    ? new Date(ts).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })
    : '—';

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.75rem' }}>

      {/* Hero greeting */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
        <div style={{
          width: 56, height: 56, borderRadius: 16, flexShrink: 0,
          background: 'linear-gradient(135deg,#7c3aed,#4f46e5 45%,#0891b2)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: '0 8px 24px rgba(79,70,229,0.28)',
        }}>
          <span style={{ color: '#fff', fontSize: '1.4rem', fontWeight: 800 }}>
            {self.username?.[0]?.toUpperCase()}
          </span>
        </div>
        <div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 800, letterSpacing: '-0.5px' }}>
            Welcome back, {self.username} 👋
          </h2>
          <p style={{ color: 'var(--text-2)', marginTop: 2, fontSize: '0.9rem' }}>
            System overview and manager performance.
          </p>
        </div>
      </div>

      {/* Admin Overall Health & Stats */}
      {stats && (
        <div className="g-card" style={{ padding: '2rem', background: 'linear-gradient(135deg, rgba(79,70,229,0.05), rgba(8,145,178,0.05))', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <div style={{ fontSize: '0.8rem', fontWeight: 800, textTransform: 'uppercase', color: 'var(--indigo)', letterSpacing: '1px' }}>
              Overall System Health
            </div>
            <div style={{ fontSize: '3rem', fontWeight: 900, color: 'var(--text-1)', lineHeight: 1 }}>
              {stats.overall_system_health}%
            </div>
          </div>
          <div style={{ display: 'flex', gap: '2rem' }}>
             <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--text-1)' }}>{stats.manager_stats?.reduce((acc, m) => acc + m.projectCount, 0) || 0}</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-3)', fontWeight: 600 }}>Total Projects</div>
             </div>
             <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: '2rem', fontWeight: 800, color: '#ef4444' }}>{stats.manager_stats?.reduce((acc, m) => acc + m.activeIncidents, 0) || 0}</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-3)', fontWeight: 600 }}>Active Incidents</div>
             </div>
          </div>
        </div>
      )}

      {/* Manager Comparison Grid */}
      {stats && stats.manager_stats && (
        <ManagerComparisonChart managerStats={stats.manager_stats} />
      )}

      {/* Projects List */}
      {stats && stats.projects_health && (
        <ProjectHealthList projectsHealth={stats.projects_health} />
      )}

    </div>
  );
}


function CreateProjectManager() {
  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [msg, setMsg] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await createProjectManager(form);
      setMsg({ ok: true, text: 'Project Manager account created successfully.' });
      setForm({ username: '', email: '', password: '' });
    } catch {
      setMsg({ ok: false, text: 'Failed to create manager. Username or email may already exist.' });
    } finally { setLoading(false); }
  };

  return (
    <div style={{ maxWidth: 480 }}>
      <div style={{ marginBottom: '1.75rem' }}>
        <h2 style={{ fontSize: '1.4rem', fontWeight: 800, letterSpacing: '-0.5px' }}>Create Project Manager</h2>
        <p style={{ color: 'var(--text-2)', marginTop: 4, fontSize: '0.9rem' }}>Add a new project manager account to the system.</p>
      </div>

      <div className="g-card">
        <div className="mac-bar">New Account</div>
        <div style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
          <Field label="Username">
            <input className="inp no-icon" placeholder="e.g. JSmith" required value={form.username}
              onChange={e => setForm({ ...form, username: e.target.value })} />
          </Field>
          <Field label="Email">
            <input className="inp no-icon" type="email" placeholder="manager@company.com" required value={form.email}
              onChange={e => setForm({ ...form, email: e.target.value })} />
          </Field>
          <Field label="Password">
            <input className="inp no-icon" type="password" placeholder="••••••••" required value={form.password}
              onChange={e => setForm({ ...form, password: e.target.value })} />
          </Field>

          <Msg msg={msg} />

          <button className="btn btn-primary btn-full" onClick={handleSubmit} disabled={loading}
            style={{ borderRadius: 10, padding: '0.75rem' }}>
            <UserPlus size={15} /> {loading ? 'Creating…' : 'Create Account'}
          </button>
        </div>
      </div>
    </div>
  );
}

function ProjectManagerList() {
  const [managers, setManagers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState('');
  const [toggling, setToggling] = useState(null);

  const fetch = async () => {
    setLoading(true);
    try { setManagers(await getProjectManagersInfo()); } catch {}
    finally { setLoading(false); }
  };

  useEffect(() => { fetch(); }, []);

  const filtered = useMemo(() =>
    managers.filter(m =>
      (m.username || '').toLowerCase().includes(query.toLowerCase()) ||
      (m.email || '').toLowerCase().includes(query.toLowerCase())
    ), [managers, query]);

  const handleToggle = async (id, isCurrentlyEnabled) => {
    setToggling(id);
    const newValidity = isCurrentlyEnabled ? false : true;
    try { await changeProjectManagerValidity(id, newValidity); await fetch(); }
    catch { alert('Failed to update.'); }
    finally { setToggling(null); }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      {/* Header row */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '0.75rem' }}>
        <div>
          <h2 style={{ fontSize: '1.4rem', fontWeight: 800, letterSpacing: '-0.5px' }}>
            Project Managers
            <span style={{ marginLeft: 10, fontSize: '0.85rem', fontWeight: 700, background: 'rgba(79,70,229,0.1)', color: 'var(--indigo)', padding: '2px 10px', borderRadius: 20, verticalAlign: 'middle' }}>
              {managers.length} total
            </span>
          </h2>
          <p style={{ color: 'var(--text-2)', marginTop: 2, fontSize: '0.9rem' }}>Manage all project manager accounts.</p>
        </div>
        <div style={{ display: 'flex', gap: '0.6rem' }}>
          {/* Search */}
          <div className="input-wrap" style={{ width: 220 }}>
            <Search size={15} />
            <input className="inp" placeholder="Search name or email…"
              value={query} onChange={e => setQuery(e.target.value)} />
          </div>
          <button className="btn btn-ghost btn-sm" onClick={fetch} title="Refresh">
            <RefreshCw size={14} />
          </button>
        </div>
      </div>

      {/* Table */}
      <div className="g-card" style={{ overflow: 'hidden' }}>
        <div className="mac-bar">Directory</div>
        <div style={{ overflowX: 'auto' }}>
          {loading ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-3)' }}>Loading managers…</div>
          ) : filtered.length === 0 ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-3)' }}>
              <Users size={32} style={{ opacity: 0.3, marginBottom: 8 }} /><br />
              {query ? 'No results match your search.' : 'No managers found.'}
            </div>
          ) : (
            <table className="tbl">
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Email</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((m, i) => (
                  <tr key={i}>
                    <td style={{ fontWeight: 600 }}>{m.username}</td>
                    <td style={{ color: 'var(--text-2)' }}>{m.email}</td>
                    <td>
                        <span className={`pill ${m.isEnabled ? 'pill-active' : 'pill-disabled'}`}>
                          {m.isEnabled ? 'Active' : 'Disabled'}
                        </span>
                      </td>
                    <td>
                      <button
                        className={`btn btn-sm ${m.isEnabled ? 'btn-danger' : 'btn-mint'}`}
                        disabled={toggling === m.id}
                        onClick={() => handleToggle(m.id, m.isEnabled)}
                      >
                        {toggling === m.id
                          ? '…'
                          : m.isEnabled
                            ? <><XCircle size={12} /> Disable</>
                            : <><CheckCircle2 size={12} /> Enable</>
                        }
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

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
    <div style={{ maxWidth: 480 }}>
      <div style={{ marginBottom: '1.75rem' }}>
        <h2 style={{ fontSize: '1.4rem', fontWeight: 800, letterSpacing: '-0.5px' }}>My Profile</h2>
        <p style={{ color: 'var(--text-2)', marginTop: 4, fontSize: '0.9rem' }}>Update your own name, email, or password.</p>
      </div>

      {self && (
        <div className="g-panel" style={{ padding: '1rem 1.25rem', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.9rem' }}>
          <div style={{ width: 40, height: 40, borderRadius: 12, background: 'var(--grad-vibrancy, linear-gradient(135deg,#7c3aed,#0891b2))', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <User size={18} color="#fff" />
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>{self.username}</div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-3)' }}>{self.email}</div>
          </div>
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
            style={{ borderRadius: 10, padding: '0.75rem' }}>
            <Settings size={15} /> {loading ? 'Saving…' : 'Save Changes'}
          </button>
        </div>
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────
   Main Dashboard
───────────────────────────────────────── */
export default function AdminDashboard() {
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
      case 'create':   return <CreateProjectManager />;
      case 'managers': return <ProjectManagerList />;
      case 'profile':  return <Profile self={self} />;
      default:         return null;
    }
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', position: 'relative', zIndex: 1 }}>

      {/* ── Sidebar ── */}
      <aside style={{
        width: 220,
        flexShrink: 0,
        display: 'flex',
        flexDirection: 'column',
        padding: '1.5rem 1rem',
        background: 'rgba(255,255,255,0.45)',
        backdropFilter: 'blur(28px) saturate(180%)',
        WebkitBackdropFilter: 'blur(28px) saturate(180%)',
        borderRight: '1px solid rgba(255,255,255,0.65)',
        position: 'sticky',
        top: 0,
        height: '100vh',
        boxShadow: '2px 0 24px rgba(30,40,90,0.06)',
      }}>
        {/* Brand */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 9, marginBottom: '2.5rem', paddingLeft: 4 }}>
          <div style={{
            width: 34, height: 34, borderRadius: 10,
            background: 'linear-gradient(135deg,#7c3aed,#4f46e5 45%,#0891b2)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            boxShadow: '0 4px 12px rgba(79,70,229,0.3)',
            flexShrink: 0
          }}>
            <ShieldCheck size={17} color="#fff" strokeWidth={2} />
          </div>
          <div>
            <div style={{ fontSize: '0.95rem', fontWeight: 800, letterSpacing: '-0.4px', color: 'var(--text-1)' }}>DriftGuard</div>
            <div style={{ fontSize: '0.68rem', fontWeight: 600, color: 'var(--indigo)', letterSpacing: '0.4px', marginTop: -1 }}>ADMIN</div>
          </div>
        </div>

        {/* Nav */}
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', flex: 1 }}>
          {NAV.map(({ id, label, icon: Icon }) => {
            const isActive = active === id;
            return (
              <button
                key={id}
                onClick={() => setActive(id)}
                style={{
                  display: 'flex', alignItems: 'center', gap: 10,
                  padding: '0.6rem 0.9rem',
                  borderRadius: 9,
                  border: 'none',
                  background: isActive ? 'rgba(79,70,229,0.12)' : 'transparent',
                  color: isActive ? 'var(--indigo)' : 'var(--text-2)',
                  fontWeight: isActive ? 700 : 500,
                  fontSize: '0.88rem',
                  cursor: 'pointer',
                  transition: 'all 0.15s',
                  textAlign: 'left',
                  width: '100%',
                  boxShadow: isActive ? '0 0 0 1px rgba(79,70,229,0.2)' : 'none',
                }}
              >
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

      {/* ── Main content ── */}
      <main style={{
        flex: 1,
        padding: '2.5rem 2.5rem',
        overflowY: 'auto',
        maxHeight: '100vh',
      }}>
        {renderPanel()}
      </main>
    </div>
  );
}
