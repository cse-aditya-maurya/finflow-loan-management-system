const statusConfig = {
  APPROVED: {
    label: 'Approved',
    className: 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30',
    dot: 'bg-emerald-400',
  },
  REJECTED: {
    label: 'Rejected',
    className: 'bg-red-500/10 text-red-400 border border-red-500/30',
    dot: 'bg-red-400',
  },
  SUBMITTED: {
    label: 'Under Review',
    className: 'bg-blue-500/10 text-blue-400 border border-blue-500/30',
    dot: 'bg-blue-400',
  },
  PENDING: {
    label: 'Pending',
    className: 'bg-amber-500/10 text-amber-400 border border-amber-500/30',
    dot: 'bg-amber-400',
  },
  DRAFT: {
    label: 'Draft',
    className: 'bg-slate-500/10 text-slate-400 border border-slate-500/30',
    dot: 'bg-slate-400',
  },
};

export function Badge({ status, className = '' }) {
  const cfg = statusConfig[status] ?? {
    label: status,
    className: 'bg-slate-500/10 text-slate-400 border border-slate-500/30',
    dot: 'bg-slate-400',
  };

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${cfg.className} ${className}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full pulse-dot ${cfg.dot}`} />
      {cfg.label}
    </span>
  );
}
