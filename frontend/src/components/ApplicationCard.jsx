import React from 'react';
import { Badge } from './Badge';
import { Calendar, DollarSign, Clock, ChevronRight } from 'lucide-react';
import { Link } from 'react-router-dom';

const loanTypeIcons = {
  EDUCATION: '🎓',
  HOME: '🏠',
  BUSINESS: '💼',
  VEHICLE: '🚗',
  PERSONAL: '👤',
  MARRIAGE: '💍',
};

export function ApplicationCard({ application, linkPrefix = '/application' }) {
  const icon = loanTypeIcons[application.loanType] || '📄';
  const date = new Date(application.createdAt).toLocaleDateString('en-IN', {
    year: 'numeric', month: 'short', day: 'numeric',
  });

  return (
    <Link
      to={`${linkPrefix}/${application.id}`}
      className="block glass rounded-2xl p-5 card-hover border border-slate-700/40 hover:border-blue-500/30 transition-all duration-200 group"
    >
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-xl">
            {icon}
          </div>
          <div>
            <p className="text-white font-semibold text-sm capitalize">
              {application.loanType.charAt(0) + application.loanType.slice(1).toLowerCase()} Loan
            </p>
            <p className="text-slate-500 text-xs">ID: #{application.id}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Badge status={application.status} />
          <ChevronRight className="w-4 h-4 text-slate-600 group-hover:text-blue-400 transition-colors" />
        </div>
      </div>

      <div className="grid grid-cols-3 gap-3">
        <div className="flex items-center gap-2">
          <DollarSign className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
          <div>
            <p className="text-xs text-slate-500">Amount</p>
            <p className="text-sm font-semibold text-white">
              ₹{application.amount?.toLocaleString('en-IN')}
            </p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Clock className="w-3.5 h-3.5 text-blue-400 shrink-0" />
          <div>
            <p className="text-xs text-slate-500">Tenure</p>
            <p className="text-sm font-semibold text-white">{application.tenure} mo</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Calendar className="w-3.5 h-3.5 text-slate-400 shrink-0" />
          <div>
            <p className="text-xs text-slate-500">Applied</p>
            <p className="text-sm font-semibold text-white">{date}</p>
          </div>
        </div>
      </div>
    </Link>
  );
}
