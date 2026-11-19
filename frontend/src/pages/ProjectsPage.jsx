import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { projectApi } from '../api';

function CreateProjectModal({ orgId, onClose, onCreated }) {
  const [form, setForm] = useState({ name: '', keyIdentifier: '', description: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const autoKey = (name) => name.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 10);

  const handleNameChange = (e) => {
    const name = e.target.value;
    setForm(p => ({ ...p, name, keyIdentifier: p.keyIdentifier || autoKey(name) }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const resp = await projectApi.create(orgId, form);
      onCreated(resp.data);
      onClose();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Create New Project</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        {error && (
          <div style={{ background: 'rgba(239,68,68,0.12)', border: '1px solid rgba(239,68,68,0.4)', borderRadius: 8, padding: '10px 14px', marginBottom: 16, fontSize: '0.85rem', color: '#f87171' }}>
            {error}
          </div>
        )}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="proj-name">Project Name</label>
            <input id="proj-name" type="text" className="form-input" placeholder="My Awesome Project"
              value={form.name} onChange={handleNameChange} required />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="proj-key">Project Key</label>
            <input id="proj-key" type="text" className="form-input" placeholder="MYPROJ"
              value={form.keyIdentifier}
              onChange={e => setForm(p => ({ ...p, keyIdentifier: e.target.value.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 10) }))}
              required maxLength={10} />
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: 4 }}>Unique identifier, alphanumeric, max 10 chars</div>
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="proj-desc">Description <span style={{ color: 'var(--text-muted)', fontWeight: 400 }}>(optional)</span></label>
            <textarea id="proj-desc" className="form-textarea" placeholder="What is this project about?"
              value={form.description} onChange={e => setForm(p => ({ ...p, description: e.target.value }))}
              rows={3} style={{ resize: 'vertical' }} />
          </div>
          <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 8 }}>
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating…' : 'Create Project'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function ProjectCard({ project, orgId, onArchive }) {
  const navigate = useNavigate();
  const [archiving, setArchiving] = useState(false);

  const handleArchive = async (e) => {
    e.stopPropagation();
    if (!confirm(`Archive project "${project.name}"?`)) return;
    setArchiving(true);
    try {
      await projectApi.archive(orgId, project.id);
      onArchive(project.id);
    } catch (err) {
      alert('Archive failed: ' + err.message);
    } finally {
      setArchiving(false);
    }
  };

  return (
    <div
      className="glass-card glass-card-interactive"
      style={{ cursor: 'pointer' }}
      onClick={() => navigate(`/projects/${project.id}`)}
    >
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: 14 }}>
        <div style={{
          width: 44, height: 44, borderRadius: 10,
          background: 'linear-gradient(135deg, rgba(99,102,241,0.25), rgba(168,85,247,0.25))',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontWeight: 800, fontSize: '0.85rem', color: 'var(--primary-light)',
          border: '1px solid rgba(99,102,241,0.3)'
        }}>
          {project.keyIdentifier?.slice(0, 3)}
        </div>
        <span className={`badge ${project.status === 'ACTIVE' ? 'badge-verified' : 'badge-unavailable'}`}>
          {project.status}
        </span>
      </div>
      <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: 4 }}>{project.name}</h3>
      <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginBottom: 14, lineHeight: 1.5 }}>
        {project.description || 'No description provided.'}
      </p>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span className="code-pill">{project.keyIdentifier}</span>
        {project.status === 'ACTIVE' && (
          <button
            className="btn btn-danger btn-sm"
            onClick={handleArchive}
            disabled={archiving}
            style={{ fontSize: '0.75rem' }}
          >
            {archiving ? '…' : 'Archive'}
          </button>
        )}
      </div>
    </div>
  );
}

export default function ProjectsPage() {
  const { activeOrg } = useAuth();
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [filter, setFilter] = useState('ACTIVE');

  const loadProjects = useCallback(async () => {
    if (!activeOrg?.id) { setLoading(false); return; }
    setLoading(true);
    setError('');
    try {
      const resp = await projectApi.list(activeOrg.id);
      const data = resp.data?.content || resp.data || [];
      setProjects(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [activeOrg?.id]);

  useEffect(() => { loadProjects(); }, [loadProjects]);

  const handleCreated = (newProject) => setProjects(p => [newProject, ...p]);
  const handleArchive = (id) => setProjects(p => p.map(pr => pr.id === id ? { ...pr, status: 'ARCHIVED' } : pr));

  const filtered = projects.filter(p => filter === 'ALL' || p.status === filter);

  if (!activeOrg) {
    return (
      <div className="page-container">
        <div className="glass-card" style={{ textAlign: 'center', padding: 48 }}>
          <p style={{ color: 'var(--text-secondary)' }}>Select an organization to view projects.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="flex-between" style={{ marginBottom: 24 }}>
        <div>
          <h2 style={{ fontSize: '1.4rem', fontWeight: 800 }}>Projects</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', marginTop: 2 }}>
            {activeOrg.name} — {projects.length} total projects
          </p>
        </div>
        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          <select className="form-select" style={{ width: 140, fontSize: '0.85rem', padding: '7px 10px' }} value={filter} onChange={e => setFilter(e.target.value)}>
            <option value="ACTIVE">Active</option>
            <option value="ARCHIVED">Archived</option>
            <option value="ALL">All</option>
          </select>
          <button id="create-project-btn" className="btn btn-primary" onClick={() => setShowCreate(true)}>
            + New Project
          </button>
        </div>
      </div>

      {error && (
        <div style={{ background: 'rgba(239,68,68,0.12)', border: '1px solid rgba(239,68,68,0.4)', borderRadius: 10, padding: '12px 18px', marginBottom: 20, color: '#f87171' }}>
          ⚠️ {error}
        </div>
      )}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-muted)' }}>
          <div style={{ fontSize: 40, marginBottom: 12 }}>⏳</div>
          <p>Loading projects…</p>
        </div>
      ) : filtered.length === 0 ? (
        <div className="glass-card" style={{ textAlign: 'center', padding: '60px 0' }}>
          <div style={{ fontSize: 48, marginBottom: 14 }}>📂</div>
          <h3 style={{ marginBottom: 8 }}>No {filter !== 'ALL' ? filter.toLowerCase() : ''} projects</h3>
          <p style={{ color: 'var(--text-secondary)', marginBottom: 20 }}>
            {filter === 'ACTIVE' ? 'Create your first project to get started.' : 'No projects match this filter.'}
          </p>
          {filter === 'ACTIVE' && (
            <button className="btn btn-primary" onClick={() => setShowCreate(true)}>+ Create First Project</button>
          )}
        </div>
      ) : (
        <div className="grid-cols-2">
          {filtered.map(p => (
            <ProjectCard key={p.id} project={p} orgId={activeOrg.id} onArchive={handleArchive} />
          ))}
        </div>
      )}

      {showCreate && (
        <CreateProjectModal
          orgId={activeOrg.id}
          onClose={() => setShowCreate(false)}
          onCreated={handleCreated}
        />
      )}
    </div>
  );
}
