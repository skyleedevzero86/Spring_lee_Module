export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
}

export interface User {
  id?: number;
  username: string;
  email: string;
  displayName: string;
  userHandle: string;
  enabled?: boolean;
  accountNonExpired?: boolean;
  accountNonLocked?: boolean;
  credentialsNonExpired?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface WebAuthnCredential {
  id?: number;
  credentialId: string;
  publicKeyCose: string;
  counter: number;
  transports: string;
  label?: string;
  user?: User;
  createdAt?: string;
  lastUsedAt?: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  displayName: string;
}

export interface AuthenticationResponse {
  redirectUrl: string;
  authenticated: boolean;
  passkeyLogin?: boolean;
}

export interface LoginHistory {
  id?: number;
  userId: number;
  loginType: 'PASSWORD' | 'PASSKEY';
  sessionId: string;
  ipAddress?: string;
  userAgent?: string;
  loginAt: string;
  logoutAt?: string;
}

export interface PasskeyRegistrationRequest {
  publicKey: {
    credential: {
      id: string;
      rawId: string;
      response: {
        attestationObject: string;
        clientDataJSON: string;
        transports?: string[];
      };
      type: string;
    };
    label: string;
  };
}

export interface PasskeyAuthenticationRequest {
  id: string;
  rawId: string;
  response: {
    authenticatorData: string;
    clientDataJSON: string;
    signature: string;
    userHandle?: string | null;
  };
}

