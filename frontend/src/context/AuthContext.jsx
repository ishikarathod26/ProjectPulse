import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';

const AuthContext = createContext(null);

const TOKEN_KEY = 'projectpulse_token';
const USER_KEY = 'projectpulse_user';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem(USER_KEY);
      return stored ? JSON.parse(stored) : null;
    } catch { return null; }
  });
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [activeOrg, setActiveOrg] = useState(() => {
    try {
      const stored = localStorage.getItem(USER_KEY);
      if (stored) {
        const u = JSON.parse(stored);
        return u.activeOrg || null;
      }
    } catch {}
    return null;
  });

  const login = useCallback((authData) => {
    const { token: t, id, email, fullName, systemRole, activeOrganizationId, activeOrganizationName } = authData;
    const orgInfo = activeOrganizationId ? { id: activeOrganizationId, name: activeOrganizationName } : null;
    const userData = { id, email, fullName, systemRole, activeOrg: orgInfo };
    localStorage.setItem(TOKEN_KEY, t);
    localStorage.setItem(USER_KEY, JSON.stringify(userData));
    setToken(t);
    setUser(userData);
    setActiveOrg(orgInfo);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setToken(null);
    setUser(null);
    setActiveOrg(null);
  }, []);

  const switchOrg = useCallback((authData) => {
    const { token: t, id, email, fullName, systemRole, activeOrganizationId, activeOrganizationName } = authData;
    const orgInfo = activeOrganizationId ? { id: activeOrganizationId, name: activeOrganizationName } : null;
    const userData = { id, email, fullName, systemRole, activeOrg: orgInfo };
    localStorage.setItem(TOKEN_KEY, t);
    localStorage.setItem(USER_KEY, JSON.stringify(userData));
    setToken(t);
    setUser(userData);
    setActiveOrg(orgInfo);
  }, []);

  const isAuthenticated = !!token && !!user;
  const isPlatformAdmin = user?.systemRole === 'PLATFORM_ADMIN';

  return (
    <AuthContext.Provider value={{ user, token, activeOrg, isAuthenticated, isPlatformAdmin, login, logout, switchOrg }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
