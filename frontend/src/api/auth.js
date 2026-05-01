import api from './axios';

export const signup = (data) =>
  api.post('/auth/signup', data).then((r) => r.data);

export const login = (data) =>
  api.post('/auth/login', data).then((r) => r.data);

export const verifyOtp = (data) =>
  api.post('/auth/verify-otp', data).then((r) => r.data);

export const resendOtp = (data) =>
  api.post('/auth/resend-otp', data).then((r) => r.data);

export const forgotPassword = (email) =>
  api.post('/auth/forgot-password', { email }).then((r) => r.data);

export const resetPassword = (email, newPassword) =>
  api.post('/auth/reset-password', { email, newPassword }).then((r) => r.data);

export const getUserProfile = (userId) =>
  api.get(`/auth/user/${userId}`).then((r) => r.data);

export const getUserByEmail = (email) =>
  api.get(`/auth/user/email/${email}`).then((r) => r.data);

export const getCurrentUser = () =>
  api.get('/auth/me').then((r) => r.data);
