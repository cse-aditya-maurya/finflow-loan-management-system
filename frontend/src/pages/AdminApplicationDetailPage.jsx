import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import {
  adminGetApplicationById, adminGetDocuments,
  adminVerifyDocument, adminRejectDocument,
  adminApproveApplication, adminRejectApplication,
} from '../api/admin';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import {
  ArrowLeft, CheckCircle, XCircle, FileText, Eye,
  User, Briefcase, DollarSign, Calendar, TrendingUp, AlertCircle,
} from 'lucide-react';
import { DocumentViewerModal } from '../components/DocumentViewerModal';

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
    purple:   '#a78bfa',
    purpleBg: 'rgba(167,139,250,.12)',
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
    purple:   '#9333ea',
    purpleBg: 'rgba(147,51,234,.1)',
  };
}

function fmt(n) {
  return new Intl.NumberFormat('en-IN').format(n);
}

// ─── Reusable bits ────────────────────────────────────────────────────────────
function getStatusMeta(isDark) {
  const D = getDesignTokens(isDark);
  return {
    APPROVED:  { label: 'Approved',     color: D.green,  bg: D.greenBg  },
    REJECTED:  { label: 'Rejected',     color: D.red,    bg: D.redBg    },
    SUBMITTED: { label: 'Under Review', color: D.blue,   bg: D.blueBg   },
    PENDING:   { label: 'Pending',      color: D.amber,  bg: D.amberBg  },
    DRAFT:     { label: 'Draft',        color: D.slate,  bg: D.slateBg  },
  };
}

function StatusBadge({ status, isDark }) {
  const STATUS_META = getStatusMeta(isDark);
  const m = STATUS_META[status] ?? STATUS_META['DRAFT'];
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6, background: m.bg, color: m.color, borderRadius: 99, padding: '4px 12px', fontSize: 13, fontWeight: 600 }}>
      <span style={{ width: 7, height: 7, borderRadius: '50%', background: m.color }} />
      {m.label}
    </span>
  );
}

function Toast({ type, children, D }) {
  const c = type === 'error'
    ? { bg: D.redBg,   border: `${D.red}40`,   color: D.red,   Icon: AlertCircle }
    : { bg: D.greenBg, border: `${D.green}40`,  color: D.green, Icon: CheckCircle };
  const { Icon } = c;
  return (
    <div style={{ background: c.bg, border: `1px solid ${c.border}`, borderRadius: 10, padding: '11px 15px', marginBottom: 14, display: 'flex', alignItems: 'flex-start', gap: 9 }}>
      <Icon size={15} color={c.color} style={{ flexShrink: 0, marginTop: 1 }} />
      <p style={{ fontSize: 13, color: c.color, margin: 0 }}>{children}</p>
    </div>
  );
}

function Btn({ children, onClick, variant = 'primary', loading = false, disabled = false, id, D }) {
  const V = {
    primary:   { bg: D.accentBg, color: D.accent,  border: `${D.accent}40` },
    success:   { bg: D.greenBg,  color: D.green,   border: `${D.green}40`  },
    danger:    { bg: D.redBg,    color: D.red,      border: `${D.red}40`   },
    secondary: { bg: D.surface2, color: D.text2,   border: D.border2       },
    warning:   { bg: D.amberBg,  color: D.amber,   border: `${D.amber}40`  },
  }[variant];
  return (
    <button id={id} onClick={onClick} disabled={disabled || loading}
      style={{ display: 'inline-flex', alignItems: 'center', gap: 6, padding: '8px 16px', borderRadius: 8, fontSize: 13, fontWeight: 600, background: V.bg, color: V.color, border: `1px solid ${V.border}`, cursor: disabled || loading ? 'not-allowed' : 'pointer', opacity: disabled || loading ? 0.5 : 1, whiteSpace: 'nowrap' }}
    >
      {loading && <span style={{ width: 12, height: 12, border: '2px solid currentColor', borderTopColor: 'transparent', borderRadius: '50%', animation: 'spin 0.7s linear infinite', display: 'inline-block' }} />}
      {children}
    </button>
  );
}

// ─── Doc card ─────────────────────────────────────────────────────────────────
function DocCard({ doc, onView, onVerify, onReject, loadingMap, D }) {
  const status = doc.status?.toUpperCase();
  const isVerified = status === 'VERIFIED';
  const isRejected = status === 'REJECTED';
  const isPending  = !isVerified && !isRejected;

  const sty = isVerified ? { color: D.green,  bg: D.greenBg,  border: `${D.green}40`,  label: '✓ Verified' }
            : isRejected ? { color: D.red,     bg: D.redBg,    border: `${D.red}40`,    label: '✗ Rejected' }
            :              { color: D.amber,   bg: D.amberBg,  border: `${D.amber}40`,  label: '⏳ Pending'  };

  return (
    <div style={{ background: D.surface2, border: `1px solid ${D.border}`, borderRadius: 12, padding: '15px 18px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <div style={{ width: 38, height: 38, borderRadius: 10, background: D.border2, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <FileText size={17} color={D.text2} />
        </div>
        <div>
          <p style={{ fontSize: 14, fontWeight: 600, color: D.text1, margin: 0 }}>
            {String(doc.documentType || 'Document').replace(/_/g, ' ')}
          </p>
          {doc.remarks && <p style={{ fontSize: 12, color: D.text3, margin: '2px 0 4px' }}>Remark: {doc.remarks}</p>}
          <span style={{ fontSize: 11, fontWeight: 600, background: sty.bg, color: sty.color, border: `1px solid ${sty.border}`, borderRadius: 99, padding: '1px 8px', display: 'inline-block', marginTop: 3 }}>
            {sty.label}
          </span>
        </div>
      </div>
      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        <Btn id={`view-doc-${doc.id}`} variant="secondary" onClick={() => onView(doc.id, String(doc.documentType || 'Document'))} D={D}>
          <Eye size={13} /> View
        </Btn>
        {isPending && (
          <>
            <Btn id={`verify-doc-${doc.id}`} variant="success" loading={loadingMap[`verify-doc-${doc.id}`]} disabled={loadingMap[`reject-doc-${doc.id}`]} onClick={() => onVerify(doc.id)} D={D}>
              <CheckCircle size={13} /> Verify
            </Btn>
            <Btn id={`reject-doc-${doc.id}`} variant="danger" loading={loadingMap[`reject-doc-${doc.id}`]} disabled={loadingMap[`verify-doc-${doc.id}`]} onClick={() => onReject(doc.id)} D={D}>
              <XCircle size={13} /> Reject
            </Btn>
          </>
        )}
        {isRejected && (
          <Btn id={`reverify-doc-${doc.id}`} variant="warning" loading={loadingMap[`verify-doc-${doc.id}`]} onClick={() => onVerify(doc.id)} D={D}>
            <CheckCircle size={13} /> Re-verify
          </Btn>
        )}
      </div>
    </div>
  );
}

// ─── Main ─────────────────────────────────────────────────────────────────────
export function AdminApplicationDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isLoggedIn, isAdmin } = useAuth();
  const { isDark } = useTheme();
  const D = getDesignTokens(isDark);

  const [app, setApp]           = useState(null);
  const [docs, setDocs]         = useState([]);
  const [loading, setLoading]   = useState(true);
  const [loadingMap, setLoadingMap] = useState({});
  const [error, setError]       = useState('');
  const [success, setSuccess]   = useState('');
  const [rejectRemarks, setRejectRemarks] = useState('');
  const [showRejectBox, setShowRejectBox] = useState(false);
  const [viewingDocId, setViewingDocId]   = useState(null);
  const [viewingDocType, setViewingDocType] = useState('');

  useEffect(() => {
    if (!isLoggedIn) { navigate('/login', { replace: true }); return; }
    if (!isAdmin)    { navigate('/dashboard', { replace: true }); return; }
  }, [isLoggedIn, isAdmin, navigate]);

  useEffect(() => {
    if (!id || !isLoggedIn || !isAdmin) return;
    (async () => {
      try {
        const [appData, docData] = await Promise.all([
          adminGetApplicationById(+id),
          adminGetDocuments(+id),
        ]);
        setApp(appData);
        setDocs(Array.isArray(docData) ? docData : []);
      } catch (err) {
        setError(err?.response?.data?.message || 'Failed to load application.');
      } finally {
        setLoading(false);
      }
    })();
  }, [id, isLoggedIn, isAdmin]);

  async function doAction(action, successMsg, key) {
    setLoadingMap(p => ({ ...p, [key]: true }));
    setError(''); setSuccess('');
    try {
      await action();
      const [a, d] = await Promise.all([adminGetApplicationById(+id), adminGetDocuments(+id)]);
      setApp(a); setDocs(Array.isArray(d) ? d : []);
      setSuccess(successMsg);
      setShowRejectBox(false); setRejectRemarks('');
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Action failed.');
    } finally {
      setLoadingMap(p => ({ ...p, [key]: false }));
    }
  }

  function handleApprove() {
    const allVerified = docs.every(d => d.status?.toUpperCase() === 'VERIFIED');
    if (!allVerified) { setError('All documents must be verified before approving.'); return; }
    doAction(() => adminApproveApplication(+id), 'Application approved!', 'approve-app');
  }

  function handleReject() {
    if (!rejectRemarks.trim()) { setError('Please provide rejection remarks.'); return; }
    doAction(() => adminRejectApplication(+id, rejectRemarks), 'Application rejected.', 'reject-app');
  }

  const detailFields = app ? [
    { label: 'User ID',        value: `#${app.userId}`,                                          icon: <User size={14} /> },
    { label: 'Loan Type',      value: app.loanType.charAt(0) + app.loanType.slice(1).toLowerCase(), icon: <Briefcase size={14} /> },
    { label: 'Amount',         value: `₹${fmt(app.amount)}`,                                     icon: <DollarSign size={14} /> },
    { label: 'Tenure',         value: `${app.tenure} months`,                                    icon: <Calendar size={14} /> },
    { label: 'Monthly Income', value: `₹${fmt(app.income)}`,                                     icon: <TrendingUp size={14} /> },
    { label: 'Age',            value: app.age,                                                   icon: <User size={14} /> },
    { label: 'Occupation',     value: app.occupation || '—',                                     icon: <Briefcase size={14} /> },
    ...(app.coApplicantName
      ? [{ label: 'Co-Applicant', value: app.coApplicantName, icon: <User size={14} /> }]
      : []),
  ] : [];

  return (
    <div style={{ minHeight: '100vh', background: D.bg, paddingTop: 56 }}>
      <div style={{ maxWidth: 820, margin: '0 auto', padding: '28px 20px' }}>

        <Link to="/admin/applications" style={{ display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: 13, color: D.text2, textDecoration: 'none', marginBottom: 22, fontWeight: 500, transition: 'color 0.15s' }}>
          <ArrowLeft size={14} /> Back to Applications
        </Link>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '80px 0', color: D.text3 }}>
            <div style={{ width: 34, height: 34, border: `3px solid ${D.border2}`, borderTopColor: D.accent, borderRadius: '50%', animation: 'spin 0.8s linear infinite', margin: '0 auto 12px' }} />
            <p style={{ margin: 0, fontSize: 13 }}>Loading…</p>
          </div>
        ) : !app ? (
          <Toast type="error" D={D}>{error || 'Application not found.'}</Toast>
        ) : (
          <>
            {error   && <Toast type="error" D={D}>{error}</Toast>}
            {success && <Toast type="success" D={D}>{success}</Toast>}

            {/* ── Header card ── */}
            <div style={{ background: D.surface, border: `1px solid ${D.border}`, borderRadius: 14, padding: '22px 24px', marginBottom: 14 }}>
              <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: 12 }}>
                <div>
                  <h1 style={{ fontSize: 20, fontWeight: 700, color: D.text1, margin: '0 0 4px' }}>
                    {app.loanType.charAt(0) + app.loanType.slice(1).toLowerCase()} Loan &nbsp;
                    <span style={{ fontSize: 15, fontWeight: 400, color: D.text3 }}>#{app.id}</span>
                  </h1>
                  <p style={{ fontSize: 12, color: D.text3, margin: 0 }}>
                    Applied {new Date(app.createdAt).toLocaleDateString('en-IN', { year: 'numeric', month: 'long', day: 'numeric' })}
                  </p>
                </div>
                <StatusBadge status={app.status} isDark={isDark} />
              </div>

              {/* Amount highlight */}
              <div style={{ marginTop: 20, display: 'inline-block', background: D.purpleBg, border: `1px solid ${D.purple}30`, borderRadius: 12, padding: '14px 20px' }}>
                <p style={{ fontSize: 11, color: D.purple, fontWeight: 700, margin: '0 0 4px', letterSpacing: '0.06em', textTransform: 'uppercase' }}>Requested Amount</p>
                <p style={{ fontSize: 28, fontWeight: 700, color: D.text1, margin: 0 }}>₹{fmt(app.amount)}</p>
                <p style={{ fontSize: 12, color: D.purple, margin: '4px 0 0' }}>{app.tenure} months tenure</p>
              </div>
            </div>

            {/* ── Applicant details ── */}
            <div style={{ background: D.surface, border: `1px solid ${D.border}`, borderRadius: 14, padding: '20px 24px', marginBottom: 14 }}>
              <h2 style={{ fontSize: 14, fontWeight: 600, color: D.text1, margin: '0 0 16px', display: 'flex', alignItems: 'center', gap: 7 }}>
                <User size={15} color={D.accent} /> Applicant Details
              </h2>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(210px, 1fr))', gap: '0' }}>
                {detailFields.map(({ label, value, icon }, i) => (
                  <div key={label} style={{ padding: '11px 0', borderTop: i >= 2 ? `1px solid ${D.border}` : 'none', display: 'flex', alignItems: 'flex-start', gap: 10 }}>
                    <span style={{ color: D.text3, marginTop: 2 }}>{icon}</span>
                    <div>
                      <p style={{ fontSize: 11, color: D.text3, margin: 0, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>{label}</p>
                      <p style={{ fontSize: 14, color: D.text1, margin: '2px 0 0', fontWeight: 500 }}>{String(value)}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* ── Documents ── */}
            {docs.length > 0 && (
              <div style={{ background: D.surface, border: `1px solid ${D.border}`, borderRadius: 14, padding: '20px 24px', marginBottom: 14 }}>
                <h2 style={{ fontSize: 14, fontWeight: 600, color: D.text1, margin: '0 0 14px', display: 'flex', alignItems: 'center', gap: 7 }}>
                  <FileText size={15} color={D.accent} /> Documents
                  <span style={{ fontSize: 11, fontWeight: 700, background: D.border2, color: D.text2, borderRadius: 99, padding: '1px 9px', marginLeft: 4 }}>
                    {docs.filter(d => d.status?.toUpperCase() === 'VERIFIED').length}/{docs.length} verified
                  </span>
                </h2>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {docs.map(doc => (
                    <DocCard
                      key={doc.id}
                      doc={doc}
                      loadingMap={loadingMap}
                      D={D}
                      onView={(docId, docType) => { setViewingDocId(docId); setViewingDocType(docType); }}
                      onVerify={docId => doAction(() => adminVerifyDocument(docId), 'Document verified!', `verify-doc-${docId}`)}
                      onReject={docId => {
                        const remarks = prompt('Enter rejection reason:');
                        if (!remarks?.trim()) { setError('Please provide a reason.'); return; }
                        doAction(() => adminRejectDocument(docId, remarks), 'Document rejected.', `reject-doc-${docId}`);
                      }}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* ── Decision ── */}
            {app.status === 'SUBMITTED' && (
              <div style={{ background: D.surface, border: `1px solid ${D.border}`, borderRadius: 14, padding: '20px 24px' }}>
                <h2 style={{ fontSize: 14, fontWeight: 600, color: D.text1, margin: '0 0 14px' }}>Application Decision</h2>
                {showRejectBox ? (
                  <>
                    <textarea
                      id="reject-remarks"
                      value={rejectRemarks}
                      onChange={e => setRejectRemarks(e.target.value)}
                      placeholder="Enter rejection reason…"
                      rows={3}
                      style={{ width: '100%', background: D.surface2, border: `1px solid ${D.border2}`, borderRadius: 8, padding: '10px 12px', fontSize: 13, color: D.text1, resize: 'vertical', outline: 'none', marginBottom: 12, boxSizing: 'border-box', fontFamily: 'inherit' }}
                    />
                    <div style={{ display: 'flex', gap: 10 }}>
                      <Btn id="confirm-reject-btn" variant="danger" loading={loadingMap['reject-app']} onClick={handleReject} D={D}>
                        <XCircle size={13} /> Confirm Rejection
                      </Btn>
                      <Btn id="cancel-reject-btn" variant="secondary" onClick={() => { setShowRejectBox(false); setRejectRemarks(''); setError(''); }} D={D}>
                        Cancel
                      </Btn>
                    </div>
                  </>
                ) : (
                  <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                    <Btn id="approve-application-btn" variant="success" loading={loadingMap['approve-app']} onClick={handleApprove} D={D}>
                      <CheckCircle size={13} /> Approve Application
                    </Btn>
                    <Btn id="reject-application-btn" variant="danger" onClick={() => { setShowRejectBox(true); setError(''); }} D={D}>
                      <XCircle size={13} /> Reject Application
                    </Btn>
                  </div>
                )}
              </div>
            )}
          </>
        )}
      </div>

      {viewingDocId && (
        <DocumentViewerModal
          docId={viewingDocId}
          documentType={viewingDocType}
          onClose={() => { setViewingDocId(null); setViewingDocType(''); }}
        />
      )}

      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  );
}



