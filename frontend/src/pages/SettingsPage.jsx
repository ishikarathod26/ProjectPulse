import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { authApi, orgApi } from '../api';

export default function SettingsPage() {
  const { user, activeOrg, login } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [tab, setTab] = useState('profile');

  // Org form
  const [orgForm, setOrgForm] = useState({ name: '', description: '' });
  const [inviteEmail, setInviteEmail] = useState('');
  const [orgMembers, setOrgMembers] = useState([]);
  const [orgSaving, setOrgSaving] = useState(false);
  const [orgError, setOrgError] = useState('');
  const [orgSuccess, setOrgSuccess] = useState('');

  useEffect(() => {
    authApi.me()
      .then(r => {
        setProfile(r.data);
      })
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (tab === 'organization' && activeOrg?.id) {
      orgApi.members(activeOrg.id)
        .then(r => setOrgMembers(r.data?.content || r.data || []))
        .catch(e => setOrgError(e.message));
    }
  }, [tab, activeOrg?.id]);

  const handleInvite = async (e) => {
    e.preventDefault();
    setOrgSaving(true);
    setOrgError('');
    setOrgSuccess('');
    try {
      await orgApi.invite(activeOrg.id, { email: inviteEmail, role: 'MEMBER' });
      setOrgSuccess(`Invitation sent to ${inviteEmail}.`);
      setInviteEmail('');
      // Refresh members
      const r = await orgApi.members(activeOrg.id);
      setOrgMembers(r.data?.content || r.data || []);
    } catch (e) {
      setOrgError(e.message);
    } finally {
      setOrgSaving(false);
    }
  };

  const roleColors = { ORGANIZATION_OWNER: 'badge-verified', PROJECT_ADMIN: 'badge-pending', MEMBER: 'badge-modify', VIEWER: 'badge-unavailable' };

  return (
    <div className="page-container">
      <h2 style={{ fontSize: '1.4rem', fontWeight: 800, marginBottom: 24 }}>Settings</h2>

      <div style={{ display: 'flex', gap: 4, marginBottom: 24, background: 'rgba(255,255,255,0.03)', borderRadius: 10, padding: 4, width: 'fit-content', border: '1px solid var(--border-subtle)' }}>
        {[
          { id: 'profile', label: '👤 Profile' },
          { id: 'organization', label: '🏢 Organization' },
          { id: 'agent', label: '🖥️ Desktop Agent' },
        ].map(t => (
          <button key={t.id} onClick={() => setTab(t.id)} style={{
            padding: '7px 18px', borderRadius: 8, border: 'none', cursor: 'pointer',
            background: tab === t.id ? 'var(--primary)' : 'transparent',
            color: tab === t.id ? '#fff' : 'var(--text-secondary)',
            fontWeight: 600, fontSize: '0.875rem', transition: 'all 0.15s'
          }}>{t.label}</button>
        ))}
      </div>

      {tab === 'profile' && (
        <div className="glass-card" style={{ maxWidth: 600 }}>
          <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: 20 }}>Account Profile</h3>
          {loading ? (
            <div style={{ color: 'var(--text-muted)' }}>Loading…</div>
          ) : error ? (
            <div style={{ color: '#f87171' }}>{error}</div>
          ) : profile ? (
            <>
              <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 24, padding: '18px', background: 'rgba(255,255,255,0.03)', borderRadius: 10 }}>
                <div className="user-avatar" style={{ width: 56, height: 56, fontSize: '1.4rem' }}>
                  {profile.fullName?.charAt(0)?.toUpperCase()}
                </div>
                <div>
                  <div style={{ fontWeight: 700, fontSize: '1.1rem' }}>{profile.fullName}</div>
                  <div style={{ color: 'var(--text-secondary)', fontSize: '0.88rem' }}>{profile.email}</div>
                  <span className={`badge ${profile.systemRole === 'PLATFORM_ADMIN' ? 'badge-mismatched' : 'badge-pending'}`} style={{ marginTop: 6, display: 'inline-flex' }}>
                    {profile.systemRole}
                  </span>
                </div>
              </div>
              <div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 10 }}>
                  Organizations
                </div>
                {(profile.organizations || []).map(org => (
                  <div key={org.id} style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '10px 14px', background: 'rgba(255,255,255,0.03)', borderRadius: 8, marginBottom: 8 }}>
                    <span style={{ fontWeight: 600, flex: 1 }}>{org.name}</span>
                    <span className="code-pill" style={{ fontSize: '0.72rem' }}>{org.slug}</span>
                    <span className={`badge ${roleColors[org.role] || 'badge-unavailable'}`}>{org.role}</span>
                  </div>
                ))}
              </div>
              <div style={{ marginTop: 20, fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                Member since: {profile.createdAt ? new Date(profile.createdAt).toLocaleDateString() : '—'}
              </div>
            </>
          ) : null}
        </div>
      )}

      {tab === 'organization' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, maxWidth: 900 }}>
          <div className="glass-card">
            <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: 16 }}>Invite Member</h3>
            {orgError && <div style={{ color: '#f87171', marginBottom: 10, fontSize: '0.82rem' }}>{orgError}</div>}
            {orgSuccess && <div style={{ color: 'var(--success)', marginBottom: 10, fontSize: '0.82rem' }}>✅ {orgSuccess}</div>}
            <form onSubmit={handleInvite}>
              <div className="form-group">
                <label className="form-label">Email Address</label>
                <input type="email" className="form-input" placeholder="teammate@company.com"
                  value={inviteEmail} onChange={e => setInviteEmail(e.target.value)} required />
              </div>
              <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={orgSaving}>
                {orgSaving ? 'Inviting…' : 'Send Invitation'}
              </button>
            </form>
          </div>

          <div className="glass-card">
            <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: 16 }}>Members ({orgMembers.length})</h3>
            {orgMembers.length === 0 ? (
              <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No members loaded.</div>
            ) : (
              orgMembers.map(m => (
                <div key={m.userId || m.id} style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '10px 0', borderBottom: '1px solid var(--border-subtle)' }}>
                  <div className="user-avatar" style={{ width: 32, height: 32, fontSize: '0.78rem' }}>
                    {m.userFullName?.charAt(0) || 'U'}
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>{m.userFullName}</div>
                    <div style={{ fontSize: '0.73rem', color: 'var(--text-muted)' }}>{m.userEmail}</div>
                  </div>
                  <span className={`badge ${roleColors[m.role] || 'badge-unavailable'}`}>{m.role}</span>
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {tab === 'agent' && (
        <div className="glass-card" style={{ maxWidth: 700 }}>
          <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: 20 }}>Desktop Tracking Agent Setup</h3>
          <div style={{ marginBottom: 20 }}>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.7 }}>
              The ProjectPulse Desktop Agent monitors a local folder you choose and sends filesystem events
              (create, modify, delete, rename) to this backend. File contents are <strong>never uploaded</strong>.
              Only metadata (path, size, event type) is recorded.
            </p>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.03)', borderRadius: 10, padding: 18, marginBottom: 16 }}>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 10 }}>
              Step 1 — Build the Agent
            </div>
            <div className="code-pill" style={{ display: 'block', padding: '10px 14px', fontSize: '0.8rem', lineHeight: 1.6 }}>
              cd desktop-agent<br/>
              mvn package -DskipTests<br/>
              # Output: target/projectpulse-desktop-agent-1.0.0.jar
            </div>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.03)', borderRadius: 10, padding: 18, marginBottom: 16 }}>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 10 }}>
              Step 2 — Launch (GUI Mode)
            </div>
            <div className="code-pill" style={{ display: 'block', padding: '10px 14px', fontSize: '0.8rem', lineHeight: 1.6 }}>
              java -jar target/projectpulse-desktop-agent-1.0.0.jar
            </div>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.03)', borderRadius: 10, padding: 18, marginBottom: 16 }}>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 10 }}>
              Step 3 — Headless / Service Mode
            </div>
            <div className="code-pill" style={{ display: 'block', padding: '10px 14px', fontSize: '0.8rem', lineHeight: 1.6 }}>
              java -jar target/projectpulse-desktop-agent-1.0.0.jar \<br/>
              &nbsp;&nbsp;--headless \<br/>
              &nbsp;&nbsp;--server=http://localhost:8080 \<br/>
              &nbsp;&nbsp;--dir=/path/to/your/project
            </div>
          </div>

          <div style={{ background: 'rgba(14,165,233,0.08)', border: '1px solid rgba(14,165,233,0.3)', borderRadius: 8, padding: '10px 14px', fontSize: '0.82rem', color: '#7dd3fc' }}>
            ℹ️ Edit <strong>projectpulse-agent.properties</strong> to configure server URL, credentials, and project ID persistently.
          </div>
        </div>
      )}
    </div>
  );
}
