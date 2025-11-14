import { STORAGE_KEYS } from '@/constants/api.constants';

interface TokenData {
  userId: number;
  role: string;
  jwtToken: string | null;
  expiresAt: number;
}

const TOKEN_EXPIRY_HOURS = 24;
const TOKEN_KEY = 'auth_token';
const JWT_TOKEN_KEY = 'jwt_token';

function parseJwt(token: string): { exp?: number; userId?: number; role?: string } | null {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

function isJwtExpired(token: string): boolean {
  const decoded = parseJwt(token);
  if (!decoded || !decoded.exp) return true;
  return Date.now() >= decoded.exp * 1000;
}

export class TokenManager {
  static setToken(userId: number, role: string, jwtToken?: string): void {
    if (typeof window === 'undefined') return;

    let expiresAt = Date.now() + TOKEN_EXPIRY_HOURS * 60 * 60 * 1000;

    if (jwtToken) {
      const decoded = parseJwt(jwtToken);
      if (decoded && decoded.exp) {
        expiresAt = decoded.exp * 1000;
      }
      localStorage.setItem(JWT_TOKEN_KEY, jwtToken);
    }

    const tokenData: TokenData = {
      userId,
      role,
      jwtToken: jwtToken || null,
      expiresAt,
    };

    localStorage.setItem(TOKEN_KEY, JSON.stringify(tokenData));
    localStorage.setItem(STORAGE_KEYS.USER_ID, String(userId));
    localStorage.setItem(STORAGE_KEYS.USER_ROLE, role);
  }

  static getToken(): TokenData | null {
    if (typeof window === 'undefined') return null;

    try {
      const stored = localStorage.getItem(TOKEN_KEY);
      if (!stored) return null;

      const tokenData: TokenData = JSON.parse(stored);

      if (Date.now() >= tokenData.expiresAt) {
        this.clearToken();
        return null;
      }

      if (tokenData.jwtToken && isJwtExpired(tokenData.jwtToken)) {
        this.clearToken();
        return null;
      }

      return tokenData;
    } catch {
      this.clearToken();
      return null;
    }
  }

  static getJwtToken(): string | null {
    if (typeof window === 'undefined') return null;

    const tokenData = this.getToken();
    if (tokenData?.jwtToken) {
      return tokenData.jwtToken;
    }

    const stored = localStorage.getItem(JWT_TOKEN_KEY);
    if (stored && !isJwtExpired(stored)) {
      return stored;
    }

    return null;
  }

  static getUserId(): number | null {
    const token = this.getToken();
    return token?.userId ?? null;
  }

  static getUserRole(): string | null {
    const token = this.getToken();
    return token?.role ?? null;
  }

  static isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  static clearToken(): void {
    if (typeof window === 'undefined') return;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(JWT_TOKEN_KEY);
    localStorage.removeItem(STORAGE_KEYS.USER_ID);
    localStorage.removeItem(STORAGE_KEYS.USER_ROLE);
  }

  static async validateToken(): Promise<boolean> {
    const token = this.getJwtToken();
    if (!token) return false;

    if (isJwtExpired(token)) {
      this.clearToken();
      return false;
    }

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'}/api/v1/users/validate-token`,
        {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        this.clearToken();
        return false;
      }

      return true;
    } catch {
      return false;
    }
  }
}

