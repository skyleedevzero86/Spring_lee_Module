import Link from 'next/link';
import Header from '@/components/Header';
import Footer from '@/components/Footer';

export default function Home() {
  return (
    <>
      <a href="#main-content" className="skip-link">
        본문으로 바로가기
      </a>

      <Header />

      <main id="main-content" role="main">
        <section className="auth-page">
          <div className="auth-container">
            <div className="auth-header">
              <h1>PassyKey</h1>
              <p>안전한 패스키 인증 시스템</p>
            </div>
            <div className="auth-section">
              <h2>환영합니다</h2>
              <p style={{ marginBottom: '2rem', color: '#666' }}>
                패스키로 더 안전하고 편리한 인증을 경험하세요
              </p>
              <div className="button-group">
                <Link href="/register" className="btn btn--primary">
                  회원가입
                </Link>
                <Link href="/login" className="btn btn-secondary">
                  로그인
                </Link>
              </div>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </>
  );
}

