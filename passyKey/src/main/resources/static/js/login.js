document.addEventListener('DOMContentLoaded', function() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    const passkeyLoginForm = document.getElementById('passkeyLoginForm');
    const passkeyLoginMessage = document.getElementById('passkeyLoginMessage');
    const loginMessage = document.getElementById('loginMessage');

    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetTab = this.getAttribute('data-tab');
            
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));
            
            this.classList.add('active');
            document.getElementById(targetTab + 'Tab').classList.add('active');
        });
    });

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

    passkeyLoginForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const username = document.getElementById('passkeyUsername').value.trim();

        try {
            const optionsUrl = username 
                ? `/api/webauthn/authenticate/options?username=${encodeURIComponent(username)}`
                : '/api/webauthn/authenticate/options';

            const csrfToken = getCsrfToken();
            const csrfHeaderName = getCsrfHeaderName();
            const headers = {
                'Content-Type': 'application/json'
            };
            headers[csrfHeaderName] = csrfToken;

            const optionsResponse = await fetch(optionsUrl, {
                method: 'POST',
                headers: headers,
                credentials: 'include'
            });

            const optionsResult = await optionsResponse.json();

            if (!optionsResult.success) {
                showMessage(passkeyLoginMessage, optionsResult.message || '인증 옵션을 가져오는데 실패했습니다', 'error');
                return;
            }

            const options = optionsResult.data;
            if (options.challenge) {
                if (typeof options.challenge === 'string') {
                    options.challenge = base64UrlToArrayBuffer(options.challenge);
                } else if (options.challenge.value && Array.isArray(options.challenge.value)) {
                    options.challenge = new Uint8Array(options.challenge.value).buffer;
                } else if (Array.isArray(options.challenge)) {
                    options.challenge = new Uint8Array(options.challenge).buffer;
                } else if (!(options.challenge instanceof ArrayBuffer)) {
                    console.error('Unexpected challenge format:', options.challenge);
                    throw new Error('Invalid challenge format');
                }
            }

            if (options.allowCredentials) {
                options.allowCredentials = options.allowCredentials.map(cred => {
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

            const assertion = await navigator.credentials.get({
                publicKey: options
            });

            const authenticationRequest = {
                id: assertion.id,
                rawId: arrayBufferToBase64Url(assertion.rawId),
                response: {
                    authenticatorData: arrayBufferToBase64Url(assertion.response.authenticatorData),
                    clientDataJSON: arrayBufferToBase64Url(assertion.response.clientDataJSON),
                    signature: arrayBufferToBase64Url(assertion.response.signature),
                    userHandle: assertion.response.userHandle ? arrayBufferToBase64Url(assertion.response.userHandle) : null
                }
            };

            const authHeaders = {
                'Content-Type': 'application/json'
            };
            authHeaders[csrfHeaderName] = csrfToken;

            const authResponse = await fetch('/api/auth/webauthn/authenticate', {
                method: 'POST',
                headers: authHeaders,
                credentials: 'include',
                body: JSON.stringify(authenticationRequest)
            });

            const authResult = await authResponse.json();

            if (authResult.success && authResult.data.authenticated) {
                showMessage(passkeyLoginMessage, '인증 성공! 리다이렉트 중...', 'success');
                setTimeout(() => {
                    window.location.href = authResult.data.redirectUrl || '/dashboard';
                }, 1000);
            } else {
                showMessage(passkeyLoginMessage, authResult.message || '인증 실패', 'error');
            }
        } catch (error) {
            showMessage(passkeyLoginMessage, '인증 실패: ' + error.message, 'error');
        }
    });

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
});
