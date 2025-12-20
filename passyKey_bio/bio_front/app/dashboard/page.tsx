'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import Message from '@/components/Message';
import { api } from '@/lib/api';
import { prepareRegistrationOptions, arrayBufferToBase64Url, isMobileDevice } from '@/lib/webauthn';
import type { WebAuthnCredential } from '@/types';

export default function DashboardPage() {
  const router = useRouter();
  const [credentials, setCredentials] = useState<WebAuthnCredential[]>([]);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState<'success' | 'error' | ''>('');

  const showMessage = (msg: string, type: 'success' | 'error') => {
    setMessage(msg);
    setMessageType(type);
  };

  const loadCredentials = async () => {
    try {
      const result = await api.getCredentials();
      if (result.success && result.data) {
        setCredentials(result.data);
      } else {
        setCredentials([]);
      }
    } catch (error) {
      console.error('인증서 로드 오류:', error);
      setCredentials([]);
    }
  };

  const handleLogout = async () => {
    try {
      await api.logout();
      router.push('/login');
    } catch (error) {
      console.error('로그아웃 오류:', error);
      router.push('/login');
    }
  };

  const handleAddPasskey = async () => {
    setMessage('');
    setMessageType('');

    try {
      const isMobile = isMobileDevice();
      
      if (isMobile) {
        if (!navigator.credentials || !navigator.credentials.create) {
          showMessage('이 브라우저는 생체 인증을 지원하지 않습니다. 최신 브라우저를 사용해주세요.', 'error');
          return;
        }
      }

      const result = await api.getRegistrationOptions();

      if (!result.success) {
        showMessage(result.message || '등록 옵션을 가져오는데 실패했습니다', 'error');
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
        showMessage('패스키가 성공적으로 등록되었습니다!', 'success');
        loadCredentials();
      } else {
        showMessage(registerResult.message || '패스키 등록 실패', 'error');
      }
    } catch (error: any) {
      let errorMessage = error.message || '알 수 없는 오류';
      
      if (errorMessage.includes('not allowed by the user agent') || 
          errorMessage.includes('user denied permission')) {
        errorMessage = '생체 인증이 거부되었습니다. 브라우저 설정에서 생체 인증 권한을 확인하거나, 다른 인증 방법을 시도해주세요.';
      }
      
      showMessage('패스키 등록 실패: ' + errorMessage, 'error');
    }
  };

  const handleDeleteCredential = async (credentialId: string) => {
    if (!confirm('이 패스키를 삭제하시겠습니까?')) {
      return;
    }

    try {
      const result = await api.deleteCredential(credentialId);
      if (result.success) {
        showMessage('패스키가 성공적으로 삭제되었습니다', 'success');
        loadCredentials();
      } else {
        showMessage(result.message || '패스키 삭제 실패', 'error');
      }
    } catch (error: any) {
      showMessage('패스키 삭제 실패: ' + (error.message || '알 수 없는 오류'), 'error');
    }
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '정보 없음';
    try {
      const date = new Date(dateStr);
      if (isNaN(date.getTime())) return '정보 없음';
      return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch (e) {
      console.error('날짜 포맷팅 오류:', e, dateStr);
      return '정보 없음';
    }
  };

  useEffect(() => {
    loadCredentials();
  }, []);

  return (
    <>
      <a href="#main-content" className="skip-link">
        본문으로 바로가기
      </a>

      <Header
        showLogout={true}
        onLogout={handleLogout}
        showAddPasskey={true}
        onAddPasskey={handleAddPasskey}
      />

      <main id="main-content" role="main">
        <section className="auth-page">
          <div className="auth-container" style={{ maxWidth: '800px' }}>
            <div className="auth-header">
              <h1>대시보드</h1>
              <p>내 패스키를 관리하세요</p>
            </div>
            <div className="dashboard-card">
              <div className="dashboard-header">
                <h2>내 패스키</h2>
                <button
                  id="addPasskeyBtn"
                  className="btn btn--primary dashboard-add-btn"
                  onClick={handleAddPasskey}
                >
                  {isMobileDevice() ? '생체 인증 추가' : '새 패스키 추가'}
                </button>
              </div>
              {isMobileDevice() && (
                <div style={{ 
                  padding: '12px', 
                  marginBottom: '1rem', 
                  background: '#e3f2fd', 
                  borderRadius: '8px',
                  fontSize: '0.875rem',
                  color: '#1976d2'
                }}>
                  📱 모바일 모드: 지문 또는 얼굴 인식으로 등록합니다
                </div>
              )}
              <Message
                message={message}
                type={messageType}
                onClose={() => {
                  setMessage('');
                  setMessageType('');
                }}
              />
              <div id="credentialsList" className="credentials-list">
                {credentials.length === 0 ? (
                  <p>등록된 패스키가 없습니다.</p>
                ) : (
                  credentials.map((cred, index) => {
                    const displayLabel = cred.label && cred.label.trim() 
                      ? cred.label.trim() 
                      : `패스키 ${index + 1}`;
                    
                    return (
                      <div key={cred.credentialId} className="credential-item">
                        <div className="credential-info">
                          <h3>{displayLabel}</h3>
                          <p>생성일: {formatDate(cred.createdAt)}</p>
                          <p>마지막 사용: {formatDate(cred.lastUsedAt)}</p>
                        </div>
                        <button
                          className="btn btn-danger"
                          onClick={() => handleDeleteCredential(cred.credentialId)}
                        >
                          삭제
                        </button>
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </>
  );
}

