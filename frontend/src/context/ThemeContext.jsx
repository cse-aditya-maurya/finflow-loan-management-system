import React, { createContext, useContext, useState, useEffect } from 'react';

const ThemeContext = createContext(null);

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(() => {
    // Get theme from localStorage or default to 'dark'
    const savedTheme = localStorage.getItem('finflow_theme');
    return savedTheme || 'dark';
  });

  useEffect(() => {
    // Apply theme to document root and body
    const root = document.documentElement;
    const body = document.body;
    
    if (theme === 'light') {
      root.classList.add('light-theme');
      root.classList.remove('dark-theme');
      body.setAttribute('data-theme', 'light');
    } else {
      root.classList.add('dark-theme');
      root.classList.remove('light-theme');
      body.setAttribute('data-theme', 'dark');
    }
    
    // Save to localStorage
    localStorage.setItem('finflow_theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme(prevTheme => prevTheme === 'dark' ? 'light' : 'dark');
  };

  const value = {
    theme,
    toggleTheme,
    isDark: theme === 'dark',
    isLight: theme === 'light',
  };

  return (
    <ThemeContext.Provider value={value}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within ThemeProvider');
  }
  return context;
}
