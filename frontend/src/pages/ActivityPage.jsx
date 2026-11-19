import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { projectApi, activityApi } from '../api';

export default function ActivityPage() {
  const { activeOrg } = useAuth();
  const [projects, setProjects] = useState([]);
  const [selectedProject, setSelectedProject] = useState('all');
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [filters, setFilters] = useState({ eventType: '', dateFrom: '', dateTo: '' });

  useEffect(() => {
    if (!activeOrg?.id) return;
    projectApi.list(activeOrg.id)
      .then(r => setProjects(r.data?.content || r.data || []))
      .catch(e => setError(e.message));
  }, [activeOrg?.id]);

  const loadActivities = useCallback(async () => {
    if (!activeOrg?.id) return;
    setLoading(true);
    setError('');

    const params = { page, size: 25 };
    if (filters.eventType) params.eventType = filters.eventType;
    if (filters.dateFrom) params.dateFrom = filters.dateFrom;
    if (filters.dateTo) params.dateTo = filters.dateTo;

    try {
      let acts = [], total = 0, pages = 0;

      if (selectedProject && selectedProject !== 'all') {
        const resp = await activityApi.list(activeOrg.id, selectedProject, params);
        acts = resp.data?.content || resp.data || [];
        total = resp.data?.totalElements || acts.length;
        pages = resp.data?.totalPages || 1;
      } else {
        // Aggregate across active projects
        const activeProjs = projects.filter(p => p.status === 'ACTIVE');
        const results = await Promise.all(
          activeProjs.slice(0, 5).map(p =>
            activityApi.list(activeOrg.id, p.id, { page: 0, size: 10, ...params })
              .then(r => r.data?.content || r.data || [])
              .catch(() => [])
          )
        );
        acts = results.flat().sort((a, b) => new Date(b.eventTimestamp) - new Date(a.eventTimestamp)).slice(0, 50);
        total = acts.length;
        pages = 1;
      }

      setActivities(acts);
      setTotalElements(total);
      setTotalPages(pages);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [activeOrg?.id, selectedProject, page, filters, projects]);

  useEffect(() => { loadActivities(); }, [loadActivities]);

  const handleFilterChange = (key, val) => {
    setFilters(f => ({ ...f, [key]: val }));
    setPage(0);
  };

  const eventBadge = (type) => {
    const map = { CREATE: 'badge-create', MODIFY: 'badge-modify', DELETE: 'badge-delete', RENAME: 'badge-rename' };
    return map[type] || 'badge-unavailable';
  };

  if (!activeOrg) {
    return <div className="page-container"><div className="glass-card" style={{ textAlign: 'center', padding: 48 }}><p style={{ color: 'var(--text-secondary)' }}>Select an organization first.</p></div></div>;
  }

  return (
    <div className="page-container">
      <div className="flex-between" style={{ marginBottom: 24 }}>
        <div>
          <h2 style={{ fontSize: '1.4rem', fontWeight: 800 }}>Activity Feed</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', marginTop: 2 }}>
            {totalElements > 0 ? `${totalElements} events recorded` : 'Filesystem tracking events across projects'}
          </p>
        </div>
        <button className="btn btn-secondary" onClick={loadActivities}>↻ Refresh</button>
      </div>

      {/* Filters */}
      <div className="glass-card" style={{ padding: 18, marginBottom: 20 }}>
        <div style={{ display: 'flex', gap: 14, flexWrap: 'wrap', alignItems: 'flex-end' }}>
          <div style={{ flex: '1 1 200px' }}>
            <label className="form-label">Project</label>
            <select className="form-select" style={{ fontSize: '0.85rem' }} value={selectedProject}
              onChange={e => { setSelectedProject(e.target.value); setPage(0); }}>
              <option value="all">All Projects</option>
              {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
          </div>
          <div style={{ flex: '1 1 160px' }}>
            <label className="form-label">Event Type</label>
            <select className="form-select" style={{ fontSize: '0.85rem' }} value={filters.eventType}
              onChange={e => handleFilterChange('eventType', e.target.value)}>
              <option value="">All Types</option>
              <option value="CREATE">Create</option>
              <option value="MODIFY">Modify</option>
              <option value="DELETE">Delete</option>
              <option value="RENAME">Rename</option>
            </select>
          </div>
          <div style={{ flex: '1 1 160px' }}>
            <label className="form-label">From Date</label>
            <input type="date" className="form-input" style={{ fontSize: '0.85rem' }} value={filters.dateFrom}
              onChange={e => handleFilterChange('dateFrom', e.target.value)} />
          </div>
          <div style={{ flex: '1 1 160px' }}>
            <label className="form-label">To Date</label>
            <input type="date" className="form-input" style={{ fontSize: '0.85rem' }} value={filters.dateTo}
              onChange={e => handleFilterChange('dateTo', e.target.value)} />
          </div>
          <button className="btn btn-secondary btn-sm" style={{ marginBottom: 1 }}
            onClick={() => { setFilters({ eventType: '', dateFrom: '', dateTo: '' }); setPage(0); }}>
            Clear
          </button>
        </div>
      </div>

      {error && (
        <div style={{ background: 'rgba(239,68,68,0.12)', border: '1px solid rgba(239,68,68,0.4)', borderRadius: 10, padding: '12px 18px', marginBottom: 16, color: '#f87171' }}>
          ⚠️ {error}
        </div>
      )}

      <div className="glass-card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-muted)' }}>
            <div style={{ fontSize: 40, marginBottom: 12 }}>⏳</div>
            <p>Loading activity feed…</p>
          </div>
        ) : activities.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-muted)' }}>
            <div style={{ fontSize: 48, marginBottom: 14 }}>📭</div>
            <h3>No Activity Found</h3>
            <p style={{ marginTop: 8, fontSize: '0.875rem' }}>
              Start the ProjectPulse Desktop Agent to begin tracking filesystem events.
            </p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="pulse-table">
              <thead>
                <tr>
                  <th>Type</th>
                  <th>File Path</th>
                  <th>Project</th>
                  <th>User / Device</th>
                  <th>Size</th>
                  <th>Timestamp</th>
                </tr>
              </thead>
              <tbody>
                {activities.map(ev => (
                  <tr key={ev.id}>
                    <td><span className={`badge ${eventBadge(ev.eventType)}`}>{ev.eventType}</span></td>
                    <td style={{ maxWidth: 320 }}>
                      <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontSize: '0.85rem', fontWeight: 500 }} title={ev.filePath}>
                        {ev.filePath?.split(/[\\/]/).pop()}
                      </div>
                      <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {ev.filePath}
                      </div>
                    </td>
                    <td style={{ fontSize: '0.82rem' }}>{ev.projectName || '—'}</td>
                    <td style={{ fontSize: '0.82rem' }}>{ev.userName || 'Agent'}</td>
                    <td style={{ fontSize: '0.82rem' }}>{ev.fileSizeBytes != null ? `${(ev.fileSizeBytes / 1024).toFixed(1)} KB` : '—'}</td>
                    <td style={{ fontSize: '0.8rem', whiteSpace: 'nowrap' }}>
                      {ev.eventTimestamp ? new Date(ev.eventTimestamp).toLocaleString() : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'center', gap: 8, marginTop: 18 }}>
            <button className="btn btn-secondary btn-sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Prev</button>
            <span style={{ padding: '5px 12px', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
              Page {page + 1} of {totalPages}
            </span>
            <button className="btn btn-secondary btn-sm" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Next →</button>
          </div>
        )}
      </div>
    </div>
  );
}
