'use client';

import { useState } from 'react';
import Link from 'next/link';

interface HeaderProps {
  showLogout?: boolean;
  onLogout?: () => void;
  showAddPasskey?: boolean;
  onAddPasskey?: () => void;
}

export default function Header({ 
  showLogout = false, 
  onLogout,
  showAddPasskey = false,
  onAddPasskey 
}: HeaderProps) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const toggleMobileMenu = () => {
    setMobileMenuOpen(!mobileMenuOpen);
  };

  return (
    <header className="header" role="banner">
      <div className="header__container">
        <div className="header__logo">
          <Link href="/" aria-label="홈으로 이동">
            PassyKey
          </Link>
        </div>
        {showLogout && (
          <div className="header__utils">
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
      {(showLogout || showAddPasskey) && (
        <nav 
          className="header__mobile-menu" 
          aria-hidden={!mobileMenuOpen}
        >
          <div className="mobile-menu__content">
            {showLogout && (
              <button 
                onClick={onLogout} 
                className="btn btn-secondary mobile-menu__btn"
              >
                로그아웃
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
          </div>
        </nav>
      )}
    </header>
  );
}

