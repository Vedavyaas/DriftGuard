import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSelfInfo, changeDetails, createProject, getProjects, changeProjectStatus, changeProjectBaseline, getIncidents, injectTestEvent, getProjectIncidents, updateIncidentStatus, generateReport, clearProjectIncidents, ingestFile } from '../services/api';
import {
  LayoutDashboard, Settings, LogOut, PlusCircle,
  BarChart2, CheckCircle2, AlertCircle, User, FileUp, FolderGit2, Activity, Key, ShieldAlert,
  FlaskConical, ChevronDown, ChevronUp, Send, FileText, ArrowLeft, RefreshCw, BookOpen, Shield, Clock
} from 'lucide-react';

const NAV = [
  { id: 'overview', label: 'Overview', icon: LayoutDashboard },
  { id: 'projects', label: 'My Projects', icon: FolderGit2 },
  { id: 'create',   label: 'Create Project', icon: PlusCircle },
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
          <span className="pill pill-active" style={{ marginLeft: 'auto', fontSize: '0.7rem' }}>Project Manager</span>
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

/* ── My Projects ── */
function ProjectsList() {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState(null);
  const [selected, setSelected] = useState(null); // selected project for drill-down

  useEffect(() => { fetchProjects(); }, []);

  const fetchProjects = async () => {
    setLoading(true);
    try { setProjects(await getProjects()); }
    catch { setMsg({ ok: false, text: 'Failed to load projects.' }); }
    finally { setLoading(false); }
  };

  const handleStatusChange = async (id, newStatus) => {
    try { await changeProjectStatus(id, newStatus); setMsg({ ok: true, text: 'Status updated.' }); fetchProjects(); }
    catch { setMsg({ ok: false, text: 'Failed to update status.' }); }
  };

  const handleBaselineUpload = async (id, e) => {
    const file = e.target.files[0]; if (!file) return;
    try { await changeProjectBaseline(id, file); setMsg({ ok: true, text: 'Baseline updated.' }); }
    catch { setMsg({ ok: false, text: 'Failed to update baseline.' }); }
  };

  if (loading) return <div style={{ padding: '2rem', color: 'var(--text-3)' }}>Loading projects...</div>;

  // ── Drill-down view ──────────────────────────────────────────────────────────
  if (selected) return <ProjectDrillDown project={selected} onBack={() => setSelected(null)} />;

  return (
    <div style={{ maxWidth: 800 }}>
      <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2 style={{ fontSize: '1.3rem', fontWeight: 800, letterSpacing: '-0.5px' }}>My Projects</h2>
          <p style={{ color: 'var(--text-2)', marginTop: 4, fontSize: '0.85rem' }}>Click a project to see its incidents and generate reports.</p>
        </div>
        <button onClick={fetchProjects} className="btn" style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }}>
          <RefreshCw size={13} /> Refresh
        </button>
      </div>

      <Msg msg={msg} />

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {projects.length === 0 ? (
          <div style={{ padding: '2rem', textAlign: 'center', background: 'rgba(0,0,0,0.02)', borderRadius: 12, border: '1px dashed rgba(0,0,0,0.1)' }}>
            <FolderGit2 size={32} color="var(--text-4)" style={{ marginBottom: '1rem' }} />
            <div style={{ fontWeight: 600, color: 'var(--text-2)' }}>No projects found.</div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-3)', marginTop: 4 }}>Create one to get started!</div>
          </div>
        ) : projects.map(p => (
          <div key={p.id} className="g-card" style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {/* Clickable header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', cursor: 'pointer' }}
                 onClick={() => setSelected(p)}>
              <div>
                <div style={{ fontSize: '1.1rem', fontWeight: 800, color: 'var(--text-1)', display: 'flex', alignItems: 'center', gap: 8 }}>
                  <FolderGit2 size={18} color="var(--cyan)" /> {p.projectName}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-3)', display: 'flex', alignItems: 'center', gap: 6, marginTop: 4, fontFamily: 'monospace' }}>
                  <Key size={12} /> {p.projectHash}
                </div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <span className={`pill ${p.status === 'FINISHED' ? 'pill-active' : p.status === 'AT_RISK' ? 'pill-danger' : 'pill-disabled'}`} style={{ fontSize: '0.7rem' }}>{p.status}</span>
                <span style={{ fontSize: '0.75rem', color: 'var(--cyan)', fontWeight: 600 }}>View Incidents →</span>
              </div>
            </div>

            {/* Controls */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', background: 'rgba(0,0,0,0.015)', padding: '1rem', borderRadius: 10, border: '1px solid rgba(0,0,0,0.04)' }}>
              <div>
                <div style={{ fontSize: '0.75rem', fontWeight: 700, marginBottom: 6, display: 'flex', alignItems: 'center', gap: 6 }}><Activity size={14} /> Update Status</div>
                <select className="inp no-icon" value={p.status} onChange={e => { e.stopPropagation(); handleStatusChange(p.id, e.target.value); }} style={{ padding: '0.4rem 0.6rem', fontSize: '0.8rem' }}>
                  <option value="NOT_STARTED">Not Started</option>
                  <option value="PLANNING">Planning</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="AT_RISK">At Risk</option>
                  <option value="FINISHED">Finished</option>
                </select>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', fontWeight: 700, marginBottom: 6, display: 'flex', alignItems: 'center', gap: 6 }}><FileUp size={14} /> Update Baseline</div>
                <input type="file" onChange={e => { e.stopPropagation(); handleBaselineUpload(p.id, e); }} style={{ fontSize: '0.75rem', width: '100%' }} />
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

/* ── Project Drill-Down ── */
const SEV_COLOR = { CRITICAL: '#ef4444', HIGH: '#f97316', MEDIUM: '#eab308', LOW: '#22c55e' };
const STATUS_LABEL = { UNREAD: 'Unread', NOTIFIED: 'Notified', RESOLVED: 'Resolved' };

function ProjectDrillDown({ project, onBack }) {
  const [tab, setTab] = useState('incidents'); // 'incidents' | 'report' | 'analytics'
  const [incidents, setIncidents] = useState([]);
  const [incLoading, setIncLoading] = useState(true);
  const [report, setReport] = useState(null);
  const [reportLoading, setReportLoading] = useState(false);
  const [reportErr, setReportErr] = useState(null);
  const [selectedInc, setSelectedInc] = useState(null);
  const [statusUpdating, setStatusUpdating] = useState({});
  const [clearing, setClearing] = useState(false);

  useEffect(() => { loadIncidents(); }, []);

  const loadIncidents = async () => {
    setIncLoading(true);
    try { setIncidents(await getProjectIncidents(project.projectHash)); }
    catch { setIncidents([]); }
    finally { setIncLoading(false); }
  };

  const handleClearIncidents = async () => {
    if (!window.confirm('Clear ALL incidents for this project? This cannot be undone.')) return;
    setClearing(true);
    try {
      await clearProjectIncidents(project.projectHash);
      setIncidents([]);
      setSelectedInc(null);
    } finally { setClearing(false); }
  };

  const handleStatusChange = async (incidentId, newStatus) => {
    setStatusUpdating(s => ({ ...s, [incidentId]: true }));
    try {
      await updateIncidentStatus(incidentId, newStatus);
      setIncidents(prev => prev.map(i => i.incidentId === incidentId ? { ...i, status: newStatus } : i));
    } finally {
      setStatusUpdating(s => ({ ...s, [incidentId]: false }));
    }
  };

  const handleGenerateReport = async () => {
    setReportLoading(true); setReportErr(null); setReport(null);
    try { setReport(await generateReport(project.projectHash)); }
    catch (e) { setReportErr(e.message); }
    finally { setReportLoading(false); }
  };

  return (
    <div style={{ maxWidth: 860 }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: '1.5rem' }}>
        <button className="btn btn-ghost btn-sm" onClick={onBack} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <ArrowLeft size={14} /> Back
        </button>
        <div style={{ flex: 1 }}>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 800, letterSpacing: '-0.4px', display: 'flex', alignItems: 'center', gap: 8 }}>
            <FolderGit2 size={20} color="var(--cyan)" /> {project.projectName}
          </h2>
          <div style={{ fontSize: '0.72rem', fontFamily: 'monospace', color: 'var(--text-3)', marginTop: 2 }}>
            Hash: {project.projectHash}
          </div>
        </div>
        <button onClick={handleClearIncidents} disabled={clearing || incidents.length === 0}
          style={{ display: 'flex', alignItems: 'center', gap: 6, padding: '0.4rem 0.9rem',
            borderRadius: 8, border: '1px solid rgba(239,68,68,0.3)', background: 'rgba(239,68,68,0.07)',
            color: '#991b1b', fontWeight: 700, fontSize: '0.78rem', cursor: 'pointer' }}>
          🗑 {clearing ? 'Clearing…' : 'Clear Incidents'}
        </button>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.25rem' }}>
        {[{ id: 'incidents', label: 'Incident Log', icon: ShieldAlert },
          { id: 'report',    label: 'Report',        icon: BookOpen }].map(({ id, label, icon: Icon }) => (
          <button key={id} onClick={() => setTab(id)} style={{
            display: 'flex', alignItems: 'center', gap: 7,
            padding: '0.45rem 1rem', borderRadius: 8, border: 'none', cursor: 'pointer',
            fontSize: '0.85rem', fontWeight: 600,
            background: tab === id ? 'rgba(8,145,178,0.12)' : 'rgba(0,0,0,0.04)',
            color: tab === id ? 'var(--cyan)' : 'var(--text-2)',
            boxShadow: tab === id ? '0 0 0 1px rgba(8,145,178,0.25)' : 'none',
          }}>
            <Icon size={14} /> {label}
          </button>
        ))}
        <button onClick={loadIncidents} className="btn btn-ghost btn-sm" style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 6 }}>
          <RefreshCw size={13} /> Refresh
        </button>
      </div>

      {/* ── Incident Log Tab ──────────────────────────────────────────────────── */}
      {tab === 'incidents' && (
        <div>
          {incLoading ? (
            <div style={{ padding: '2rem', color: 'var(--text-3)' }}>Loading incidents...</div>
          ) : incidents.length === 0 ? (
            <div style={{ padding: '3rem', textAlign: 'center', background: 'rgba(0,0,0,0.02)', borderRadius: 12, border: '1px dashed rgba(0,0,0,0.1)' }}>
              <Shield size={36} color="var(--text-4)" style={{ marginBottom: '1rem' }} />
              <div style={{ fontWeight: 600, color: 'var(--text-2)' }}>No incidents detected.</div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-3)', marginTop: 4 }}>Send events via Kafka to trigger ML analysis.</div>
            </div>
          ) : selectedInc ? (
            /* Incident Detail */
            <div>
              <button className="btn btn-ghost btn-sm" onClick={() => setSelectedInc(null)} style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: 6 }}>
                <ArrowLeft size={13} /> All Incidents
              </button>
              <div className="g-card" style={{ overflow: 'hidden' }}>
                <div className="mac-bar" style={{ display: 'flex', justifyContent: 'space-between', padding: '0 15px' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <ShieldAlert size={14} /> Incident {selectedInc.incidentId.substring(0, 8)}…
                  </span>
                  <span style={{ fontSize: '0.75rem', fontWeight: 700, color: SEV_COLOR[selectedInc.severity] || 'inherit' }}>{selectedInc.severity}</span>
                </div>
                <div style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                  {/* Meta */}
                  <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
                    <span style={{ fontSize: '0.75rem', padding: '0.2rem 0.6rem', borderRadius: 20, fontWeight: 700,
                      background: `${SEV_COLOR[selectedInc.severity]}22`, color: SEV_COLOR[selectedInc.severity] }}>
                      {selectedInc.severity}
                    </span>
                    <span style={{ fontSize: '0.75rem', padding: '0.2rem 0.6rem', borderRadius: 20, fontWeight: 700,
                      background: 'rgba(0,0,0,0.04)', color: 'var(--text-2)' }}>
                      <Clock size={11} style={{ marginRight: 4 }} />{new Date(selectedInc.detectedAt).toLocaleString()}
                    </span>
                    <span style={{ fontSize: '0.75rem', padding: '0.2rem 0.6rem', borderRadius: 20, fontWeight: 700,
                      background: selectedInc.status === 'RESOLVED' ? 'rgba(16,185,129,0.12)' : 'rgba(239,68,68,0.08)',
                      color: selectedInc.status === 'RESOLVED' ? '#065f46' : '#991b1b' }}>
                      {STATUS_LABEL[selectedInc.status]}
                    </span>
                  </div>
                  {/* Description */}
                  <div>
                    <div style={{ fontWeight: 700, fontSize: '0.85rem', marginBottom: 6, color: 'var(--indigo)' }}>Problem Description</div>
                    <div className="g-panel" style={{ padding: '1rem', whiteSpace: 'pre-wrap', fontSize: '0.85rem', lineHeight: 1.6 }}>
                      {selectedInc.problemDescription || 'No description.'}
                    </div>
                  </div>
                  {/* Remediation */}
                  <div>
                    <div style={{ fontWeight: 700, fontSize: '0.85rem', marginBottom: 6, color: 'var(--mint)' }}>Remediation Steps</div>
                    <div className="g-panel" style={{ padding: '1rem', whiteSpace: 'pre-wrap', fontSize: '0.85rem', lineHeight: 1.6 }}>
                      {selectedInc.remediationSteps || 'No remediation steps.'}
                    </div>
                  </div>
                  {/* Status change */}
                  <div style={{ display: 'flex', gap: '0.5rem', paddingTop: '0.5rem', borderTop: '1px solid rgba(0,0,0,0.06)' }}>
                    <span style={{ fontSize: '0.8rem', fontWeight: 600, alignSelf: 'center' }}>Mark as:</span>
                    {['RESOLVED'].map(s => (
                      <button key={s} disabled={selectedInc.status === s || statusUpdating[selectedInc.incidentId]}
                        onClick={() => handleStatusChange(selectedInc.incidentId, s)}
                        style={{ padding: '0.3rem 0.75rem', borderRadius: 7, border: 'none', cursor: 'pointer', fontSize: '0.78rem', fontWeight: 700,
                          background: selectedInc.status === s ? 'rgba(8,145,178,0.15)' : 'rgba(0,0,0,0.06)',
                          color: selectedInc.status === s ? 'var(--cyan)' : 'var(--text-2)' }}>
                        {s}
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          ) : (
            /* Incident List */
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
              {incidents.filter(inc => inc.status !== 'RESOLVED').map(inc => (
                <div key={inc.incidentId} onClick={() => setSelectedInc(inc)}
                     className="g-card" style={{ padding: '1rem 1.25rem', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '1rem',
                       borderLeft: `3px solid ${SEV_COLOR[inc.severity] || '#ccc'}` }}>
                  <ShieldAlert size={18} color={SEV_COLOR[inc.severity] || 'var(--text-3)'} style={{ flexShrink: 0 }} />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontWeight: 700, fontSize: '0.85rem', marginBottom: 2 }}>
                      {inc.problemDescription?.split('\n')[0].substring(0, 80) || 'Incident'}
                    </div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-3)', display: 'flex', gap: 12 }}>
                      <span><Clock size={10} /> {new Date(inc.detectedAt).toLocaleString()}</span>
                    </div>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 8, flexShrink: 0 }}>
                    <div style={{ display: 'flex', gap: 6 }}>
                      <span style={{ fontSize: '0.7rem', fontWeight: 800, padding: '0.15rem 0.5rem', borderRadius: 20,
                        background: `${SEV_COLOR[inc.severity]}22`, color: SEV_COLOR[inc.severity] }}>{inc.severity}</span>
                      <span style={{ fontSize: '0.68rem', padding: '0.15rem 0.5rem', borderRadius: 20, fontWeight: 600,
                        background: inc.status === 'RESOLVED' ? 'rgba(16,185,129,0.1)' : inc.status === 'NOTIFIED' ? 'rgba(234,179,8,0.1)' : 'rgba(239,68,68,0.08)',
                        color: inc.status === 'RESOLVED' ? '#065f46' : inc.status === 'NOTIFIED' ? '#92400e' : '#991b1b' }}>
                        {STATUS_LABEL[inc.status]}
                      </span>
                    </div>
                    <button 
                      disabled={statusUpdating[inc.incidentId]}
                      onClick={(e) => { 
                        e.stopPropagation(); 
                        handleStatusChange(inc.incidentId, 'RESOLVED'); 
                      }}
                      style={{ 
                        fontSize: '0.7rem', fontWeight: 700, padding: '0.25rem 0.75rem', borderRadius: 6, 
                        border: '1px solid rgba(16,185,129,0.4)', background: 'rgba(16,185,129,0.1)', 
                        color: '#065f46', cursor: statusUpdating[inc.incidentId] ? 'not-allowed' : 'pointer',
                        transition: 'all 0.2s'
                      }}
                    >
                      {statusUpdating[inc.incidentId] ? 'Resolving…' : '✓ Resolve'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* ── Report Tab ───────────────────────────────────────────────────────── */}
      {tab === 'report' && (
        <div>
          {!report && !reportLoading && !reportErr && (
            <div style={{ padding: '3rem', textAlign: 'center', background: 'rgba(0,0,0,0.02)', borderRadius: 12, border: '1px dashed rgba(0,0,0,0.1)' }}>
              <FileText size={36} color="var(--text-4)" style={{ marginBottom: '1rem' }} />
              <div style={{ fontWeight: 700, fontSize: '1rem', marginBottom: 6 }}>Generate an AI-Powered Report</div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-3)', marginBottom: '1.5rem' }}>
                Groq LLM analyzes all received events for this project and writes an executive security narrative.
              </div>
              <button onClick={handleGenerateReport} style={{
                display: 'inline-flex', alignItems: 'center', gap: 8,
                padding: '0.65rem 1.5rem', borderRadius: 10, border: 'none', cursor: 'pointer',
                background: 'linear-gradient(135deg,#7c3aed,#0891b2)',
                color: '#fff', fontWeight: 700, fontSize: '0.9rem',
                boxShadow: '0 6px 20px rgba(124,58,237,0.3)'
              }}>
                <BookOpen size={16} /> Generate Report
              </button>
            </div>
          )}

          {reportLoading && (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-3)' }}>
              <div style={{ fontSize: '0.9rem' }}>🤖 Groq is analyzing events and writing your report…</div>
              <div style={{ fontSize: '0.78rem', marginTop: 8, color: 'var(--text-4)' }}>This may take 10–30 seconds.</div>
            </div>
          )}

          {reportErr && (
            <div style={{ padding: '1rem', background: 'rgba(239,68,68,0.08)', borderRadius: 10, color: '#991b1b', fontSize: '0.85rem', marginBottom: '1rem' }}>
              ❌ {reportErr}
              <button onClick={handleGenerateReport} style={{ marginLeft: 12, fontWeight: 700, background: 'none', border: 'none', cursor: 'pointer', color: '#991b1b' }}>Retry</button>
            </div>
          )}

          {report && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              {/* Summary cards */}
              {report.summary && (
                <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
                  {Object.entries(report.summary).map(([k, v]) => (
                    <div key={k} style={{ background: 'rgba(0,0,0,0.03)', border: '1px solid rgba(0,0,0,0.07)', borderRadius: 10, padding: '0.75rem 1.25rem', minWidth: 110 }}>
                      <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--cyan)' }}>{v}</div>
                      <div style={{ fontSize: '0.7rem', fontWeight: 600, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>{k.replace(/_/g, ' ')}</div>
                    </div>
                  ))}
                </div>
              )}

              {/* Title */}
              {report.report_title && (
                <h3 style={{ fontSize: '1.1rem', fontWeight: 800, color: 'var(--text-1)' }}>{report.report_title}</h3>
              )}

              {/* Narrative */}
              {report.executive_narrative && (
                <div className="g-card" style={{ overflow: 'hidden' }}>
                  <div className="mac-bar">Executive Narrative (Groq LLM)</div>
                  <div style={{ padding: '1.5rem', whiteSpace: 'pre-wrap', fontSize: '0.88rem', lineHeight: 1.7, color: 'var(--text-1)' }}>
                    {report.executive_narrative}
                  </div>
                </div>
              )}

              {/* Critical incidents list */}
              {report.critical_incidents && report.critical_incidents.length > 0 && (
                <div>
                  <div style={{ fontWeight: 700, marginBottom: 8, color: '#ef4444' }}>Critical Incidents Detected</div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {report.critical_incidents.map((ci, i) => (
                      <div key={i} style={{ 
                        padding: '1.25rem', background: 'rgba(239,68,68,0.04)', 
                        border: '1px solid rgba(239,68,68,0.2)', borderRadius: 12, fontSize: '0.82rem' 
                      }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 12 }}>
                          <span style={{ fontWeight: 800, fontSize: '1rem', color: '#991b1b', display: 'flex', alignItems: 'center', gap: 6 }}>
                            <ShieldAlert size={16} /> {ci.incident_id}
                          </span>
                          <span style={{ padding: '0.2rem 0.6rem', borderRadius: 20, background: 'rgba(239,68,68,0.15)', color: '#b91c1c', fontWeight: 800, fontSize: '0.75rem' }}>
                            {ci.max_severity} (Score: {ci.total_risk_score})
                          </span>
                        </div>
                        
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: 16 }}>
                          <div>
                            <div style={{ fontWeight: 700, color: 'var(--text-3)', fontSize: '0.75rem', marginBottom: 4 }}>Affected Domains</div>
                            <div style={{ color: 'var(--text-1)' }}>{ci.domains?.join(', ') || 'None'}</div>
                          </div>
                          <div>
                            <div style={{ fontWeight: 700, color: 'var(--text-3)', fontSize: '0.75rem', marginBottom: 4 }}>Actors Involved</div>
                            <div style={{ color: 'var(--text-1)' }}>{ci.actors?.join(', ') || 'Unknown'}</div>
                          </div>
                        </div>

                        <div style={{ background: '#fff', padding: '1rem', borderRadius: 8, border: '1px solid rgba(0,0,0,0.05)', marginBottom: 16 }}>
                          <div style={{ fontWeight: 800, color: 'var(--indigo)', marginBottom: 6 }}>Blast Radius</div>
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 6, color: 'var(--text-2)' }}>
                            <div><strong>Systems:</strong> {ci.blast_radius?.affected_systems?.join(', ')}</div>
                            <div><strong>Parameters:</strong> {ci.blast_radius?.exposed_parameters?.join(', ')}</div>
                          </div>
                        </div>

                        <div>
                          <div style={{ fontWeight: 800, color: 'var(--mint)', marginBottom: 6 }}>Remediation Steps</div>
                          <ul style={{ margin: 0, paddingLeft: 18, color: 'var(--text-2)', display: 'flex', flexDirection: 'column', gap: 4 }}>
                            {ci.remediation_steps?.map((step, idx) => (
                              <li key={idx}>{step}</li>
                            ))}
                          </ul>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <button onClick={() => setReport(null)} style={{ alignSelf: 'flex-start', padding: '0.4rem 0.9rem', borderRadius: 8, border: '1px solid rgba(0,0,0,0.1)', background: 'transparent', cursor: 'pointer', fontSize: '0.8rem' }}>
                Regenerate
              </button>
            </div>
          )}
        </div>
      )}

    </div>
  );
}

/* ── Create Project ── */
function CreateProject() {
  const [form, setForm] = useState({
    projectName: '',
    status: 'NOT_STARTED'
  });
  const [file, setFile] = useState(null);
  const [msg, setMsg] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file) {
      setMsg({ ok: false, text: 'A baseline configuration file is mandatory.' });
      return;
    }
    setLoading(true);
    try {
      const credentials = {
        projectName: form.projectName,
        status: form.status,
        githubIntegrationCredentials: null,
        cloudProviderCredentials: null
      };

      const formData = new FormData();
      formData.append('projectCreationCredentials', new Blob([JSON.stringify(credentials)], {
        type: "application/json"
      }));
      formData.append('baseLineFile', file);

      await createProject(formData);
      setMsg({ ok: true, text: 'Project created successfully. Check My Projects to see your Project Hash!' });
      setForm({ projectName: '', status: 'NOT_STARTED' });
      setFile(null);
    } catch {
      setMsg({ ok: false, text: 'Failed to create project. Please check the details.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 500 }}>
      <div style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.3rem', fontWeight: 800, letterSpacing: '-0.5px' }}>Register Project</h2>
        <p style={{ color: 'var(--text-2)', marginTop: 4, fontSize: '0.85rem' }}>Register a new project to generate an automated ingestion Hash.</p>
      </div>

      <div className="g-card">
        <div className="mac-bar">Project Details</div>
        <div style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          
          <Field label={<span>Project Name <span style={{ color: 'var(--text-3)', fontSize: '0.75rem', fontWeight: 500 }}>(Unchangeable later)</span></span>}>
            <input className="inp no-icon" placeholder="e.g. Phoenix-Core" required value={form.projectName}
              onChange={e => setForm({ ...form, projectName: e.target.value })} />
          </Field>
          
          <Field label="Initial Status">
            <select className="inp no-icon" value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
              <option value="NOT_STARTED">Not Started</option>
              <option value="PLANNING">Planning</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="AT_RISK">At Risk</option>
              <option value="FINISHED">Finished</option>
            </select>
          </Field>

          <div style={{ background: 'rgba(0,0,0,0.02)', padding: '1rem', borderRadius: 10, border: '1px dashed rgba(0,0,0,0.15)' }}>
            <div style={{ fontWeight: 700, fontSize: '0.85rem', marginBottom: 8, display: 'flex', alignItems: 'center', gap: 8 }}>
              <FileUp size={15} /> Baseline Configuration File <span style={{ color: 'var(--text-3)', fontSize: '0.75rem', fontWeight: 500 }}>(Required)</span>
            </div>
            <input type="file" required onChange={e => setFile(e.target.files[0])} style={{ fontSize: '0.8rem' }} />
          </div>

          <Msg msg={msg} />

          <button className="btn btn-primary btn-full" onClick={handleSubmit} disabled={loading}
            style={{ borderRadius: 10, padding: '0.75rem', background: 'linear-gradient(135deg,#0891b2,#059669)', boxShadow: '0 6px 18px rgba(8,145,178,0.25)' }}>
            <PlusCircle size={15} /> {loading ? 'Registering…' : 'Register Project'}
          </button>
        </div>
      </div>
    </div>
  );
}

/* ── Threat Incidents ── */

function IncidentsList() {
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState(null);
  const [selectedIncident, setSelectedIncident] = useState(null);

  useEffect(() => {
    fetchIncidents();
  }, []);

  const fetchIncidents = async () => {
    setLoading(true);
    try {
      const data = await getIncidents();
      setIncidents(data);
    } catch (err) {
      console.error('Incidents fetch error:', err);
      setMsg({ ok: false, text: `Failed to load incidents: ${err.message}` });
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div style={{ padding: '2rem', color: 'var(--text-3)' }}>Loading incidents...</div>;

  if (selectedIncident) {
    return (
      <div style={{ maxWidth: 800 }}>
        <button className="btn btn-ghost btn-sm" onClick={() => setSelectedIncident(null)} style={{ marginBottom: '1rem' }}>
          &larr; Back to Incidents
        </button>
        <div className="g-card" style={{ overflow: 'hidden' }}>
          <div className="mac-bar" style={{ display: 'flex', justifyContent: 'space-between', padding: '0 15px' }}>
            <span>Incident Details</span>
            <span className={`pill ${selectedIncident.severity === 'CRITICAL' ? 'pill-disabled' : selectedIncident.severity === 'HIGH' ? 'pill-disabled' : 'pill-active'}`}>
              {selectedIncident.severity}
            </span>
          </div>
          <div style={{ padding: '2rem' }}>
            <h2 style={{ fontSize: '1.2rem', fontWeight: 800, marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <ShieldAlert color="var(--rose)" /> Incident {selectedIncident.incidentId.substring(0,8)}...
            </h2>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-3)', marginBottom: '1.5rem', fontFamily: 'monospace' }}>
              Project Hash: {selectedIncident.projectHash} | Detected: {new Date(selectedIncident.detectedAt).toLocaleString()}
            </div>
            
            <div style={{ marginBottom: '1.5rem' }}>
              <h3 style={{ fontSize: '0.9rem', fontWeight: 700, marginBottom: '0.5rem', color: 'var(--indigo)' }}>Problem Description</h3>
              <div className="g-panel" style={{ padding: '1rem', whiteSpace: 'pre-wrap', fontSize: '0.9rem', lineHeight: 1.5 }}>
                {selectedIncident.problemDescription || "No description provided."}
              </div>
            </div>

            <div>
              <h3 style={{ fontSize: '0.9rem', fontWeight: 700, marginBottom: '0.5rem', color: 'var(--mint)' }}>Remediation Steps</h3>
              <div className="g-panel" style={{ padding: '1rem', whiteSpace: 'pre-wrap', fontSize: '0.9rem', lineHeight: 1.5 }}>
                {selectedIncident.remediationSteps || "No remediation steps provided."}
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 900 }}>
      <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2 style={{ fontSize: '1.3rem', fontWeight: 800, letterSpacing: '-0.5px' }}>Threat Incidents</h2>
          <p style={{ color: 'var(--text-2)', marginTop: 4, fontSize: '0.85rem' }}>AI-detected anomalies and compound threats.</p>
        </div>
        <button onClick={fetchIncidents} className="btn" style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }}>Refresh</button>
      </div>

      <Msg msg={msg} />

      <div className="grid-2">
        {incidents.length === 0 ? (
          <div style={{ padding: '2rem', textAlign: 'center', gridColumn: '1 / -1', background: 'rgba(0,0,0,0.02)', borderRadius: 12, border: '1px dashed rgba(0,0,0,0.1)' }}>
            <ShieldAlert size={32} color="var(--mint)" style={{ marginBottom: '1rem' }} />
            <div style={{ fontWeight: 600, color: 'var(--text-2)' }}>No threats detected!</div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-3)', marginTop: 4 }}>Your environment is secure.</div>
          </div>
        ) : incidents.map(inc => (
          <div 
            key={inc.incidentId} 
            className="g-card" 
            style={{ 
              padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.8rem', cursor: 'pointer',
              border: inc.severity === 'CRITICAL' ? '1px solid rgba(225,29,72,0.4)' : undefined,
              boxShadow: inc.severity === 'CRITICAL' ? '0 4px 20px rgba(225,29,72,0.15)' : undefined
            }}
            onClick={() => setSelectedIncident(inc)}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span className={`pill ${inc.severity === 'CRITICAL' ? 'pill-disabled' : inc.severity === 'HIGH' ? 'pill-disabled' : 'pill-active'}`} style={{ fontSize: '0.7rem' }}>
                {inc.severity}
              </span>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-3)' }}>
                {new Date(inc.detectedAt).toLocaleTimeString()}
              </span>
            </div>
            <div style={{ fontSize: '1.05rem', fontWeight: 700, color: 'var(--text-1)' }}>
              Incident {inc.incidentId.substring(0,8)}...
            </div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-2)', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
              {inc.problemDescription}
            </div>
            <div style={{ marginTop: 'auto', paddingTop: '0.5rem', borderTop: '1px solid rgba(255,255,255,0.4)', fontSize: '0.75rem', color: 'var(--text-3)', display: 'flex', alignItems: 'center', gap: 6, fontFamily: 'monospace' }}>
              <Key size={12} /> {inc.projectHash.substring(0,16)}...
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

/* ── Predefined Event Injector ── */
import { sampleCriticalSingle, sampleCompoundLowEvents, sampleInsiderThreat } from '../utils/samples';

function FileEventInjector() {
  const [open, setOpen]       = useState(false);
  const [projects, setProjects] = useState([]);
  const [selHash, setSelHash] = useState('');
  const [sampleType, setSampleType] = useState('critical');
  const [status, setStatus]   = useState(null);
  const [busy, setBusy]       = useState(false);

  // Lazy-load projects when panel opens
  const handleOpen = async () => {
    const next = !open;
    setOpen(next);
    if (next && projects.length === 0) {
      try {
        const data = await getProjects();
        setProjects(data);
        if (data.length > 0) setSelHash(data[0].projectHash);
      } catch { setProjects([]); }
    }
  };

  const handleInject = async () => {
    if (!selHash) { setStatus({ ok: false, text: 'Select a project.' }); return; }
    setBusy(true); setStatus(null);
    try {
      let events = [];
      if (sampleType === 'critical') events = sampleCriticalSingle;
      else if (sampleType === 'compound') events = sampleCompoundLowEvents;
      else if (sampleType === 'insider') events = sampleInsiderThreat;

      // Stamp the selected project hash onto every event
      const stamped = events.map(e => ({ ...e, project_hash: selHash, timestamp: new Date().toISOString() }));
      
      // Inject events individually as requested
      let sentCount = 0;
      for (const e of stamped) {
        const res = await injectTestEvent([e]);
        sentCount += res.events_sent || 1;
      }
      
      setStatus({ ok: true, text: `✅ Injected ${sentCount} event(s).` });
    } catch (e) {
      setStatus({ ok: false, text: `❌ ${e.message}` });
    } finally {
      setBusy(false);
    }
  };

  return (
    <div style={{ marginTop: '0.75rem', borderTop: '1px solid rgba(255,255,255,0.45)', paddingTop: '0.75rem' }}>
      <button onClick={handleOpen} style={{
        width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        background: 'rgba(124,58,237,0.10)', border: '1px solid rgba(124,58,237,0.25)',
        borderRadius: 8, padding: '0.45rem 0.75rem', cursor: 'pointer',
        fontSize: '0.78rem', fontWeight: 700, color: '#5b21b6', gap: 6,
      }}>
        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <FlaskConical size={14} /> Inject Events
        </span>
        {open ? <ChevronUp size={13} /> : <ChevronDown size={13} />}
      </button>

      {open && (
        <div style={{
          marginTop: '0.5rem', padding: '0.75rem',
          background: 'rgba(237,233,254,0.5)', border: '1px solid rgba(124,58,237,0.2)',
          borderRadius: 8, display: 'flex', flexDirection: 'column', gap: '0.5rem',
        }}>
          <div style={{ fontSize: '0.7rem', color: '#5b21b6', fontWeight: 600 }}>
            Inject Predefined Sample
          </div>

          {/* Project selector */}
          <select value={selHash} onChange={e => setSelHash(e.target.value)} style={{
            fontSize: '0.72rem', padding: '0.35rem 0.4rem', borderRadius: 6,
            border: '1px solid rgba(124,58,237,0.3)', background: 'rgba(255,255,255,0.8)',
            width: '100%',
          }}>
            {projects.length === 0
              ? <option value="">Loading projects…</option>
              : projects.map(p => <option key={p.projectHash} value={p.projectHash}>{p.projectName}</option>)
            }
          </select>

          {/* Sample selector */}
          <select value={sampleType} onChange={e => setSampleType(e.target.value)} style={{
            fontSize: '0.72rem', padding: '0.35rem 0.4rem', borderRadius: 6,
            border: '1px solid rgba(124,58,237,0.3)', background: 'rgba(255,255,255,0.8)',
            width: '100%',
          }}>
            <option value="critical">1 Single CRITICAL Event</option>
            <option value="compound">5 LOW Events (Compound Critical)</option>
            <option value="insider">5 MIXED Events (Insider Threat)</option>
          </select>

          <button onClick={handleInject} disabled={busy || !selHash} style={{
            display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
            padding: '0.4rem', borderRadius: 6, border: 'none',
            cursor: busy ? 'not-allowed' : 'pointer',
            background: busy ? 'rgba(124,58,237,0.3)' : 'rgba(124,58,237,0.8)',
            color: '#fff', fontWeight: 700, fontSize: '0.78rem',
          }}>
            <Send size={12} /> {busy ? 'Sending…' : 'Inject to Kafka'}
          </button>

          {status && (
            <div style={{
              fontSize: '0.7rem', padding: '0.3rem 0.5rem', borderRadius: 5,
              background: status.ok ? 'rgba(16,185,129,0.12)' : 'rgba(239,68,68,0.1)',
              color: status.ok ? '#065f46' : '#991b1b', fontWeight: 600,
            }}>{status.text}</div>
          )}
        </div>
      )}
    </div>
  );
}

/* ── Main ── */
export default function ProjectManagerDashboard() {
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
      case 'projects': return <ProjectsList />;
      case 'create':   return <CreateProject />;
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
            <div style={{ fontSize: '0.68rem', fontWeight: 600, color: 'var(--cyan)', letterSpacing: '0.4px', marginTop: -1 }}>PROJECT MANAGER</div>
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

        {/* ── File Event Injector ── */}
        <FileEventInjector />

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
