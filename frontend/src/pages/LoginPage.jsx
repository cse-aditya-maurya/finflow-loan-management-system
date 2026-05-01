import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, Eye, EyeOff, ArrowRight, AlertCircle, TrendingUp, Shield, Zap } from 'lucide-react';
import { login as loginApi } from '../api/auth';
import { useAuth } from '../context/AuthContext';
import { parseRole } from '../context/AuthContext';
import { Toast } from '../components/Toast';

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({ email: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.email || !form.password) { setError('Please fill in all fields.'); return; }
    setLoading(true);
    setError('');
    try {
      const res = await loginApi(form);
      console.log('Login API response:', res);
      if (res.token) {
        const role = parseRole(res.token);
        await login(res.token, res.userId ?? 0, role);
        setToast({ message: 'Login successful! Redirecting...', type: 'success' });
        setTimeout(() => navigate(role === 'ADMIN' ? '/admin/dashboard' : '/dashboard'), 1000);
      } else {
        setError(res.message || 'Login failed. Please try again.');
      }
    } catch (err) {
      const msg = err?.response?.data?.message;
      const errorMsg = msg || 'Unable to connect to server. Please ensure backend is running.';
      setError(errorMsg);
      setToast({ message: errorMsg, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">
      {toast && <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />}

      {/* Left Panel */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-blue-600 via-blue-700 to-indigo-800 flex-col justify-between p-12 relative overflow-hidden">
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-20 left-20 w-64 h-64 rounded-full bg-white blur-3xl" />
          <div className="absolute bottom-20 right-20 w-48 h-48 rounded-full bg-indigo-300 blur-3xl" />
        </div>
        <div className="relative z-10">
          <div className="flex items-center gap-3 mb-16">
            <div className="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center">
              <span className="text-white font-black text-lg">FF</span>
            </div>
            <span className="text-white font-bold text-xl">FinFlow</span>
          </div>
          <h2 className="text-4xl font-bold text-white leading-tight mb-4">
            Smart loans for<br />smarter people
          </h2>
          <p className="text-blue-100 text-lg">Get instant access to funds with competitive rates and flexible terms.</p>
        </div>
        <div className="relative z-10 space-y-4">
          {[
            { icon: <TrendingUp className="w-5 h-5" />, text: 'Competitive interest rates from 3.5%' },
            { icon: <Zap className="w-5 h-5" />, text: 'Instant approval in minutes' },
            { icon: <Shield className="w-5 h-5" />, text: 'Bank-grade security & encryption' },
          ].map((item, i) => (
            <div key={i} className="flex items-center gap-3 text-blue-100">
              <div className="w-8 h-8 rounded-lg bg-white/15 flex items-center justify-center flex-shrink-0">{item.icon}</div>
              <span className="text-sm">{item.text}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Right Panel */}
      <div className="w-full lg:w-1/2 flex items-center justify-center bg-white p-8">
        <div className="w-full max-w-md">
          {/* Mobile logo */}
          <div className="flex items-center gap-2 mb-8 lg:hidden">
            <div className="w-9 h-9 rounded-xl bg-blue-600 flex items-center justify-center">
              <span className="text-white font-black">FF</span>
            </div>
            <span className="text-gray-900 font-bold text-lg">FinFlow</span>
          </div>

          <h1 className="text-3xl font-bold text-gray-900 mb-2">Welcome back</h1>
          <p className="text-gray-500 mb-8">Sign in to your account to continue</p>

          {error && (
            <div className="mb-6 p-4 rounded-xl bg-red-50 border border-red-200 text-red-700 text-sm flex items-start gap-3">
              <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5 text-red-500" />
              <div>
                <p className="font-semibold">Login Failed</p>
                <p className="text-red-600 text-xs mt-0.5">{error}</p>
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input
                  name="email" type="email" value={form.email} onChange={handleChange}
                  placeholder="you@example.com" autoComplete="email" id="login-email"
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl text-gray-900 placeholder-gray-400 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all bg-gray-50 hover:bg-white"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">Password</label>
              <div className="relative">
                <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input
                  name="password" type={showPassword ? 'text' : 'password'} value={form.password}
                  onChange={handleChange} placeholder="Your password" autoComplete="current-password" id="login-password"
                  className="w-full pl-10 pr-11 py-3 border border-gray-300 rounded-xl text-gray-900 placeholder-gray-400 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all bg-gray-50 hover:bg-white"
                />
                <button type="button" onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors">
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <div className="flex justify-end">
              <Link to="/forgot-password" id="forgot-password-link" className="text-sm text-blue-600 hover:text-blue-700 font-medium transition-colors">
                Forgot password?
              </Link>
            </div>

            <button type="submit" disabled={loading} id="login-submit"
              className="w-full flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-semibold py-3 px-6 rounded-xl transition-all duration-200 text-sm shadow-lg shadow-blue-500/25 hover:shadow-blue-500/40">
              {loading ? (
                <svg className="animate-spin w-5 h-5" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                </svg>
              ) : (
                <><span>Sign In</span><ArrowRight className="w-4 h-4" /></>
              )}
            </button>
          </form>

          <p className="text-center text-gray-500 text-sm mt-8">
            Don't have an account?{' '}
            <Link to="/signup" id="go-to-signup" className="text-blue-600 hover:text-blue-700 font-semibold transition-colors">
              Create one for free
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
