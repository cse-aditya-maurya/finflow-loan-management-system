import { useEffect, useState, useRef } from 'react';
import { Link } from 'react-router-dom';
import { adminDashboard, adminGetAllApplications } from '../api/admin';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import {
  LayoutDashboard, FileText, CheckCircle, XCircle, Clock,
  Users, ChevronRight, TrendingUp, AlertCircle,
} from 'lucide-react';

function getDesignTokens(isDark) {
  return isDark ? {
    bg:       '#0d0f14',
    surface:  '#13151c',
    surface2: '#191c25',
    border:   '#23273a',
    border2:  '#2e3347',
    text1:    '#f0f2f9',
    text2:    '#8891a8',
    text3:    '#50566e',
    accent:   '#6366f1',
    accentBg: 'rgba(99,102,241,.12)',
    green:    '#22c55e',
    greenBg:  'rgba(34,197,94,.12)',
    red:      '#f87171',
    redBg:    'rgba(248,113,113,.12)',
    amber:    '#fbbf24',
    amberBg:  'rgba(251,191,36,.12)',
    blue:     '#60a5fa',
    blueBg:   'rgba(96,165,250,.12)',
    slate:    '#8891a8',
    slateBg:  'rgba(136,145,168,.10)',
  } : {
    bg:       '#f8fafc',
    surface:  '#ffffff',
    surface2: '#f1f5f9',
    border:   '#e2e8f0',
    border2:  '#cbd5e1',
    text1:    '#0f172a',
    text2:    '#64748b',
    text3:    '#94a3b8',
    accent:   '#6366f1',
    accentBg: 'rgba(99,102,241,.1)',
    green:    '#16a34a',
    greenBg:  'rgba(22,163,74,.1)',
    red:      '#dc2626',
    redBg:    'rgba(220,38,38,.1)',
    amber:    '#d97706',
    amberBg:  'rgba(217,119,6,.1)',
    blue:     '#2563eb',
    blueBg:   'rgba(37,99,235,.1)',
    slate:    '#64748b',
    slateBg:  'rgba(100,116,139,.1)',
  };
}

function fmt(n) {
  return new Intl.NumberFormat('en-IN').format(n);
}

function getStatusMeta(isDark) {
  const D = getDesignTokens(isDark);
  return {
    APPROVED:  { label: 'Approved',     color: D.green, bg: D.greenBg },
    REJECTED:  { label: 'Rejected',     color: D.red,   bg: D.redBg   },
    SUBMITTED: { label: 'Under Review', color: D.blue,  bg: D.blueBg  },
    PENDING:   { label: 'Pending',      color: D.amber, bg: D.amberBg },
    DRAFT:     { label: 'Draft',        color: D.slate, bg: D.slateBg },
  };
}

const LOAN_COLORS = {
  HOME: '#3b82f6', EDUCATION: '#8b5cf6', BUSINESS: '#10b981',
  VEHICLE: '#f59e0b', PERSONAL: '#ec4899', MARRIAGE: '#ef4444',
};

const LOAN_TYPES = ['HOME', 'EDUCATION', 'BUSINESS', 'VEHICLE', 'PERSONAL', 'MARRIAGE'];

function StatusBadge({ status, isDark }) {
  const STATUS_META = getStatusMeta(isDark);
  const m = STATUS_META[status] ?? STATUS_META['DRAFT'];
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6, background: m.bg, color: m.color, borderRadius: 99, padding: '3px 10px', fontSize: 12, fontWeight: 600 }}>
      <span style={{ width: 6, height: 6, borderRadius: '50%', background: m.color }} />
      {m.label}
    </span>
  );
}

function Stat({ label, value, icon, color, sub, D }) {
  return (
    <div style={{ background: D.surface, border: `1px solid ${D.border}`, borderRadius: 14, padding: '20px 22px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 }}>
        <span style={{ fontSize: 12, fontWeight: 600, color: D.text2, letterSpacing: '0.04em', textTransform: 'uppercase' }}>{label}</span>
        <span style={{ width: 34, height: 34, borderRadius: 10, background: `${color}18`, display: 'flex', alignItems: 'center', justifyContent: 'center', color }}>{icon}</span>
      </div>
      <p style={{ fontSize: 32, fontWeight: 700, color: D.text1, margin: '0 0 2px', lineHeight: 1 }}>{value}</p>
      {sub && <p style={{ fontSize: 12, color: D.text3, margin: 0 }}>{sub}</p>}
    </div>
  );
}

export function AdminDashboard() {
  const { user } = useAuth();
  const { isDark } = useTheme();
  const D = getDesignTokens(isDark);
  const [applications, setApplications] = useState([]);
  const [, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const loaded = useRef(false);

  useEffect(() => {
    if (!user?.token || loaded.current) return;
    loaded.current = true;
    Promise.allSettled([adminDashboard(), adminGetAllApplications()])
      .then(([rpt, apps]) => {
        if (rpt.status === 'fulfilled') setReport(rpt.value);
        if (apps.status === 'fulfilled') setApplications(apps.value);
      })
      .finally(() => setLoading(false));
  }, [user?.token]);

  const total    = applications.length;
  const approved = applications.filter(a => a.status === 'APPROVED').length;
  const rejected = applications.filter(a => a.status === 'REJECTED').length;
  const review   = applications.filter(a => a.status === 'SUBMITTED' || a.status === 'PENDING').length;
  const drafts   = applications.filter(a => a.status === 'DRAFT').length;

  const recent = [...applications]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 8);

  return (
    <div style={{ minHeight: '100vh', background: D.bg, paddingTop: 56 }}>
      <div style={{ maxWidth: 1100, margin: '0 auto', padding: '32px 20px' }}>

        {/* Page header */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 12, marginBottom: 28 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <LayoutDashboard size={20} color={D.accent} />
            <div>
              <h1 style={{ fontSize: 21, fontWeight: 700, color: D.text1, margin: 0 }}>Admin Dashboard</h1>
              <p style={{ fontSize: 13, color: D.text3, margin: 0 }}>
                Welcome, <span style={{ color: D.accent, fontWeight: 600 }}>{user?.profile?.name || 'Admin'}</span>
              </p>
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6, background: D.greenBg, border: `1px solid ${D.green}30`, borderRadius: 99, padding: '5px 12px' }}>
            <span style={{ width: 7, height: 7, borderRadius: '50%', background: D.green, display: 'inline-block', boxShadow: `0 0 6px ${D.green}` }} />
            <span style={{ fontSize: 12, fontWeight: 600, color: D.green }}>System Active</span>
          </div>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '80px 0', color: D.text3 }}>
            <div style={{ width: 36, height: 36, border: `3px solid ${D.border2}`, borderTopColor: D.accent, borderRadius: '50%', animation: 'spin 0.8s linear infinite', margin: '0 auto 12px' }} />
            <p style={{ margin: 0, fontSize: 13 }}>Loading dashboard…</p>
          </div>
        ) : (
          <>
            {/* KPI Cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(170px, 1fr))', gap: 14, marginBottom: 20 }}>
              <Stat label="Total"       value={total}    icon={<FileText size={16} />}    color={D.accent} D={D} />
              <Stat label="Approved"    value={approved} icon={<CheckCircle size={16} />} color={D.green} D={D}
                sub={total ? `${Math.round(approved / total * 100)}% approval` : undefined} />
              <Stat label="Rejected"    value={rejected} icon={<XCircle size={16} />}     color={D.red} D={D} />
              <Stat label="Under Review" value={review}  icon={<Clock size={16} />}       color={D.amber} D={D} />
              <Stat label="Drafts"      value={drafts}   icon={<Users size={16} />}       color={D.slate} D={D} />
            </div>

            {/* Two-column section */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 }}>

              {/* Loan type bars */}
              <div style={{ background: D.surface, border: `1px solid ${D.border}`, borderRadius: 14, padding: '20px 22px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 18 }}>
                  <TrendingUp size={15} color={D.accent} />
                  <h2 style={{ fontSize: 14, fontWeight: 600, color: D.text1, margin: 0 }}>Loan Type Breakdown</h2>
                </div>
                {total === 0 ? (
                  <p style={{ fontSize: 13, color: D.text3, textAlign: 'center', padding: '24px 0', margin: 0 }}>No applications yet</p>
                ) : LOAN_TYPES.map(type => {
                  const cnt = applications.filter(a => a.loanType === type).length;
                  const pct = total ? Math.round(cnt / total * 100) : 0;
                  return (
                    <div key={type} style={{ marginBottom: 12 }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 5 }}>
                        <span style={{ fontSize: 13, color: D.text2, fontWeight: 500 }}>
                          {type.charAt(0) + type.slice(1).toLowerCase()}
                        </span>
                        <span style={{ fontSize: 12, color: D.text3 }}>{cnt} ({pct}%)</span>
                      </div>
                      <div style={{ height: 6, background: D.border2, borderRadius: 99, overflow: 'hidden' }}>
                        <div style={{ height: '100%', width: `${pct}%`, background: LOAN_COLORS[type], borderRadius: 99, transition: 'width 0.6s ease' }} />
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Needs attention */}
              <div style={{ background: D.surface, border: `1px solid ${D.border}`, borderRadius: 14, padding: '20px 22px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 18 }}>
                  <AlertCircle size={15} color={D.amber} />
                  <h2 style={{ fontSize: 14, fontWeight: 600, color: D.text1, margin: 0 }}>Needs Attention</h2>
                  {review > 0 && (
                    <span style={{ fontSize: 11, fontWeight: 700, background: D.amberBg, color: D.amber, borderRadius: 99, padding: '1px 8px', marginLeft: 'auto' }}>
                      {review}
                    </span>
                  )}
                </div>
                {review === 0 ? (
                  <p style={{ fontSize: 13, color: D.text3, textAlign: 'center', padding: '24px 0', margin: 0 }}>All caught up! 🎉</p>
                ) : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    {applications.filter(a => a.status === 'SUBMITTED' || a.status === 'PENDING').slice(0, 5).map(app => (
                      <Link key={app.id} to={`/admin/applications/${app.id}`} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 12px', background: D.surface2, border: `1px solid ${D.border}`, borderRadius: 10, textDecoration: 'none', transition: 'border-color 0.15s' }}>
                        <div>
                          <p style={{ fontSize: 13, fontWeight: 600, color: D.text1, margin: 0 }}>
                            {app.loanType.charAt(0) + app.loanType.slice(1).toLowerCase()} Loan · #{app.id}
                          </p>
                          <p style={{ fontSize: 11, color: D.text3, margin: '2px 0 0' }}>₹{fmt(app.amount)} · {app.tenure} mo</p>
                        </div>
                        <ChevronRight size={14} color={D.text3} />
                      </Link>
                    ))}
                    {review > 5 && (
                      <Link to="/admin/applications" style={{ fontSize: 12, color: D.accent, fontWeight: 500, textDecoration: 'none', textAlign: 'center', paddingTop: 4 }}>
                        +{review - 5} more →
                      </Link>
                    )}
                  </div>
                )}
              </div>
            </div>

            {/* Recent table */}
            <div style={{ background: D.surface, border: `1px solid ${D.border}`, borderRadius: 14, overflow: 'hidden' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '16px 22px', borderBottom: `1px solid ${D.border}` }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <FileText size={15} color={D.accent} />
                  <h2 style={{ fontSize: 14, fontWeight: 600, color: D.text1, margin: 0 }}>Recent Applications</h2>
                </div>
                <Link to="/admin/applications" style={{ fontSize: 13, color: D.accent, fontWeight: 500, textDecoration: 'none', display: 'flex', alignItems: 'center', gap: 4 }}>
                  View all <ChevronRight size={13} />
                </Link>
              </div>

              {recent.length === 0 ? (
                <p style={{ textAlign: 'center', padding: '48px 0', color: D.text3, fontSize: 13, margin: 0 }}>No applications yet</p>
              ) : (
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr>
                      {['ID', 'Type', 'Amount', 'Tenure', 'Date', 'Status', ''].map(h => (
                        <th key={h} style={{ padding: '10px 18px', textAlign: 'left', fontSize: 11, fontWeight: 600, color: D.text3, letterSpacing: '0.06em', textTransform: 'uppercase', background: D.surface2, borderBottom: `1px solid ${D.border}` }}>{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {recent.map((app, i) => (
                      <tr key={app.id} style={{ borderTop: i > 0 ? `1px solid ${D.border}` : 'none' }}
                        onMouseEnter={e => (e.currentTarget.style.background = D.surface2)}
                        onMouseLeave={e => (e.currentTarget.style.background = '')}>
                        <td style={{ padding: '12px 18px', fontSize: 13, color: D.text3, fontWeight: 500 }}>#{app.id}</td>
                        <td style={{ padding: '12px 18px', fontSize: 13, color: D.text1, fontWeight: 500 }}>
                          {app.loanType.charAt(0) + app.loanType.slice(1).toLowerCase()}
                        </td>
                        <td style={{ padding: '12px 18px', fontSize: 13, color: D.text1 }}>₹{fmt(app.amount)}</td>
                        <td style={{ padding: '12px 18px', fontSize: 13, color: D.text2 }}>{app.tenure} mo</td>
                        <td style={{ padding: '12px 18px', fontSize: 12, color: D.text3 }}>
                          {new Date(app.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: '2-digit' })}
                        </td>
                        <td style={{ padding: '12px 18px' }}><StatusBadge status={app.status} isDark={isDark} /></td>
                        <td style={{ padding: '12px 18px', textAlign: 'right' }}>
                          <Link to={`/admin/applications/${app.id}`} style={{ fontSize: 12, color: D.accent, fontWeight: 600, textDecoration: 'none' }}>Review →</Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </>
        )}
      </div>
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  );
}
