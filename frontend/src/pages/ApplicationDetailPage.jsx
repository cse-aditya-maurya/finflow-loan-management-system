import { useEffect, useState } from 'react';
import { useParams, useLocation, Link } from 'react-router-dom';
import { getApplicationById, submitApplication } from '../api/applications';
import { uploadDocument, replaceDocument, getDocumentsByApplication } from '../api/documents';
import { Badge } from '../components/Badge';
import { Button } from '../components/Button';
import { LoadingSpinner } from '../components/LoadingSpinner';
import {
  DollarSign, Calendar, TrendingUp, User, Briefcase, ArrowLeft, Send, CheckCircle, Upload, FileText,
} from 'lucide-react';

const REQUIRED_DOCS = {
  HOME: ['AADHAR', 'PAN', 'PROPERTY_DOCUMENT', 'LAND_REGISTRATION', 'INCOME_PROOF'],
  EDUCATION: ['AADHAR', 'PAN', 'STUDENT_ID', 'ADMISSION_LETTER'],
  BUSINESS: ['AADHAR', 'PAN', 'GST_CERTIFICATE', 'BUSINESS_PROOF', 'INCOME_PROOF'],
  VEHICLE: ['AADHAR', 'PAN', 'VEHICLE_QUOTATION', 'INCOME_PROOF'],
  MARRIAGE: ['AADHAR', 'PAN', 'MARRIAGE_PROOF', 'INCOME_PROOF'],
  PERSONAL: ['AADHAR', 'PAN', 'INCOME_PROOF'],
};

export function ApplicationDetailPage() {
  const { id } = useParams();
  const location = useLocation();
  const [app, setApp] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(
    location.state?.justCreated
      ? 'Application created! Upload your documents below, then submit.'
      : ''
  );
  const [documents, setDocuments] = useState([]);
  const [uploading, setUploading] = useState({});
  const [uploadError, setUploadError] = useState('');

  useEffect(() => {
    if (!id) return;
    getApplicationById(+id)
      .then((a) => {
        setApp(a);
        return getDocumentsByApplication(+id);
      })
      .then(setDocuments)
      .catch(() => setError('Failed to load application.'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleFileUpload = async (docType, file) => {
    if (!app) return;
    setUploading((p) => ({ ...p, [docType]: true }));
    setUploadError('');
    try {
      const uploaded = documents.find((d) => d.documentType === docType);
      if (uploaded) {
        await replaceDocument(uploaded.id, file);
      } else {
        await uploadDocument(app.id, app.loanType, docType, file);
      }
      const updated = await getDocumentsByApplication(app.id);
      setDocuments(updated);
    } catch (err) {
      const msg = err?.response?.data?.message;
      setUploadError(msg || `Failed to upload ${docType}`);
    } finally {
      setUploading((p) => ({ ...p, [docType]: false }));
    }
  };

  const handleSubmit = async () => {
    if (!app) return;
    setSubmitting(true);
    setError('');
    try {
      const updated = await submitApplication(app.id);
      setApp(updated);
      setSuccess('Application submitted successfully! We are reviewing it.');
    } catch (err) {
      const msg = err?.response?.data?.message;
      setError(msg || 'Failed to submit application.');
    } finally {
      setSubmitting(false);
    }
  };

  const fields = app
    ? [
        { label: 'Application ID', value: `#${app.id}`, icon: <CheckCircle className="w-4 h-4" /> },
        { label: 'Loan Type', value: app.loanType, icon: <Briefcase className="w-4 h-4" /> },
        { label: 'Amount', value: `₹${app.amount?.toLocaleString('en-IN')}`, icon: <DollarSign className="w-4 h-4" /> },
        { label: 'Tenure', value: `${app.tenure} months`, icon: <Calendar className="w-4 h-4" /> },
        { label: 'Monthly Income', value: `₹${app.income?.toLocaleString('en-IN')}`, icon: <TrendingUp className="w-4 h-4" /> },
        { label: 'Age', value: app.age, icon: <User className="w-4 h-4" /> },
        { label: 'Occupation', value: app.occupation ?? '—', icon: <Briefcase className="w-4 h-4" /> },
        ...(app.coApplicantName
          ? [
              { label: 'Co-Applicant', value: app.coApplicantName, icon: <User className="w-4 h-4" /> },
              { label: 'Co-Applicant Income', value: `₹${(app.coApplicantIncome ?? 0).toLocaleString('en-IN')}`, icon: <TrendingUp className="w-4 h-4" /> },
            ]
          : []),
      ]
    : [];

  return (
    <div className="min-h-screen hero-bg pt-20 px-4 pb-10">
      <div className="max-w-2xl mx-auto fade-in-up">
        <Link to="/my-applications" className="inline-flex items-center gap-2 text-slate-400 hover:text-white text-sm mb-6 transition-colors">
          <ArrowLeft className="w-4 h-4" /> Back to Applications
        </Link>

        {loading ? (
          <div className="flex justify-center py-20">
            <LoadingSpinner size="lg" text="Loading application..." />
          </div>
        ) : !app ? (
          <div className="text-center py-16 glass rounded-2xl border border-red-500/20">
            <p className="text-red-400">{error || 'Application not found.'}</p>
          </div>
        ) : (
          <>
            {/* Status banner */}
            <div className="glass rounded-2xl p-6 border border-slate-700/40 mb-5">
              <div className="flex items-start justify-between mb-4">
                <div>
                  <h1 className="text-xl font-bold text-white">
                    {app.loanType.charAt(0) + app.loanType.slice(1).toLowerCase()} Loan Application
                  </h1>
                  <p className="text-slate-500 text-sm">Created {new Date(app.createdAt).toLocaleDateString('en-IN', { year: 'numeric', month: 'long', day: 'numeric' })}</p>
                </div>
                <Badge status={app.status} />
              </div>

              {/* Amount big display */}
              <div className="text-center py-6 border border-dashed border-slate-700 rounded-xl">
                <p className="text-slate-400 text-sm mb-1">Requested Amount</p>
                <p className="text-4xl font-black gradient-text">₹{app.amount?.toLocaleString('en-IN')}</p>
                <p className="text-slate-500 text-sm mt-1">{app.tenure} months tenure</p>
              </div>
            </div>

            {success && (
              <div className="mb-5 p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-sm fade-in">
                ✓ {success}
              </div>
            )}
            {error && (
              <div className="mb-5 p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-sm fade-in">
                ⚠ {error}
              </div>
            )}

            {/* Details */}
            <div className="glass rounded-2xl p-6 border border-slate-700/40 mb-5">
              <h2 className="text-base font-semibold text-white mb-4">Application Details</h2>
              <div className="space-y-3">
                {fields.map(({ label, value, icon }) => (
                  <div key={label} className="flex justify-between items-center py-2.5 border-b border-slate-800/60 last:border-0">
                    <span className="flex items-center gap-2 text-slate-400 text-sm">
                      <span className="text-slate-600">{icon}</span>
                      {label}
                    </span>
                    <span className="text-white font-semibold text-sm capitalize">{String(value)}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Document Upload */}
            {app && (app.status === 'DRAFT' || app.status === 'SUBMITTED') && (
              <div className="glass rounded-2xl p-6 border border-slate-700/40 mb-5">
                <h2 className="text-base font-semibold text-white mb-1">Required Documents</h2>
                <p className="text-slate-500 text-xs mb-4">Upload all required documents for your {app.loanType.toLowerCase()} loan</p>

                {uploadError && (
                  <div className="mb-4 p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-sm">
                    ⚠ {uploadError}
                  </div>
                )}

                <div className="space-y-3">
                  {(REQUIRED_DOCS[app.loanType] ?? []).map((docType) => {
                    const uploaded = documents.find((d) => d.documentType === docType);
                    const isUploading = uploading[docType];
                    return (
                      <div key={docType} className="flex items-center justify-between p-3 rounded-xl bg-slate-800/40 border border-slate-700/40">
                        <div className="flex items-center gap-3">
                          <FileText className="w-4 h-4 text-slate-500" />
                          <div>
                            <p className="text-sm text-white font-medium">{docType.replace(/_/g, ' ')}</p>
                            {uploaded && (
                              <p className={`text-xs mt-0.5 ${uploaded.status === 'VERIFIED' ? 'text-emerald-400' : uploaded.status === 'REJECTED' ? 'text-red-400' : 'text-amber-400'}`}>
                                {uploaded.status}
                              </p>
                            )}
                          </div>
                        </div>
                        <label className={`cursor-pointer flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${uploaded ? 'bg-slate-700 text-slate-300 hover:bg-slate-600' : 'bg-blue-600 text-white hover:bg-blue-500'}`}>
                          {isUploading ? (
                            <span>Uploading...</span>
                          ) : (
                            <>
                              <Upload className="w-3 h-3" />
                              {uploaded ? 'Replace' : 'Upload'}
                            </>
                          )}
                          <input
                            type="file"
                            className="hidden"
                            accept=".pdf,.jpg,.jpeg,.png"
                            disabled={isUploading}
                            onChange={(e) => {
                              const f = e.target.files?.[0];
                              if (f) handleFileUpload(docType, f);
                              e.target.value = '';
                            }}
                          />
                        </label>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Action */}
            {app.status === 'DRAFT' && (
              <Button
                fullWidth
                size="lg"
                loading={submitting}
                onClick={handleSubmit}
                icon={<Send className="w-4 h-4" />}
                id="submit-application-action-btn"
              >
                Submit Application for Review
              </Button>
            )}
          </>
        )}
      </div>
    </div>
  );
}
