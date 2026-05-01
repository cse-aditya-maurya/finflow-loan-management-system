import { useEffect, useState } from 'react';
import { getMyApplications } from '../api/applications';
import { ApplicationCard } from '../components/ApplicationCard';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { Button } from '../components/Button';
import { Link } from 'react-router-dom';
import { FileText, PlusCircle, Search, SlidersHorizontal } from 'lucide-react';

const STATUS_FILTERS = [
  { value: 'ALL', label: 'All' },
  { value: 'DRAFT', label: 'Drafts' },
  { value: 'SUBMITTED', label: 'Under Review' },
  { value: 'APPROVED', label: 'Approved' },
  { value: 'REJECTED', label: 'Rejected' },
];

export function MyApplicationsPage() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ALL');
  const [search, setSearch] = useState('');

  useEffect(() => {
    getMyApplications()
      .then(setApplications)
      .catch(() => setError('Failed to load applications.'))
      .finally(() => setLoading(false));
  }, []);

  const filtered = applications.filter((a) => {
    const matchStatus = filter === 'ALL' || a.status === filter;
    const matchSearch = !search || a.loanType.toLowerCase().includes(search.toLowerCase()) || String(a.id).includes(search);
    return matchStatus && matchSearch;
  });

  return (
    <div className="min-h-screen hero-bg pt-20 px-4 pb-10">
      <div className="max-w-7xl mx-auto fade-in-up">
        {/* Header */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
          <div>
            <h1 className="text-2xl font-bold text-white">My Applications</h1>
            <p className="text-slate-400 text-sm mt-1">{applications.length} total applications</p>
          </div>
          <Link to="/apply">
            <Button variant="primary" icon={<PlusCircle className="w-4 h-4" />} id="new-app-btn">
              New Application
            </Button>
          </Link>
        </div>

        {/* Filters */}
        <div className="flex flex-col sm:flex-row gap-3 mb-6">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by type or ID..."
              className="w-full bg-slate-900/60 border border-slate-700 rounded-xl pl-10 pr-4 py-2.5 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-500/60"
              id="application-search"
            />
          </div>
          <div className="flex items-center gap-2">
            <SlidersHorizontal className="w-4 h-4 text-slate-500" />
            <div className="flex gap-1">
              {STATUS_FILTERS.map((sf) => (
                <button
                  key={sf.value}
                  id={`filter-${sf.value}`}
                  onClick={() => setFilter(sf.value)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all
                    ${filter === sf.value
                      ? 'bg-blue-500/20 text-blue-300 border border-blue-500/40'
                      : 'text-slate-400 hover:text-white hover:bg-slate-800'}`}
                >
                  {sf.label}
                </button>
              ))}
            </div>
          </div>
        </div>

        {loading ? (
          <div className="flex justify-center py-20">
            <LoadingSpinner size="lg" text="Loading applications..." />
          </div>
        ) : error ? (
          <div className="text-center py-16 glass rounded-2xl border border-red-500/20">
            <p className="text-red-400">{error}</p>
          </div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-20 glass rounded-2xl border border-dashed border-slate-700">
            <FileText className="w-12 h-12 text-slate-600 mx-auto mb-4" />
            <p className="text-slate-400 font-medium mb-1">No applications found</p>
            <p className="text-slate-600 text-sm">
              {filter !== 'ALL' || search ? 'Try adjusting your filters' : 'Start your first loan application'}
            </p>
          </div>
        ) : (
          <div className="grid md:grid-cols-2 xl:grid-cols-3 gap-4">
            {filtered.map((app) => (
              <ApplicationCard key={app.id} application={app} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
