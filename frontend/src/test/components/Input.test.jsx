import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '../test-utils';
import userEvent from '@testing-library/user-event';
import { Input } from '../../components/Input';

describe('Input Component', () => {
  it('renders input with label', () => {
    render(<Input label="Email" name="email" value="" onChange={vi.fn()} />);
    expect(screen.getByLabelText('Email')).toBeInTheDocument();
  });

  it('displays error message', () => {
    render(
      <Input
        label="Email"
        name="email"
        value=""
        onChange={vi.fn()}
        error="Email is required"
      />
    );
    expect(screen.getByText('Email is required')).toBeInTheDocument();
  });

  it('calls onChange when typing', async () => {
    const handleChange = vi.fn();
    const user = userEvent.setup();
    
    render(<Input label="Email" name="email" value="" onChange={handleChange} />);
    const input = screen.getByLabelText('Email');
    
    await user.type(input, 'test@example.com');
    
    expect(handleChange).toHaveBeenCalled();
  });

  it('renders with icon', () => {
    const Icon = () => <span data-testid="icon">Icon</span>;
    render(
      <Input
        label="Email"
        name="email"
        value=""
        onChange={vi.fn()}
        icon={<Icon />}
      />
    );
    expect(screen.getByTestId('icon')).toBeInTheDocument();
  });

  it('renders with right element', () => {
    const RightElement = () => <button data-testid="right-btn">Toggle</button>;
    render(
      <Input
        label="Password"
        name="password"
        value=""
        onChange={vi.fn()}
        rightElement={<RightElement />}
      />
    );
    expect(screen.getByTestId('right-btn')).toBeInTheDocument();
  });

  it('applies error styling when error exists', () => {
    const { container } = render(
      <Input
        label="Email"
        name="email"
        value=""
        onChange={vi.fn()}
        error="Invalid email"
      />
    );
    const input = screen.getByLabelText('Email');
    expect(input).toHaveClass('border-red-500/50');
  });

  it('supports different input types', () => {
    const { rerender } = render(
      <Input label="Email" name="email" type="email" value="" onChange={vi.fn()} />
    );
    expect(screen.getByLabelText('Email')).toHaveAttribute('type', 'email');
    
    rerender(
      <Input label="Password" name="password" type="password" value="" onChange={vi.fn()} />
    );
    expect(screen.getByLabelText('Password')).toHaveAttribute('type', 'password');
  });
});
