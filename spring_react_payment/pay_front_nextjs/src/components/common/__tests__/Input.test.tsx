import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Input } from '../Input';

describe('Input', () => {
  it('should render input with label', () => {
    render(<Input label="Test Label" />);

    expect(screen.getByLabelText('Test Label')).toBeInTheDocument();
  });

  it('should render input without label', () => {
    render(<Input />);

    const input = screen.getByRole('textbox');
    expect(input).toBeInTheDocument();
  });

  it('should display error message when error prop is provided', () => {
    render(<Input label="Test Label" error="This is an error" />);

    expect(screen.getByText('This is an error')).toBeInTheDocument();
    expect(screen.getByRole('textbox')).toHaveAttribute('aria-invalid', 'true');
  });

  it('should display helper text when helperText prop is provided', () => {
    render(<Input label="Test Label" helperText="This is helper text" />);

    expect(screen.getByText('This is helper text')).toBeInTheDocument();
  });

  it('should not display helper text when error is present', () => {
    render(
      <Input
        label="Test Label"
        error="This is an error"
        helperText="This is helper text"
      />
    );

    expect(screen.getByText('This is an error')).toBeInTheDocument();
    expect(screen.queryByText('This is helper text')).not.toBeInTheDocument();
  });

  it('should be disabled when disabled prop is true', () => {
    render(<Input label="Test Label" disabled />);

    expect(screen.getByRole('textbox')).toBeDisabled();
  });

  it('should apply error styles when error is present', () => {
    render(<Input label="Test Label" error="Error" />);

    const input = screen.getByRole('textbox');
    expect(input).toHaveClass('border-red-300');
  });

  it('should forward ref', () => {
    const ref = { current: null };
    render(<Input ref={ref} />);

    expect(ref.current).toBeInstanceOf(HTMLInputElement);
  });

  it('should handle user input', async () => {
    const user = userEvent.setup();
    render(<Input label="Test Label" />);

    const input = screen.getByRole('textbox');
    await user.type(input, 'test input');

    expect(input).toHaveValue('test input');
  });

  it('should pass through additional props', () => {
    render(<Input label="Test Label" data-testid="custom-input" />);

    expect(screen.getByTestId('custom-input')).toBeInTheDocument();
  });
});

