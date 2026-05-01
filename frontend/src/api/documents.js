import api from './axios';

const LOAN_TYPE_PATH = {
  HOME: 'home',
  EDUCATION: 'education',
  BUSINESS: 'business',
  VEHICLE: 'vehicle',
  MARRIAGE: 'marriage',
  PERSONAL: 'personal',
};

export const uploadDocument = (
  applicationId,
  loanType,
  documentType,
  file
) => {
  const path = LOAN_TYPE_PATH[loanType.toUpperCase()] ?? 'personal';
  const form = new FormData();
  form.append('applicationId', String(applicationId));
  form.append('documentType', documentType);
  form.append('file', file);
  return api
    .post(`/documents/upload/${path}`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((r) => r.data);
};

export const replaceDocument = (id, file) => {
  const form = new FormData();
  form.append('file', file);
  return api
    .put(`/documents/${id}`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((r) => r.data);
};

export const getDocumentsByApplication = (applicationId) =>
  api.get(`/documents/application/${applicationId}`).then((r) => r.data);
