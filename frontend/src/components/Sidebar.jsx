import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authApi } from '../api';

function LogoIcon() {
  return (
    <div className="logo-icon">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 2L2 7l10 5 10-5-10-5z" />
        <path d="M2 17l10 5 10-5" />
        <path d="M2 12l10 5 10-5" />
      </svg>
    </div>
  );
}

const NAV_ITEMS = [
  { to: '/dashboard', icon: '⬛', label: 'Dashboard' },
  { to: '/projects', icon: '📁', label: 'Projects' },
  { to: '/activity', icon: '📊', label: 'Activity' },
  { to: '/collaboration', icon: '💬', label: 'Collaboration' },
  { to: '/blockchain', icon: '🔗', label: 'Blockchain' },
  { to: '/settings', icon: '⚙️', label: 'Settings' },
];

export default function Sidebar({ orgs, activeOrg, onSwitchOrg }) {
  const { user, isPlatformAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [switchingOrg, setSwitchingOrg] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleOrgSwitch = async (orgId) => {
    if (orgId === activeOrg?.id) return;
    setSwitchingOrg(true);
    try {
      const data = await authApi.switchOrg(orgId);
      onSwitchOrg(data.data);
    } catch (err) {
      console.error('Org switch failed:', err.message);
    } finally {
      setSwitchingOrg(false);
    }
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <LogoIcon />
        <span className="logo-title">ProjectPulse</span>
      </div>

      {/* Organization Switcher */}
      {orgs && orgs.length > 0 && (
        <div style={{ padding: '14px 16px', borderBottom: '1px solid var(--border-subtle)' }}>
          <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: '6px' }}>
            Active Workspace
          </div>
          <select
            className="form-select"
            style={{ fontSize: '0.85rem', padding: '7px 10px' }}
            value={activeOrg?.id || ''}
            onChange={(e) => handleOrgSwitch(Number(e.target.value))}
            disabled={switchingOrg}
          >
            {orgs.map(org => (
              <option key={org.id} value={org.id}>{org.name}</option>
            ))}
          </select>
        </div>
      )}

      <nav className="sidebar-nav">
        {NAV_ITEMS.map(item => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
          >
            <span style={{ fontSize: '1rem', lineHeight: 1 }}>{item.icon}</span>
            <span>{item.label}</span>
          </NavLink>
        ))}
        {isPlatformAdmin && (
          <NavLink
            to="/admin"
            className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
          >
            <span style={{ fontSize: '1rem', lineHeight: 1 }}>🛡️</span>
            <span>Admin Panel</span>
          </NavLink>
        )}
      </nav>

      <div className="sidebar-footer">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '10px' }}>
          <div className="user-avatar" style={{ width: 32, height: 32, fontSize: '0.85rem' }}>
            {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
          </div>
          <div>
            <div style={{ fontSize: '0.82rem', color: 'var(--text-main)', fontWeight: 600 }}>{user?.fullName}</div>
            <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>{user?.email}</div>
          </div>
        </div>
        <button
          onClick={handleLogout}
          className="btn btn-secondary btn-sm"
          style={{ width: '100%', marginTop: '4px' }}
        >
          Sign Out
        </button>
      </div>
    </aside>
  );
}
