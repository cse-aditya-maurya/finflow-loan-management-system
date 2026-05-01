import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LayoutDashboard, FileText, Users, BarChart3, PlusCircle, LogOut, Menu, X, ChevronDown, Sun, Moon } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';

export function Navbar() {
  const { user, isLoggedIn, isAdmin, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
    setIsMobileMenuOpen(false);
  };

  const isActive = (path) => location.pathname === path;

  const userName = user?.profile?.name || user?.profile?.email?.split('@')[0] || user?.email?.split('@')[0] || 'User';

  const userLinks = [
    { path: '/dashboard', label: 'Dashboard', icon: <LayoutDashboard className="w-4 h-4" /> },
    { path: '/apply', label: 'Apply Loan', icon: <PlusCircle className="w-4 h-4" /> },
    { path: '/my-applications', label: 'My Applications', icon: <FileText className="w-4 h-4" /> },
  ];

  const adminLinks = [
    { path: '/admin/dashboard', label: 'Dashboard', icon: <LayoutDashboard className="w-4 h-4" /> },
    { path: '/admin/applications', label: 'Applications', icon: <FileText className="w-4 h-4" /> },
    { path: '/admin/users', label: 'Users', icon: <Users className="w-4 h-4" /> },
    { path: '/admin/reports', label: 'Reports', icon: <BarChart3 className="w-4 h-4" /> },
  ];

  const links = isAdmin ? adminLinks : userLinks;

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 glass border-b border-slate-700/40">
      <div className="max-w-7xl mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-3 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center shadow-lg shadow-blue-500/30 group-hover:shadow-blue-500/50 transition-all">
              <span className="text-white font-black text-lg">FF</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-white font-bold text-xl">FinFlow</span>
              {isAdmin && (
                <span className="px-2 py-0.5 text-xs font-bold bg-blue-500/20 text-blue-400 border border-blue-500/30 rounded-md">
                  ADMIN
                </span>
              )}
            </div>
          </Link>

          {/* Desktop Navigation */}
          {isLoggedIn && (
            <div className="hidden md:flex items-center gap-2">
              {links.map((link) => (
                <Link
                  key={link.path}
                  to={link.path}
                  onClick={() => setIsMobileMenuOpen(false)}
                  className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                    isActive(link.path)
                      ? 'bg-blue-500/20 text-blue-400 border border-blue-500/30'
                      : 'text-slate-300 hover:text-white hover:bg-slate-800/50'
                  }`}
                >
                  {link.icon}
                  <span>{link.label}</span>
                </Link>
              ))}
            </div>
          )}

          {/* Right Side */}
          <div className="hidden md:flex items-center gap-3">
            {/* Theme Toggle */}
            <button
              onClick={toggleTheme}
              title={theme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
              className="p-2 rounded-lg text-slate-300 hover:text-white hover:bg-slate-800/50 transition-all"
            >
              {theme === 'dark' ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
            </button>

            {!isLoggedIn ? (
              <>
                <Link
                  to="/login"
                  className="px-4 py-2 text-sm font-medium text-slate-300 hover:text-white transition-colors"
                >
                  Sign In
                </Link>
                <Link
                  to="/signup"
                  className="px-5 py-2 text-sm font-semibold bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-500 hover:to-blue-400 text-white rounded-lg transition-all shadow-lg shadow-blue-500/25 hover:shadow-blue-500/40"
                >
                  Get Started
                </Link>
              </>
            ) : (
              <div className="relative">
                <button
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  className="flex items-center gap-3 px-3 py-2 rounded-lg bg-slate-800/50 border border-slate-700/40 hover:border-slate-600 transition-all"
                >
                  <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center text-white font-bold text-sm">
                    {userName[0].toUpperCase()}
                  </div>
                  <span className="text-sm font-medium text-white max-w-[120px] truncate">{userName}</span>
                  <ChevronDown className="w-4 h-4 text-slate-400" />
                </button>

                {showUserMenu && (
                  <>
                    <div className="fixed inset-0 z-40" onClick={() => setShowUserMenu(false)} />
                    <div className="absolute right-0 mt-2 w-56 glass rounded-xl border border-slate-700/40 shadow-2xl z-50">
                      <div className="p-4 border-b border-slate-700/40">
                        <p className="text-xs text-slate-500 uppercase tracking-wide">Signed in as</p>
                        <p className="text-sm font-semibold text-white mt-1 truncate">{userName}</p>
                        {user?.email && (
                          <p className="text-xs text-slate-400 mt-0.5 truncate">{user.email}</p>
                        )}
                      </div>
                      <button
                        onClick={handleLogout}
                        className="w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-red-400 hover:bg-red-500/10 transition-colors"
                      >
                        <LogOut className="w-4 h-4" />
                        <span>Sign Out</span>
                      </button>
                    </div>
                  </>
                )}
              </div>
            )}
          </div>

          {/* Mobile Menu Button */}
          <button
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            className="md:hidden p-2 rounded-lg text-slate-300 hover:text-white hover:bg-slate-800/50 transition-all"
          >
            {isMobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>

        {/* Mobile Menu */}
        {isMobileMenuOpen && (
          <div className="md:hidden py-4 border-t border-slate-700/40">
            {!isLoggedIn ? (
              <div className="space-y-2">
                <button
                  onClick={toggleTheme}
                  className="w-full flex items-center gap-3 px-4 py-2 text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800/50 rounded-lg transition-all"
                >
                  {theme === 'dark' ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
                  <span>{theme === 'dark' ? 'Light Mode' : 'Dark Mode'}</span>
                </button>
                <Link
                  to="/login"
                  onClick={() => setIsMobileMenuOpen(false)}
                  className="block px-4 py-2 text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800/50 rounded-lg transition-all"
                >
                  Sign In
                </Link>
                <Link
                  to="/signup"
                  onClick={() => setIsMobileMenuOpen(false)}
                  className="block px-4 py-2 text-sm font-semibold bg-gradient-to-r from-blue-600 to-blue-500 text-white rounded-lg text-center"
                >
                  Get Started
                </Link>
              </div>
            ) : (
              <div className="space-y-1">
                <div className="px-4 py-3 mb-2 bg-slate-800/50 rounded-lg border border-slate-700/40">
                  <p className="text-xs text-slate-500 uppercase tracking-wide">Signed in as</p>
                  <p className="text-sm font-semibold text-white mt-1">{userName}</p>
                </div>
                {links.map((link) => (
                  <Link
                    key={link.path}
                    to={link.path}
                    onClick={() => setIsMobileMenuOpen(false)}
                    className={`flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-all ${
                      isActive(link.path)
                        ? 'bg-blue-500/20 text-blue-400 border border-blue-500/30'
                        : 'text-slate-300 hover:text-white hover:bg-slate-800/50'
                    }`}
                  >
                    {link.icon}
                    <span>{link.label}</span>
                  </Link>
                ))}
                <button
                  onClick={toggleTheme}
                  className="w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800/50 rounded-lg transition-all"
                >
                  {theme === 'dark' ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
                  <span>{theme === 'dark' ? 'Light Mode' : 'Dark Mode'}</span>
                </button>
                <button
                  onClick={handleLogout}
                  className="w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-red-400 hover:bg-red-500/10 rounded-lg transition-all mt-2"
                >
                  <LogOut className="w-4 h-4" />
                  <span>Sign Out</span>
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </nav>
  );
}
