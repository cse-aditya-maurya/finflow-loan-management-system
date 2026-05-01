import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '../test-utils';
import userEvent from '@testing-library/user-event';
import { Toast } from '../../components/Toast';

describe('Toast Component', () => {
  it('renders toast with message', () => {
    render(<Toast message="Test message" onClose={vi.fn()} />);
    expect(screen.getByText('Test message')).toBeInTheDocument();
  });

  it('renders success type with correct styling', () => {
    const { container } = render(
      <Toast message="Success" type="success" onClose={vi.fn()} />
    );
    expect(container.firstChild).toHaveClass('text-emerald-400');
  });

  it('renders error type with correct styling', () => {
    const { container } = render(
      <Toast message="Error" type="error" onClose={vi.fn()} />
    );
    expect(container.firstChild).toHaveClass('text-red-400');
  });

  it('calls onClose when close button clicked', async () => {
    const handleClose = vi.fn();
    const user = userEvent.setup();
    
    render(<Toast message="Test" onClose={handleClose} />);
    const closeButton = screen.getByRole('button');
    await user.click(closeButton);
    
    await waitFor(() => {
      expect(handleClose).toHaveBeenCalled();
    });
  });

  it('auto-dismisses after duration', async () => {
    const handleClose = vi.fn();
    vi.useFakeTimers();
    
    render(<Toast message="Test" duration={1000} onClose={handleClose} />);
    
    vi.advanceTimersByTime(1300);
    
    await waitFor(() => {
      expect(handleClose).toHaveBeenCalled();
    });
    
    vi.useRealTimers();
  });

  it('renders all toast types', () => {
    const types = ['success', 'error', 'warning', 'info'] as const;
    
    types.forEach(type => {
      const { unmount } = render(
        <Toast message={`${type} message`} type={type} onClose={vi.fn()} />
      );
      expect(screen.getByText(`${type} message`)).toBeInTheDocument();
      unmount();
    });
  });
});
