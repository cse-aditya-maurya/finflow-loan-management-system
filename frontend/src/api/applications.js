import api from './axios';

export const createApplication = (data) =>
  api.post('/applications', data).then((r) => r.data);

export const updateApplication = (id, data) =>
  api.put(`/applications/${id}`, data).then((r) => r.data);

export const submitApplication = (id) =>
  api.post(`/applications/${id}/submit`).then((r) => r.data);

export const getMyApplications = () =>
  api.get('/applications/my').then((r) => r.data);

export const getApplicationById = (id) =>
  api.get(`/applications/${id}`).then((r) => r.data);

export const getApplicationStatus = (id) =>
  api.get(`/applications/${id}/status`).then((r) => r.data);
