'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import Message from '@/components/Message';
import { api } from '@/lib/api';
import { prepareRegistrationOptions, arrayBufferToBase64Url, isMobileDevice } from '@/lib/webauthn';

export default function RegisterPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    displayName: '',
    password: '',
  });
  const [errors, setErrors] = useState({
    username: '',
    email: '',
  });
  const [registerMessage, setRegisterMessage] = useState('');
  const [registerMessageType, setRegisterMessageType] = useState<'success' | 'error' | ''>('');
  const [passkeyMessage, setPasskeyMessage] = useState('');
  const [passkeyMessageType, setPasskeyMessageType] = useState<'success' | 'error' | ''>('');
  const [showAddPasskey, setShowAddPasskey] = useState(false);
  const [registeredUsername, setRegisteredUsername] = useState<string | null>(null);
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    setIsMobile(isMobileDevice());
  }, []);

  const showRegisterMessage = (msg: string, type: 'success' | 'error') => {
    setRegisterMessage(msg);
    setRegisterMessageType(type);
  };

  const showPasskeyMessage = (msg: string, type: 'success' | 'error') => {
    setPasskeyMessage(msg);
    setPasskeyMessageType(type);
  };

  const handleUsernameBlur = async () => {
    if (formData.username.length < 3) return;

    try {
      const result = await api.checkUsername(formData.username);
      if (result.data) {
        setErrors((prev) => ({ ...prev, username: '이미 존재하는 사용자명입니다' }));
      } else {
        setErrors((prev) => ({ ...prev, username: '' }));
      }
    } catch (error) {
      console.error('사용자명 확인 오류:', error);
    }
  };

  const handleEmailBlur = async () => {
    if (!formData.email) return;

    try {
      const result = await api.checkEmail(formData.email);
      if (result.data) {
        setErrors((prev) => ({ ...prev, email: '이미 존재하는 이메일입니다' }));
      } else {
        setErrors((prev) => ({ ...prev, email: '' }));
      }
    } catch (error) {
      console.error('이메일 확인 오류:', error);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setRegisterMessage('');
    setRegisterMessageType('');

    try {
      const result = await api.register(formData);
      if (result.success) {
        setRegisteredUsername(formData.username);
        showRegisterMessage('등록 성공! 이제 패스키를 추가할 수 있습니다.', 'success');
        setShowAddPasskey(true);
      } else {
        showRegisterMessage(result.message || '등록 실패', 'error');
      }
    } catch (error: any) {
      showRegisterMessage('등록 실패: ' + (error.message || '알 수 없는 오류'), 'error');
    }
  };

  const handleAddPasskey = async () => {
    if (!registeredUsername) {
      showPasskeyMessage('먼저 회원가입을 완료해주세요.', 'error');
      return;
    }

    setPasskeyMessage('');
    setPasskeyMessageType('');

    try {
      if (isMobile) {
        if (!navigator.credentials || !navigator.credentials.create) {
          showPasskeyMessage('이 브라우저는 생체 인증을 지원하지 않습니다. 최신 브라우저를 사용해주세요.', 'error');
          return;
        }
      }

      const result = await api.getRegistrationOptions(registeredUsername);

      if (!result.success) {
        showPasskeyMessage(result.message || '등록 옵션을 가져오는데 실패했습니다', 'error');
        return;
      }

      const options = prepareRegistrationOptions(result.data);

      const credential = await navigator.credentials.create({
        publicKey: options,
      }) as PublicKeyCredential;

      if (!credential || !credential.response) {
        throw new Error('패스키 생성 실패');
      }

      const response = credential.response as AuthenticatorAttestationResponse;
      const label = prompt('이 패스키의 이름을 입력하세요:') || '내 패스키';

      const publicKeyCredential = {
        publicKey: {
          credential: {
            id: credential.id,
            rawId: arrayBufferToBase64Url(credential.rawId),
            response: {
              attestationObject: arrayBufferToBase64Url(response.attestationObject),
              clientDataJSON: arrayBufferToBase64Url(response.clientDataJSON),
              transports: response.getTransports ? response.getTransports() : [],
            },
            type: credential.type,
          },
          label: label,
        },
      };

      const registerResult = await api.registerCredential(publicKeyCredential);

      if (registerResult.success) {
        showPasskeyMessage('패스키가 성공적으로 등록되었습니다!', 'success');
        setTimeout(() => {
          router.push('/login');
        }, 2000);
      } else {
        showPasskeyMessage(registerResult.message || '패스키 등록 실패', 'error');
      }
    } catch (error: any) {
      let errorMessage = error.message || '알 수 없는 오류';
      
      if (errorMessage.includes('not allowed by the user agent') || 
          errorMessage.includes('user denied permission')) {
        errorMessage = '생체 인증이 거부되었습니다. 브라우저 설정에서 생체 인증 권한을 확인하거나, 다른 인증 방법을 시도해주세요.';
      }
      
      showPasskeyMessage('패스키 등록 실패: ' + errorMessage, 'error');
    }
  };

  return (
    <>
      <Header />

      <main id="main-content" role="main">
        <section className="auth-page">
          <div className="auth-container">
            <div className="auth-header">
              <h1>회원가입</h1>
              <p>PassyKey 계정을 만들어보세요</p>
            </div>
            <div className="auth-card">
              <form id="registerForm" onSubmit={handleRegister}>
                <div className="form-group">
                  <label htmlFor="username">사용자명</label>
                  <input
                    type="text"
                    id="username"
                    name="username"
                    value={formData.username}
                    onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                    onBlur={handleUsernameBlur}
                    required
                    minLength={3}
                    maxLength={50}
                  />
                  {errors.username && (
                    <span className="error-message">{errors.username}</span>
                  )}
                </div>
                <div className="form-group">
                  <label htmlFor="email">이메일</label>
                  <input
                    type="email"
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    onBlur={handleEmailBlur}
                    required
                  />
                  {errors.email && (
                    <span className="error-message">{errors.email}</span>
                  )}
                </div>
                <div className="form-group">
                  <label htmlFor="displayName">표시 이름</label>
                  <input
                    type="text"
                    id="displayName"
                    name="displayName"
                    value={formData.displayName}
                    onChange={(e) => setFormData({ ...formData, displayName: e.target.value })}
                    required
                    maxLength={100}
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="password">비밀번호</label>
                  <input
                    type="password"
                    id="password"
                    name="password"
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    required
                    minLength={6}
                  />
                </div>
                <button type="submit" className="btn btn--primary" style={{ width: '100%' }}>
                  회원가입
                </button>
              </form>

              <Message
                message={registerMessage}
                type={registerMessageType}
                onClose={() => {
                  setRegisterMessage('');
                  setRegisterMessageType('');
                }}
              />

              <div className="divider">
                <span>회원가입 후 패스키를 추가하세요</span>
              </div>

              {showAddPasskey && (
                <>
                  {isMobile && (
                    <div style={{ 
                      padding: '12px', 
                      marginTop: '1rem',
                      marginBottom: '1rem', 
                      background: '#e3f2fd', 
                      borderRadius: '8px',
                      fontSize: '0.875rem',
                      color: '#1976d2'
                    }}>
                      📱 모바일 모드: 지문 또는 얼굴 인식으로 등록합니다
                    </div>
                  )}
                  <button
                    id="addPasskeyBtn"
                    className="btn btn-secondary"
                    style={{ width: '100%', marginTop: '1rem' }}
                    onClick={handleAddPasskey}
                  >
                    {isMobile ? '생체 인증 등록' : '패스키 추가'}
                  </button>
                </>
              )}

              <Message
                message={passkeyMessage}
                type={passkeyMessageType}
                onClose={() => {
                  setPasskeyMessage('');
                  setPasskeyMessageType('');
                }}
              />

              <div className="link-section">
                <Link href="/login">이미 계정이 있으신가요? 로그인</Link>
              </div>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </>
  );
}

