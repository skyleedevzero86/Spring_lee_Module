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

  it('?�원가?????�더�?, () => {
    render(<RegisterForm />);

    expect(screen.getByLabelText('?�메??)).toBeInTheDocument();
    expect(screen.getByLabelText('비�?번호')).toBeInTheDocument();
    expect(screen.getByLabelText('?�름')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '?�원가?? })).toBeInTheDocument();
  });

  it('?�효???�보�??�원가???�공', async () => {
    const user = userEvent.setup({ delay: null });
    mockRegister.mockResolvedValue({
      id: 1,
      email: 'test@example.com',
      name: '?�스??,
      role: 'USER',
    });

    render(<RegisterForm />);

    await user.type(screen.getByLabelText('?�메??), 'test@example.com');
    await user.type(screen.getByLabelText('비�?번호'), 'password123');
    await user.type(screen.getByLabelText('?�름'), '?�스??);

    await user.click(screen.getByRole('button', { name: '?�원가?? }));

    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123',
        name: '?�스??,
      });
    });

    await waitFor(() => {
      expect(screen.getByText('?�원가?�이 ?�료?�었?�니??')).toBeInTheDocument();
    });
  });

  it('?�메???�식 검�??�패', async () => {
    const user = userEvent.setup({ delay: null });

    render(<RegisterForm />);

    await user.type(screen.getByLabelText('?�메??), 'invalid-email');
    await user.type(screen.getByLabelText('비�?번호'), 'password123');
    await user.type(screen.getByLabelText('?�름'), '?�스??);

    await user.click(screen.getByRole('button', { name: '?�원가?? }));

    await waitFor(() => {
      expect(screen.getByText(/?�바�??�메???�식???�닙?�다/)).toBeInTheDocument();
    });

    expect(mockRegister).not.toHaveBeenCalled();
  });

  it('비�?번호 최소 길이 검�??�패', async () => {
    const user = userEvent.setup({ delay: null });

    render(<RegisterForm />);

    await user.type(screen.getByLabelText('?�메??), 'test@example.com');
    await user.type(screen.getByLabelText('비�?번호'), 'short');
    await user.type(screen.getByLabelText('?�름'), '?�스??);

    await user.click(screen.getByRole('button', { name: '?�원가?? }));

    await waitFor(() => {
      expect(screen.getByText(/최소 8???�상?�어???�니??)).toBeInTheDocument();
    });

    expect(mockRegister).not.toHaveBeenCalled();
  });

  it('?�원가???�패 ???�러 메시지 ?�시', async () => {
    const user = userEvent.setup({ delay: null });
    const apiError = new ApiError('EMAIL_ALREADY_EXISTS', 400, '?��? 존재?�는 ?�메?�입?�다.');
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

    await user.type(screen.getByLabelText('?�메??), 'test@example.com');
    await user.type(screen.getByLabelText('비�?번호'), 'password123');
    await user.type(screen.getByLabelText('?�름'), '?�스??);

    await user.click(screen.getByRole('button', { name: '?�원가?? }));

    await waitFor(() => {
      expect(screen.getByText('?��? 존재?�는 ?�메?�입?�다.')).toBeInTheDocument();
    });
  });

  it('로딩 중일 ??버튼 비활?�화', () => {
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

    expect(screen.getByRole('button', { name: '?�원가?? })).toBeDisabled();
    expect(screen.getByLabelText('?�메??)).toBeDisabled();
    expect(screen.getByLabelText('비�?번호')).toBeDisabled();
    expect(screen.getByLabelText('?�름')).toBeDisabled();
  });

  it('?�원가???�공 ??2�????�이지 ?�동', async () => {
    const user = userEvent.setup({ delay: null });
    mockRegister.mockResolvedValue({
      id: 1,
      email: 'test@example.com',
      name: '?�스??,
      role: 'USER',
    });

    const mockPush = jest.fn();
    jest.doMock('next/navigation', () => ({
      useRouter: () => ({
        push: mockPush,
      }),
    }));

    render(<RegisterForm />);

    await user.type(screen.getByLabelText('?�메??), 'test@example.com');
    await user.type(screen.getByLabelText('비�?번호'), 'password123');
    await user.type(screen.getByLabelText('?�름'), '?�스??);

    await user.click(screen.getByRole('button', { name: '?�원가?? }));

    await waitFor(() => {
      expect(screen.getByText('?�원가?�이 ?�료?�었?�니??')).toBeInTheDocument();
    });

    jest.advanceTimersByTime(2000);
  });
});

