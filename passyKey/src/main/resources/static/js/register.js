document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('registerForm');
    const addPasskeyBtn = document.getElementById('addPasskeyBtn');
    const registerMessage = document.getElementById('registerMessage');
    const passkeyMessage = document.getElementById('passkeyMessage');
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');

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

    usernameInput.addEventListener('blur', async function() {
        const username = this.value.trim();
        if (username.length < 3) return;

        try {
            const response = await fetch(`/api/public/check-username?username=${encodeURIComponent(username)}`);
            const result = await response.json();
            const errorElement = document.getElementById('usernameError');
            
            if (result.data) {
                errorElement.textContent = '이미 존재하는 사용자명입니다';
            } else {
                errorElement.textContent = '';
            }
        } catch (error) {
            console.error('사용자명 확인 오류:', error);
        }
    });

    emailInput.addEventListener('blur', async function() {
        const email = this.value.trim();
        if (!email) return;

        try {
            const response = await fetch(`/api/public/check-email?email=${encodeURIComponent(email)}`);
            const result = await response.json();
            const errorElement = document.getElementById('emailError');
            
            if (result.data) {
                errorElement.textContent = '이미 존재하는 이메일입니다';
            } else {
                errorElement.textContent = '';
            }
        } catch (error) {
            console.error('이메일 확인 오류:', error);
        }
    });

    registerForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const formData = {
            username: document.getElementById('username').value,
            email: document.getElementById('email').value,
            displayName: document.getElementById('displayName').value,
            password: document.getElementById('password').value
        };

        try {
            const response = await fetch('/api/public/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            const result = await response.json();

            if (result.success) {
                showMessage(registerMessage, '등록 성공! 이제 패스키를 추가할 수 있습니다.', 'success');
                addPasskeyBtn.style.display = 'block';
            } else {
                showMessage(registerMessage, result.message || '등록 실패', 'error');
            }
        } catch (error) {
            showMessage(registerMessage, '등록 실패: ' + error.message, 'error');
        }
    });

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
                setTimeout(() => {
                    window.location.href = '/login';
                }, 2000);
            } else {
                showMessage(passkeyMessage, registerResult.message || '패스키 등록 실패', 'error');
            }
        } catch (error) {
            showMessage(passkeyMessage, '패스키 등록 실패: ' + error.message, 'error');
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
