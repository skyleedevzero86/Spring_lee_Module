document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('registerForm');
    const addPasskeyBtn = document.getElementById('addPasskeyBtn');
    const registerMessage = document.getElementById('registerMessage');
    const passkeyMessage = document.getElementById('passkeyMessage');
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    let registeredUsername = null;

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
                registeredUsername = formData.username;
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
            if (!registeredUsername) {
                showMessage(passkeyMessage, '먼저 회원가입을 완료해주세요.', 'error');
                return;
            }

            const csrfToken = getCsrfToken();
            const csrfHeaderName = getCsrfHeaderName();
            const headers = {
                'Content-Type': 'application/json'
            };
            headers[csrfHeaderName] = csrfToken;

            const response = await fetch(`/api/webauthn/register/options?username=${encodeURIComponent(registeredUsername)}`, {
                method: 'POST',
                headers: headers,
                credentials: 'include'
            });

            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                const text = await response.text();
                showMessage(passkeyMessage, '등록 옵션을 가져오는데 실패했습니다. 다시 로그인해주세요.', 'error');
                console.error('Expected JSON but got:', contentType, text.substring(0, 100));
                return;
            }

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

            const registerContentType = registerResponse.headers.get('content-type');
            if (!registerContentType || !registerContentType.includes('application/json')) {
                const text = await registerResponse.text();
                showMessage(passkeyMessage, '패스키 등록 실패: 서버 오류가 발생했습니다. 다시 시도해주세요.', 'error');
                console.error('Expected JSON but got:', registerContentType, text.substring(0, 100));
                return;
            }

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
