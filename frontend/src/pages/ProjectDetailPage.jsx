import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { projectApi, activityApi, blockchainApi, orgApi } from '../api';

function BlockchainModal({ activityId, onClose }) {
  const [record, setRecord] = useState(null);
  const [loading, setLoading] = useState(true);
  const [verifying, setVerifying] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    blockchainApi.getRecord(activityId)
      .then(r => setRecord(r.data))
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, [activityId]);

  const handleVerify = async () => {
    setVerifying(true);
    try {
      const r = await blockchainApi.verify(activityId);
      setRecord(r.data);
    } catch (e) {
      setError(e.message);
    } finally {
      setVerifying(false);
    }
  };

  const statusBadge = (status) => {
    const map = {
      VERIFIED: 'badge-verified',
      MISMATCHED: 'badge-mismatched',
      PENDING: 'badge-pending',
      UNAVAILABLE: 'badge-unavailable',
    };
    return map[status] || 'badge-unavailable';
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3>🔗 Blockchain Record — Activity #{activityId}</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        {loading && <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>Loading blockchain record…</div>}
        {error && <div style={{ color: '#f87171', padding: '16px 0', fontSize: '0.875rem' }}>{error}</div>}

        {record && !loading && (
          <div>
            <div style={{ marginBottom: 16 }}>
              <div className="flex-between" style={{ marginBottom: 12 }}>
                <span style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>Anchoring Status</span>
                <span className={`badge ${statusBadge(record.anchoringStatus || record.status)}`}>{record.anchoringStatus || record.status}</span>
              </div>
              <div style={{ marginBottom: 8 }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: 4 }}>SHA-256 Digest</div>
                <div className="code-pill" style={{ display: 'block', wordBreak: 'break-all', padding: '8px 10px', fontSize: '0.75rem' }}>
                  {record.recordHash || record.recalculatedHash}
                </div>
              </div>
              {record.txId && (
                <div style={{ marginTop: 10 }}>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: 4 }}>Transaction ID</div>
                  <div className="code-pill" style={{ display: 'block', wordBreak: 'break-all', padding: '8px 10px', fontSize: '0.75rem' }}>
                    {record.txId}
                  </div>
                </div>
              )}
              {record.channelName && (
                <div style={{ marginTop: 10, display: 'flex', gap: 20 }}>
                  <div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Channel</div>
                    <div style={{ fontWeight: 700, marginTop: 2 }}>{record.channelName}</div>
                  </div>
                  <div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Chaincode</div>
                    <div style={{ fontWeight: 700, marginTop: 2 }}>{record.chaincodeName}</div>
                  </div>
                </div>
              )}
              {record.explanation && (
                <div style={{ marginTop: 14, background: 'rgba(99,102,241,0.08)', border: '1px solid rgba(99,102,241,0.2)', borderRadius: 8, padding: '10px 14px', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                  {record.explanation}
                </div>
              )}
            </div>
            <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
              <button className="btn btn-secondary" onClick={onClose}>Close</button>
              <button className="btn btn-primary" onClick={handleVerify} disabled={verifying}>
                {verifying ? 'Verifying…' : '🔍 Verify Cryptographic Hash'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

function EditProjectModal({ project, orgId, onClose, onUpdated }) {
  const [form, setForm] = useState({
    name: project.name || '',
    description: project.description || '',
    status: project.status || 'ACTIVE'
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const resp = await projectApi.update(orgId, project.id, form);
      onUpdated(resp.data);
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
          <h3>Edit Project — {project.name}</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        {error && (
          <div style={{ background: 'rgba(239,68,68,0.12)', border: '1px solid rgba(239,68,68,0.4)', borderRadius: 8, padding: '10px 14px', marginBottom: 16, fontSize: '0.85rem', color: '#f87171' }}>
            {error}
          </div>
        )}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="edit-name">Project Name</label>
            <input id="edit-name" type="text" className="form-input" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} required />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="edit-desc">Description</label>
            <textarea id="edit-desc" className="form-textarea" rows={3} value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="edit-status">Status</label>
            <select id="edit-status" className="form-select" value={form.status} onChange={e => setForm(f => ({ ...f, status: e.target.value }))}>
              <option value="ACTIVE">ACTIVE</option>
              <option value="PAUSED">PAUSED</option>
              <option value="ARCHIVED">ARCHIVED</option>
            </select>
          </div>
          <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 16 }}>
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving…' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function AddMemberModal({ orgId, projectId, existingMemberIds, onClose, onAdded }) {
  const [orgMembers, setOrgMembers] = useState([]);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [role, setRole] = useState('MEMBER');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    orgApi.members(orgId)
      .then(r => {
        const mems = r.data?.content || r.data || [];
        const available = mems.filter(m => !existingMemberIds.includes(m.userId));
        setOrgMembers(available);
        if (available.length > 0) setSelectedUserId(available[0].userId);
      })
      .catch(e => setError(e.message));
  }, [orgId, existingMemberIds]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedUserId) {
      setError('Please select an organization member to add.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const resp = await projectApi.addMember(orgId, projectId, { userId: Number(selectedUserId), role });
      onAdded(resp.data);
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
          <h3>Assign Team Member</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        {error && (
          <div style={{ background: 'rgba(239,68,68,0.12)', border: '1px solid rgba(239,68,68,0.4)', borderRadius: 8, padding: '10px 14px', marginBottom: 16, fontSize: '0.85rem', color: '#f87171' }}>
            {error}
          </div>
        )}
        {orgMembers.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '24px 0', color: 'var(--text-muted)' }}>
            <p>All members of this organization are already assigned to this project.</p>
            <button className="btn btn-secondary" style={{ marginTop: 12 }} onClick={onClose}>Close</button>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label" htmlFor="select-user">Select Member</label>
              <select id="select-user" className="form-select" value={selectedUserId} onChange={e => setSelectedUserId(e.target.value)} required>
                {orgMembers.map(m => (
                  <option key={m.userId} value={m.userId}>
                    {m.userFullName || m.userEmail} ({m.userEmail})
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="select-role">Project Role</label>
              <select id="select-role" className="form-select" value={role} onChange={e => setRole(e.target.value)}>
                <option value="MEMBER">MEMBER (Can track activities & collaborate)</option>
                <option value="PROJECT_ADMIN">PROJECT_ADMIN (Can manage project settings & members)</option>
                <option value="VIEWER">VIEWER (Read-only access)</option>
              </select>
            </div>
            <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 16 }}>
              <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Assigning…' : 'Assign to Project'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}

export default function ProjectDetailPage() {
  const { projectId } = useParams();
  const { activeOrg } = useAuth();
  const navigate = useNavigate();

  const [project, setProject] = useState(null);
  const [members, setMembers] = useState([]);
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [eventTypeFilter, setEventTypeFilter] = useState('');
  const [selectedActivity, setSelectedActivity] = useState(null);
  const [activeTab, setActiveTab] = useState('activity');
  const [showEdit, setShowEdit] = useState(false);
  const [showAddMember, setShowAddMember] = useState(false);
  const [archiving, setArchiving] = useState(false);

  const loadData = useCallback(async () => {
    if (!activeOrg?.id || !projectId) return;
    setLoading(true);
    setError('');
    try {
      const [projResp, membersResp, actResp] = await Promise.all([
        projectApi.get(activeOrg.id, projectId),
        projectApi.members(activeOrg.id, projectId),
        activityApi.list(activeOrg.id, projectId, { page, size: 20, eventType: eventTypeFilter || undefined }),
      ]);
      setProject(projResp.data);
      setMembers(membersResp.data?.content || membersResp.data || []);
      const actData = actResp.data;
      setActivities(actData?.content || actData || []);
      setTotalPages(actData?.totalPages || 0);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [activeOrg?.id, projectId, page, eventTypeFilter]);

  useEffect(() => { loadData(); }, [loadData]);

  const handleArchive = async () => {
    if (!confirm(`Are you sure you want to archive project "${project.name}"?`)) return;
    setArchiving(true);
    try {
      await projectApi.archive(activeOrg.id, project.id);
      setProject(p => ({ ...p, status: 'ARCHIVED' }));
    } catch (err) {
      alert('Archive failed: ' + err.message);
    } finally {
      setArchiving(false);
    }
  };

  const handleRemoveMember = async (userId, memberName) => {
    if (!confirm(`Remove ${memberName} from this project?`)) return;
    try {
      await projectApi.removeMember(activeOrg.id, projectId, userId);
      setMembers(mems => mems.filter(m => (m.userId || m.id) !== userId));
    } catch (err) {
      alert('Failed to remove member: ' + err.message);
    }
  };

  const eventBadge = (type) => {
    const map = { CREATE: 'badge-create', MODIFY: 'badge-modify', DELETE: 'badge-delete', RENAME: 'badge-rename' };
    return map[type] || 'badge-unavailable';
  };

  if (!activeOrg) return null;

  return (
    <div className="page-container">
      {/* Back button & Action buttons */}
      <div className="flex-between" style={{ marginBottom: 20 }}>
        <button className="btn btn-secondary btn-sm" onClick={() => navigate('/projects')}>
          ← Back to Projects
        </button>
        {project && (
          <div style={{ display: 'flex', gap: 10 }}>
            <button className="btn btn-secondary btn-sm" onClick={() => setShowEdit(true)}>
              ✏️ Edit Project
            </button>
            {project.status === 'ACTIVE' && (
              <button className="btn btn-danger btn-sm" onClick={handleArchive} disabled={archiving}>
                {archiving ? '…' : '📁 Archive Project'}
              </button>
            )}
          </div>
        )}
      </div>

      {error && (
        <div style={{ background: 'rgba(239,68,68,0.12)', border: '1px solid rgba(239,68,68,0.4)', borderRadius: 10, padding: '12px 18px', marginBottom: 20, color: '#f87171' }}>
          ⚠️ {error}
        </div>
      )}

      {loading && !project ? (
        <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-muted)' }}>Loading project…</div>
      ) : project ? (
        <>
          {/* Project Header */}
          <div className="glass-card" style={{ marginBottom: 24 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 12 }}>
              <div style={{
                width: 52, height: 52, borderRadius: 12,
                background: 'linear-gradient(135deg, rgba(99,102,241,0.4), rgba(168,85,247,0.4))',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontWeight: 800, fontSize: '1rem', color: 'var(--primary-light)'
              }}>
                {project.keyIdentifier?.slice(0, 3)}
              </div>
              <div style={{ flex: 1 }}>
                <h2 style={{ fontSize: '1.35rem', fontWeight: 800, marginBottom: 2 }}>{project.name}</h2>
                <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                  <span className="code-pill">{project.keyIdentifier}</span>
                  <span className={`badge ${project.status === 'ACTIVE' ? 'badge-verified' : 'badge-unavailable'}`}>{project.status}</span>
                </div>
              </div>
            </div>
            {project.description && (
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>{project.description}</p>
            )}
          </div>

          {/* Tabs */}
          <div style={{ display: 'flex', gap: 4, marginBottom: 20, background: 'rgba(255,255,255,0.03)', borderRadius: 10, padding: 4, width: 'fit-content', border: '1px solid var(--border-subtle)' }}>
            {[
              { id: 'activity', label: '📊 Activity' },
              { id: 'members', label: `👥 Members (${members.length})` },
            ].map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                style={{
                  padding: '7px 18px', borderRadius: 8, border: 'none', cursor: 'pointer',
                  background: activeTab === tab.id ? 'var(--primary)' : 'transparent',
                  color: activeTab === tab.id ? '#fff' : 'var(--text-secondary)',
                  fontWeight: 600, fontSize: '0.875rem', transition: 'all 0.15s'
                }}
              >
                {tab.label}
              </button>
            ))}
          </div>

          {activeTab === 'activity' && (
            <div className="glass-card">
              <div className="flex-between" style={{ marginBottom: 16 }}>
                <h3 style={{ fontSize: '1rem', fontWeight: 700 }}>Activity Timeline</h3>
                <select className="form-select" style={{ width: 160, fontSize: '0.85rem', padding: '6px 10px' }}
                  value={eventTypeFilter} onChange={e => { setEventTypeFilter(e.target.value); setPage(0); }}>
                  <option value="">All Events</option>
                  <option value="CREATE">Create</option>
                  <option value="MODIFY">Modify</option>
                  <option value="DELETE">Delete</option>
                  <option value="RENAME">Rename</option>
                </select>
              </div>

              {loading ? (
                <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-muted)' }}>Loading activity…</div>
              ) : activities.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-muted)' }}>
                  <div style={{ fontSize: 36, marginBottom: 10 }}>📭</div>
                  <p>No activity events found.</p>
                  <p style={{ fontSize: '0.82rem', marginTop: 4, color: 'var(--text-muted)' }}>
                    Filesystem change telemetry from Desktop Agent will appear here automatically.
                  </p>
                </div>
              ) : (
                <div className="table-responsive">
                  <table className="pulse-table">
                    <thead>
                      <tr>
                        <th>Event</th>
                        <th>File</th>
                        <th>User / Device</th>
                        <th>Timestamp</th>
                        <th>Blockchain</th>
                      </tr>
                    </thead>
                    <tbody>
                      {activities.map(ev => (
                        <tr key={ev.id}>
                          <td><span className={`badge ${eventBadge(ev.eventType)}`}>{ev.eventType}</span></td>
                          <td>
                            <div style={{ maxWidth: 280, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontSize: '0.85rem' }} title={ev.filePath}>
                              {ev.filePath?.split(/[\\/]/).pop()}
                            </div>
                            <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>{ev.filePath}</div>
                          </td>
                          <td style={{ fontSize: '0.82rem' }}>{ev.userName || 'Agent'}</td>
                          <td style={{ fontSize: '0.82rem', whiteSpace: 'nowrap' }}>
                            {ev.eventTimestamp ? new Date(ev.eventTimestamp).toLocaleString() : '—'}
                          </td>
                          <td>
                            <button
                              className="btn btn-secondary btn-sm"
                              style={{ fontSize: '0.72rem' }}
                              onClick={() => setSelectedActivity(ev.id)}
                            >
                              🔗 Verify
                            </button>
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
          )}

          {activeTab === 'members' && (
            <div className="glass-card">
              <div className="flex-between" style={{ marginBottom: 16 }}>
                <h3 style={{ fontSize: '1rem', fontWeight: 700 }}>Project Team Members ({members.length})</h3>
                <button className="btn btn-primary btn-sm" onClick={() => setShowAddMember(true)}>
                  ➕ Assign Member
                </button>
              </div>

              {members.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '30px 0', color: 'var(--text-muted)' }}>
                  <p>No members assigned to this project yet.</p>
                  <button className="btn btn-primary btn-sm" style={{ marginTop: 10 }} onClick={() => setShowAddMember(true)}>
                    Assign First Member
                  </button>
                </div>
              ) : (
                <div>
                  {members.map(m => (
                    <div key={m.userId || m.id} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '12px 0', borderBottom: '1px solid var(--border-subtle)' }}>
                      <div className="user-avatar" style={{ width: 38, height: 38, fontSize: '0.9rem' }}>
                        {m.userFullName?.charAt(0)?.toUpperCase() || 'U'}
                      </div>
                      <div style={{ flex: 1 }}>
                        <div style={{ fontWeight: 600, fontSize: '0.88rem' }}>{m.userFullName || m.userEmail}</div>
                        <div style={{ fontSize: '0.76rem', color: 'var(--text-muted)' }}>{m.userEmail}</div>
                      </div>
                      <span className="badge badge-pending" style={{ marginRight: 8 }}>{m.role}</span>
                      <button
                        className="btn btn-danger btn-sm"
                        style={{ fontSize: '0.72rem', padding: '4px 8px' }}
                        onClick={() => handleRemoveMember(m.userId || m.id, m.userFullName || m.userEmail)}
                      >
                        Remove
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </>
      ) : null}

      {showEdit && project && (
        <EditProjectModal
          project={project}
          orgId={activeOrg.id}
          onClose={() => setShowEdit(false)}
          onUpdated={updated => setProject(updated)}
        />
      )}

      {showAddMember && (
        <AddMemberModal
          orgId={activeOrg.id}
          projectId={projectId}
          existingMemberIds={members.map(m => m.userId || m.id)}
          onClose={() => setShowAddMember(false)}
          onAdded={newMember => setMembers(mems => [...mems, newMember])}
        />
      )}

      {selectedActivity && (
        <BlockchainModal activityId={selectedActivity} onClose={() => setSelectedActivity(null)} />
      )}
    </div>
  );
}
