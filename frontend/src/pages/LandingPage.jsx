import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  ArrowRight, Shield, Zap, BarChart3, CheckCircle,
  Users, Clock, FileCheck, Menu, X
} from 'lucide-react';
import './LandingPage.css';

/* ── Data ── */
const features = [
  {
    icon: <Zap size={20} />,
    title: 'Instant Decisions',
    desc: 'AI-powered risk assessment delivers loan decisions in minutes, not days.',
    color: 'blue',
  },
  {
    icon: <Shield size={20} />,
    title: 'Bank-Grade Security',
    desc: 'End-to-end encryption and OTP verification to protect your data.',
    color: 'green',
  },
  {
    icon: <BarChart3 size={20} />,
    title: 'Real-time Tracking',
    desc: 'Follow your application status with live updates at every step.',
    color: 'amber',
  },
  {
    icon: <FileCheck size={20} />,
    title: 'Easy Documentation',
    desc: 'Upload documents digitally — no paperwork, no branch visits.',
    color: 'purple',
  },
  {
    icon: <Users size={20} />,
    title: 'Multiple Loan Types',
    desc: 'Home, education, business, vehicle, personal — all in one place.',
    color: 'cyan',
  },
  {
    icon: <Clock size={20} />,
    title: 'Quick Disbursement',
    desc: 'Once approved, funds are transferred directly to your account.',
    color: 'rose',
  },
];

const stats = [
  { value: '50K+', label: 'Happy Borrowers' },
  { value: '₹2Cr+', label: 'Loans Disbursed' },
  { value: '< 5min', label: 'Avg. Approval' },
  { value: '99.9%', label: 'Uptime' },
];

const steps = [
  { num: '1', title: 'Create Account', desc: 'Sign up with email and OTP verification in 2 minutes.' },
  { num: '2', title: 'Apply Online', desc: 'Fill our guided form and upload documents digitally.' },
  { num: '3', title: 'Get Approved', desc: 'Receive instant decision and track your progress live.' },
];

const trustItems = ['No hidden fees', '100% Digital', 'RBI Compliant', 'Secure & Private'];

/* ── Component ── */
export function LandingPage() {
  const { isLoggedIn, isAdmin } = useAuth();
  const dashLink = isAdmin ? '/admin/dashboard' : '/dashboard';
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className="landing">

      {/* ── Navigation ── */}
      <nav className="landing-nav">
        <Link to="/" className="landing-nav-logo">
          <div className="landing-nav-logo-box">FF</div>
          <span className="landing-nav-logo-text">FinFlow</span>
        </Link>

        <button
          className="landing-nav-toggle"
          onClick={() => setMenuOpen(!menuOpen)}
          aria-label="Toggle menu"
        >
          {menuOpen ? <X size={22} /> : <Menu size={22} />}
        </button>

        <div className={`landing-nav-links${menuOpen ? ' open' : ''}`}>
          {isLoggedIn ? (
            <Link to={dashLink} className="landing-nav-cta" onClick={() => setMenuOpen(false)}>
              Go to Dashboard
            </Link>
          ) : (
            <>
              <Link to="/login" className="landing-nav-link" onClick={() => setMenuOpen(false)}>
                Sign In
              </Link>
              <Link to="/signup" className="landing-nav-cta" onClick={() => setMenuOpen(false)}>
                Get Started
              </Link>
            </>
          )}
        </div>
      </nav>

      {/* ── Hero ── */}
      <section className="landing-hero">
        <div className="landing-hero-inner landing-fade-up">
          <div className="landing-badge">
            <span className="landing-badge-dot" />
            Trusted by 50,000+ borrowers across India
          </div>

          <h1 className="landing-h1">
            Smart Loans,<br />
            <span className="landing-h1-accent">Faster Decisions</span>
          </h1>

          <p className="landing-hero-sub">
            Apply online in minutes, track your application in real&#8209;time,
            and get funds faster than any traditional bank.
          </p>

          <div className="landing-hero-actions">
            {isLoggedIn ? (
              <Link to={dashLink} className="landing-btn-primary">
                Go to Dashboard <ArrowRight size={16} />
              </Link>
            ) : (
              <>
                <Link to="/signup" className="landing-btn-primary">
                  Apply Now — It's Free <ArrowRight size={16} />
                </Link>
                <Link to="/login" className="landing-btn-secondary">
                  Sign In
                </Link>
              </>
            )}
          </div>

          <div className="landing-trust-row">
            {trustItems.map(t => (
              <span key={t} className="landing-trust-item">
                <CheckCircle className="landing-trust-icon" />
                {t}
              </span>
            ))}
          </div>
        </div>
      </section>

      {/* ── Social Proof / Stats ── */}
      <section className="landing-section landing-stats">
        <div className="landing-stats-grid">
          {stats.map(s => (
            <div key={s.label} className="landing-stat landing-fade-up">
              <div className="landing-stat-value">{s.value}</div>
              <div className="landing-stat-label">{s.label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* ── Features ── */}
      <section className="landing-section">
        <div className="landing-section-header">
          <p className="landing-section-label">Why FinFlow</p>
          <h2 className="landing-h2">Everything you need, nothing you don't</h2>
          <p className="landing-section-sub">
            Built for speed, security, and simplicity — from application to disbursement.
          </p>
        </div>
        <div className="landing-features-grid">
          {features.map(f => (
            <div key={f.title} className="landing-feature-card landing-fade-up">
              <div className={`landing-feature-icon ${f.color}`}>
                {f.icon}
              </div>
              <h3 className="landing-feature-title">{f.title}</h3>
              <p className="landing-feature-desc">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── How it Works ── */}
      <section className="landing-section">
        <div className="landing-section-header">
          <p className="landing-section-label">How It Works</p>
          <h2 className="landing-h2">Three simple steps to your loan</h2>
          <p className="landing-section-sub">
            No paperwork, no branch visits — everything happens online.
          </p>
        </div>
        <div className="landing-steps">
          {steps.map(step => (
            <div key={step.num} className="landing-step landing-fade-up">
              <div className="landing-step-num">{step.num}</div>
              <h3 className="landing-step-title">{step.title}</h3>
              <p className="landing-step-desc">{step.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── Final CTA ── */}
      {!isLoggedIn && (
        <section className="landing-cta-section">
          <div className="landing-cta-box landing-fade-up">
            <h2 className="landing-cta-title">Ready to get started?</h2>
            <p className="landing-cta-sub">
              Join thousands who trust FinFlow for their financial needs.
              It takes less than 2 minutes to create an account.
            </p>
            <Link to="/signup" className="landing-btn-primary">
              Create Free Account <ArrowRight size={16} />
            </Link>
          </div>
        </section>
      )}

      {/* ── Footer ── */}
      <footer className="landing-footer">
        <div className="landing-footer-row">
          <Link to="/login" className="landing-footer-link">Sign In</Link>
          <Link to="/signup" className="landing-footer-link">Sign Up</Link>
          <span className="landing-footer-link">Privacy</span>
          <span className="landing-footer-link">Terms</span>
        </div>
        <p className="landing-footer-copy">
          © {new Date().getFullYear()} FinFlow Loan Management System. All rights reserved.
        </p>
      </footer>
    </div>
  );
}
