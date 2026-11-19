import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { projectApi, activityApi, orgApi } from '../api';

function StatCard({ icon, label, value, color = 'primary', trend }) {
  return (
    <div className="glass-card glass-card-interactive" style={{ cursor: 'default' }}>
      <div className="stat-card">
        <div className={`stat-icon ${color}`}>{icon}</div>
        <div>
          <div className="stat-value">{value ?? '—'}</div>
          <div className="stat-label">{label}</div>
          {trend && <div style={{ fontSize: '0.75rem', color: 'var(--success)', marginTop: 2 }}>{trend}</div>}
        </div>
      </div>
    </div>
  );
}

function ActivityItem({ event }) {
  const eventType = event.eventType?.toLowerCase();
  const badgeClass = eventType === 'create' ? 'badge-create'
    : eventType === 'modify' ? 'badge-modify'
    : eventType === 'delete' ? 'badge-delete'
    : 'badge-rename';

  const filename = event.filePath?.split(/[\\/]/).pop() || event.filePath;
  const ts = event.eventTimestamp
    ? new Date(event.eventTimestamp).toLocaleString()
    : '—';

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 14, padding: '12px 0', borderBottom: '1px solid var(--border-subtle)' }}>
      <span className={`badge ${badgeClass}`}>{event.eventType}</span>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: '0.88rem', color: 'var(--text-main)', fontWeight: 500, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={event.filePath}>
          {filename}
        </div>
        <div style={{ fontSize: '0.76rem', color: 'var(--text-muted)', marginTop: 1 }}>{ts}</div>
      </div>
      <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', flexShrink: 0 }}>
        {event.userName || 'Agent'}
      </div>
    </div>
  );
}

export default function DashboardPage() {
  const { activeOrg } = useAuth();
  const [projects, setProjects] = useState([]);
  const [recentActivity, setRecentActivity] = useState([]);
  const [memberCount, setMemberCount] = useState(0);
  const [totalEventCount, setTotalEventCount] = useState(0);
  const [verifiedCount, setVerifiedCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = useCallback(async () => {
    if (!activeOrg?.id) { setLoading(false); return; }
    setLoading(true);
    setError('');
    try {
      const [projResp, memResp] = await Promise.all([
        projectApi.list(activeOrg.id),
        orgApi.members(activeOrg.id).catch(() => ({ data: [] })),
      ]);
      const projs = projResp.data?.content || projResp.data || [];
      setProjects(projs);

      const mems = memResp.data?.content || memResp.data || [];
      setMemberCount(mems.length);

      // Load recent activity from first few projects
      const activityPromises = projs.slice(0, 5).map(p =>
        activityApi.list(activeOrg.id, p.id, { page: 0, size: 20 })
          .then(r => r.data?.content || r.data || [])
          .catch(() => [])
      );
      const allActivity = (await Promise.all(activityPromises)).flat();
      allActivity.sort((a, b) => new Date(b.eventTimestamp) - new Date(a.eventTimestamp));
      setRecentActivity(allActivity.slice(0, 10));
      setTotalEventCount(allActivity.length);
      const verified = allActivity.filter(a => !!a.payloadHash).length;
      setVerifiedCount(verified);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [activeOrg?.id]);

  useEffect(() => { loadData(); }, [loadData]);

  const activeProjects = projects.filter(p => p.status === 'ACTIVE');
  const archivedProjects = projects.filter(p => p.status !== 'ACTIVE');

  if (!activeOrg) {
    return (
      <div className="page-container">
        <div className="glass-card" style={{ textAlign: 'center', padding: 48 }}>
          <div style={{ fontSize: 48, marginBottom: 16 }}>🏢</div>
          <h2 style={{ marginBottom: 8 }}>No Organization Selected</h2>
          <p style={{ color: 'var(--text-secondary)' }}>
            You're not a member of any organization yet. Create one after registration.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      {/* Stats */}
      <div className="grid-cols-4" style={{ marginBottom: 28 }}>
        <StatCard icon="📁" label="Active Projects" value={loading ? '…' : activeProjects.length} color="primary" />
        <StatCard icon="📊" label="Tracked Events" value={loading ? '…' : totalEventCount} color="success" />
        <StatCard icon="👥" label="Team Members" value={loading ? '…' : memberCount} color="warning" />
        <StatCard icon="🔗" label="Verified Hashes" value={loading ? '…' : verifiedCount} color="danger" />
      </div>

      {error && (
        <div style={{ background: 'rgba(239,68,68,0.12)', border: '1px solid rgba(239,68,68,0.4)', borderRadius: 10, padding: '12px 18px', marginBottom: 24, color: '#f87171' }}>
          ⚠️ {error}
        </div>
      )}

      <div className="grid-cols-2">
        {/* Projects Overview */}
        <div className="glass-card">
          <div className="flex-between" style={{ marginBottom: 18 }}>
            <h3 style={{ fontSize: '1rem', fontWeight: 700 }}>Active Projects</h3>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{activeProjects.length} projects</span>
          </div>
          {loading ? (
            <div style={{ color: 'var(--text-muted)', padding: '20px 0', textAlign: 'center' }}>Loading…</div>
          ) : activeProjects.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '28px 0', color: 'var(--text-muted)' }}>
              <div style={{ fontSize: 32, marginBottom: 8 }}>📂</div>
              <p>No active projects yet.</p>
              <p style={{ fontSize: '0.82rem', marginTop: 4 }}>Go to Projects to create your first one.</p>
            </div>
          ) : (
            <div>
              {activeProjects.slice(0, 6).map(p => (
                <div key={p.id} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 0', borderBottom: '1px solid var(--border-subtle)' }}>
                  <div style={{
                    width: 36, height: 36, borderRadius: 8,
                    background: 'linear-gradient(135deg, rgba(99,102,241,0.3), rgba(168,85,247,0.3))',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: '0.8rem', fontWeight: 700, color: 'var(--primary-light)',
                    flexShrink: 0
                  }}>
                    {p.keyIdentifier?.slice(0,3) || 'PRJ'}
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontWeight: 600, fontSize: '0.88rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{p.name}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{p.keyIdentifier}</div>
                  </div>
                  <span className="badge badge-verified" style={{ fontSize: '0.68rem' }}>ACTIVE</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Recent Activity */}
        <div className="glass-card">
          <div className="flex-between" style={{ marginBottom: 18 }}>
            <h3 style={{ fontSize: '1rem', fontWeight: 700 }}>Recent Activity</h3>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Latest events</span>
          </div>
          {loading ? (
            <div style={{ color: 'var(--text-muted)', padding: '20px 0', textAlign: 'center' }}>Loading…</div>
          ) : recentActivity.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '28px 0', color: 'var(--text-muted)' }}>
              <div style={{ fontSize: 32, marginBottom: 8 }}>📭</div>
              <p>No activity recorded yet.</p>
              <p style={{ fontSize: '0.82rem', marginTop: 4 }}>Start the Desktop Agent to begin tracking.</p>
            </div>
          ) : (
            <div>
              {recentActivity.map(ev => (
                <ActivityItem key={ev.id} event={ev} />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
