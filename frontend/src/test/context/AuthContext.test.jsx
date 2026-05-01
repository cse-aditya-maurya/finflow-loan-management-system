import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { AuthProvider, useAuth } from '../../context/AuthContext';
import { ThemeProvider } from '../../context/ThemeContext';
import * as authApi from '../../api/auth';

vi.mock('../../api/auth');

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <ThemeProvider>
    <AuthProvider>{children}</AuthProvider>
  </ThemeProvider>
);

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('provides initial unauthenticated state', () => {
    const { result } = renderHook(() => useAuth(), { wrapper });

    expect(result.current.isLoggedIn).toBe(false);
    expect(result.current.user).toBeNull();
  });

  it('logs in user successfully', async () => {
    vi.spyOn(authApi, 'getCurrentUser').mockResolvedValue({
      email: 'user@test.com',
      name: 'Test User',
    });

    const { result } = renderHook(() => useAuth(), { wrapper });

    await act(async () => {
      await result.current.login('fake-token', 1, 'USER');
    });

    await waitFor(() => {
      expect(result.current.isLoggedIn).toBe(true);
    });
  });

  it('logs out user', async () => {
    localStorage.setItem('finflow_token', 'fake-token');
    localStorage.setItem('finflow_user', JSON.stringify({ email: 'user@test.com' }));

    const { result } = renderHook(() => useAuth(), { wrapper });

    act(() => {
      result.current.logout();
    });

    expect(result.current.isLoggedIn).toBe(false);
    expect(result.current.user).toBeNull();
    expect(localStorage.getItem('finflow_token')).toBeNull();
  });

  it('restores session from localStorage', () => {
    localStorage.setItem('finflow_token', 'fake-token');
    localStorage.setItem('finflow_user', JSON.stringify({ 
      token: 'fake-token',
      userId: 1,
      role: 'USER',
      email: 'user@test.com',
      profile: { name: 'Test User', email: 'user@test.com' }
    }));

    const { result } = renderHook(() => useAuth(), { wrapper });

    expect(result.current.isLoggedIn).toBe(true);
  });
});
