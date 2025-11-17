import { STORAGE_KEYS } from '@/constants/api.constants';
import { setEncryptedItem, getEncryptedItem, removeEncryptedItem } from './storage-encryption';

interface TokenData {
  userId: number;
  role: string;
  jwtToken: string | null;
  expiresAt: number;
}

const TOKEN_EXPIRY_HOURS = 24;
const TOKEN_KEY = 'auth_token';
const JWT_TOKEN_KEY = 'jwt_token';
const USE_ENCRYPTION = true;

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
  static async setToken(userId: number, role: string, jwtToken?: string): Promise<void> {
    if (typeof window === 'undefined') return;

    let expiresAt = Date.now() + TOKEN_EXPIRY_HOURS * 60 * 60 * 1000;

    if (jwtToken) {
      const decoded = parseJwt(jwtToken);
      if (decoded && decoded.exp) {
        expiresAt = decoded.exp * 1000;
      }
      if (USE_ENCRYPTION) {
        await setEncryptedItem(JWT_TOKEN_KEY, jwtToken);
      } else {
        localStorage.setItem(JWT_TOKEN_KEY, jwtToken);
      }
    }

    const tokenData: TokenData = {
      userId,
      role,
      jwtToken: jwtToken || null,
      expiresAt,
    };

    const tokenDataString = JSON.stringify(tokenData);
    if (USE_ENCRYPTION) {
      await setEncryptedItem(TOKEN_KEY, tokenDataString);
      await setEncryptedItem(STORAGE_KEYS.USER_ID, String(userId));
      await setEncryptedItem(STORAGE_KEYS.USER_ROLE, role);
    } else {
      localStorage.setItem(TOKEN_KEY, tokenDataString);
      localStorage.setItem(STORAGE_KEYS.USER_ID, String(userId));
      localStorage.setItem(STORAGE_KEYS.USER_ROLE, role);
    }
  }

  static async getToken(): Promise<TokenData | null> {
    if (typeof window === 'undefined') return null;

    try {
      const stored = USE_ENCRYPTION 
        ? await getEncryptedItem(TOKEN_KEY)
        : localStorage.getItem(TOKEN_KEY);
      
      if (!stored) {
        const syncToken = this.getTokenSync();
        return syncToken;
      }

      const tokenData: TokenData = JSON.parse(stored);

      if (Date.now() >= tokenData.expiresAt) {
        await this.clearToken();
        return null;
      }

      if (tokenData.jwtToken && isJwtExpired(tokenData.jwtToken)) {
        await this.clearToken();
        return null;
      }

      return tokenData;
    } catch {
      const syncToken = this.getTokenSync();
      if (syncToken) {
        return syncToken;
      }
      return null;
    }
  }

  static getTokenSync(): TokenData | null {
    if (typeof window === 'undefined') return null;

    try {
      const stored = localStorage.getItem(TOKEN_KEY);
      if (!stored) return null;

      let tokenDataString = stored;
      
      try {
        const parsed = JSON.parse(stored);
        if (parsed && typeof parsed === 'object' && 'userId' in parsed) {
          tokenDataString = stored;
        } else {
          return null;
        }
      } catch {
        return null;
      }

      const tokenData: TokenData = JSON.parse(tokenDataString);

      if (Date.now() >= tokenData.expiresAt) {
        this.clearTokenSync();
        return null;
      }

      if (tokenData.jwtToken && isJwtExpired(tokenData.jwtToken)) {
        this.clearTokenSync();
        return null;
      }

      return tokenData;
    } catch {
      return null;
    }
  }

  static async getJwtToken(): Promise<string | null> {
    if (typeof window === 'undefined') return null;

    const tokenData = await this.getToken();
    if (tokenData?.jwtToken) {
      return tokenData.jwtToken;
    }

    const stored = USE_ENCRYPTION
      ? await getEncryptedItem(JWT_TOKEN_KEY)
      : localStorage.getItem(JWT_TOKEN_KEY);
    
    if (stored && !isJwtExpired(stored)) {
      return stored;
    }

    return null;
  }

  static getJwtTokenSync(): string | null {
    if (typeof window === 'undefined') return null;

    const tokenData = this.getTokenSync();
    if (tokenData?.jwtToken) {
      return tokenData.jwtToken;
    }

    const stored = localStorage.getItem(JWT_TOKEN_KEY);
    if (stored && !isJwtExpired(stored)) {
      return stored;
    }

    return null;
  }

  static async getUserId(): Promise<number | null> {
    const token = await this.getToken();
    return token?.userId ?? null;
  }

  static getUserIdSync(): number | null {
    const token = this.getTokenSync();
    return token?.userId ?? null;
  }

  static async getUserRole(): Promise<string | null> {
    const token = await this.getToken();
    return token?.role ?? null;
  }

  static getUserRoleSync(): string | null {
    const token = this.getTokenSync();
    return token?.role ?? null;
  }

  static isAuthenticated(): boolean {
    const tokenData = this.getTokenSync();
    if (!tokenData) return false;
    
    const jwtToken = this.getJwtTokenSync();
    if (jwtToken && isJwtExpired(jwtToken)) {
      this.clearTokenSync();
      return false;
    }
    
    return true;
  }

  static async clearToken(): Promise<void> {
    if (typeof window === 'undefined') return;
    
    if (USE_ENCRYPTION) {
      await removeEncryptedItem(TOKEN_KEY);
      await removeEncryptedItem(JWT_TOKEN_KEY);
      await removeEncryptedItem(STORAGE_KEYS.USER_ID);
      await removeEncryptedItem(STORAGE_KEYS.USER_ROLE);
    } else {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(JWT_TOKEN_KEY);
      localStorage.removeItem(STORAGE_KEYS.USER_ID);
      localStorage.removeItem(STORAGE_KEYS.USER_ROLE);
    }
  }

  static clearTokenSync(): void {
    if (typeof window === 'undefined') return;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(JWT_TOKEN_KEY);
    localStorage.removeItem(STORAGE_KEYS.USER_ID);
    localStorage.removeItem(STORAGE_KEYS.USER_ROLE);
  }

  static async validateToken(): Promise<boolean> {
    try {
      let tokenData = await this.getToken();
      
      if (!tokenData) {
        tokenData = this.getTokenSync();
      }
      
      if (!tokenData) return false;

      if (Date.now() >= tokenData.expiresAt) {
        await this.clearToken();
        return false;
      }

      const jwtToken = await this.getJwtToken();
      if (!jwtToken) {
        const jwtTokenSync = this.getJwtTokenSync();
        if (jwtTokenSync && isJwtExpired(jwtTokenSync)) {
          await this.clearToken();
          return false;
        }
      } else if (isJwtExpired(jwtToken)) {
        await this.clearToken();
        return false;
      }

      return true;
    } catch (error) {
      console.error('토큰 검증 실패:', error);
      return false;
    }
  }
}

