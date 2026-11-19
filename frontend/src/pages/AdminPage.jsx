import React, { useState, useEffect, useCallback } from 'react';
import { adminApi } from '../api';

export default function AdminPage() {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [orgs, setOrgs] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [tab, setTab] = useState('overview');

  const loadData = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [statsResp, usersResp, orgsResp] = await Promise.all([
        adminApi.stats(),
        adminApi.users({ page: 0, size: 25 }),
        adminApi.orgs({ page: 0, size: 25 }),
      ]);
      setStats(statsResp.data);
      setUsers(usersResp.data?.content || usersResp.data || []);
      setOrgs(orgsResp.data?.content || orgsResp.data || []);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  const loadAuditLogs = useCallback(async () => {
    try {
      const resp = await adminApi.auditLogs({ page: 0, size: 30 });
      setAuditLogs(resp.data?.content || resp.data || []);
    } catch (e) {
      setError(e.message);
    }
  }, []);

  useEffect(() => { loadData(); }, [loadData]);
  useEffect(() => { if (tab === 'audit') loadAuditLogs(); }, [tab, loadAuditLogs]);

  const handleSuspend = async (userId) => {
    if (!confirm('Suspend this user?')) return;
    try {
      await adminApi.suspendUser(userId);
      setUsers(u => u.map(usr => usr.id === userId ? { ...usr, status: 'SUSPENDED' } : usr));
    } catch (e) { alert(e.message); }
  };

  const handleActivate = async (userId) => {
    try {
      await adminApi.activateUser(userId);
      setUsers(u => u.map(usr => usr.id === userId ? { ...usr, status: 'ACTIVE' } : usr));
    } catch (e) { alert(e.message); }
  };

  return (
    <div className="page-container">
      <div style={{ marginBottom: 24 }}>
        <h2 style={{ fontSize: '1.4rem', fontWeight: 800, marginBottom: 4 }}>🛡️ Platform Administration</h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>System-wide oversight. Accessible to PLATFORM_ADMIN only.</p>
      </div>

      {error && (
        <div style={{ background: 'rgba(239,68,68,0.12)', border: '1px solid rgba(239,68,68,0.4)', borderRadius: 10, padding: '12px 18px', marginBottom: 20, color: '#f87171' }}>
          ⚠️ {error}
        </div>
      )}

      {/* Tab bar */}
      <div style={{ display: 'flex', gap: 4, marginBottom: 24, background: 'rgba(255,255,255,0.03)', borderRadius: 10, padding: 4, width: 'fit-content', border: '1px solid var(--border-subtle)' }}>
        {[
          { id: 'overview', label: '📊 Overview' },
          { id: 'users', label: '👥 Users' },
          { id: 'orgs', label: '🏢 Organizations' },
          { id: 'audit', label: '📋 Audit Logs' },
        ].map(t => (
          <button key={t.id} onClick={() => setTab(t.id)} style={{
            padding: '7px 16px', borderRadius: 8, border: 'none', cursor: 'pointer',
            background: tab === t.id ? 'var(--primary)' : 'transparent',
            color: tab === t.id ? '#fff' : 'var(--text-secondary)',
            fontWeight: 600, fontSize: '0.875rem', transition: 'all 0.15s'
          }}>{t.label}</button>
        ))}
      </div>

      {tab === 'overview' && (
        <div>
          <div className="grid-cols-4" style={{ marginBottom: 24 }}>
            {[
              { label: 'Total Users', value: stats?.totalUsers, icon: '👥', color: 'primary' },
              { label: 'Organizations', value: stats?.totalOrganizations, icon: '🏢', color: 'success' },
              { label: 'Projects', value: stats?.totalProjects, icon: '📁', color: 'warning' },
              { label: 'Activity Events', value: stats?.totalActivities, icon: '📊', color: 'danger' },
            ].map((s, i) => (
              <div key={i} className="glass-card glass-card-interactive">
                <div className="stat-card">
                  <div className={`stat-icon ${s.color}`} style={{ fontSize: '1.3rem' }}>{s.icon}</div>
                  <div>
                    <div className="stat-value">{loading ? '…' : s.value ?? '—'}</div>
                    <div className="stat-label">{s.label}</div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {stats && (
            <div className="grid-cols-2">
              <div className="glass-card">
                <h3 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: 14 }}>System Health</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {[
                    { label: 'Database', ok: true },
                    { label: 'JWT Authentication', ok: true },
                    { label: 'Flyway Migrations', ok: true },
                    { label: 'Hyperledger Fabric', ok: false, note: 'Requires external network' },
                  ].map(item => (
                    <div key={item.label} className="flex-between">
                      <span style={{ fontSize: '0.875rem' }}>{item.label}</span>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        {item.note && <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>{item.note}</span>}
                        <span style={{ color: item.ok ? 'var(--success)' : 'var(--warning)', fontWeight: 700, fontSize: '0.82rem' }}>
                          {item.ok ? '✅ Healthy' : '⚠️ Offline'}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
              <div className="glass-card">
                <h3 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: 14 }}>Platform Info</h3>
                <div style={{ fontSize: '0.875rem', display: 'flex', flexDirection: 'column', gap: 8 }}>
                  {[
                    ['Version', '1.0.0'],
                    ['Java', '21 LTS'],
                    ['Spring Boot', '3.3.4'],
                    ['Database', 'H2 (dev) / MySQL 8 (prod)'],
                    ['Blockchain', 'Hyperledger Fabric (optional)'],
                  ].map(([k, v]) => (
                    <div key={k} className="flex-between">
                      <span style={{ color: 'var(--text-secondary)' }}>{k}</span>
                      <span className="code-pill">{v}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {tab === 'users' && (
        <div className="glass-card">
          <div className="flex-between" style={{ marginBottom: 16 }}>
            <h3 style={{ fontSize: '1rem', fontWeight: 700 }}>All Users ({users.length})</h3>
            <button className="btn btn-secondary btn-sm" onClick={loadData}>↻ Refresh</button>
          </div>
          {loading ? <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>Loading…</div> : (
            <div className="table-responsive">
              <table className="pulse-table">
                <thead>
                  <tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Joined</th><th>Actions</th></tr>
                </thead>
                <tbody>
                  {users.map(u => (
                    <tr key={u.id}>
                      <td style={{ fontWeight: 600 }}>{u.fullName}</td>
                      <td style={{ fontSize: '0.83rem' }}>{u.email}</td>
                      <td><span className={`badge ${u.systemRole === 'PLATFORM_ADMIN' ? 'badge-mismatched' : 'badge-pending'}`}>{u.systemRole}</span></td>
                      <td><span className={`badge ${u.status === 'ACTIVE' ? 'badge-verified' : 'badge-unavailable'}`}>{u.status}</span></td>
                      <td style={{ fontSize: '0.8rem' }}>{u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}</td>
                      <td>
                        {u.status === 'ACTIVE' ? (
                          <button className="btn btn-danger btn-sm" style={{ fontSize: '0.72rem' }} onClick={() => handleSuspend(u.id)}>Suspend</button>
                        ) : (
                          <button className="btn btn-secondary btn-sm" style={{ fontSize: '0.72rem' }} onClick={() => handleActivate(u.id)}>Activate</button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {tab === 'orgs' && (
        <div className="glass-card">
          <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: 16 }}>All Organizations ({orgs.length})</h3>
          {loading ? <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>Loading…</div> : (
            <div className="table-responsive">
              <table className="pulse-table">
                <thead>
                  <tr><th>Name</th><th>Slug</th><th>Status</th><th>Created</th></tr>
                </thead>
                <tbody>
                  {orgs.map(o => (
                    <tr key={o.id}>
                      <td style={{ fontWeight: 600 }}>{o.name}</td>
                      <td><span className="code-pill">{o.slug}</span></td>
                      <td><span className={`badge ${o.status === 'ACTIVE' ? 'badge-verified' : 'badge-unavailable'}`}>{o.status}</span></td>
                      <td style={{ fontSize: '0.8rem' }}>{o.createdAt ? new Date(o.createdAt).toLocaleDateString() : '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {tab === 'audit' && (
        <div className="glass-card">
          <div className="flex-between" style={{ marginBottom: 16 }}>
            <h3 style={{ fontSize: '1rem', fontWeight: 700 }}>Audit Logs</h3>
            <button className="btn btn-secondary btn-sm" onClick={loadAuditLogs}>↻ Refresh</button>
          </div>
          {auditLogs.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-muted)' }}>
              <div style={{ fontSize: 36, marginBottom: 8 }}>📋</div>
              <p>No audit logs yet.</p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="pulse-table">
                <thead>
                  <tr><th>Action</th><th>Entity</th><th>User</th><th>Organization</th><th>Timestamp</th></tr>
                </thead>
                <tbody>
                  {auditLogs.map(log => (
                    <tr key={log.id}>
                      <td><span className="code-pill">{log.action}</span></td>
                      <td style={{ fontSize: '0.82rem' }}>{log.entityType} #{log.entityId}</td>
                      <td style={{ fontSize: '0.82rem' }}>{log.userId || '—'}</td>
                      <td style={{ fontSize: '0.82rem' }}>{log.organizationId || '—'}</td>
                      <td style={{ fontSize: '0.8rem', whiteSpace: 'nowrap' }}>{log.createdAt ? new Date(log.createdAt).toLocaleString() : '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
