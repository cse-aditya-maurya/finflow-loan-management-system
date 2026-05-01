import React, { useState, useRef, useEffect } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { verifyOtp, resendOtp } from '../api/auth';
import { useAuth } from '../context/AuthContext';
import { parseRole } from '../context/AuthContext';
import { Button } from '../components/Button';
import { ShieldCheck, RefreshCw } from 'lucide-react';
import './VerifyOtpPage.css';

export function VerifyOtpPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { login } = useAuth();

  const state = location.state;
  const email = state?.email || '';

  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [countdown, setCountdown] = useState(60);
  const inputRefs = useRef([]);

  useEffect(() => {
    if (!email) { navigate('/signup'); return; }
    inputRefs.current[0]?.focus();
  }, [email, navigate]);

  useEffect(() => {
    if (countdown <= 0) return;
    const t = setTimeout(() => setCountdown((c) => c - 1), 1000);
    return () => clearTimeout(t);
  }, [countdown]);

  const handleDigit = (index, value) => {
    if (!/^\d?$/.test(value)) return;
    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);
    setError('');
    if (value && index < 5) inputRefs.current[index + 1]?.focus();
    if (!value && index > 0) inputRefs.current[index - 1]?.focus();
  };

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e) => {
    const text = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    if (text.length === 6) {
      setOtp(text.split(''));
      inputRefs.current[5]?.focus();
    }
  };

  const handleVerify = async () => {
    const code = otp.join('');
    if (code.length < 6) { setError('Please enter the complete 6-digit OTP.'); return; }
    setLoading(true);
    setError('');
    try {
      const res = await verifyOtp({ email, otp: code });
      if (res.token) {
        const role = parseRole(res.token);
        setSuccess('OTP verified! Redirecting...');
        await login(res.token, res.userId ?? 0, role);
        setTimeout(() => navigate('/dashboard'), 1000);
      } else {
        setError(res.message || 'Verification failed');
      }
    } catch (err) {
      const msg = err?.response?.data?.message;
      setError(msg || 'Invalid OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setResending(true);
    setError('');
    try {
      await resendOtp({ email });
      setCountdown(60);
      setOtp(['', '', '', '', '', '']);
      inputRefs.current[0]?.focus();
      setSuccess('New OTP sent to your email!');
      setTimeout(() => setSuccess(''), 3000);
    } catch {
      setError('Failed to resend OTP. Please try again.');
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="verify-otp-container hero-bg">
      <div className="verify-otp-content fade-in-up">
        <div className="verify-otp-header">
          <div className="verify-otp-icon-wrapper">
            <ShieldCheck className="verify-otp-icon" />
          </div>
          <h1 className="verify-otp-title">Verify Your Email</h1>
          <p className="verify-otp-subtitle">
            We sent a 6-digit code to{' '}
            <span className="email-highlight">{email}</span>
          </p>
        </div>

        <div className="verify-otp-card glass">
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

          <div className="otp-inputs-wrapper" onPaste={handlePaste}>
            {otp.map((digit, i) => (
              <input
                key={i}
                ref={(el) => { inputRefs.current[i] = el; }}
                type="text"
                inputMode="numeric"
                maxLength={1}
                value={digit}
                id={`otp-digit-${i}`}
                onChange={(e) => handleDigit(i, e.target.value)}
                onKeyDown={(e) => handleKeyDown(i, e)}
                className={`otp-digit-input ${digit ? 'filled' : 'empty'}`}
              />
            ))}
          </div>

          <Button
            onClick={handleVerify}
            fullWidth
            loading={loading}
            size="lg"
            id="otp-verify-btn"
          >
            Verify OTP
          </Button>

          <div className="resend-section">
            {countdown > 0 ? (
              <p className="countdown-text">
                Resend OTP in <span className="countdown-timer">{countdown}s</span>
              </p>
            ) : (
              <button
                id="resend-otp-btn"
                onClick={handleResend}
                disabled={resending}
                className="resend-btn"
              >
                <RefreshCw className={`resend-icon ${resending ? 'spinning' : ''}`} />
                {resending ? 'Sending...' : 'Resend OTP'}
              </button>
            )}
          </div>

          <p className="back-link">
            <Link to="/signup">
              ← Back to signup
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
