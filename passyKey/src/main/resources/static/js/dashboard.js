document.addEventListener('DOMContentLoaded', function() {
    const addPasskeyBtn = document.getElementById('addPasskeyBtn');
    const passkeyMessage = document.getElementById('passkeyMessage');
    const credentialsList = document.getElementById('credentialsList');
    const logoutBtn = document.getElementById('logoutBtn');

    function showMessage(element, message, type) {
        element.textContent = message;
        element.className = 'message ' + type;
        setTimeout(() => {
            element.className = 'message';
            element.textContent = '';
        }, 5000);
    }

    function getCsrfToken() {
        return document.querySelector('meta[name="_csrf"]')?.content || 
               document.querySelector('input[name="_csrf"]')?.value || '';
    }

    function getCsrfHeaderName() {
        return document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    }

    async function loadCredentials() {
        try {
            const csrfToken = getCsrfToken();
            const csrfHeaderName = getCsrfHeaderName();
            const headers = {};
            headers[csrfHeaderName] = csrfToken;

            const response = await fetch('/api/webauthn/credentials', {
                method: 'GET',
                headers: headers,
                credentials: 'include'
            });

            const result = await response.json();

            if (result.success && result.data) {
                displayCredentials(result.data);
            } else {
                credentialsList.innerHTML = '<p>등록된 패스키가 없습니다.</p>';
            }
        } catch (error) {
            console.error('인증서 로드 오류:', error);
            credentialsList.innerHTML = '<p>인증서를 불러오는 중 오류가 발생했습니다.</p>';
        }
    }

    function displayCredentials(credentials) {
        if (credentials.length === 0) {
            credentialsList.innerHTML = '<p>등록된 패스키가 없습니다.</p>';
            return;
        }

        credentialsList.innerHTML = credentials.map(cred => `
            <div class="credential-item">
                <div class="credential-info">
                    <h3>${cred.label || '이름 없는 패스키'}</h3>
                    <p>생성일: ${new Date(cred.createdAt).toLocaleDateString('ko-KR')}</p>
                    <p>마지막 사용: ${new Date(cred.lastUsedAt).toLocaleDateString('ko-KR')}</p>
                </div>
                <button class="btn btn-danger" onclick="deleteCredential('${cred.credentialId}')">삭제</button>
            </div>
        `).join('');
    }

    window.deleteCredential = async function(credentialId) {
        if (!confirm('이 패스키를 삭제하시겠습니까?')) {
            return;
        }

        try {
            const csrfToken = getCsrfToken();
            const csrfHeaderName = getCsrfHeaderName();
            const headers = {};
            headers[csrfHeaderName] = csrfToken;

            const response = await fetch(`/api/webauthn/credentials/${encodeURIComponent(credentialId)}`, {
                method: 'DELETE',
                headers: headers,
                credentials: 'include'
            });

            const result = await response.json();

            if (result.success) {
                showMessage(passkeyMessage, '패스키가 성공적으로 삭제되었습니다', 'success');
                loadCredentials();
            } else {
                showMessage(passkeyMessage, result.message || '패스키 삭제 실패', 'error');
            }
        } catch (error) {
            showMessage(passkeyMessage, '패스키 삭제 실패: ' + error.message, 'error');
        }
    };

    addPasskeyBtn.addEventListener('click', async function() {
        try {
            const csrfToken = getCsrfToken();
            const csrfHeaderName = getCsrfHeaderName();
            const headers = {
                'Content-Type': 'application/json'
            };
            headers[csrfHeaderName] = csrfToken;

            const response = await fetch('/api/webauthn/register/options', {
                method: 'POST',
                headers: headers,
                credentials: 'include'
            });

            const result = await response.json();

            if (!result.success) {
                showMessage(passkeyMessage, result.message || '등록 옵션을 가져오는데 실패했습니다', 'error');
                return;
            }

            const options = result.data;
            options.challenge = base64UrlToArrayBuffer(options.challenge);
            options.user.id = base64UrlToArrayBuffer(options.user.id);

            if (options.excludeCredentials) {
                options.excludeCredentials = options.excludeCredentials.map(cred => ({
                    ...cred,
                    id: base64UrlToArrayBuffer(cred.id)
                }));
            }

            const credential = await navigator.credentials.create({
                publicKey: options
            });

            const publicKeyCredential = {
                publicKey: {
                    credential: {
                        id: credential.id,
                        rawId: arrayBufferToBase64Url(credential.rawId),
                        response: {
                            attestationObject: arrayBufferToBase64Url(credential.response.attestationObject),
                            clientDataJSON: arrayBufferToBase64Url(credential.response.clientDataJSON),
                            transports: credential.response.getTransports ? credential.response.getTransports() : []
                        },
                        type: credential.type
                    },
                    label: prompt('이 패스키의 이름을 입력하세요:') || '내 패스키'
                }
            };

            const registerHeaders = {
                'Content-Type': 'application/json'
            };
            registerHeaders[csrfHeaderName] = csrfToken;

            const registerResponse = await fetch('/api/webauthn/register', {
                method: 'POST',
                headers: registerHeaders,
                credentials: 'include',
                body: JSON.stringify(publicKeyCredential)
            });

            const registerResult = await registerResponse.json();

            if (registerResult.success) {
                showMessage(passkeyMessage, '패스키가 성공적으로 등록되었습니다!', 'success');
                loadCredentials();
            } else {
                showMessage(passkeyMessage, registerResult.message || '패스키 등록 실패', 'error');
            }
        } catch (error) {
            showMessage(passkeyMessage, '패스키 등록 실패: ' + error.message, 'error');
        }
    });

    logoutBtn.addEventListener('click', async function() {
        try {
            const csrfToken = getCsrfToken();
            const csrfHeaderName = getCsrfHeaderName();
            const headers = {};
            headers[csrfHeaderName] = csrfToken;

            const response = await fetch('/api/auth/logout', {
                method: 'POST',
                headers: headers,
                credentials: 'include'
            });

            window.location.href = '/login';
        } catch (error) {
            console.error('로그아웃 오류:', error);
            window.location.href = '/login';
        }
    });

    function base64UrlToArrayBuffer(base64url) {
        const base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
        const binary = atob(base64);
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i++) {
            bytes[i] = binary.charCodeAt(i);
        }
        return bytes.buffer;
    }

    function arrayBufferToBase64Url(buffer) {
        const bytes = new Uint8Array(buffer);
        let binary = '';
        for (let i = 0; i < bytes.length; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    }

    loadCredentials();
});
