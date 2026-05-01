import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '../test-utils';
import userEvent from '@testing-library/user-event';
import { LoginPage } from '../../pages/LoginPage';
import * as authApi from '../../api/auth';

vi.mock('../../api/auth');

describe('LoginPage Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('renders login form', () => {
    render(<LoginPage />);
    
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('shows validation error for empty fields', async () => {
    const user = userEvent.setup();
    render(<LoginPage />);
    
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    await user.click(submitButton);
    
    expect(screen.getByText(/please fill in all fields/i)).toBeInTheDocument();
  });

  it('handles successful login', async () => {
    const user = userEvent.setup();
    const mockLogin = vi.spyOn(authApi, 'login').mockResolvedValue({
      token: 'fake-jwt-token',
      userId: 1,
      message: 'Login successful',
    });
    
    render(<LoginPage />);
    
    await user.type(screen.getByLabelText(/email/i), 'user@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /sign in/i }));
    
    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({
        email: 'user@example.com',
        password: 'password123',
      });
    });
  });

  it('handles login error', async () => {
    const user = userEvent.setup();
    vi.spyOn(authApi, 'login').mockRejectedValue({
      response: { data: { message: 'Invalid credentials' } },
    });
    
    render(<LoginPage />);
    
    await user.type(screen.getByLabelText(/email/i), 'wrong@example.com');
    await user.type(screen.getByLabelText(/password/i), 'wrongpass');
    await user.click(screen.getByRole('button', { name: /sign in/i }));
    
    await waitFor(() => {
      expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
    });
  });

  it('toggles password visibility', async () => {
    const user = userEvent.setup();
    render(<LoginPage />);
    
    const passwordInput = screen.getByLabelText(/password/i);
    expect(passwordInput).toHaveAttribute('type', 'password');
    
    const toggleButton = screen.getByRole('button', { name: '' });
    await user.click(toggleButton);
    
    expect(passwordInput).toHaveAttribute('type', 'text');
  });

  it('shows loading state during login', async () => {
    const user = userEvent.setup();
    vi.spyOn(authApi, 'login').mockImplementation(
      () => new Promise(resolve => setTimeout(resolve, 1000))
    );
    
    render(<LoginPage />);
    
    await user.type(screen.getByLabelText(/email/i), 'user@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /sign in/i }));
    
    expect(screen.getByRole('button', { name: /sign in/i })).toBeDisabled();
  });

  it('has link to signup page', () => {
    render(<LoginPage />);
    expect(screen.getByText(/create one for free/i)).toBeInTheDocument();
  });

  it('has link to forgot password', () => {
    render(<LoginPage />);
    expect(screen.getByText(/forgot password/i)).toBeInTheDocument();
  });
});
