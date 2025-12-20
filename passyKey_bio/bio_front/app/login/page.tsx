'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import Message from '@/components/Message';
import { api } from '@/lib/api';
import { prepareAuthenticationOptions, arrayBufferToBase64Url, isMobileDevice } from '@/lib/webauthn';

export default function LoginPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<'password' | 'passkey'>('password');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [passkeyUsername, setPasskeyUsername] = useState('');
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState<'success' | 'error' | ''>('');
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    setIsMobile(isMobileDevice());
  }, []);

  const showMessage = (msg: string, type: 'success' | 'error') => {
    setMessage(msg);
    setMessageType(type);
  };

  const handlePasswordLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage('');
    setMessageType('');
    
    if (!username || !password) {
      showMessage('사용자명과 비밀번호를 입력해주세요', 'error');
      return;
    }

    try {
      const formData = new FormData();
      formData.append('username', username);
      formData.append('password', password);

      const response = await fetch('/api/auth/login', {
        method: 'POST',
        body: formData,
        credentials: 'include',
        redirect: 'manual',
      });

      if (response.status === 200 || response.status === 302 || response.type === 'opaqueredirect') {
        showMessage('로그인 성공! 리다이렉트 중...', 'success');
        setTimeout(() => {
          router.push('/dashboard');
        }, 1000);
      } else if (response.status === 401 || response.status === 403) {
        const errorText = await response.text();
        try {
          const errorJson = JSON.parse(errorText);
          if (errorJson.message && (errorJson.message.includes('중복 로그인') || errorJson.message.includes('다른 세션'))) {
            showMessage('이미 다른 곳에서 로그인되어 있습니다. 중복 로그인은 허용되지 않습니다.', 'error');
          } else {
            showMessage(errorJson.message || '로그인 실패: 사용자명 또는 비밀번호가 올바르지 않습니다', 'error');
          }
        } catch {
          showMessage('로그인 실패: 사용자명 또는 비밀번호가 올바르지 않습니다', 'error');
        }
      } else {
        const errorText = await response.text();
        try {
          const errorJson = JSON.parse(errorText);
          showMessage(errorJson.message || '로그인 실패', 'error');
        } catch {
          showMessage('로그인 실패: 서버 오류가 발생했습니다', 'error');
        }
      }
    } catch (error: any) {
      showMessage('로그인 실패: ' + (error.message || '알 수 없는 오류'), 'error');
    }
  };

  const handlePasskeyLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage('');
    setMessageType('');

    try {
      if (isMobile) {
        if (!navigator.credentials || !navigator.credentials.get) {
          showMessage('이 브라우저는 생체 인증을 지원하지 않습니다. 최신 브라우저를 사용해주세요.', 'error');
          return;
        }
      }

      const optionsResult = await api.getAuthenticationOptions(passkeyUsername || undefined);

      if (!optionsResult.success) {
        showMessage(optionsResult.message || '인증 옵션을 가져오는데 실패했습니다', 'error');
        return;
      }

      const options = prepareAuthenticationOptions(optionsResult.data);

      const assertion = await navigator.credentials.get({
        publicKey: options,
      }) as PublicKeyCredential;

      if (!assertion || !assertion.response) {
        throw new Error('인증 실패');
      }

      const response = assertion.response as AuthenticatorAssertionResponse;
      const clientDataJSONEncoded = arrayBufferToBase64Url(response.clientDataJSON);

      const authenticationRequest = {
        id: assertion.id,
        rawId: arrayBufferToBase64Url(assertion.rawId),
        response: {
          authenticatorData: arrayBufferToBase64Url(response.authenticatorData),
          clientDataJSON: clientDataJSONEncoded,
          signature: arrayBufferToBase64Url(response.signature),
          userHandle: response.userHandle ? arrayBufferToBase64Url(response.userHandle) : null,
        },
      };

      const authResult = await api.authenticate(authenticationRequest);

      if (authResult.success && authResult.data?.authenticated) {
        if (authResult.data?.passkeyLogin) {
          showMessage('패스키로 로그인되었습니다! 리다이렉트 중...', 'success');
        } else {
          showMessage('인증 성공! 리다이렉트 중...', 'success');
        }
        setTimeout(() => {
          router.push(authResult.data?.redirectUrl || '/dashboard');
        }, 1000);
      } else {
        showMessage(authResult.message || '인증 실패', 'error');
      }
    } catch (error: any) {
      let errorMessage = error.message || '알 수 없는 오류';
      if (errorMessage.includes('중복 로그인') || errorMessage.includes('다른 세션')) {
        errorMessage = '이미 다른 곳에서 로그인되어 있습니다. 중복 로그인은 허용되지 않습니다.';
      } else if (errorMessage.includes('not allowed by the user agent') || 
          errorMessage.includes('user denied permission') ||
          errorMessage.includes('The request is not allowed')) {
        errorMessage = '생체 인증이 거부되었습니다. 브라우저 설정에서 생체 인증 권한을 확인하거나, 다른 인증 방법을 시도해주세요.';
      }
      showMessage('인증 실패: ' + errorMessage, 'error');
    }
  };

  return (
    <>
      <Header />

      <main id="main-content" role="main">
        <section className="auth-page">
          <div className="auth-container">
            <div className="auth-header">
              <h1>로그인</h1>
              <p>PassyKey에 오신 것을 환영합니다</p>
            </div>
            <div className="auth-card">
              <div className="tab-container">
                <button
                  className={`tab-btn ${activeTab === 'password' ? 'active' : ''}`}
                  onClick={() => setActiveTab('password')}
                >
                  비밀번호 로그인
                </button>
                <button
                  className={`tab-btn ${activeTab === 'passkey' ? 'active' : ''}`}
                  onClick={() => setActiveTab('passkey')}
                >
                  패스키 로그인
                </button>
              </div>

              <div
                id="passwordTab"
                className={`tab-content ${activeTab === 'password' ? 'active' : ''}`}
              >
                <form id="passwordLoginForm" onSubmit={handlePasswordLogin}>
                  <div className="form-group">
                    <label htmlFor="username">사용자명</label>
                    <input
                      type="text"
                      id="username"
                      name="username"
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="password">비밀번호</label>
                    <input
                      type="password"
                      id="password"
                      name="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                    />
                  </div>
                  <button type="submit" className="btn btn--primary" style={{ width: '100%' }}>
                    로그인
                  </button>
                </form>
                <Message
                  message={message}
                  type={messageType}
                  onClose={() => {
                    setMessage('');
                    setMessageType('');
                  }}
                />
              </div>

              <div
                id="passkeyTab"
                className={`tab-content ${activeTab === 'passkey' ? 'active' : ''}`}
              >
                {isMobile && (
                  <div style={{ 
                    padding: '12px', 
                    marginBottom: '1rem', 
                    background: '#e3f2fd', 
                    borderRadius: '8px',
                    fontSize: '0.875rem',
                    color: '#1976d2'
                  }}>
                    📱 모바일 모드: 지문 또는 얼굴 인식으로 로그인합니다
                  </div>
                )}
                <form id="passkeyLoginForm" onSubmit={handlePasskeyLogin}>
                  <div className="form-group">
                    <label htmlFor="passkeyUsername">사용자명 (선택사항)</label>
                    <input
                      type="text"
                      id="passkeyUsername"
                      name="username"
                      value={passkeyUsername}
                      onChange={(e) => setPasskeyUsername(e.target.value)}
                    />
                    <small>등록된 패스키를 사용하려면 비워두세요</small>
                  </div>
                  <button type="submit" className="btn btn--primary" style={{ width: '100%' }}>
                    {isMobile ? '생체 인증으로 로그인' : '패스키로 로그인'}
                  </button>
                </form>
                <Message
                  message={message}
                  type={messageType}
                  onClose={() => {
                    setMessage('');
                    setMessageType('');
                  }}
                />
              </div>

              <div className="link-section">
                <Link href="/register">계정이 없으신가요? 회원가입</Link>
              </div>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </>
  );
}

