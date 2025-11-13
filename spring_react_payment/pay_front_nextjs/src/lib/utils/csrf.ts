const CSRF_TOKEN_KEY = 'csrf-token';
const CSRF_HEADER = 'X-CSRF-Token';

function generateRandomToken(length: number): string {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

export class CsrfTokenManager {
  static generateToken(): string {
    return generateRandomToken(32);
  }

  static getToken(): string | null {
    if (typeof window === 'undefined') return null;
    return sessionStorage.getItem(CSRF_TOKEN_KEY);
  }

  static setToken(token: string): void {
    if (typeof window === 'undefined') return;
    sessionStorage.setItem(CSRF_TOKEN_KEY, token);
  }

  static clearToken(): void {
    if (typeof window === 'undefined') return;
    sessionStorage.removeItem(CSRF_TOKEN_KEY);
  }

  static initToken(): string {
    let token = this.getToken();
    if (!token) {
      token = this.generateToken();
      this.setToken(token);
    }
    return token;
  }

  static getHeaderName(): string {
    return CSRF_HEADER;
  }
}

