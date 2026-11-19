import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { projectApi, activityApi, blockchainApi } from '../api';

function VerificationBadge({ status }) {
  const map = {
    VERIFIED: { cls: 'badge-verified', icon: '✅', label: 'Verified' },
    MISMATCHED: { cls: 'badge-mismatched', icon: '❌', label: 'Mismatch' },
    PENDING: { cls: 'badge-pending', icon: '⏳', label: 'Pending' },
    UNAVAILABLE: { cls: 'badge-unavailable', icon: '⛔', label: 'Unavailable' },
  };
  const info = map[status] || map.UNAVAILABLE;
  return <span className={`badge ${info.cls}`}>{info.icon} {info.label}</span>;
}

export default function BlockchainPage() {
  const { activeOrg } = useAuth();
  const [projects, setProjects] = useState([]);
  const [selectedProject, setSelectedProject] = useState(null);
  const [activities, setActivities] = useState([]);
  const [blockchainRecords, setBlockchainRecords] = useState({});
  const [loading, setLoading] = useState(false);
  const [verifying, setVerifying] = useState({});
  const [error, setError] = useState('');

  useEffect(() => {
    if (!activeOrg?.id) return;
    projectApi.list(activeOrg.id)
      .then(r => {
        const projs = r.data?.content || r.data || [];
        setProjects(projs.filter(p => p.status === 'ACTIVE'));
        if (projs.length > 0) setSelectedProject(projs[0]);
      })
      .catch(e => setError(e.message));
  }, [activeOrg?.id]);

  const loadActivities = useCallback(async () => {
    if (!selectedProject || !activeOrg?.id) return;
    setLoading(true);
    setError('');
    try {
      const resp = await activityApi.list(activeOrg.id, selectedProject.id, { page: 0, size: 30 });
      const acts = resp.data?.content || resp.data || [];
      setActivities(acts);

      // Load blockchain records for each activity
      const records = {};
      await Promise.all(acts.slice(0, 20).map(async (act) => {
        try {
          const r = await blockchainApi.getRecord(act.id);
          records[act.id] = r.data;
        } catch {
          records[act.id] = null;
        }
      }));
      setBlockchainRecords(records);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [selectedProject, activeOrg?.id]);

  useEffect(() => { loadActivities(); }, [loadActivities]);

  const handleVerify = async (activityId) => {
    setVerifying(v => ({ ...v, [activityId]: true }));
    try {
      const r = await blockchainApi.verify(activityId);
      setBlockchainRecords(prev => ({ ...prev, [activityId]: r.data }));
    } catch (e) {
      setError(e.message);
    } finally {
      setVerifying(v => ({ ...v, [activityId]: false }));
    }
  };

  const handleAnchor = async (activityId) => {
    setVerifying(v => ({ ...v, [activityId]: true }));
    try {
      const r = await blockchainApi.anchor(activityId);
      setBlockchainRecords(prev => ({ ...prev, [activityId]: r.data }));
    } catch (e) {
      setError(e.message);
    } finally {
      setVerifying(v => ({ ...v, [activityId]: false }));
    }
  };

  const stats = {
    total: activities.length,
    verified: Object.values(blockchainRecords).filter(r => r?.anchoringStatus === 'VERIFIED').length,
    pending: Object.values(blockchainRecords).filter(r => r?.anchoringStatus === 'PENDING').length,
    unavailable: Object.values(blockchainRecords).filter(r => !r || r?.anchoringStatus === 'UNAVAILABLE').length,
  };

  if (!activeOrg) {
    return <div className="page-container"><div className="glass-card" style={{ textAlign: 'center', padding: 48 }}><p style={{ color: 'var(--text-secondary)' }}>Select an organization first.</p></div></div>;
  }

  return (
    <div className="page-container">
      <div style={{ marginBottom: 28 }}>
        <h2 style={{ fontSize: '1.4rem', fontWeight: 800, marginBottom: 4 }}>🔗 Blockchain Verification</h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
          SHA-256 integrity hashing and Hyperledger Fabric anchoring for all tracked activities.
        </p>
      </div>

      {/* Info Banner */}
      <div style={{ background: 'rgba(14,165,233,0.08)', border: '1px solid rgba(14,165,233,0.3)', borderRadius: 10, padding: '12px 18px', marginBottom: 24, fontSize: '0.85rem', color: '#7dd3fc' }}>
        ℹ️ <strong>Fabric Network:</strong> Each activity event is SHA-256 hashed deterministically. When the Hyperledger Fabric network is running,
        hashes are anchored to the <code>projectpulse-channel</code> / <code>activitytracker</code> chaincode.
        Records show PENDING until anchored and VERIFIED after successful on-chain confirmation.
      </div>

      {/* Stats */}
      <div className="grid-cols-4" style={{ marginBottom: 24 }}>
        <div className="glass-card" style={{ textAlign: 'center', padding: 18 }}>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--primary-light)' }}>{stats.total}</div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Total Activities</div>
        </div>
        <div className="glass-card" style={{ textAlign: 'center', padding: 18 }}>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--success)' }}>{stats.verified}</div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Verified on Chain</div>
        </div>
        <div className="glass-card" style={{ textAlign: 'center', padding: 18 }}>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--warning)' }}>{stats.pending}</div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Pending Anchoring</div>
        </div>
        <div className="glass-card" style={{ textAlign: 'center', padding: 18 }}>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--text-muted)' }}>{stats.unavailable}</div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>No Record</div>
        </div>
      </div>

      {/* Project selector */}
      {projects.length > 1 && (
        <div style={{ marginBottom: 16, display: 'flex', gap: 8, alignItems: 'center' }}>
          <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>Project:</label>
          <select className="form-select" style={{ width: 240, fontSize: '0.85rem', padding: '7px 10px' }}
            value={selectedProject?.id || ''} onChange={e => setSelectedProject(projects.find(p => p.id === Number(e.target.value)))}>
            {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
        </div>
      )}

      {error && (
        <div style={{ background: 'rgba(239,68,68,0.12)', border: '1px solid rgba(239,68,68,0.4)', borderRadius: 10, padding: '12px 18px', marginBottom: 16, color: '#f87171' }}>
          ⚠️ {error}
        </div>
      )}

      <div className="glass-card">
        <div className="flex-between" style={{ marginBottom: 16 }}>
          <h3 style={{ fontSize: '1rem', fontWeight: 700 }}>Activity Integrity Records</h3>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            {activities.length} events · {selectedProject?.name}
          </span>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '50px 0', color: 'var(--text-muted)' }}>Loading records…</div>
        ) : activities.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '50px 0', color: 'var(--text-muted)' }}>
            <div style={{ fontSize: 40, marginBottom: 12 }}>🔍</div>
            <p>No activity events to verify. Start the Desktop Agent to begin tracking.</p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="pulse-table">
              <thead>
                <tr>
                  <th>Activity</th>
                  <th>File</th>
                  <th>Timestamp</th>
                  <th>SHA-256 Hash</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {activities.map(act => {
                  const record = blockchainRecords[act.id];
                  const status = record?.anchoringStatus || 'UNAVAILABLE';
                  return (
                    <tr key={act.id}>
                      <td>
                        <span className={`badge badge-${act.eventType?.toLowerCase()}`}>{act.eventType}</span>
                      </td>
                      <td style={{ maxWidth: 200 }}>
                        <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontSize: '0.83rem' }}>
                          {act.filePath?.split(/[\\/]/).pop()}
                        </div>
                      </td>
                      <td style={{ fontSize: '0.8rem', whiteSpace: 'nowrap' }}>
                        {act.eventTimestamp ? new Date(act.eventTimestamp).toLocaleString() : '—'}
                      </td>
                      <td>
                        {record?.recordHash ? (
                          <span className="code-pill" style={{ fontSize: '0.7rem' }}>
                            {record.recordHash.slice(0, 12)}…
                          </span>
                        ) : (
                          <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>Not hashed</span>
                        )}
                      </td>
                      <td><VerificationBadge status={status} /></td>
                      <td>
                        <div style={{ display: 'flex', gap: 6 }}>
                          <button className="btn btn-secondary btn-sm" style={{ fontSize: '0.72rem' }}
                            onClick={() => handleVerify(act.id)} disabled={verifying[act.id]}>
                            {verifying[act.id] ? '…' : '🔍 Verify'}
                          </button>
                          {status === 'PENDING' && (
                            <button className="btn btn-primary btn-sm" style={{ fontSize: '0.72rem' }}
                              onClick={() => handleAnchor(act.id)} disabled={verifying[act.id]}>
                              ⚓ Anchor
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
