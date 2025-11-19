document.addEventListener('DOMContentLoaded', function() {
    const addPasskeyBtn = document.getElementById('addPasskeyBtn');
    const passkeyMessage = document.getElementById('passkeyMessage');
    const credentialsList = document.getElementById('credentialsList');
    const logoutBtn = document.getElementById('logoutBtn');
    const mobileToggle = document.getElementById('mobileToggle');
    const mobileMenu = document.getElementById('mobileMenu');
    const mobileLogoutBtn = document.getElementById('mobileLogoutBtn');
    const mobileAddPasskeyBtn = document.getElementById('mobileAddPasskeyBtn');
    const header = document.querySelector('.header');

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

        credentialsList.innerHTML = credentials.map(cred => {
            const formatDate = (dateStr) => {
                if (!dateStr) return '정보 없음';
                try {
                    const date = new Date(dateStr);
                    if (isNaN(date.getTime())) return '정보 없음';
                    return date.toLocaleString('ko-KR', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                    });
                } catch (e) {
                    console.error('날짜 포맷팅 오류:', e, dateStr);
                    return '정보 없음';
                }
            };
            
            return `
                <div class="credential-item">
                    <div class="credential-info">
                        <h3>${cred.label || '이름 없는 패스키'}</h3>
                        <p>생성일: ${formatDate(cred.createdAt)}</p>
                        <p>마지막 사용: ${formatDate(cred.lastUsedAt)}</p>
                    </div>
                    <button class="btn btn-danger" onclick="deleteCredential('${cred.credentialId}')">삭제</button>
                </div>
            `;
        }).join('');
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


    function handleLogout() {
        return async function() {
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
        };
    }

    logoutBtn.addEventListener('click', handleLogout());
    mobileLogoutBtn.addEventListener('click', handleLogout());

    function handleAddPasskey() {
        return async function() {
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
                
                if (!options.challenge) {
                    console.error('Challenge is missing from options');
                    throw new Error('Challenge is required but missing');
                }
                
                try {
                    if (typeof options.challenge === 'string') {
                        options.challenge = base64UrlToArrayBuffer(options.challenge);
                    } else if (options.challenge.value !== undefined) {
                        if (Array.isArray(options.challenge.value)) {
                            options.challenge = new Uint8Array(options.challenge.value).buffer;
                        } else if (typeof options.challenge.value === 'string') {
                            options.challenge = base64UrlToArrayBuffer(options.challenge.value);
                        } else if (options.challenge.value instanceof ArrayBuffer) {
                            options.challenge = options.challenge.value;
                        } else {
                            const value = options.challenge.value;
                            if (value && typeof value === 'object') {
                                const arr = Object.values(value).map(v => typeof v === 'number' ? v : 0);
                                options.challenge = new Uint8Array(arr).buffer;
                            } else {
                                throw new Error('Unsupported challenge.value type: ' + typeof value);
                            }
                        }
                    } else if (Array.isArray(options.challenge)) {
                        options.challenge = new Uint8Array(options.challenge).buffer;
                    } else if (options.challenge instanceof ArrayBuffer) {
                    } else if (options.challenge instanceof Uint8Array) {
                        options.challenge = options.challenge.buffer;
                    } else if (typeof options.challenge === 'object') {
                        const values = Object.values(options.challenge);
                        if (values.length > 0 && values.every(v => typeof v === 'number')) {
                            options.challenge = new Uint8Array(values).buffer;
                        } else {
                            const arr = [];
                            for (const key in options.challenge) {
                                const val = options.challenge[key];
                                if (typeof val === 'number') {
                                    arr.push(val);
                                }
                            }
                            if (arr.length > 0) {
                                options.challenge = new Uint8Array(arr).buffer;
                            } else {
                                throw new Error('Cannot convert challenge object to ArrayBuffer: ' + JSON.stringify(options.challenge));
                            }
                        }
                    } else {
                        throw new Error('Unsupported challenge type: ' + typeof options.challenge);
                    }
                } catch (error) {
                    console.error('Challenge conversion error:', error);
                    console.error('Challenge value:', options.challenge);
                    console.error('Challenge type:', typeof options.challenge);
                    if (options.challenge && typeof options.challenge === 'object') {
                        console.error('Challenge keys:', Object.keys(options.challenge));
                    }
                    throw new Error('Invalid challenge format: ' + error.message);
                }
                
                if (options.user && options.user.id) {
                    if (typeof options.user.id === 'string') {
                        options.user.id = base64UrlToArrayBuffer(options.user.id);
                    } else if (Array.isArray(options.user.id)) {
                        options.user.id = new Uint8Array(options.user.id).buffer;
                    } else if (options.user.id.value && Array.isArray(options.user.id.value)) {
                        options.user.id = new Uint8Array(options.user.id.value).buffer;
                    } else if (!(options.user.id instanceof ArrayBuffer)) {
                        console.error('Unexpected user.id format:', options.user.id);
                        throw new Error('Invalid user.id format');
                    }
                }

                if (options.excludeCredentials && Array.isArray(options.excludeCredentials)) {
                    options.excludeCredentials = options.excludeCredentials.map(cred => {
                        let id = cred.id;
                        if (typeof id === 'string') {
                            id = base64UrlToArrayBuffer(id);
                        } else if (Array.isArray(id)) {
                            id = new Uint8Array(id).buffer;
                        } else if (id && id.value && Array.isArray(id.value)) {
                            id = new Uint8Array(id.value).buffer;
                        }
                        return {
                            ...cred,
                            id: id
                        };
                    });
                }

                if (options.hints !== undefined) {
                    if (!Array.isArray(options.hints)) {
                        delete options.hints;
                    } else if (options.hints.length === 0) {
                        delete options.hints;
                    }
                }

                console.log('WebAuthn options before create:', {
                    ...options,
                    challenge: '[ArrayBuffer]',
                    user: options.user ? { ...options.user, id: '[ArrayBuffer]' } : undefined,
                    excludeCredentials: options.excludeCredentials ? options.excludeCredentials.map(c => ({ ...c, id: '[ArrayBuffer]' })) : undefined
                });

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
        };
    }

    addPasskeyBtn.addEventListener('click', handleAddPasskey());
    mobileAddPasskeyBtn.addEventListener('click', handleAddPasskey());

    if (mobileToggle && mobileMenu) {
        mobileToggle.addEventListener('click', function() {
            const isExpanded = mobileToggle.getAttribute('aria-expanded') === 'true';
            mobileToggle.setAttribute('aria-expanded', !isExpanded);
            mobileMenu.setAttribute('aria-hidden', isExpanded);
            
            if (!isExpanded) {
                header.classList.add('header__mobile-menu-open');
                document.body.classList.add('header__mobile-menu-open');
            } else {
                header.classList.remove('header__mobile-menu-open');
                document.body.classList.remove('header__mobile-menu-open');
            }
        });
    }

    function base64UrlToArrayBuffer(base64url) {
        if (base64url instanceof ArrayBuffer) {
            return base64url;
        }
        if (base64url instanceof Uint8Array) {
            return base64url.buffer;
        }
        if (typeof base64url !== 'string') {
            console.error('base64UrlToArrayBuffer: Invalid input type', typeof base64url, base64url);
            throw new Error('base64url must be a string, ArrayBuffer, or Uint8Array');
        }
        const base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
        const paddedBase64 = base64 + '='.repeat((4 - base64.length % 4) % 4);
        const binary = atob(paddedBase64);
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i++) {
            bytes[i] = binary.charCodeAt(i);
        }
        return bytes.buffer;
    }

    function arrayBufferToBase64Url(buffer) {
        if (!(buffer instanceof ArrayBuffer)) {
            if (buffer instanceof Uint8Array) {
                buffer = buffer.buffer;
            } else {
                console.error('arrayBufferToBase64Url: Invalid input type', typeof buffer, buffer);
                throw new Error('buffer must be an ArrayBuffer or Uint8Array');
            }
        }
        const bytes = new Uint8Array(buffer);
        let binary = '';
        for (let i = 0; i < bytes.length; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    }

    loadCredentials();
});
