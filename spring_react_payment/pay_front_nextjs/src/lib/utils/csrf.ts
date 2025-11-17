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

  static async initToken(): Promise<string> {
    let token = this.getToken();
    if (!token) {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'}/api/v1/csrf-token`,
          {
            method: 'GET',
            credentials: 'include',
          }
        );
        if (response.ok) {
          const data = await response.json();
          token = data.token || this.generateToken();
        } else {
          token = this.generateToken();
        }
      } catch {
        token = this.generateToken();
      }
      this.setToken(token);
    }
    return token;
  }

  static async validateToken(token: string): Promise<boolean> {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'}/api/v1/csrf-token/validate`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            [this.getHeaderName()]: token,
          },
          credentials: 'include',
          body: JSON.stringify({ token }),
        }
      );
      return response.ok;
    } catch {
      return false;
    }
  }

  static getHeaderName(): string {
    return CSRF_HEADER;
  }
}

