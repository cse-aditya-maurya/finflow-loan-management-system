import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '../test-utils';
import userEvent from '@testing-library/user-event';
import { SignupPage } from '../../pages/SignupPage';
import * as authApi from '../../api/auth';

vi.mock('../../api/auth');

describe('SignupPage Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders signup form', () => {
    render(<SignupPage />);
    
    expect(screen.getByLabelText(/full name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^password$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/confirm password/i)).toBeInTheDocument();
  });

  it('validates required fields', async () => {
    const user = userEvent.setup();
    render(<SignupPage />);
    
    await user.click(screen.getByRole('button', { name: /create account/i }));
    
    expect(screen.getByText(/full name is required/i)).toBeInTheDocument();
  });

  it('validates email format', async () => {
    const user = userEvent.setup();
    render(<SignupPage />);
    
    await user.type(screen.getByLabelText(/full name/i), 'John Doe');
    await user.type(screen.getByLabelText(/email address/i), 'invalid-email');
    await user.type(screen.getByLabelText(/^password$/i), 'password123');
    await user.type(screen.getByLabelText(/confirm password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /create account/i }));
    
    expect(screen.getByText(/enter a valid email/i)).toBeInTheDocument();
  });

  it('validates password length', async () => {
    const user = userEvent.setup();
    render(<SignupPage />);
    
    await user.type(screen.getByLabelText(/full name/i), 'John Doe');
    await user.type(screen.getByLabelText(/email address/i), 'john@example.com');
    await user.type(screen.getByLabelText(/^password$/i), '12345');
    await user.type(screen.getByLabelText(/confirm password/i), '12345');
    await user.click(screen.getByRole('button', { name: /create account/i }));
    
    expect(screen.getByText(/password must be at least 6 characters/i)).toBeInTheDocument();
  });

  it('validates password match', async () => {
    const user = userEvent.setup();
    render(<SignupPage />);
    
    await user.type(screen.getByLabelText(/full name/i), 'John Doe');
    await user.type(screen.getByLabelText(/email address/i), 'john@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'password123');
    await user.type(screen.getByLabelText(/confirm password/i), 'different123');
    await user.click(screen.getByRole('button', { name: /create account/i }));
    
    expect(screen.getByText(/passwords do not match/i)).toBeInTheDocument();
  });

  it('shows password strength indicator', async () => {
    const user = userEvent.setup();
    render(<SignupPage />);
    
    const passwordInput = screen.getByLabelText(/^password$/i);
    
    await user.type(passwordInput, '12345');
    expect(screen.getByText(/weak/i)).toBeInTheDocument();
    
    await user.clear(passwordInput);
    await user.type(passwordInput, '12345678');
    expect(screen.getByText(/fair/i)).toBeInTheDocument();
    
    await user.clear(passwordInput);
    await user.type(passwordInput, '1234567890');
    expect(screen.getByText(/strong/i)).toBeInTheDocument();
  });

  it('handles successful signup', async () => {
    const user = userEvent.setup();
    const mockSignup = vi.spyOn(authApi, 'signup').mockResolvedValue({
      userId: 1,
      message: 'OTP sent to email',
    });
    
    render(<SignupPage />);
    
    await user.type(screen.getByLabelText(/full name/i), 'John Doe');
    await user.type(screen.getByLabelText(/email address/i), 'john@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'password123');
    await user.type(screen.getByLabelText(/confirm password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /create account/i }));
    
    await waitFor(() => {
      expect(mockSignup).toHaveBeenCalledWith({
        name: 'John Doe',
        email: 'john@example.com',
        password: 'password123',
      });
    });
  });

  it('handles signup error', async () => {
    const user = userEvent.setup();
    vi.spyOn(authApi, 'signup').mockRejectedValue({
      response: { data: { message: 'Email already exists' } },
    });
    
    render(<SignupPage />);
    
    await user.type(screen.getByLabelText(/full name/i), 'John Doe');
    await user.type(screen.getByLabelText(/email address/i), 'existing@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'password123');
    await user.type(screen.getByLabelText(/confirm password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /create account/i }));
    
    await waitFor(() => {
      expect(screen.getByText(/email already exists/i)).toBeInTheDocument();
    });
  });

  it('has link to login page', () => {
    render(<SignupPage />);
    expect(screen.getByText(/sign in/i)).toBeInTheDocument();
  });
});
