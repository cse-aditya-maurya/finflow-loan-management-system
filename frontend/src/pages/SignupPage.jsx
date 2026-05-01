import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Mail, Lock, Eye, EyeOff, ArrowRight, CheckCircle, AlertCircle, TrendingUp, Shield, Zap } from 'lucide-react';
import { signup } from '../api/auth';
import { Toast } from '../components/Toast';

export function SignupPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', password: '', confirmPassword: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [globalError, setGlobalError] = useState('');
  const [toast, setToast] = useState(null);

  const validate = () => {
    const e = {};
    if (!form.name.trim()) e.name = 'Full name is required';
    if (!form.email) e.email = 'Email is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = 'Enter a valid email';
    if (!form.password) e.password = 'Password is required';
    else if (form.password.length < 6) e.password = 'Password must be at least 6 characters';
    if (form.password !== form.confirmPassword) e.confirmPassword = 'Passwords do not match';
    return e;
  };

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setErrors((prev) => ({ ...prev, [e.target.name]: '' }));
    setGlobalError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setLoading(true);
    try {
      const res = await signup({ name: form.name.trim(), email: form.email.trim().toLowerCase(), password: form.password });
      setToast({ message: 'Account created! Please verify your email.', type: 'success' });
      setTimeout(() => navigate('/verify-otp', { state: { email: form.email.trim().toLowerCase(), userId: res.userId, message: res.message } }), 1000);
    } catch (err) {
      const msg = err?.response?.data?.message;
      const errorMsg = msg || 'Unable to connect to server. Please ensure backend is running.';
      setGlobalError(errorMsg);
      setToast({ message: errorMsg, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const strength = (() => {
    const p = form.password;
    if (!p) return null;
    if (p.length < 6) return { label: 'Weak', color: 'bg-red-500', width: '33%' };
    if (p.length < 10) return { label: 'Fair', color: 'bg-amber-500', width: '66%' };
    return { label: 'Strong', color: 'bg-emerald-500', width: '100%' };
  })();

  const inputClass = (field) =>
    `w-full py-3 border rounded-xl text-gray-900 placeholder-gray-400 text-sm focus:outline-none focus:ring-2 focus:border-transparent transition-all bg-gray-50 hover:bg-white ${
      errors[field] ? 'border-red-400 focus:ring-red-400' : 'border-gray-300 focus:ring-blue-500'
    }`;

  return (
    <div className="min-h-screen flex">
      {toast && <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />}

      {/* Left Panel */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-indigo-600 via-blue-700 to-blue-800 flex-col justify-between p-12 relative overflow-hidden">
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-20 left-20 w-64 h-64 rounded-full bg-white blur-3xl" />
          <div className="absolute bottom-20 right-20 w-48 h-48 rounded-full bg-blue-300 blur-3xl" />
        </div>
        <div className="relative z-10">
          <div className="flex items-center gap-3 mb-16">
            <div className="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center">
              <span className="text-white font-black text-lg">FF</span>
            </div>
            <span className="text-white font-bold text-xl">FinFlow</span>
          </div>
          <h2 className="text-4xl font-bold text-white leading-tight mb-4">
            Your financial<br />journey starts here
          </h2>
          <p className="text-blue-100 text-lg">Join thousands who trust FinFlow for fast, transparent lending.</p>
        </div>
        <div className="relative z-10 space-y-4">
          {[
            { icon: <TrendingUp className="w-5 h-5" />, text: 'Loans from $1,000 to $100,000' },
            { icon: <Zap className="w-5 h-5" />, text: 'Funds deposited within 24 hours' },
            { icon: <Shield className="w-5 h-5" />, text: 'No hidden fees, ever' },
          ].map((item, i) => (
            <div key={i} className="flex items-center gap-3 text-blue-100">
              <div className="w-8 h-8 rounded-lg bg-white/15 flex items-center justify-center flex-shrink-0">{item.icon}</div>
              <span className="text-sm">{item.text}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Right Panel */}
      <div className="w-full lg:w-1/2 flex items-center justify-center bg-white p-8 overflow-y-auto">
        <div className="w-full max-w-md py-8">
          {/* Mobile logo */}
          <div className="flex items-center gap-2 mb-8 lg:hidden">
            <div className="w-9 h-9 rounded-xl bg-blue-600 flex items-center justify-center">
              <span className="text-white font-black">FF</span>
            </div>
            <span className="text-gray-900 font-bold text-lg">FinFlow</span>
          </div>

          <h1 className="text-3xl font-bold text-gray-900 mb-2">Create your account</h1>
          <p className="text-gray-500 mb-8">Join FinFlow and get your loan today</p>

          {globalError && (
            <div className="mb-6 p-4 rounded-xl bg-red-50 border border-red-200 text-red-700 text-sm flex items-start gap-3">
              <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5 text-red-500" />
              <div>
                <p className="font-semibold">Registration Failed</p>
                <p className="text-red-600 text-xs mt-0.5">{globalError}</p>
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Full Name */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">Full Name</label>
              <div className="relative">
                <User className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input name="name" type="text" value={form.name} onChange={handleChange}
                  placeholder="John Doe" autoComplete="name" id="signup-name"
                  className={`${inputClass('name')} pl-10 pr-4`} />
              </div>
              {errors.name && <p className="text-xs text-red-500 mt-1 flex items-center gap-1"><AlertCircle className="w-3 h-3" />{errors.name}</p>}
            </div>

            {/* Email */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input name="email" type="email" value={form.email} onChange={handleChange}
                  placeholder="you@example.com" autoComplete="email" id="signup-email"
                  className={`${inputClass('email')} pl-10 pr-4`} />
              </div>
              {errors.email && <p className="text-xs text-red-500 mt-1 flex items-center gap-1"><AlertCircle className="w-3 h-3" />{errors.email}</p>}
            </div>

            {/* Password */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">Password</label>
              <div className="relative">
                <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input name="password" type={showPassword ? 'text' : 'password'} value={form.password}
                  onChange={handleChange} placeholder="Min. 6 characters" autoComplete="new-password" id="signup-password"
                  className={`${inputClass('password')} pl-10 pr-11`} />
                <button type="button" onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors">
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              {errors.password && <p className="text-xs text-red-500 mt-1 flex items-center gap-1"><AlertCircle className="w-3 h-3" />{errors.password}</p>}
              {strength && (
                <div className="mt-2">
                  <div className="h-1.5 bg-gray-200 rounded-full overflow-hidden">
                    <div className={`h-full rounded-full transition-all duration-500 ${strength.color}`} style={{ width: strength.width }} />
                  </div>
                  <p className="text-xs text-gray-500 mt-1">Strength: <span className="font-medium text-gray-700">{strength.label}</span></p>
                </div>
              )}
            </div>

            {/* Confirm Password */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">Confirm Password</label>
              <div className="relative">
                <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input name="confirmPassword" type="password" value={form.confirmPassword}
                  onChange={handleChange} placeholder="Re-enter password" autoComplete="new-password" id="signup-confirm-password"
                  className={`${inputClass('confirmPassword')} pl-10 pr-11`} />
                {form.confirmPassword && form.password === form.confirmPassword && (
                  <CheckCircle className="absolute right-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-emerald-500" />
                )}
              </div>
              {errors.confirmPassword && <p className="text-xs text-red-500 mt-1 flex items-center gap-1"><AlertCircle className="w-3 h-3" />{errors.confirmPassword}</p>}
            </div>

            <button type="submit" disabled={loading} id="signup-submit"
              className="w-full flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-semibold py-3 px-6 rounded-xl transition-all duration-200 text-sm shadow-lg shadow-blue-500/25 hover:shadow-blue-500/40 mt-2">
              {loading ? (
                <svg className="animate-spin w-5 h-5" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                </svg>
              ) : (
                <><span>Create Account</span><ArrowRight className="w-4 h-4" /></>
              )}
            </button>
          </form>

          <p className="text-center text-gray-500 text-sm mt-8">
            Already have an account?{' '}
            <Link to="/login" id="go-to-login" className="text-blue-600 hover:text-blue-700 font-semibold transition-colors">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
