'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import Message from '@/components/Message';
import { api } from '@/lib/api';
import { prepareRegistrationOptions, arrayBufferToBase64Url, isMobileDevice } from '@/lib/webauthn';
import type { WebAuthnCredential, LoginHistory } from '@/types';

export default function DashboardPage() {
  const router = useRouter();
  const [credentials, setCredentials] = useState<WebAuthnCredential[]>([]);
  const [loginHistory, setLoginHistory] = useState<LoginHistory[]>([]);
  const [showHistory, setShowHistory] = useState(true);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState<'success' | 'error' | ''>('');
  const [isMobile, setIsMobile] = useState(false);
  const [showLabelModal, setShowLabelModal] = useState(false);
  const [labelInput, setLabelInput] = useState('');
  const [editingCredentialId, setEditingCredentialId] = useState<string | null>(null);
  const [pendingCredential, setPendingCredential] = useState<any>(null);

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
    } catch (error: any) {
      console.error('인증서 로드 오류:', error);
      if (error.message && error.message.includes('인증이 필요합니다')) {
        router.push('/login');
        return;
      }
      setCredentials([]);
    }
  };

  const getDisplayName = () => {
    if (credentials.length > 0 && credentials[0].user?.displayName) {
      return credentials[0].user.displayName;
    }
    return null;
  };

  const handleLogout = async () => {
    try {
      await api.logout();
      router.push('/login');
    } catch (error: any) {
      console.error('로그아웃 오류:', error);
      router.push('/login');
    }
  };

  const handleAddPasskey = async () => {
    setMessage('');
    setMessageType('');

    try {
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

      setPendingCredential({
            id: credential.id,
            rawId: arrayBufferToBase64Url(credential.rawId),
            response: {
              attestationObject: arrayBufferToBase64Url(response.attestationObject),
              clientDataJSON: arrayBufferToBase64Url(response.clientDataJSON),
              transports: response.getTransports ? response.getTransports() : [],
            },
            type: credential.type,
      });
      
      setLabelInput('');
      setEditingCredentialId(null);
      setShowLabelModal(true);
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

  const handleEditLabel = (credential: WebAuthnCredential) => {
    setLabelInput(credential.label || '');
    setEditingCredentialId(credential.credentialId);
    setShowLabelModal(true);
  };

  const handleLabelModalConfirm = async () => {
    const trimmedLabel = labelInput.trim() || '내 패스키';
    
    if (editingCredentialId) {
      try {
        const result = await api.updateCredentialLabel(editingCredentialId, trimmedLabel);
        if (result.success) {
          showMessage('패스키 이름이 변경되었습니다', 'success');
          loadCredentials();
        } else {
          showMessage(result.message || '패스키 이름 변경 실패', 'error');
        }
      } catch (error: any) {
        showMessage('패스키 이름 변경 실패: ' + (error.message || '알 수 없는 오류'), 'error');
      }
      setShowLabelModal(false);
      setEditingCredentialId(null);
      setLabelInput('');
    } else if (pendingCredential) {
      setShowLabelModal(false);
      
      const publicKeyCredential = {
        publicKey: {
          credential: {
            id: pendingCredential.id,
            rawId: pendingCredential.rawId,
            response: pendingCredential.response,
            type: pendingCredential.type,
          },
          label: trimmedLabel,
        },
      };

      try {
        const registerResult = await api.registerCredential(publicKeyCredential);
        if (registerResult.success) {
          showMessage('패스키가 성공적으로 등록되었습니다!', 'success');
          loadCredentials();
        } else {
          showMessage(registerResult.message || '패스키 등록 실패', 'error');
        }
      } catch (error: any) {
        showMessage('패스키 등록 실패: ' + (error.message || '알 수 없는 오류'), 'error');
      }
      setPendingCredential(null);
      setLabelInput('');
    }
  };

  const handleLabelModalCancel = () => {
    setShowLabelModal(false);
    setEditingCredentialId(null);
    setLabelInput('');
    setPendingCredential(null);
  };

  const loadLoginHistory = async () => {
    try {
      const result = await api.getLoginHistory(20);
      if (result.success && result.data) {
        setLoginHistory(result.data);
      } else {
        setLoginHistory([]);
      }
    } catch (error: any) {
      console.error('로그인 이력 로드 오류:', error);
      setLoginHistory([]);
      if (error.message && (error.message.includes('인증이 필요합니다') || 
          error.message.includes('세션이 만료') || 
          error.message.includes('Unauthorized'))) {
        router.push('/login');
        return;
      }
      setLoginHistory([]);
    }
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '정보 없음';
    try {
      let date: Date;
      const trimmed = dateStr.trim();
      
      if (trimmed.includes('T')) {
        date = new Date(trimmed);
      } else if (trimmed.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/)) {
        date = new Date(trimmed.replace(' ', 'T'));
      } else if (trimmed.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}/)) {
        date = new Date(trimmed.replace(' ', 'T') + ':00');
      } else {
        date = new Date(trimmed);
      }
      
      if (isNaN(date.getTime())) {
        console.error('유효하지 않은 날짜:', dateStr);
        return '정보 없음';
      }
      
      return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      });
    } catch (e) {
      console.error('날짜 포맷팅 오류:', e, dateStr);
      return '정보 없음';
    }
  };

  useEffect(() => {
    setIsMobile(isMobileDevice());
    loadCredentials();
    loadLoginHistory();
  }, []);

  const handleShowHistory = () => {
    setShowHistory(true);
    loadLoginHistory();
    const historySection = document.getElementById('login-history-section');
    if (historySection) {
      historySection.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  return (
    <>
      <Header
        showLogout={true}
        onLogout={handleLogout}
        showAddPasskey={true}
        onAddPasskey={handleAddPasskey}
        showLoginHistory={true}
        onShowLoginHistory={handleShowHistory}
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
                  {isMobile ? '생체 인증 추가' : '새 패스키 추가'}
                </button>
              </div>
              {isMobile && (
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
                  credentials.map((cred) => {
                    const displayName = getDisplayName();
                    const displayLabel = cred.label && cred.label.trim() 
                      ? cred.label.trim() 
                      : (displayName || '패스키');
                    
                    return (
                      <div key={cred.credentialId} className="credential-item">
                        <div className="credential-info">
                          <h3>{displayLabel}</h3>
                          <p>생성일: {formatDate(cred.createdAt)}</p>
                          <p>마지막 사용: {formatDate(cred.lastUsedAt)}</p>
                        </div>
                        <div style={{ display: 'flex', gap: '8px' }}>
                          <button
                            className="btn btn--secondary"
                            onClick={() => handleEditLabel(cred)}
                            style={{ fontSize: '0.875rem', padding: '8px 16px' }}
                          >
                            이름 변경
                          </button>
                        <button
                          className="btn btn-danger"
                          onClick={() => handleDeleteCredential(cred.credentialId)}
                        >
                          삭제
                        </button>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>

            <div id="login-history-section" className="dashboard-card" style={{ marginTop: '2rem' }}>
              <div className="dashboard-header">
                <h2>로그인 이력</h2>
                <button
                  className="btn btn--secondary"
                  onClick={() => {
                    setShowHistory(!showHistory);
                    if (!showHistory) {
                      loadLoginHistory();
                    }
                  }}
                >
                  {showHistory ? '숨기기' : '보기'}
                </button>
              </div>
              {showHistory && (
                <div className="login-history-list" style={{ marginTop: '1rem' }}>
                  {loginHistory.length === 0 ? (
                    <p>로그인 이력이 없습니다.</p>
                  ) : (
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                      <thead>
                        <tr style={{ borderBottom: '2px solid #ddd' }}>
                          <th style={{ padding: '12px', textAlign: 'left' }}>로그인 방식</th>
                          <th style={{ padding: '12px', textAlign: 'left' }}>로그인 시간</th>
                          <th style={{ padding: '12px', textAlign: 'left' }}>IP 주소</th>
                          <th style={{ padding: '12px', textAlign: 'left' }}>상태</th>
                        </tr>
                      </thead>
                      <tbody>
                        {loginHistory.map((history) => (
                          <tr key={history.id} style={{ borderBottom: '1px solid #eee' }}>
                            <td style={{ padding: '12px' }}>
                              {history.loginType === 'PASSKEY' ? (
                                <span style={{ color: '#1976d2', fontWeight: 'bold' }}>패스키</span>
                              ) : (
                                <span>비밀번호</span>
                              )}
                            </td>
                            <td style={{ padding: '12px' }}>{formatDate(history.loginAt)}</td>
                            <td style={{ padding: '12px' }}>{history.ipAddress || '정보 없음'}</td>
                            <td style={{ padding: '12px' }}>
                              {history.logoutAt ? (
                                <span style={{ color: '#666' }}>로그아웃</span>
                              ) : (
                                <span style={{ color: '#4caf50', fontWeight: 'bold' }}>로그인 중</span>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}
                </div>
              )}
            </div>
          </div>
        </section>
      </main>

      {showLabelModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '24px',
            borderRadius: '8px',
            maxWidth: '400px',
            width: '90%',
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
          }}>
            <h3 style={{ marginTop: 0, marginBottom: '16px' }}>
              {editingCredentialId ? '패스키 이름 변경' : '패스키 이름 입력'}
            </h3>
            <div className="form-group" style={{ marginBottom: '16px' }}>
              <label htmlFor="labelInput">패스키 이름</label>
              <input
                type="text"
                id="labelInput"
                value={labelInput}
                onChange={(e) => setLabelInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    handleLabelModalConfirm();
                  } else if (e.key === 'Escape') {
                    handleLabelModalCancel();
                  }
                }}
                autoFocus
                placeholder="패스키 이름을 입력하세요"
                style={{ width: '100%', padding: '8px', fontSize: '1rem' }}
              />
            </div>
            <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
              <button
                className="btn btn--secondary"
                onClick={handleLabelModalCancel}
              >
                취소
              </button>
              <button
                className="btn btn--primary"
                onClick={handleLabelModalConfirm}
              >
                확인
              </button>
            </div>
          </div>
        </div>
      )}

      <Footer />
    </>
  );
}

