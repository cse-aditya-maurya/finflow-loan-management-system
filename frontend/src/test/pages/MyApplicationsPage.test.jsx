import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '../test-utils';
import { MyApplicationsPage } from '../../pages/MyApplicationsPage';
import * as applicationsApi from '../../api/applications';

vi.mock('../../api/applications');

describe('MyApplicationsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('displays loading state', () => {
    vi.spyOn(applicationsApi, 'getMyApplications').mockImplementation(
      () => new Promise(() => {})
    );
    
    render(<MyApplicationsPage />);
    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });

  it('displays applications list', async () => {
    vi.spyOn(applicationsApi, 'getMyApplications').mockResolvedValue([
      { id: 1, loanType: 'Personal Loan', amount: 50000, status: 'SUBMITTED' },
      { id: 2, loanType: 'Home Loan', amount: 200000, status: 'APPROVED' },
    ]);
    
    render(<MyApplicationsPage />);
    
    await waitFor(() => {
      expect(screen.getByText('Personal Loan')).toBeInTheDocument();
      expect(screen.getByText('Home Loan')).toBeInTheDocument();
    });
  });

  it('displays empty state when no applications', async () => {
    vi.spyOn(applicationsApi, 'getMyApplications').mockResolvedValue([]);
    
    render(<MyApplicationsPage />);
    
    await waitFor(() => {
      expect(screen.getByText(/no applications found/i)).toBeInTheDocument();
    });
  });

  it('handles fetch error', async () => {
    vi.spyOn(applicationsApi, 'getMyApplications').mockRejectedValue(
      new Error('Failed to fetch')
    );
    
    render(<MyApplicationsPage />);
    
    await waitFor(() => {
      expect(screen.getByText(/error loading applications/i)).toBeInTheDocument();
    });
  });
});
