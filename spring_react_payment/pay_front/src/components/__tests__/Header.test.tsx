import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Header from '../Header/Header';
import { useAuthStore } from '@/store/authStore';
import { useAuth } from '@/hooks/useAuth';

jest.mock('@/store/authStore');
jest.mock('@/hooks/useAuth');

describe('Header', () => {
  const mockLogout = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useAuth as jest.Mock).mockReturnValue({
      logout: mockLogout,
    });
  });

  it('로그인하지 않은 사용자에게 로그인 및 회원가입 버튼을 표시해야 함', () => {
    // given
    (useAuthStore as jest.Mock).mockReturnValue({
      user: null,
      isAuthenticated: () => false,
    });

    // when
    render(
      <BrowserRouter>
        <Header />
      </BrowserRouter>
    );

    // then
    expect(screen.getByText('로그인')).toBeInTheDocument();
    expect(screen.getByText('회원가입')).toBeInTheDocument();
  });

  it('로그인한 사용자에게 사용자 이름과 로그아웃 버튼을 표시해야 함', () => {
    // given
    const userName = '테스트 사용자';
    (useAuthStore as jest.Mock).mockReturnValue({
      user: {
        userId: 1,
        email: 'test@example.com',
        name: userName,
        role: 'USER',
      },
      isAuthenticated: () => true,
    });

    // when
    render(
      <BrowserRouter>
        <Header />
      </BrowserRouter>
    );

    // then
    expect(screen.getByText(`${userName}님`)).toBeInTheDocument();
    expect(screen.getByText('로그아웃')).toBeInTheDocument();
  });

  it('로고를 클릭하면 홈으로 이동하는 링크가 있어야 함', () => {
    // given
    (useAuthStore as jest.Mock).mockReturnValue({
      user: null,
      isAuthenticated: () => false,
    });

    // when
    render(
      <BrowserRouter>
        <Header />
      </BrowserRouter>
    );

    // then
    const logoLink = screen.getByText('토스 결제').closest('a');
    expect(logoLink).toHaveAttribute('href', '/');
  });

  it('로그아웃 버튼을 클릭하면 logout 함수가 호출되어야 함', () => {
    // given
    (useAuthStore as jest.Mock).mockReturnValue({
      user: {
        userId: 1,
        email: 'test@example.com',
        name: '테스트 사용자',
        role: 'USER',
      },
      isAuthenticated: () => true,
    });

    // when
    render(
      <BrowserRouter>
        <Header />
      </BrowserRouter>
    );
    const logoutButton = screen.getByText('로그아웃');
    logoutButton.click();

    // then
    expect(mockLogout).toHaveBeenCalled();
  });
});




