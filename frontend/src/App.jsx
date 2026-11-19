import React, { useState, useEffect } from 'react';
import { Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Sidebar from './components/Sidebar';
import Navbar from './components/Navbar';

// Pages
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import ProjectsPage from './pages/ProjectsPage';
import ProjectDetailPage from './pages/ProjectDetailPage';
import ActivityPage from './pages/ActivityPage';
import CollaborationPage from './pages/CollaborationPage';
import BlockchainPage from './pages/BlockchainPage';
import SettingsPage from './pages/SettingsPage';
import AdminPage from './pages/AdminPage';

import { authApi } from './api';

function AppLayout() {
  const { isAuthenticated, user, activeOrg, switchOrg } = useAuth();
  const [orgs, setOrgs] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated) {
      authApi.me()
        .then(r => {
          const profile = r.data;
          setOrgs(profile.organizations || []);
        })
        .catch(() => {});
    }
  }, [isAuthenticated, activeOrg?.id]);

  if (!isAuthenticated) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  return (
    <div className="app-container">
      <Sidebar orgs={orgs} activeOrg={activeOrg} onSwitchOrg={switchOrg} />
      <div className="main-content-wrapper">
        <Navbar activeOrg={activeOrg} />
        <main style={{ flex: 1 }}>
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={
              <ProtectedRoute><DashboardPage /></ProtectedRoute>
            } />
            <Route path="/projects" element={
              <ProtectedRoute><ProjectsPage /></ProtectedRoute>
            } />
            <Route path="/projects/:projectId" element={
              <ProtectedRoute><ProjectDetailPage /></ProtectedRoute>
            } />
            <Route path="/activity" element={
              <ProtectedRoute><ActivityPage /></ProtectedRoute>
            } />
            <Route path="/collaboration" element={
              <ProtectedRoute><CollaborationPage /></ProtectedRoute>
            } />
            <Route path="/blockchain" element={
              <ProtectedRoute><BlockchainPage /></ProtectedRoute>
            } />
            <Route path="/settings" element={
              <ProtectedRoute><SettingsPage /></ProtectedRoute>
            } />
            <Route path="/admin" element={
              <ProtectedRoute adminOnly><AdminPage /></ProtectedRoute>
            } />
            <Route path="/login" element={<Navigate to="/dashboard" replace />} />
            <Route path="/register" element={<Navigate to="/dashboard" replace />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppLayout />
    </AuthProvider>
  );
}
