import React from 'react';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const PAGE_TITLES = {
  '/dashboard': 'Dashboard',
  '/projects': 'Projects',
  '/activity': 'Activity Feed',
  '/collaboration': 'Collaboration',
  '/blockchain': 'Blockchain Verification',
  '/settings': 'Account Settings',
  '/admin': 'Platform Administration',
};

export default function Navbar({ activeOrg }) {
  const { user } = useAuth();
  const location = useLocation();

  const title = Object.entries(PAGE_TITLES).find(([path]) =>
    location.pathname.startsWith(path)
  )?.[1] || 'ProjectPulse';

  return (
    <header className="navbar">
      <div className="navbar-left">
        <h2 style={{ fontSize: '1.15rem', fontWeight: 700, color: '#fff' }}>{title}</h2>
        {activeOrg && (
          <span style={{
            background: 'rgba(99,102,241,0.15)',
            border: '1px solid rgba(99,102,241,0.35)',
            borderRadius: 'var(--radius-full)',
            padding: '3px 12px',
            fontSize: '0.78rem',
            color: 'var(--primary-light)',
            fontWeight: 600
          }}>
            {activeOrg.name}
          </span>
        )}
      </div>
      <div className="navbar-right">
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: '0.8rem', color: 'var(--text-muted)' }}>
          <span className="pulse-indicator" />
          <span>Backend Connected</span>
        </div>
        <div className="user-badge">
          <div className="user-avatar">
            {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
          </div>
          <span style={{ fontSize: '0.85rem', fontWeight: 500 }}>{user?.fullName}</span>
        </div>
      </div>
    </header>
  );
}
