import React, { useState, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Mail, KeyRound, ArrowRight, Lock, Eye, EyeOff } from 'lucide-react';
import { forgotPassword, verifyOtp, resetPassword } from '../api/auth';
import { Input } from '../components/Input';
import { Button } from '../components/Button';
import './ForgotPasswordPage.css';

export function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState('email');
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const inputRefs = useRef([]);

  const handleSendOtp = async (e) => {
    e.preventDefault();
    if (!email) { setError('Please enter your email address.'); return; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { setError('Please enter a valid email.'); return; }
    setLoading(true);
    setError('');
    try {
      const res = await forgotPassword(email.trim().toLowerCase());
      if (res.message.toLowerCase().includes('already verified')) {
        setError('Email already verified. Please use the login page or contact support.');
      } else {
        setSuccess('OTP sent! Check your email inbox.');
        setStep('otp');
        setTimeout(() => { setSuccess(''); inputRefs.current[0]?.focus(); }, 500);
      }
    } catch (err) {
      const msg = err?.response?.data?.message;
      setError(msg || 'Failed to send OTP. Please check your email and try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleDigit = (index, value) => {
    if (!/^\d?$/.test(value)) return;
    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);
    setError('');
    if (value && index < 5) inputRefs.current[index + 1]?.focus();
  };

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) inputRefs.current[index - 1]?.focus();
  };

  const handlePaste = (e) => {
    const text = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    if (text.length === 6) { setOtp(text.split('')); inputRefs.current[5]?.focus(); }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    const code = otp.join('');
    if (code.length < 6) { setError('Please enter the complete 6-digit OTP.'); return; }
    setLoading(true);
    setError('');
    try {
      await verifyOtp({ email: email.trim().toLowerCase(), otp: code });
      setSuccess('OTP verified! Now set your new password.');
      setStep('password');
      setTimeout(() => setSuccess(''), 500);
    } catch (err) {
      const msg = err?.response?.data?.message;
      setError(msg || 'Invalid OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError('');
    
    if (!newPassword || !confirmPassword) {
      setError('Please fill in both password fields.');
      return;
    }
    
    if (newPassword.length < 8) {
      setError('Password must be at least 8 characters long.');
      return;
    }
    
    if (newPassword !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }
    
    setLoading(true);
    try {
      await resetPassword(email.trim().toLowerCase(), newPassword);
      setSuccess('Password reset successful! Redirecting to login...');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      const msg = err?.response?.data?.message;
      setError(msg || 'Failed to reset password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="forgot-password-container hero-bg">
      <div className="forgot-password-content fade-in-up">
        <div className="forgot-password-header">
          <div className="forgot-password-icon-wrapper">
            <Lock className="forgot-password-icon" />
          </div>
          <h1 className="forgot-password-title">
            {step === 'email' ? 'Forgot Password?' : step === 'otp' ? 'Verify Your Identity' : 'Set New Password'}
          </h1>
          <p className="forgot-password-subtitle">
            {step === 'email'
              ? 'Enter your registered email to receive an OTP.'
              : step === 'otp'
              ? `Enter the OTP sent to ${email}`
              : 'Create a strong password for your account'}
          </p>
        </div>

        <div className="forgot-password-card glass">
          <div className="steps-indicator">
            <div className={`step-bar ${step === 'email' ? 'email' : 'email-done'}`} />
            <div className={`step-bar ${step === 'otp' ? 'otp' : step === 'password' ? 'otp-done' : 'inactive'}`} />
            <div className={`step-bar ${step === 'password' ? 'password' : 'inactive'}`} />
          </div>

          {error && (
            <div className="error-message fade-in">
              ⚠ {error}
            </div>
          )}
          {success && (
            <div className="error-message fade-in" style={{ backgroundColor: 'rgba(16, 185, 129, 0.1)', borderColor: 'rgba(16, 185, 129, 0.3)', color: '#34d399' }}>
              ✓ {success}
            </div>
          )}

          {step === 'email' ? (
            <form onSubmit={handleSendOtp} className="forgot-form">
              <Input
                label="Registered Email"
                name="email"
                type="email"
                value={email}
                onChange={(e) => { setEmail(e.target.value); setError(''); }}
                placeholder="you@example.com"
                icon={<Mail className="w-4 h-4" />}
                autoComplete="email"
                id="forgot-email"
              />
              <Button
                type="submit"
                fullWidth
                loading={loading}
                size="lg"
                icon={<ArrowRight className="w-5 h-5" />}
                id="send-otp-btn"
              >
                Send Reset OTP
              </Button>
            </form>
          ) : step === 'otp' ? (
            <form onSubmit={handleVerifyOtp} className="forgot-form">
              <div>
                <div className="otp-label-wrapper">
                  <KeyRound className="otp-icon" />
                  <p className="otp-label">Enter 6-digit OTP</p>
                </div>
                <div className="otp-inputs" onPaste={handlePaste}>
                  {otp.map((digit, i) => (
                    <input
                      key={i}
                      ref={(el) => { inputRefs.current[i] = el; }}
                      type="text"
                      inputMode="numeric"
                      maxLength={1}
                      value={digit}
                      id={`forgot-otp-${i}`}
                      onChange={(e) => handleDigit(i, e.target.value)}
                      onKeyDown={(e) => handleKeyDown(i, e)}
                      className={`otp-input ${digit ? 'filled' : 'empty'}`}
                    />
                  ))}
                </div>
              </div>
              <Button
                type="submit"
                fullWidth
                loading={loading}
                size="lg"
                icon={<ArrowRight className="w-5 h-5" />}
                id="verify-reset-otp-btn"
              >
                Verify OTP
              </Button>
              <button
                type="button"
                onClick={() => { setStep('email'); setOtp(['','','','','','']); setError(''); }}
                className="change-email-btn"
              >
                ← Change email
              </button>
            </form>
          ) : (
            <form onSubmit={handleResetPassword} className="forgot-form">
              <div className="password-input-wrapper">
                <Input
                  label="New Password"
                  name="newPassword"
                  type={showPassword ? 'text' : 'password'}
                  value={newPassword}
                  onChange={(e) => { setNewPassword(e.target.value); setError(''); }}
                  placeholder="Enter new password"
                  icon={<Lock className="w-4 h-4" />}
                  autoComplete="new-password"
                  id="new-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="password-toggle-btn"
                >
                  {showPassword ? <EyeOff className="password-toggle-icon" /> : <Eye className="password-toggle-icon" />}
                </button>
              </div>
              <div className="password-input-wrapper">
                <Input
                  label="Confirm New Password"
                  name="confirmPassword"
                  type={showConfirmPassword ? 'text' : 'password'}
                  value={confirmPassword}
                  onChange={(e) => { setConfirmPassword(e.target.value); setError(''); }}
                  placeholder="Re-enter new password"
                  icon={<Lock className="w-4 h-4" />}
                  autoComplete="new-password"
                  id="confirm-password"
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="password-toggle-btn"
                >
                  {showConfirmPassword ? <EyeOff className="password-toggle-icon" /> : <Eye className="password-toggle-icon" />}
                </button>
              </div>
              <Button
                type="submit"
                fullWidth
                loading={loading}
                size="lg"
                icon={<ArrowRight className="w-5 h-5" />}
                id="reset-password-btn"
              >
                Reset Password
              </Button>
            </form>
          )}

          <p className="footer-text">
            Remember your password?{' '}
            <Link to="/login" className="footer-link">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
