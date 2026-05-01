import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { adminGetAllApplications } from '../api/admin';
import { useTheme } from '../context/ThemeContext';
import { Search, ChevronRight, FileText } from 'lucide-react';

// ─── Design tokens ────────────────────────────────────────────────────────────
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

const LOAN_ICONS = {
  HOME: '🏠', EDUCATION: '🎓', BUSINESS: '💼',
  VEHICLE: '🚗', PERSONAL: '👤', MARRIAGE: '💍',
};

const FILTERS = [
  { value: 'ALL',       label: 'All'          },
  { value: 'SUBMITTED', label: 'Under Review'  },
  { value: 'PENDING',   label: 'Pending'       },
  { value: 'APPROVED',  label: 'Approved'      },
  { value: 'REJECTED',  label: 'Rejected'      },
  { value: 'DRAFT',     label: 'Drafts'        },
];

function fmt(n) {
  return new Intl.NumberFormat('en-IN').format(n);
}

function StatusBadge({ status, isDark }) {
  const STATUS_META = getStatusMeta(isDark);
  const m = STATUS_META[status] ?? STATUS_META['DRAFT'];
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5, background: m.bg, color: m.color, borderRadius: 99, padding: '3px 10px', fontSize: 12, fontWeight: 600 }}>
      <span style={{ width: 5, height: 5, borderRadius: '50%', background: m.color }} />
      {m.label}
    </span>
  );
}

export function AdminApplicationsPage() {
  const { isDark } = useTheme();
  const D = getDesignTokens(isDark);
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ALL');
  const [search, setSearch] = useState('');

  useEffect(() => {
    adminGetAllApplications()
      .then(setApplications)
      .catch(() => setError('Failed to load applications.'))
      .finally(() => setLoading(false));
  }, []);

  const filtered = applications.filter(a => {
    const matchStatus = filter === 'ALL' || a.status === filter;
    const q = search.toLowerCase();
    const matchSearch = !q ||
      a.loanType.toLowerCase().includes(q) ||
      String(a.id).includes(q) ||
      String(a.userId).includes(q);
    return matchStatus && matchSearch;
  });

  const counts = FILTERS.reduce((acc, f) => {
    acc[f.value] = f.value === 'ALL'
      ? applications.length
      : applications.filter(a => a.status === f.value).length;
    return acc;
  }, {});

  return (
    <div style={{ minHeight: '100vh', background: D.bg, paddingTop: 56 }}>
      <div style={{ maxWidth: 1100, margin: '0 auto', padding: '32px 20px' }}>

        {/* Header */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 24 }}>
          <FileText size={20} color={D.accent} />
          <div>
            <h1 style={{ fontSize: 21, fontWeight: 700, color: D.text1, margin: 0 }}>All Applications</h1>
            <p style={{ fontSize: 13, color: D.text3, margin: 0 }}>{applications.length} total · {filtered.length} shown</p>
          </div>
        </div>

        {/* Toolbar card */}
        <div style={{ background: D.surface, border: `1px solid ${D.border}`, borderRadius: 14, marginBottom: 16, overflow: 'hidden' }}>

          {/* Filter tabs */}
          <div style={{ display: 'flex', borderBottom: `1px solid ${D.border}`, overflowX: 'auto' }}>
            {FILTERS.map(f => {
              const active = filter === f.value;
              const statusColor = {
                ALL: D.accent, SUBMITTED: D.blue, APPROVED: D.green, REJECTED: D.red, DRAFT: D.slate, PENDING: D.amber,
              }[f.value] ?? D.accent;
              return (
                <button
                  key={f.value}
                  id={`filter-${f.value}`}
                  onClick={() => setFilter(f.value)}
                  style={{
                    flex: '0 0 auto', padding: '13px 20px',
                    fontSize: 13, fontWeight: active ? 600 : 500,
                    color: active ? statusColor : D.text2,
                    background: 'none', border: 'none',
                    borderBottom: active ? `2px solid ${statusColor}` : '2px solid transparent',
                    cursor: 'pointer', whiteSpace: 'nowrap',
                    display: 'flex', alignItems: 'center', gap: 7,
                    transition: 'color 0.15s',
                    marginBottom: -1,
                  }}
                >
                  {f.label}
                  <span style={{
                    fontSize: 11, fontWeight: 700,
                    background: active ? `${statusColor}20` : D.border2,
                    color: active ? statusColor : D.text3,
                    borderRadius: 99, padding: '1px 7px',
                  }}>
                    {counts[f.value]}
                  </span>
                </button>
              );
            })}
          </div>

          {/* Search */}
          <div style={{ padding: '10px 16px', display: 'flex', alignItems: 'center', gap: 8 }}>
            <Search size={14} color={D.text3} />
            <input
              id="admin-search"
              type="text"
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Search by ID, loan type, user ID…"
              style={{
                flex: 1, border: 'none', outline: 'none',
                fontSize: 13, color: D.text1, background: 'transparent',
              }}
            />
            {search && (
              <button onClick={() => setSearch('')} style={{ border: 'none', background: 'none', cursor: 'pointer', fontSize: 13, color: D.text3 }}>✕</button>
            )}
          </div>
        </div>

        {/* Content */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: '80px 0', color: D.text3 }}>
            <div style={{ width: 34, height: 34, border: `3px solid ${D.border2}`, borderTopColor: D.accent, borderRadius: '50%', animation: 'spin 0.8s linear infinite', margin: '0 auto 12px' }} />
            <p style={{ margin: 0, fontSize: 13 }}>Loading…</p>
          </div>
        ) : error ? (
          <div style={{ background: D.redBg, border: `1px solid ${D.red}40`, borderRadius: 12, padding: '14px 18px', color: D.red, fontSize: 13 }}>{error}</div>
        ) : filtered.length === 0 ? (
          <div style={{ background: D.surface, border: `1px solid ${D.border}`, borderRadius: 14, padding: '60px 0', textAlign: 'center' }}>
            <p style={{ color: D.text3, fontSize: 14, margin: 0 }}>No applications match your filters.</p>
          </div>
        ) : (
          <div style={{ background: D.surface, border: `1px solid ${D.border}`, borderRadius: 14, overflow: 'hidden' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  {['ID', 'Loan Type', 'Amount', 'Tenure', 'User', 'Applied', 'Status', ''].map(h => (
                    <th key={h} style={{ padding: '11px 18px', textAlign: 'left', fontSize: 11, fontWeight: 600, color: D.text3, letterSpacing: '0.06em', textTransform: 'uppercase', background: D.surface2, borderBottom: `1px solid ${D.border}` }}>
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {filtered.map((app, i) => (
                  <tr
                    key={app.id}
                    style={{ borderTop: i > 0 ? `1px solid ${D.border}` : 'none', transition: 'background 0.12s' }}
                    onMouseEnter={e => (e.currentTarget.style.background = D.surface2)}
                    onMouseLeave={e => (e.currentTarget.style.background = '')}
                  >
                    <td style={{ padding: '13px 18px', fontSize: 13, color: D.text3, fontWeight: 500 }}>#{app.id}</td>
                    <td style={{ padding: '13px 18px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 9 }}>
                        <span style={{ fontSize: 18 }}>{LOAN_ICONS[app.loanType] || '📄'}</span>
                        <span style={{ fontSize: 13, color: D.text1, fontWeight: 600 }}>
                          {app.loanType.charAt(0) + app.loanType.slice(1).toLowerCase()}
                        </span>
                      </div>
                    </td>
                    <td style={{ padding: '13px 18px', fontSize: 13, color: D.text1, fontWeight: 500 }}>₹{fmt(app.amount)}</td>
                    <td style={{ padding: '13px 18px', fontSize: 13, color: D.text2 }}>{app.tenure} mo</td>
                    <td style={{ padding: '13px 18px', fontSize: 12, color: D.text3 }}>#{app.userId}</td>
                    <td style={{ padding: '13px 18px', fontSize: 12, color: D.text3 }}>
                      {new Date(app.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: '2-digit' })}
                    </td>
                    <td style={{ padding: '13px 18px' }}><StatusBadge status={app.status} isDark={isDark} /></td>
                    <td style={{ padding: '13px 18px', textAlign: 'right' }}>
                      <Link
                        to={`/admin/applications/${app.id}`}
                        style={{ display: 'inline-flex', alignItems: 'center', gap: 4, fontSize: 12, color: D.accent, fontWeight: 600, textDecoration: 'none' }}
                      >
                        Review <ChevronRight size={12} />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  );
}
