import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { RegisterForm } from '../RegisterForm';
import { useMember } from '@/src/hooks/use-member';
import { ApiError } from '@/src/domain/types/error.types';

jest.mock('@/src/hooks/use-member');
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

  it('회원가입 폼 렌더링', () => {
    render(<RegisterForm />);

    expect(screen.getByLabelText('이메일')).toBeInTheDocument();
    expect(screen.getByLabelText('비밀번호')).toBeInTheDocument();
    expect(screen.getByLabelText('이름')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '회원가입' })).toBeInTheDocument();
  });

  it('유효한 정보로 회원가입 성공', async () => {
    const user = userEvent.setup({ delay: null });
    mockRegister.mockResolvedValue({
      id: 1,
      email: 'test@example.com',
      name: '테스트',
      role: 'USER',
    });

    render(<RegisterForm />);

    await user.type(screen.getByLabelText('이메일'), 'test@example.com');
    await user.type(screen.getByLabelText('비밀번호'), 'password123');
    await user.type(screen.getByLabelText('이름'), '테스트');

    await user.click(screen.getByRole('button', { name: '회원가입' }));

    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123',
        name: '테스트',
      });
    });

    await waitFor(() => {
      expect(screen.getByText('회원가입이 완료되었습니다.')).toBeInTheDocument();
    });
  });

  it('이메일 형식 검증 실패', async () => {
    const user = userEvent.setup({ delay: null });

    render(<RegisterForm />);

    await user.type(screen.getByLabelText('이메일'), 'invalid-email');
    await user.type(screen.getByLabelText('비밀번호'), 'password123');
    await user.type(screen.getByLabelText('이름'), '테스트');

    await user.click(screen.getByRole('button', { name: '회원가입' }));

    await waitFor(() => {
      expect(screen.getByText(/올바른 이메일 형식이 아닙니다/)).toBeInTheDocument();
    });

    expect(mockRegister).not.toHaveBeenCalled();
  });

  it('비밀번호 최소 길이 검증 실패', async () => {
    const user = userEvent.setup({ delay: null });

    render(<RegisterForm />);

    await user.type(screen.getByLabelText('이메일'), 'test@example.com');
    await user.type(screen.getByLabelText('비밀번호'), 'short');
    await user.type(screen.getByLabelText('이름'), '테스트');

    await user.click(screen.getByRole('button', { name: '회원가입' }));

    await waitFor(() => {
      expect(screen.getByText(/최소 8자 이상이어야 합니다/)).toBeInTheDocument();
    });

    expect(mockRegister).not.toHaveBeenCalled();
  });

  it('회원가입 실패 시 에러 메시지 표시', async () => {
    const user = userEvent.setup({ delay: null });
    const apiError = new ApiError('EMAIL_ALREADY_EXISTS', 400, '이미 존재하는 이메일입니다.');
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

    await user.type(screen.getByLabelText('이메일'), 'test@example.com');
    await user.type(screen.getByLabelText('비밀번호'), 'password123');
    await user.type(screen.getByLabelText('이름'), '테스트');

    await user.click(screen.getByRole('button', { name: '회원가입' }));

    await waitFor(() => {
      expect(screen.getByText('이미 존재하는 이메일입니다.')).toBeInTheDocument();
    });
  });

  it('로딩 중일 때 버튼 비활성화', () => {
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

    expect(screen.getByRole('button', { name: '회원가입' })).toBeDisabled();
    expect(screen.getByLabelText('이메일')).toBeDisabled();
    expect(screen.getByLabelText('비밀번호')).toBeDisabled();
    expect(screen.getByLabelText('이름')).toBeDisabled();
  });

  it('회원가입 성공 후 2초 뒤 페이지 이동', async () => {
    const user = userEvent.setup({ delay: null });
    mockRegister.mockResolvedValue({
      id: 1,
      email: 'test@example.com',
      name: '테스트',
      role: 'USER',
    });

    const mockPush = jest.fn();
    jest.doMock('next/navigation', () => ({
      useRouter: () => ({
        push: mockPush,
      }),
    }));

    render(<RegisterForm />);

    await user.type(screen.getByLabelText('이메일'), 'test@example.com');
    await user.type(screen.getByLabelText('비밀번호'), 'password123');
    await user.type(screen.getByLabelText('이름'), '테스트');

    await user.click(screen.getByRole('button', { name: '회원가입' }));

    await waitFor(() => {
      expect(screen.getByText('회원가입이 완료되었습니다.')).toBeInTheDocument();
    });

    jest.advanceTimersByTime(2000);
  });
});

