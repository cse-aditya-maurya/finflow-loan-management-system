import React, { useState, useEffect } from 'react';
import {
  BarChart3, IndianRupee, FileText, Clock,
  CheckCircle, XCircle, TrendingUp, AlertCircle, Users,
} from 'lucide-react';
import { adminGetAllApplications } from '../api/admin';
import './AdminReportsPage.css';

export function AdminReportsPage() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading]           = useState(true);
  const [error, setError]               = useState('');

  useEffect(() => { fetchData(); }, []);

  const fetchData = async () => {
    setLoading(true); setError('');
    try {
      const token = localStorage.getItem('finflow_token');
      if (!token) { setError('Please login to continue'); setLoading(false); return; }
      const userStr = localStorage.getItem('finflow_user');
      if (userStr && JSON.parse(userStr).role !== 'ADMIN') {
        setError('Access Denied: Admin only'); setLoading(false); return;
      }
      const data = await adminGetAllApplications().catch(() => []);
      setApplications(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err?.response?.status === 403 ? 'Access Denied' : 'Failed to load data.');
    } finally {
      setLoading(false);
    }
  };

  /* ── Stats ─────────────────────────────────────────────── */
  const total     = applications.length;
  const approved  = applications.filter(a => a.status === 'APPROVED');
  const rejected  = applications.filter(a => a.status === 'REJECTED');
  const pending   = applications.filter(a => a.status === 'SUBMITTED' || a.status === 'PENDING');
  const disbursed = approved.reduce((s, a) => s + (a.amount || 0), 0);
  const rate      = total > 0 ? ((approved.length / total) * 100).toFixed(1) : '0.0';

  const cards = [
    { label: 'Total Disbursed',    value: `₹${disbursed.toLocaleString('en-IN')}`, icon: IndianRupee, iconCls: 'ic-indigo', sub: `${approved.length} approved loan${approved.length !== 1 ? 's' : ''}` },
    { label: 'Total Applications', value: total,                                    icon: FileText,    iconCls: 'ic-blue',   sub: 'all submissions'           },
    { label: 'Approved',           value: approved.length,                          icon: CheckCircle, iconCls: 'ic-green',  sub: `${rate}% approval rate`    },
    { label: 'Pending Review',     value: pending.length,                           icon: Clock,       iconCls: 'ic-amber',  sub: 'awaiting decision'         },
    { label: 'Rejected',           value: rejected.length,                          icon: XCircle,     iconCls: 'ic-red',    sub: 'applications declined'     },
    { label: 'Approval Rate',      value: `${rate}%`,                               icon: TrendingUp,  iconCls: 'ic-purple', sub: `${approved.length} of ${total} approved` },
  ];

  /* ── Activity ───────────────────────────────────────────── */
  const timeAgo = d => {
    const s = Math.floor((new Date() - d) / 1000);
    if (s < 60)    return 'Just now';
    if (s < 3600)  return `${Math.floor(s / 60)}m ago`;
    if (s < 86400) return `${Math.floor(s / 3600)}h ago`;
    return `${Math.floor(s / 86400)}d ago`;
  };

  const activity = [...applications]
    .sort((a, b) => new Date(b.updatedAt || b.createdAt) - new Date(a.updatedAt || a.createdAt))
    .slice(0, 8)
    .map(app => {
      const t = timeAgo(new Date(app.updatedAt || app.createdAt));
      if (app.status === 'APPROVED')
        return { cls: 'act-green', badge: 'Approved', text: `Loan #${app.id}`,        sub: `₹${app.amount?.toLocaleString('en-IN')}`, time: t };
      if (app.status === 'REJECTED')
        return { cls: 'act-red',   badge: 'Rejected', text: `Loan #${app.id}`,        sub: 'Declined',        time: t };
      if (app.status === 'SUBMITTED' || app.status === 'PENDING')
        return { cls: 'act-amber', badge: 'Pending',  text: `Application #${app.id}`, sub: 'Awaiting review', time: t };
      return   { cls: 'act-grey',  badge: app.status, text: `Application #${app.id}`, sub: '',                time: t };
    });

  return (
    <div className="rp-page">
      <div className="rp-wrap">

        {/* Header */}
        <div className="rp-header">
          <div className="rp-header-left">
            <div className="rp-header-icon">
              <BarChart3 size={20} />
            </div>
            <div>
              <h1 className="rp-title">Reports & Analytics</h1>
              <p className="rp-sub">System performance overview</p>
            </div>
          </div>
        </div>

        {/* Error */}
        {error && (
          <div className="rp-error">
            <AlertCircle size={16} /> {error}
          </div>
        )}

        {loading ? (
          <div className="rp-loading">
            <div className="rp-spinner" />
            <p>Loading…</p>
          </div>
        ) : (
          <>
            {/* KPI Cards */}
            <div className="rp-grid">
              {cards.map(({ label, value, icon: Icon, iconCls, sub }) => (
                <div key={label} className="rp-card">
                  <div className={`rp-icon ${iconCls}`}>
                    <Icon size={17} />
                  </div>
                  <div className="rp-card-value">{value}</div>
                  <div className="rp-card-label">{label}</div>
                  <div className="rp-card-sub">{sub}</div>
                </div>
              ))}
            </div>

            {/* Recent Activity */}
            {activity.length > 0 && (
              <div className="rp-activity">
                <div className="rp-activity-header">
                  <Users size={14} />
                  <span>Recent Activity</span>
                  <span className="rp-count">{activity.length}</span>
                </div>
                <div className="rp-rows">
                  {activity.map((item, i) => (
                    <div key={i} className="rp-row">
                      <span className={`rp-badge ${item.cls}`}>{item.badge}</span>
                      <span className="rp-row-text">
                        {item.text}
                        {item.sub && <span className="rp-row-sub"> · {item.sub}</span>}
                      </span>
                      <span className="rp-row-time">{item.time}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
