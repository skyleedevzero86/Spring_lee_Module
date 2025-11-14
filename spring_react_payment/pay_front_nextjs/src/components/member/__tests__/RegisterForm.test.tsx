import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { RegisterForm } from '../RegisterForm';
import { useMember } from '@/hooks/use-member';
import { ApiError } from '@/domain/types/error.types';

jest.mock('@/hooks/use-member');
jest.mock('next/navigation', () => ({
  useRouter: () => ({
    push: jest.fn(),
  }),
}));

const mockUseMember = useMember as jest.MockedFunction<typeof useMember>;

describe('RegisterForm', () => {
  const mockRegister = jest.fn();
  const mockRouterPush = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseMember.mockReturnValue({
      loading: false,
      error: null,
      register: mockRegister,
      findByEmail: jest.fn(),
      findById: jest.fn(),
      searchByName: jest.fn(),
      searchByEmail: jest.fn(),
      searchAll: jest.fn(),
      searchByNamePage: jest.fn(),
      searchByEmailPage: jest.fn(),
      searchAllPage: jest.fn(),
      resetPassword: jest.fn(),
      logout: jest.fn(),
    });

    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  it('?Œì›ê°€?????Œë”ë§?, () => {
    render(<RegisterForm />);

    expect(screen.getByLabelText('?´ë©”??)).toBeInTheDocument();
    expect(screen.getByLabelText('ë¹„ë?ë²ˆí˜¸')).toBeInTheDocument();
    expect(screen.getByLabelText('?´ë¦„')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '?Œì›ê°€?? })).toBeInTheDocument();
  });

  it('? íš¨???•ë³´ë¡??Œì›ê°€???±ê³µ', async () => {
    const user = userEvent.setup({ delay: null });
    mockRegister.mockResolvedValue({
      id: 1,
      email: 'test@example.com',
      name: '?ŒìŠ¤??,
      role: 'USER',
    });

    render(<RegisterForm />);

    await user.type(screen.getByLabelText('?´ë©”??), 'test@example.com');
    await user.type(screen.getByLabelText('ë¹„ë?ë²ˆí˜¸'), 'password123');
    await user.type(screen.getByLabelText('?´ë¦„'), '?ŒìŠ¤??);

    await user.click(screen.getByRole('button', { name: '?Œì›ê°€?? }));

    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123',
        name: '?ŒìŠ¤??,
      });
    });

    await waitFor(() => {
      expect(screen.getByText('?Œì›ê°€?…ì´ ?„ë£Œ?˜ì—ˆ?µë‹ˆ??')).toBeInTheDocument();
    });
  });

  it('?´ë©”???•ì‹ ê²€ì¦??¤íŒ¨', async () => {
    const user = userEvent.setup({ delay: null });

    render(<RegisterForm />);

    await user.type(screen.getByLabelText('?´ë©”??), 'invalid-email');
    await user.type(screen.getByLabelText('ë¹„ë?ë²ˆí˜¸'), 'password123');
    await user.type(screen.getByLabelText('?´ë¦„'), '?ŒìŠ¤??);

    await user.click(screen.getByRole('button', { name: '?Œì›ê°€?? }));

    await waitFor(() => {
      expect(screen.getByText(/?¬ë°”ë¥??´ë©”???•ì‹???„ë‹™?ˆë‹¤/)).toBeInTheDocument();
    });

    expect(mockRegister).not.toHaveBeenCalled();
  });

  it('ë¹„ë?ë²ˆí˜¸ ìµœì†Œ ê¸¸ì´ ê²€ì¦??¤íŒ¨', async () => {
    const user = userEvent.setup({ delay: null });

    render(<RegisterForm />);

    await user.type(screen.getByLabelText('?´ë©”??), 'test@example.com');
    await user.type(screen.getByLabelText('ë¹„ë?ë²ˆí˜¸'), 'short');
    await user.type(screen.getByLabelText('?´ë¦„'), '?ŒìŠ¤??);

    await user.click(screen.getByRole('button', { name: '?Œì›ê°€?? }));

    await waitFor(() => {
      expect(screen.getByText(/ìµœì†Œ 8???´ìƒ?´ì–´???©ë‹ˆ??)).toBeInTheDocument();
    });

    expect(mockRegister).not.toHaveBeenCalled();
  });

  it('?Œì›ê°€???¤íŒ¨ ???ëŸ¬ ë©”ì‹œì§€ ?œì‹œ', async () => {
    const user = userEvent.setup({ delay: null });
    const apiError = new ApiError('EMAIL_ALREADY_EXISTS', 400, '?´ë? ì¡´ì¬?˜ëŠ” ?´ë©”?¼ì…?ˆë‹¤.');
    mockRegister.mockRejectedValue(apiError);

    mockUseMember.mockReturnValue({
      loading: false,
      error: apiError,
      register: mockRegister,
      findByEmail: jest.fn(),
      findById: jest.fn(),
      searchByName: jest.fn(),
      searchByEmail: jest.fn(),
      searchAll: jest.fn(),
      searchByNamePage: jest.fn(),
      searchByEmailPage: jest.fn(),
      searchAllPage: jest.fn(),
      resetPassword: jest.fn(),
      logout: jest.fn(),
    });

    render(<RegisterForm />);

    await user.type(screen.getByLabelText('?´ë©”??), 'test@example.com');
    await user.type(screen.getByLabelText('ë¹„ë?ë²ˆí˜¸'), 'password123');
    await user.type(screen.getByLabelText('?´ë¦„'), '?ŒìŠ¤??);

    await user.click(screen.getByRole('button', { name: '?Œì›ê°€?? }));

    await waitFor(() => {
      expect(screen.getByText('?´ë? ì¡´ì¬?˜ëŠ” ?´ë©”?¼ì…?ˆë‹¤.')).toBeInTheDocument();
    });
  });

  it('ë¡œë”© ì¤‘ì¼ ??ë²„íŠ¼ ë¹„í™œ?±í™”', () => {
    mockUseMember.mockReturnValue({
      loading: true,
      error: null,
      register: mockRegister,
      findByEmail: jest.fn(),
      findById: jest.fn(),
      searchByName: jest.fn(),
      searchByEmail: jest.fn(),
      searchAll: jest.fn(),
      searchByNamePage: jest.fn(),
      searchByEmailPage: jest.fn(),
      searchAllPage: jest.fn(),
      resetPassword: jest.fn(),
      logout: jest.fn(),
    });

    render(<RegisterForm />);

    expect(screen.getByRole('button', { name: '?Œì›ê°€?? })).toBeDisabled();
    expect(screen.getByLabelText('?´ë©”??)).toBeDisabled();
    expect(screen.getByLabelText('ë¹„ë?ë²ˆí˜¸')).toBeDisabled();
    expect(screen.getByLabelText('?´ë¦„')).toBeDisabled();
  });

  it('?Œì›ê°€???±ê³µ ??2ì´????˜ì´ì§€ ?´ë™', async () => {
    const user = userEvent.setup({ delay: null });
    mockRegister.mockResolvedValue({
      id: 1,
      email: 'test@example.com',
      name: '?ŒìŠ¤??,
      role: 'USER',
    });

    const mockPush = jest.fn();
    jest.doMock('next/navigation', () => ({
      useRouter: () => ({
        push: mockPush,
      }),
    }));

    render(<RegisterForm />);

    await user.type(screen.getByLabelText('?´ë©”??), 'test@example.com');
    await user.type(screen.getByLabelText('ë¹„ë?ë²ˆí˜¸'), 'password123');
    await user.type(screen.getByLabelText('?´ë¦„'), '?ŒìŠ¤??);

    await user.click(screen.getByRole('button', { name: '?Œì›ê°€?? }));

    await waitFor(() => {
      expect(screen.getByText('?Œì›ê°€?…ì´ ?„ë£Œ?˜ì—ˆ?µë‹ˆ??')).toBeInTheDocument();
    });

    jest.advanceTimersByTime(2000);
  });
});

