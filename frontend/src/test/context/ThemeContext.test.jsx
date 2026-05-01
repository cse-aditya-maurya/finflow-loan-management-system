import { describe, it, expect, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { ThemeProvider, useTheme } from '../../context/ThemeContext';

describe('ThemeContext', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('provides default dark theme', () => {
    const { result } = renderHook(() => useTheme(), {
      wrapper: ThemeProvider,
    });

    expect(result.current.theme).toBe('dark');
    expect(result.current.isDark).toBe(true);
    expect(result.current.isLight).toBe(false);
  });

  it('toggles theme from dark to light', () => {
    const { result } = renderHook(() => useTheme(), {
      wrapper: ThemeProvider,
    });

    act(() => {
      result.current.toggleTheme();
    });

    expect(result.current.theme).toBe('light');
    expect(result.current.isDark).toBe(false);
    expect(result.current.isLight).toBe(true);
  });

  it('toggles theme from light to dark', () => {
    localStorage.setItem('finflow_theme', 'light');

    const { result } = renderHook(() => useTheme(), {
      wrapper: ThemeProvider,
    });

    expect(result.current.theme).toBe('light');

    act(() => {
      result.current.toggleTheme();
    });

    expect(result.current.theme).toBe('dark');
  });

  it('persists theme to localStorage', () => {
    const { result } = renderHook(() => useTheme(), {
      wrapper: ThemeProvider,
    });

    act(() => {
      result.current.toggleTheme();
    });

    expect(localStorage.getItem('finflow_theme')).toBe('light');
  });

  it('restores theme from localStorage', () => {
    localStorage.setItem('finflow_theme', 'light');

    const { result } = renderHook(() => useTheme(), {
      wrapper: ThemeProvider,
    });

    expect(result.current.theme).toBe('light');
    expect(result.current.isLight).toBe(true);
  });
});
