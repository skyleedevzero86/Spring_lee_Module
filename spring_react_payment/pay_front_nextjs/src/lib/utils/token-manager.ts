import { STORAGE_KEYS } from '@/src/constants/api.constants';

interface TokenData {
  userId: number;
  role: string;
  expiresAt: number;
}

const TOKEN_EXPIRY_HOURS = 24;
const TOKEN_KEY = 'auth_token';

function encrypt(data: string): string {
  if (typeof window === 'undefined') return data;
  try {
    return btoa(encodeURIComponent(data));
  } catch {
    return data;
  }
}

function decrypt(encrypted: string): string {
  if (typeof window === 'undefined') return encrypted;
  try {
    return decodeURIComponent(atob(encrypted));
  } catch {
    return encrypted;
  }
}

function isExpired(expiresAt: number): boolean {
  return Date.now() >= expiresAt;
}

export class TokenManager {
  static setToken(userId: number, role: string): void {
    if (typeof window === 'undefined') return;

    const expiresAt = Date.now() + TOKEN_EXPIRY_HOURS * 60 * 60 * 1000;
    const tokenData: TokenData = {
      userId,
      role,
      expiresAt,
    };

    const encrypted = encrypt(JSON.stringify(tokenData));
    localStorage.setItem(TOKEN_KEY, encrypted);
    localStorage.setItem(STORAGE_KEYS.USER_ID, String(userId));
    localStorage.setItem(STORAGE_KEYS.USER_ROLE, role);
  }

  static getToken(): TokenData | null {
    if (typeof window === 'undefined') return null;

    const encrypted = localStorage.getItem(TOKEN_KEY);
    if (!encrypted) return null;

    try {
      const decrypted = decrypt(encrypted);
      const tokenData: TokenData = JSON.parse(decrypted);

      if (isExpired(tokenData.expiresAt)) {
        this.clearToken();
        return null;
      }

      return tokenData;
    } catch {
      this.clearToken();
      return null;
    }
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
    localStorage.removeItem(STORAGE_KEYS.USER_ID);
    localStorage.removeItem(STORAGE_KEYS.USER_ROLE);
  }

  static refreshToken(): boolean {
    const token = this.getToken();
    if (!token) return false;

    if (isExpired(token.expiresAt)) {
      this.clearToken();
      return false;
    }

    this.setToken(token.userId, token.role);
    return true;
  }
}

