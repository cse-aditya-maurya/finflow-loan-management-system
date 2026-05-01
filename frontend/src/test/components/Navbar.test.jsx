import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '../test-utils';
import userEvent from '@testing-library/user-event';
import { Navbar } from '../../components/Navbar';
import * as AuthContext from '../../context/AuthContext';
import * as ThemeContext from '../../context/ThemeContext';

vi.mock('../../context/AuthContext');
vi.mock('../../context/ThemeContext');

describe('Navbar', () => {
  beforeEach(() => {
    // Mock ThemeContext for all tests
    vi.spyOn(ThemeContext, 'useTheme').mockReturnValue({
      theme: 'dark',
      toggleTheme: vi.fn(),
      isDark: true,
      isLight: false,
    });
  });

  it('shows login/signup when not authenticated', () => {
    vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
      user: null,
      login: vi.fn(),
      logout: vi.fn(),
      isLoggedIn: false,
      isAdmin: false,
      loadingProfile: false,
    });

    render(<Navbar />);
    
    expect(screen.getByText(/sign in/i)).toBeInTheDocument();
    expect(screen.getByText(/get started/i)).toBeInTheDocument();
  });

  it('shows user menu when authenticated', () => {
    vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
      user: { email: 'user@test.com', role: 'USER', profile: { name: 'Test User', email: 'user@test.com' } },
      login: vi.fn(),
      logout: vi.fn(),
      isLoggedIn: true,
      isAdmin: false,
      loadingProfile: false,
    });

    render(<Navbar />);
    
    expect(screen.getByText(/dashboard/i)).toBeInTheDocument();
  });

  it('shows theme toggle button', () => {
    vi.spyOn(AuthContext, 'useAuth').mockReturnValue({
      user: null,
      login: vi.fn(),
      logout: vi.fn(),
      isLoggedIn: false,
      isAdmin: false,
      loadingProfile: false,
    });

    render(<Navbar />);
    
    // Theme toggle button should be present
    const themeButton = screen.getByTitle(/switch to light mode/i);
    expect(themeButton).toBeInTheDocument();
  });
});
