'use client';

import Link from 'next/link';
import Header from '@/components/Header';
import Footer from '@/components/Footer';

export default function Home() {
  return (
    <>
      <Header />

      <main id="main-content" role="main">
        <section className="auth-page">
          <div className="auth-container">
            <div className="auth-section">
              <h2>환영합니다</h2>
              <p style={{ marginBottom: '2rem', color: '#666' }}>
                패스키로 더 안전하고 편리한 인증을 경험하세요
              </p>
            </div>
            <div className="button-group button-group--home">
              <Link href="/register" className="btn btn--primary">
                회원가입
              </Link>
              <Link href="/login" className="btn btn-secondary">
                로그인
              </Link>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </>
  );
}

