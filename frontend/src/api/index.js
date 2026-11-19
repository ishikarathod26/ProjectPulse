import api from './client';

// ─── Auth ─────────────────────────────────────────────────────────────────────
export const authApi = {
  register: (data) => api.post('/api/auth/register', data),
  login: (data) => api.post('/api/auth/login', data),
  me: () => api.get('/api/auth/me'),
  switchOrg: (orgId) => api.post(`/api/auth/switch-org/${orgId}`),
  health: () => api.get('/api/health'),
};

// ─── Organizations ────────────────────────────────────────────────────────────
export const orgApi = {
  list: () => api.get('/api/organizations'),
  create: (data) => api.post('/api/organizations', data),
  get: (id) => api.get(`/api/organizations/${id}`),
  update: (id, data) => api.put(`/api/organizations/${id}`, data),
  members: (id) => api.get(`/api/organizations/${id}/members`),
  invite: (id, data) => api.post(`/api/organizations/${id}/members`, data),
  removeMember: (orgId, userId) => api.delete(`/api/organizations/${orgId}/members/${userId}`),
};

// ─── Projects ─────────────────────────────────────────────────────────────────
export const projectApi = {
  list: (orgId) => api.get('/api/projects', { params: { organizationId: orgId } }),
  create: (orgId, data) => api.post('/api/projects', { ...data, organizationId: orgId }),
  get: (orgId, projectId) => api.get(`/api/projects/${projectId}`),
  update: (orgId, projectId, data) => api.put(`/api/projects/${projectId}`, data),
  archive: (orgId, projectId) => api.patch(`/api/projects/${projectId}/archive`),
  members: (orgId, projectId) => api.get(`/api/projects/${projectId}/members`),
  addMember: (orgId, projectId, data) => api.post(`/api/projects/${projectId}/members`, data),
  removeMember: (orgId, projectId, userId) => api.delete(`/api/projects/${projectId}/members/${userId}`),
};

// ─── Activities ───────────────────────────────────────────────────────────────
export const activityApi = {
  list: (orgId, projectId, params) =>
    api.get('/api/activities', { params: { projectId, ...params } }),
  ingest: (orgId, projectId, data) =>
    api.post('/api/activities/batch', { ...data, projectId }),
  stats: (projectId) => api.get('/api/activities/stats', { params: { projectId } }),
  registerDevice: (data) => api.post('/api/devices/register', data),
};

// ─── Blockchain ───────────────────────────────────────────────────────────────
export const blockchainApi = {
  getRecord: (activityId) => api.get(`/api/blockchain/verify/${activityId}`),
  verify: (activityId) => api.get(`/api/blockchain/verify/${activityId}`),
  status: () => api.get('/api/blockchain/status'),
};

// ─── Collaboration ────────────────────────────────────────────────────────────
export const collaborationApi = {
  messages: (orgId, projectId, params) =>
    api.get(`/api/projects/${projectId}/messages`, { params }),
  sendMessage: (orgId, projectId, data) =>
    api.post(`/api/projects/${projectId}/messages`, data),
  notes: (orgId, projectId) =>
    api.get(`/api/projects/${projectId}/notes`),
  createNote: (orgId, projectId, data) =>
    api.post(`/api/projects/${projectId}/notes`, data),
  updateNote: (orgId, projectId, noteId, data) =>
    api.put(`/api/projects/${projectId}/notes/${noteId}`, data),
  deleteNote: (orgId, projectId, noteId) =>
    api.delete(`/api/projects/${projectId}/notes/${noteId}`),
  statusUpdates: (orgId, projectId) =>
    api.get(`/api/projects/${projectId}/status-updates`),
  createStatusUpdate: (orgId, projectId, data) =>
    api.post(`/api/projects/${projectId}/status-updates`, data),
};

// ─── Admin ────────────────────────────────────────────────────────────────────
export const adminApi = {
  stats: () => api.get('/api/admin/stats'),
  users: (params) => api.get('/api/admin/users', { params }),
  orgs: (params) => api.get('/api/admin/organizations', { params }),
  auditLogs: (params) => api.get('/api/admin/audit-logs', { params }),
  suspendUser: (userId) => api.patch(`/api/admin/users/${userId}/suspend`),
  activateUser: (userId) => api.patch(`/api/admin/users/${userId}/activate`),
};
