import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { getUserProfile, getUserByEmail, getCurrentUser } from '../api/auth';

const AuthContext = createContext(null);

function parseRole(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role || payload.Role || '';
  } catch {
    return '';
  }
}

function parseEmail(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.sub || payload.email || '';
  } catch {
    return '';
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loadingProfile, setLoadingProfile] = useState(false);
  const [initializing, setInitializing] = useState(true);

  // Restore session on mount
  useEffect(() => {
    const token = localStorage.getItem('finflow_token');
    const storedUser = localStorage.getItem('finflow_user');
    
    if (token && storedUser) {
      try {
        const parsed = JSON.parse(storedUser);
        setUser({ ...parsed, token });
      } catch {
        localStorage.removeItem('finflow_token');
        localStorage.removeItem('finflow_user');
      }
    }
    setInitializing(false);
  }, []);

  const login = useCallback(async (token, userId, role) => {
    const email = parseEmail(token);
    const authUser = { token, userId, role, email };
    setUser(authUser);
    localStorage.setItem('finflow_token', token);

    // Fetch profile - try multiple endpoints
    setLoadingProfile(true);
    try {
      let profile;
      
      // 1. Try /auth/me (most reliable - uses token)
      try {
        profile = await getCurrentUser();
        console.log('Profile from /auth/me:', profile);
      } catch (e1) {
        console.log('/auth/me failed, trying email endpoint');
        
        // 2. Try by email
        if (email) {
          try {
            profile = await getUserByEmail(email);
            console.log('Profile from email:', profile);
          } catch (e2) {
            console.log('Email endpoint failed, trying userId');
            
            // 3. Try by userId
            if (userId && userId !== 0) {
              try {
                profile = await getUserProfile(userId);
                console.log('Profile from userId:', profile);
              } catch (e3) {
                console.log('All endpoints failed');
              }
            }
          }
        }
      }
      
      // Fallback: create profile from token email
      if (!profile || !profile.name) {
        profile = { 
          email: email || 'user@example.com', 
          name: email ? email.split('@')[0] : 'User' 
        };
        console.log('Using fallback profile:', profile);
      }
      
      console.log('Final user profile:', profile);
      const fullUser = { ...authUser, profile };
      setUser(fullUser);
      localStorage.setItem('finflow_user', JSON.stringify(fullUser));
    } catch (err) {
      console.error('Failed to fetch user profile:', err);
      // Fallback: use email from token
      const fallbackProfile = { 
        email: email || 'user@example.com', 
        name: email ? email.split('@')[0] : 'User' 
      };
      const fullUser = { ...authUser, profile: fallbackProfile };
      setUser(fullUser);
      localStorage.setItem('finflow_user', JSON.stringify(fullUser));
    } finally {
      setLoadingProfile(false);
    }
  }, []);

  const logout = useCallback(() => {
    setUser(null);
    localStorage.removeItem('finflow_token');
    localStorage.removeItem('finflow_user');
  }, []);

  const value = {
    user,
    isLoggedIn: !!user,
    isAdmin: user?.role === 'ADMIN',
    login,
    logout,
    loadingProfile,
  };

  // Show nothing while checking localStorage
  if (initializing) {
    return (
      <div className="min-h-screen hero-bg flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-amber-500/30 border-t-amber-500 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-slate-400">Loading...</p>
        </div>
      </div>
    );
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

export { parseRole, parseEmail };
