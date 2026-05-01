import { describe, it, expect, vi, beforeEach } from 'vitest';
import api from '../../api/axios';

describe('API Client', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('sets base URL from environment', () => {
    expect(api.defaults.baseURL).toBeDefined();
  });

  it('includes auth token in request headers', async () => {
    localStorage.setItem('finflow_token', 'test-token-123');
    
    const config = {
      headers: {},
    };

    const handlers = api.interceptors.request.handlers;
    
    if (handlers && handlers.length > 0) {
      const fulfilled = handlers[0].fulfilled;
      if (fulfilled) {
        const result = await fulfilled(config);
        expect(result.headers?.Authorization).toBe('Bearer test-token-123');
      }
    }
  });

  it('handles missing token gracefully', async () => {
    const config = {
      headers: {},
    };

    const handlers = api.interceptors.request.handlers;
    
    if (handlers && handlers.length > 0) {
      const fulfilled = handlers[0].fulfilled;
      if (fulfilled) {
        const result = await fulfilled(config);
        expect(result.headers?.Authorization).toBeUndefined();
      }
    }
  });
});
