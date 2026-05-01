import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '../test-utils';
import userEvent from '@testing-library/user-event';
import { ApplyLoanPage } from '../../pages/ApplyLoanPage';
import * as applicationsApi from '../../api/applications';

vi.mock('../../api/applications');

describe('ApplyLoanPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders loan application form', () => {
    render(<ApplyLoanPage />);
    
    expect(screen.getByLabelText(/loan type/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/amount/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /submit/i })).toBeInTheDocument();
  });

  it('validates required fields', async () => {
    const user = userEvent.setup();
    render(<ApplyLoanPage />);
    
    await user.click(screen.getByRole('button', { name: /submit/i }));
    
    expect(screen.getByText(/please fill all required fields/i)).toBeInTheDocument();
  });

  it('submits loan application successfully', async () => {
    const user = userEvent.setup();
    const mockCreate = vi.spyOn(applicationsApi, 'createApplication').mockResolvedValue({
      id: 1,
      status: 'DRAFT',
    });
    
    render(<ApplyLoanPage />);
    
    await user.type(screen.getByLabelText(/amount/i), '50000');
    await user.click(screen.getByRole('button', { name: /submit/i }));
    
    await waitFor(() => {
      expect(mockCreate).toHaveBeenCalled();
    });
  });

  it('handles submission error', async () => {
    const user = userEvent.setup();
    vi.spyOn(applicationsApi, 'createApplication').mockRejectedValue({
      response: { data: { message: 'Submission failed' } },
    });
    
    render(<ApplyLoanPage />);
    
    await user.type(screen.getByLabelText(/amount/i), '50000');
    await user.click(screen.getByRole('button', { name: /submit/i }));
    
    await waitFor(() => {
      expect(screen.getByText(/submission failed/i)).toBeInTheDocument();
    });
  });
});
