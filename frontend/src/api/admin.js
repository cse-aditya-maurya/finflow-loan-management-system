import api from './axios';

export const adminGetAllApplications = () =>
  api.get('/admin/applications').then((r) => r.data);

export const adminGetApplicationById = (appId) =>
  api.get(`/admin/applications/${appId}`).then((r) => r.data);

export const adminGetDocuments = (appId) =>
  api.get(`/admin/documents/${appId}`).then((r) => r.data);

export const adminViewDocument = (docId) =>
  api.get(`/admin/documents/view/${docId}`).then((r) => r.data);

export const adminVerifyDocument = (docId) =>
  api.put(`/admin/documents/${docId}/verify`).then((r) => r.data);

export const adminRejectDocument = (docId, remarks) =>
  api.put(`/admin/documents/${docId}/reject`, null, { params: { remarks } }).then((r) => r.data);

export const adminApproveApplication = (appId) =>
  api.post(`/admin/applications/${appId}/approve`).then((r) => r.data);

export const adminRejectApplication = (appId, remarks) =>
  api.post(`/admin/applications/${appId}/reject`, null, { params: { remarks } }).then((r) => r.data);

export const adminDashboard = () =>
  api.get('/admin/dashboard').then((r) => r.data);

export const adminGetAllUsers = () =>
  api.get('/admin/users').then((r) => r.data);

export const adminGetUserById = (userId) =>
  api.get(`/admin/users/${userId}`).then((r) => r.data);

export const adminGetReports = () =>
  api.get('/admin/reports').then((r) => r.data);

export const adminGetStatistics = () =>
  api.get('/admin/statistics').then((r) => r.data);
