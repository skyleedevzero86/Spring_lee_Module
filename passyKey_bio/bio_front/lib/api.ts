import { ApiResponse, User, WebAuthnCredential, RegisterRequest, AuthenticationResponse, PasskeyRegistrationRequest, PasskeyAuthenticationRequest } from '@/types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || '';

async function fetchApi<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const defaultHeaders: HeadersInit = {};
  
  if (options.body && !(options.body instanceof FormData)) {
    defaultHeaders['Content-Type'] = 'application/json';
  }

  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers,
      },
      credentials: 'include',
    });

    if (!response.ok) {
      const errorText = await response.text();
      try {
        const errorJson = JSON.parse(errorText);
        throw new Error(errorJson.message || `API 요청 실패 (${response.status})`);
      } catch {
        throw new Error(errorText || `API 요청 실패 (${response.status})`);
      }
    }

    return response.json();
  } catch (error: any) {
    if (error.message && error.message.includes('Failed to fetch')) {
      throw new Error('서버에 연결할 수 없습니다. 백엔드 서버가 실행 중인지 확인해주세요.');
    }
    throw error;
  }
}

export const api = {
  // User APIs
  register: async (data: RegisterRequest): Promise<ApiResponse<User>> => {
    return fetchApi<User>('/api/public/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  checkUsername: async (username: string): Promise<ApiResponse<boolean>> => {
    return fetchApi<boolean>(`/api/public/check-username?username=${encodeURIComponent(username)}`);
  },

  checkEmail: async (email: string): Promise<ApiResponse<boolean>> => {
    return fetchApi<boolean>(`/api/public/check-email?email=${encodeURIComponent(email)}`);
  },

  // WebAuthn Registration APIs
  getRegistrationOptions: async (username?: string): Promise<ApiResponse<any>> => {
    const url = username 
      ? `/api/webauthn/register/options?username=${encodeURIComponent(username)}`
      : '/api/webauthn/register/options';
    return fetchApi<any>(url, {
      method: 'POST',
    });
  },

  registerCredential: async (data: PasskeyRegistrationRequest): Promise<ApiResponse<{ success: boolean }>> => {
    return fetchApi<{ success: boolean }>('/api/webauthn/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  // WebAuthn Authentication APIs
  getAuthenticationOptions: async (username?: string): Promise<ApiResponse<any>> => {
    const url = username
      ? `/api/webauthn/authenticate/options?username=${encodeURIComponent(username)}`
      : '/api/webauthn/authenticate/options';
    return fetchApi<any>(url, {
      method: 'POST',
    });
  },

  authenticate: async (data: PasskeyAuthenticationRequest): Promise<ApiResponse<AuthenticationResponse>> => {
    return fetchApi<AuthenticationResponse>('/api/auth/webauthn/authenticate', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  // Credential Management APIs
  getCredentials: async (): Promise<ApiResponse<WebAuthnCredential[]>> => {
    return fetchApi<WebAuthnCredential[]>('/api/webauthn/credentials', {
      method: 'GET',
    });
  },

  deleteCredential: async (credentialId: string): Promise<ApiResponse<void>> => {
    return fetchApi<void>(`/api/webauthn/credentials/${encodeURIComponent(credentialId)}`, {
      method: 'DELETE',
    });
  },

  // Auth APIs
  logout: async (): Promise<ApiResponse<void>> => {
    return fetchApi<void>('/api/auth/logout', {
      method: 'POST',
    });
  },
};

