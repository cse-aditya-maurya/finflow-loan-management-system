import { describe, it, expect } from 'vitest';
import { render, screen } from '../test-utils';
import { ApplicationCard } from '../../components/ApplicationCard';

describe('ApplicationCard', () => {
  const mockApplication = {
    id: 1,
    userId: 1,
    loanType: 'PERSONAL',
    amount: 50000,
    tenure: 12,
    income: 50000,
    status: 'SUBMITTED',
    createdAt: '2024-01-15',
    updatedAt: '2024-01-15',
    age: 30,
    occupation: 'SALARIED',
  };

  it('renders application details', () => {
    render(<ApplicationCard application={mockApplication} />);
    
    expect(screen.getByText('Personal Loan')).toBeInTheDocument();
    expect(screen.getByText(/50,000/)).toBeInTheDocument();
  });



  it('renders as a link to application detail', () => {
    render(<ApplicationCard application={mockApplication} />);
    const link = screen.getByRole('link');
    expect(link).toHaveAttribute('href', '/application/1');
  });

  it('uses custom link prefix', () => {
    render(<ApplicationCard application={mockApplication} linkPrefix="/admin/applications" />);
    const link = screen.getByRole('link');
    expect(link).toHaveAttribute('href', '/admin/applications/1');
  });
});
