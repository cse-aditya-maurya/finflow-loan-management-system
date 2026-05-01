import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createApplication } from '../api/applications';
import { Input } from '../components/Input';
import { Button } from '../components/Button';
import { DollarSign, Calendar, TrendingUp, User, ChevronRight, CheckCircle } from 'lucide-react';
import './ApplyLoanPage.css';

const LOAN_TYPES = [
  { value: 'HOME', label: 'Home Loan', icon: '🏠' },
  { value: 'EDUCATION', label: 'Education', icon: '🎓' },
  { value: 'BUSINESS', label: 'Business', icon: '💼' },
  { value: 'VEHICLE', label: 'Vehicle', icon: '🚗' },
  { value: 'PERSONAL', label: 'Personal', icon: '👤' },
  { value: 'MARRIAGE', label: 'Marriage', icon: '💍' },
];

const OCCUPATIONS = [
  { value: 'SALARIED', label: 'Salaried' },
  { value: 'SELF_EMPLOYED', label: 'Self-Employed' },
  { value: 'BUSINESS', label: 'Business Owner' },
  { value: 'STUDENT', label: 'Student' },
  { value: 'RETIRED', label: 'Retired' },
  { value: 'OTHER', label: 'Other' },
];

export function ApplyLoanPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState('type');
  const [form, setForm] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const steps = ['type', 'details', 'coapplicant', 'review'];
  const stepIdx = steps.indexOf(step);

  const handleField = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setError('');
  };

  const validateStep = () => {
    if (step === 'type' && !form.loanType) return 'Please select a loan type.';
    if (step === 'details') {
      if (!form.amount || form.amount <= 0) return 'Enter a valid loan amount.';
      if (!form.tenure || form.tenure <= 0) return 'Enter the repayment tenure.';
      if (!form.income || form.income <= 0) return 'Enter your monthly income.';
      if (!form.age || form.age < 18) return 'Age must be at least 18.';
      if (!form.occupation) return 'Select your occupation type.';
    }
    return '';
  };

  const handleNext = () => {
    const err = validateStep();
    if (err) { setError(err); return; }
    setError('');
    setStep(steps[stepIdx + 1]);
  };

  const handleSubmit = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await createApplication(form);
      navigate('/application/' + res.id, { state: { justCreated: true } });
    } catch (err) {
      const msg = err?.response?.data?.message;
      setError(msg || 'Failed to create application. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const stepLabels = ['Loan Type', 'Details', 'Co-Applicant', 'Review'];

  return (
    <div className="apply-loan-container hero-bg">
      <div className="apply-loan-content fade-in-up">
        <div className="apply-loan-header">
          <h1 className="apply-loan-title">Apply for a Loan</h1>
          <p className="apply-loan-subtitle">Fill in your details to get an instant decision</p>
        </div>

        <div className="progress-container">
          {stepLabels.map((label, i) => {
            const current = i === stepIdx;
            const done = i < stepIdx;
            return (
              <React.Fragment key={label}>
                <div className="progress-step">
                  <div className={`progress-circle ${done ? 'done' : current ? 'current' : 'pending'}`}>
                    {done ? <CheckCircle className="w-4 h-4" /> : i + 1}
                  </div>
                  <span className={`progress-label ${current ? 'current' : done ? 'done' : 'pending'}`}>
                    {label}
                  </span>
                </div>
                {i < stepLabels.length - 1 && (
                  <div className={`progress-line ${done || current ? 'active' : 'inactive'}`} />
                )}
              </React.Fragment>
            );
          })}
        </div>

        <div className="form-card glass">
          {error && (
            <div className="error-message fade-in">
              ⚠ {error}
            </div>
          )}

          {step === 'type' && (
            <div>
              <h2 className="step-title">What kind of loan do you need?</h2>
              <div className="loan-types-grid">
                {LOAN_TYPES.map((lt) => (
                  <button
                    key={lt.value}
                    id={`loan-type-${lt.value}`}
                    onClick={() => handleField('loanType', lt.value)}
                    className={`loan-type-btn ${form.loanType === lt.value ? 'selected' : 'unselected'}`}
                  >
                    <div className="loan-type-icon">{lt.icon}</div>
                    <p className="loan-type-label">{lt.label}</p>
                  </button>
                ))}
              </div>
            </div>
          )}

          {step === 'details' && (
            <div className="form-fields">
              <h2 className="step-title">Tell us about yourself</h2>
              <div className="form-grid">
                <Input
                  label="Loan Amount (₹)"
                  type="number"
                  value={form.amount || ''}
                  onChange={(e) => handleField('amount', +e.target.value)}
                  placeholder="e.g. 500000"
                  id="loan-amount"
                  min="0"
                  step="1000"
                />
                <Input
                  label="Tenure (months)"
                  type="number"
                  value={form.tenure || ''}
                  onChange={(e) => handleField('tenure', +e.target.value)}
                  placeholder="e.g. 24"
                  id="loan-tenure"
                  min="1"
                  max="360"
                />
                <Input
                  label="Monthly Income (₹)"
                  type="number"
                  value={form.income || ''}
                  onChange={(e) => handleField('income', +e.target.value)}
                  placeholder="e.g. 50000"
                  id="loan-income"
                  min="0"
                  step="1000"
                />
                <Input
                  label="Your Age"
                  type="number"
                  value={form.age || ''}
                  onChange={(e) => handleField('age', +e.target.value)}
                  placeholder="e.g. 30"
                  id="loan-age"
                  min="18"
                  max="100"
                />
              </div>
              <div className="occupation-label">
                <label>Occupation Type</label>
                <div className="occupation-grid">
                  {OCCUPATIONS.map((o) => (
                    <button
                      key={o.value}
                      id={`occupation-${o.value}`}
                      type="button"
                      onClick={() => handleField('occupation', o.value)}
                      className={`occupation-btn ${form.occupation === o.value ? 'selected' : 'unselected'}`}
                    >
                      {o.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          )}

          {step === 'coapplicant' && (
            <div className="form-fields">
              <div className="info-box">
                <User className="info-icon" />
                <div>
                  <p className="info-title">Co-Applicant (Optional)</p>
                  <p className="info-text">Adding a co-applicant with stable income can improve your loan approval chances.</p>
                </div>
              </div>
              <Input
                label="Co-Applicant Name"
                value={form.coApplicantName || ''}
                onChange={(e) => handleField('coApplicantName', e.target.value)}
                placeholder="Full name"
                id="co-name"
              />
              <Input
                label="Co-Applicant Monthly Income (₹)"
                type="number"
                value={form.coApplicantIncome || ''}
                onChange={(e) => handleField('coApplicantIncome', +e.target.value)}
                placeholder="e.g. 40000"
                id="co-income"
                min="0"
                step="1000"
              />
              <Input
                label="Co-Applicant Occupation"
                value={form.coApplicantOccupation || ''}
                onChange={(e) => handleField('coApplicantOccupation', e.target.value)}
                placeholder="e.g. Salaried, Business Owner"
                id="co-occupation"
              />
            </div>
          )}

          {step === 'review' && (
            <div>
              <h2 className="step-title">Review Your Application</h2>
              <div className="review-list">
                {[
                  { label: 'Loan Type', value: form.loanType },
                  { label: 'Amount', value: form.amount ? `₹${form.amount.toLocaleString('en-IN')}` : '—' },
                  { label: 'Tenure', value: form.tenure ? `${form.tenure} months` : '—' },
                  { label: 'Monthly Income', value: form.income ? `₹${form.income.toLocaleString('en-IN')}` : '—' },
                  { label: 'Age', value: form.age ?? '—' },
                  { label: 'Occupation', value: form.occupation ?? '—' },
                  ...(form.coApplicantName ? [{ label: 'Co-Applicant', value: form.coApplicantName }] : []),
                ].map(({ label, value }) => (
                  <div key={label} className="review-item">
                    <span className="review-label">{label}</span>
                    <span className="review-value">{value}</span>
                  </div>
                ))}
              </div>
              <div className="terms-box">
                By submitting this application, you agree to our terms and authorize FinFlow to verify your information.
              </div>
            </div>
          )}

          <div className="nav-buttons">
            {stepIdx > 0 && (
              <Button
                variant="secondary"
                onClick={() => setStep(steps[stepIdx - 1])}
                id="prev-step-btn"
              >
                ← Back
              </Button>
            )}
            {step === 'review' ? (
              <Button
                fullWidth
                loading={loading}
                onClick={handleSubmit}
                id="submit-application-btn"
              >
                Submit Application
              </Button>
            ) : (
              <Button
                fullWidth
                onClick={handleNext}
                id="next-step-btn"
              >
                {step === 'coapplicant' && !form.coApplicantName ? 'Skip & Review' : 'Next'}
              </Button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
