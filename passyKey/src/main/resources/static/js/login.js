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
            options.challenge = base64UrlToArrayBuffer(options.challenge);

            if (options.allowCredentials) {
                options.allowCredentials = options.allowCredentials.map(cred => ({
                    ...cred,
                    id: base64UrlToArrayBuffer(cred.id)
                }));
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
});
