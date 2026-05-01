import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Attach JWT token from localStorage to every request
api.interceptors.request.use(
  (config) => {
    try {
      const token = localStorage.getItem('finflow_token');
      if (token) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${token}`;
      }
    } catch (error) {
      console.error('Error reading token from localStorage:', error);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Handle 401 globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('finflow_token');
      localStorage.removeItem('finflow_user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
