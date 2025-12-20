'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

interface HeaderProps {
  showLogout?: boolean;
  onLogout?: () => void;
  showAddPasskey?: boolean;
  onAddPasskey?: () => void;
  showLoginHistory?: boolean;
  onShowLoginHistory?: () => void;
}

export default function Header({ 
  showLogout = false, 
  onLogout,
  showAddPasskey = false,
  onAddPasskey,
  showLoginHistory = false,
  onShowLoginHistory
}: HeaderProps) {
  const router = useRouter();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const toggleMobileMenu = () => {
    setMobileMenuOpen(!mobileMenuOpen);
  };

  const handleLogoClick = (e: React.MouseEvent<HTMLAnchorElement>) => {
    if (showLogout) {
      e.preventDefault();
      router.push('/dashboard');
    }
  };

  return (
    <header className="header" role="banner">
      <div className="header__container">
        <div className="header__logo">
          <Link 
            href={showLogout ? "/dashboard" : "/"} 
            aria-label={showLogout ? "대시보드로 이동" : "홈으로 이동"}
            onClick={handleLogoClick}
          >
            PassyKey
          </Link>
        </div>
        {showLogout && (
          <div className="header__utils">
            {showLoginHistory && onShowLoginHistory && (
              <button 
                onClick={onShowLoginHistory} 
                className="btn btn--secondary"
                style={{ marginRight: '0.5rem' }}
              >
                로그인 이력
              </button>
            )}
            <button onClick={onLogout} className="btn btn-secondary">
              로그아웃
            </button>
          </div>
        )}
        <button
          className="header__mobile-toggle"
          aria-label="모바일 메뉴 토글"
          aria-expanded={mobileMenuOpen}
          onClick={toggleMobileMenu}
        >
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>
      {(showLogout || showAddPasskey || showLoginHistory) && (
        <nav 
          className="header__mobile-menu" 
          aria-hidden={!mobileMenuOpen}
        >
          <div className="mobile-menu__content">
            {showLoginHistory && onShowLoginHistory && (
              <button 
                onClick={onShowLoginHistory} 
                className="btn btn--secondary mobile-menu__btn"
              >
                로그인 이력
              </button>
            )}
            {showAddPasskey && (
              <button 
                onClick={onAddPasskey} 
                className="btn btn--primary mobile-menu__btn mobile-add-passkey"
              >
                새 패스키 추가
              </button>
            )}
            {showLogout && (
              <button 
                onClick={onLogout} 
                className="btn btn-secondary mobile-menu__btn"
              >
                로그아웃
              </button>
            )}
          </div>
        </nav>
      )}
    </header>
  );
}

