import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { projectApi, collaborationApi } from '../api';

function Tab({ id, active, label, onClick }) {
  return (
    <button
      onClick={() => onClick(id)}
      style={{
        padding: '8px 20px', borderRadius: 8, border: 'none', cursor: 'pointer',
        background: active ? 'var(--primary)' : 'transparent',
        color: active ? '#fff' : 'var(--text-secondary)',
        fontWeight: 600, fontSize: '0.875rem', transition: 'all 0.15s'
      }}
    >{label}</button>
  );
}

function ChatPanel({ orgId, projectId }) {
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const bottomRef = useRef(null);

  const loadMessages = useCallback(async () => {
    try {
      const resp = await collaborationApi.messages(orgId, projectId, { page: 0, size: 50 });
      const msgs = resp.data?.content || resp.data || [];
      setMessages([...msgs].reverse());
    } catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }, [orgId, projectId]);

  useEffect(() => { loadMessages(); }, [loadMessages]);
  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!input.trim()) return;
    setSending(true);
    try {
      const resp = await collaborationApi.sendMessage(orgId, projectId, { content: input.trim() });
      setMessages(m => [...m, resp.data]);
      setInput('');
    } catch (e) { setError(e.message); }
    finally { setSending(false); }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: 520 }}>
      {error && <div style={{ color: '#f87171', padding: '8px 0', fontSize: '0.82rem' }}>{error}</div>}
      <div style={{ flex: 1, overflowY: 'auto', padding: '8px 0', marginBottom: 16 }}>
        {loading ? (
          <div style={{ textAlign: 'center', color: 'var(--text-muted)', marginTop: 40 }}>Loading messages…</div>
        ) : messages.length === 0 ? (
          <div style={{ textAlign: 'center', color: 'var(--text-muted)', marginTop: 60 }}>
            <div style={{ fontSize: 36, marginBottom: 8 }}>💬</div>
            <p>No messages yet. Start the conversation!</p>
          </div>
        ) : (
          messages.map(msg => {
            const isMe = msg.userId === user?.id;
            return (
              <div key={msg.id} style={{ display: 'flex', flexDirection: isMe ? 'row-reverse' : 'row', gap: 10, marginBottom: 14 }}>
                <div className="user-avatar" style={{ width: 32, height: 32, fontSize: '0.78rem', flexShrink: 0 }}>
                  {msg.userFullName?.charAt(0)?.toUpperCase() || 'U'}
                </div>
                <div style={{ maxWidth: '70%' }}>
                  <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginBottom: 3, textAlign: isMe ? 'right' : 'left' }}>
                    {msg.userFullName} · {msg.createdAt ? new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                  </div>
                  <div style={{
                    padding: '10px 14px', borderRadius: isMe ? '14px 14px 4px 14px' : '14px 14px 14px 4px',
                    background: isMe ? 'linear-gradient(135deg, var(--primary), #4f46e5)' : 'rgba(255,255,255,0.06)',
                    border: isMe ? 'none' : '1px solid var(--border-subtle)',
                    fontSize: '0.875rem', lineHeight: 1.5,
                  }}>
                    {msg.content}
                  </div>
                </div>
              </div>
            );
          })
        )}
        <div ref={bottomRef} />
      </div>
      <form onSubmit={handleSend} style={{ display: 'flex', gap: 10 }}>
        <input
          type="text" className="form-input" placeholder="Type a message…"
          value={input} onChange={e => setInput(e.target.value)}
          disabled={sending} style={{ flex: 1 }}
        />
        <button type="submit" className="btn btn-primary" disabled={sending || !input.trim()}>
          {sending ? '…' : 'Send'}
        </button>
      </form>
    </div>
  );
}

function NotesPanel({ orgId, projectId }) {
  const [notes, setNotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ title: '', content: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    collaborationApi.notes(orgId, projectId)
      .then(r => setNotes(r.data?.content || r.data || []))
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, [orgId, projectId]);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const resp = await collaborationApi.createNote(orgId, projectId, form);
      setNotes(n => [resp.data, ...n]);
      setForm({ title: '', content: '' });
      setShowForm(false);
    } catch (e) { setError(e.message); }
    finally { setSaving(false); }
  };

  return (
    <div>
      {error && <div style={{ color: '#f87171', marginBottom: 12, fontSize: '0.82rem' }}>{error}</div>}
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 16 }}>
        <button className="btn btn-primary btn-sm" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : '+ New Note'}
        </button>
      </div>
      {showForm && (
        <form onSubmit={handleSave} style={{ marginBottom: 20, background: 'rgba(255,255,255,0.03)', borderRadius: 10, padding: 16, border: '1px solid var(--border-subtle)' }}>
          <div className="form-group">
            <label className="form-label">Title</label>
            <input type="text" className="form-input" value={form.title} onChange={e => setForm(p => ({ ...p, title: e.target.value }))} required />
          </div>
          <div className="form-group">
            <label className="form-label">Content</label>
            <textarea className="form-textarea" rows={4} value={form.content} onChange={e => setForm(p => ({ ...p, content: e.target.value }))} required style={{ resize: 'vertical' }} />
          </div>
          <button type="submit" className="btn btn-primary btn-sm" disabled={saving}>{saving ? 'Saving…' : 'Save Note'}</button>
        </form>
      )}
      {loading ? (
        <div style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 30 }}>Loading notes…</div>
      ) : notes.length === 0 ? (
        <div style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 40 }}>
          <div style={{ fontSize: 36, marginBottom: 8 }}>📝</div>
          <p>No notes yet. Create the first one!</p>
        </div>
      ) : (
        notes.map(n => (
          <div key={n.id} className="glass-card" style={{ marginBottom: 12 }}>
            <div className="flex-between" style={{ marginBottom: 8 }}>
              <h4 style={{ fontSize: '0.95rem', fontWeight: 700 }}>{n.title}</h4>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                {n.authorName} · {n.createdAt ? new Date(n.createdAt).toLocaleDateString() : ''}
              </span>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.6, whiteSpace: 'pre-wrap' }}>{n.content}</p>
          </div>
        ))
      )}
    </div>
  );
}

function StatusPanel({ orgId, projectId }) {
  const [updates, setUpdates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ statusLabel: 'ON_TRACK', summary: '', details: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    collaborationApi.statusUpdates(orgId, projectId)
      .then(r => setUpdates(r.data?.content || r.data || []))
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, [orgId, projectId]);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const resp = await collaborationApi.createStatusUpdate(orgId, projectId, form);
      setUpdates(u => [resp.data, ...u]);
      setForm({ statusLabel: 'ON_TRACK', summary: '', details: '' });
      setShowForm(false);
    } catch (e) { setError(e.message); }
    finally { setSaving(false); }
  };

  const labelColor = (label) => {
    const map = { ON_TRACK: 'badge-verified', AT_RISK: 'badge-pending', OFF_TRACK: 'badge-mismatched', COMPLETED: 'badge-unavailable' };
    return map[label] || 'badge-unavailable';
  };

  return (
    <div>
      {error && <div style={{ color: '#f87171', marginBottom: 12, fontSize: '0.82rem' }}>{error}</div>}
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 16 }}>
        <button className="btn btn-primary btn-sm" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : '+ Post Update'}
        </button>
      </div>
      {showForm && (
        <form onSubmit={handleSave} style={{ marginBottom: 20, background: 'rgba(255,255,255,0.03)', borderRadius: 10, padding: 16, border: '1px solid var(--border-subtle)' }}>
          <div className="form-group">
            <label className="form-label">Status</label>
            <select className="form-select" value={form.statusLabel} onChange={e => setForm(p => ({ ...p, statusLabel: e.target.value }))}>
              <option value="ON_TRACK">✅ On Track</option>
              <option value="AT_RISK">⚠️ At Risk</option>
              <option value="OFF_TRACK">❌ Off Track</option>
              <option value="COMPLETED">🎉 Completed</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Summary</label>
            <input type="text" className="form-input" value={form.summary} onChange={e => setForm(p => ({ ...p, summary: e.target.value }))} required placeholder="Brief summary of current status" />
          </div>
          <div className="form-group">
            <label className="form-label">Details (optional)</label>
            <textarea className="form-textarea" rows={3} value={form.details} onChange={e => setForm(p => ({ ...p, details: e.target.value }))} style={{ resize: 'vertical' }} />
          </div>
          <button type="submit" className="btn btn-primary btn-sm" disabled={saving}>{saving ? 'Posting…' : 'Post Update'}</button>
        </form>
      )}
      {loading ? (
        <div style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 30 }}>Loading updates…</div>
      ) : updates.length === 0 ? (
        <div style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 40 }}>
          <div style={{ fontSize: 36, marginBottom: 8 }}>📡</div>
          <p>No status updates yet.</p>
        </div>
      ) : (
        updates.map(u => (
          <div key={u.id} style={{ display: 'flex', gap: 14, padding: '14px 0', borderBottom: '1px solid var(--border-subtle)' }}>
            <span className={`badge ${labelColor(u.statusLabel)}`}>{u.statusLabel?.replace('_', ' ')}</span>
            <div>
              <div style={{ fontWeight: 600, fontSize: '0.88rem' }}>{u.summary}</div>
              {u.details && <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginTop: 4 }}>{u.details}</div>}
              <div style={{ fontSize: '0.73rem', color: 'var(--text-muted)', marginTop: 4 }}>
                {u.authorName} · {u.createdAt ? new Date(u.createdAt).toLocaleString() : ''}
              </div>
            </div>
          </div>
        ))
      )}
    </div>
  );
}

export default function CollaborationPage() {
  const { activeOrg } = useAuth();
  const [projects, setProjects] = useState([]);
  const [selectedProject, setSelectedProject] = useState(null);
  const [loadingProjects, setLoadingProjects] = useState(true);
  const [activeTab, setActiveTab] = useState('chat');

  useEffect(() => {
    if (!activeOrg?.id) { setLoadingProjects(false); return; }
    projectApi.list(activeOrg.id)
      .then(r => {
        const projs = r.data?.content || r.data || [];
        const active = projs.filter(p => p.status === 'ACTIVE');
        setProjects(active);
        if (active.length > 0) setSelectedProject(active[0]);
      })
      .catch(console.error)
      .finally(() => setLoadingProjects(false));
  }, [activeOrg?.id]);

  if (!activeOrg) {
    return <div className="page-container"><div className="glass-card" style={{ textAlign: 'center', padding: 48 }}><p style={{ color: 'var(--text-secondary)' }}>Select an organization first.</p></div></div>;
  }

  return (
    <div className="page-container">
      <h2 style={{ fontSize: '1.4rem', fontWeight: 800, marginBottom: 20 }}>Team Collaboration</h2>
      <div style={{ display: 'grid', gridTemplateColumns: '260px 1fr', gap: 20, alignItems: 'start' }}>
        {/* Project list */}
        <div className="glass-card" style={{ padding: 16 }}>
          <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 12 }}>
            Projects
          </div>
          {loadingProjects ? (
            <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Loading…</div>
          ) : projects.length === 0 ? (
            <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No active projects.</div>
          ) : (
            projects.map(p => (
              <button
                key={p.id}
                onClick={() => setSelectedProject(p)}
                style={{
                  width: '100%', textAlign: 'left', background: selectedProject?.id === p.id ? 'rgba(99,102,241,0.18)' : 'transparent',
                  border: selectedProject?.id === p.id ? '1px solid rgba(99,102,241,0.4)' : '1px solid transparent',
                  borderRadius: 8, padding: '9px 12px', cursor: 'pointer', marginBottom: 4, transition: 'all 0.15s'
                }}
              >
                <div style={{ fontWeight: 600, fontSize: '0.875rem', color: selectedProject?.id === p.id ? 'var(--primary-light)' : 'var(--text-main)' }}>{p.name}</div>
                <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: 1 }}>{p.keyIdentifier}</div>
              </button>
            ))
          )}
        </div>

        {/* Content */}
        {selectedProject ? (
          <div className="glass-card">
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: 16 }}>{selectedProject.name}</h3>
            <div style={{ display: 'flex', gap: 4, marginBottom: 20, background: 'rgba(255,255,255,0.03)', borderRadius: 10, padding: 4, width: 'fit-content', border: '1px solid var(--border-subtle)' }}>
              <Tab id="chat" active={activeTab === 'chat'} label="💬 Chat" onClick={setActiveTab} />
              <Tab id="notes" active={activeTab === 'notes'} label="📝 Notes" onClick={setActiveTab} />
              <Tab id="status" active={activeTab === 'status'} label="📡 Status" onClick={setActiveTab} />
            </div>
            {activeTab === 'chat' && <ChatPanel orgId={activeOrg.id} projectId={selectedProject.id} />}
            {activeTab === 'notes' && <NotesPanel orgId={activeOrg.id} projectId={selectedProject.id} />}
            {activeTab === 'status' && <StatusPanel orgId={activeOrg.id} projectId={selectedProject.id} />}
          </div>
        ) : (
          <div className="glass-card" style={{ textAlign: 'center', padding: 48 }}>
            <div style={{ fontSize: 40, marginBottom: 12 }}>📁</div>
            <p style={{ color: 'var(--text-secondary)' }}>Select a project to start collaborating.</p>
          </div>
        )}
      </div>
    </div>
  );
}
