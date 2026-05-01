import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getMyApplications } from '../api/applications';
import { Plus, TrendingUp, CheckCircle, Clock } from 'lucide-react';

export function UserDashboard() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadApplications();
  }, []);

  const loadApplications = async () => {
    setLoading(true);
    try {
      const data = await getMyApplications();
      setApplications(data || []);
    } catch (err) {
      setError('Failed to load applications');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'APPROVED': return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'REJECTED': return 'bg-red-50 text-red-700 border-red-200';
      case 'PENDING': return 'bg-amber-50 text-amber-700 border-amber-200';
      default: return 'bg-gray-50 text-gray-700 border-gray-200';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen hero-bg flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-blue-500/30 border-t-blue-500 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-slate-400">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  const userName = user?.profile?.name || user?.profile?.email || user?.email || 'there';
  const isNameAvailable = user?.profile?.name && user?.profile?.name !== user?.profile?.email?.split('@')[0];

  // Calculate total loan amount
  const totalLoanAmount = applications.reduce((sum, app) => sum + (app.amount || 0), 0);
  const approvedLoanAmount = applications
    .filter(app => app.status === 'APPROVED')
    .reduce((sum, app) => sum + (app.amount || 0), 0);

  return (
    <div className="min-h-screen hero-bg pt-24 py-8 px-4">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold text-white">
              {isNameAvailable ? `Welcome back, ${userName}!` : 'Welcome to Your Dashboard!'}
            </h1>
            <p className="text-slate-400 mt-1">Track and manage all your loan applications in one place</p>
          </div>
          <button
            onClick={() => navigate('/apply')}
            className="flex items-center justify-center gap-2 bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-500 hover:to-blue-400 text-white font-semibold px-6 py-3 rounded-xl transition-all shadow-lg shadow-blue-500/25 hover:shadow-blue-500/40"
          >
            <Plus className="w-5 h-5" />
            <span>New Loan Application</span>
          </button>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="mb-6 p-4 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-sm">
            {error}
          </div>
        )}

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="glass rounded-2xl p-6 border border-slate-700/40">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-xl bg-blue-500/20 flex items-center justify-center">
                <TrendingUp className="w-6 h-6 text-blue-400" />
              </div>
              <div>
                <p className="text-sm text-slate-400 font-medium">Total Applications</p>
                <h3 className="text-2xl font-bold text-white">{applications.length}</h3>
              </div>
            </div>
          </div>

          <div className="glass rounded-2xl p-6 border border-slate-700/40">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-xl bg-purple-500/20 flex items-center justify-center">
                <TrendingUp className="w-6 h-6 text-purple-400" />
              </div>
              <div>
                <p className="text-sm text-slate-400 font-medium">Total Loan Amount</p>
                <h3 className="text-xl font-bold text-white">₹{totalLoanAmount.toLocaleString('en-IN')}</h3>
              </div>
            </div>
          </div>

          <div className="glass rounded-2xl p-6 border border-slate-700/40">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-xl bg-emerald-500/20 flex items-center justify-center">
                <CheckCircle className="w-6 h-6 text-emerald-400" />
              </div>
              <div>
                <p className="text-sm text-slate-400 font-medium">Approved</p>
                <h3 className="text-2xl font-bold text-white">
                  {applications.filter(app => app.status === 'APPROVED').length}
                </h3>
              </div>
            </div>
          </div>

          <div className="glass rounded-2xl p-6 border border-slate-700/40">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-xl bg-amber-500/20 flex items-center justify-center">
                <Clock className="w-6 h-6 text-amber-400" />
              </div>
              <div>
                <p className="text-sm text-slate-400 font-medium">Pending</p>
                <h3 className="text-2xl font-bold text-white">
                  {applications.filter(app => app.status === 'PENDING' || app.status === 'SUBMITTED').length}
                </h3>
              </div>
            </div>
          </div>
        </div>

        {/* Recent Applications */}
        <div className="glass rounded-2xl border border-slate-700/40">
          <div className="p-6 border-b border-slate-700/40">
            <h2 className="text-xl font-bold text-white">Recent Applications</h2>
          </div>
          
          <div className="p-6">
            {applications.length === 0 ? (
              <div className="text-center py-12">
                <div className="w-16 h-16 rounded-full bg-slate-800 flex items-center justify-center mx-auto mb-4">
                  <TrendingUp className="w-8 h-8 text-slate-500" />
                </div>
                <p className="text-slate-400 mb-4">No applications yet</p>
                <button
                  onClick={() => navigate('/apply')}
                  className="inline-flex items-center gap-2 bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-500 hover:to-blue-400 text-white font-semibold px-6 py-3 rounded-xl transition-all shadow-lg shadow-blue-500/25"
                >
                  <Plus className="w-5 h-5" />
                  Apply for Your First Loan
                </button>
              </div>
            ) : (
              <div className="space-y-4">
                {applications.slice(0, 5).map(app => (
                  <div key={app.id} className="flex items-center justify-between p-4 rounded-xl bg-slate-800/50 border border-slate-700/40 hover:border-blue-500/40 hover:bg-slate-800/70 transition-all cursor-pointer" onClick={() => navigate(`/application/${app.id}`)}>
                    <div className="flex-1">
                      <h4 className="font-semibold text-white">{app.loanType}</h4>
                      <p className="text-sm text-slate-400 mt-1">₹{app.amount?.toLocaleString()}</p>
                      <p className="text-xs text-slate-500 mt-1">Applied on {new Date(app.createdAt).toLocaleDateString()}</p>
                    </div>
                    <span className={`px-3 py-1 rounded-lg text-xs font-semibold border ${getStatusColor(app.status)}`}>
                      {app.status}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {applications.length > 0 && (
            <div className="p-6 border-t border-slate-700/40">
              <button
                onClick={() => navigate('/my-applications')}
                className="w-full text-center text-blue-400 hover:text-blue-300 font-semibold py-2 rounded-xl hover:bg-slate-800/50 transition-all"
              >
                View All Applications
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}