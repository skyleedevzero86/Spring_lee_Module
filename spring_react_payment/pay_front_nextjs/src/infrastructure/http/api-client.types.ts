import type { InternalAxiosRequestConfig } from 'axios';

export interface ApiClientConfig {
  baseURL: string;
  timeout: number;
}

export interface ApiClientMethods {
  get<T>(url: string, config?: InternalAxiosRequestConfig): Promise<T>;
  post<T>(url: string, data?: unknown, config?: InternalAxiosRequestConfig): Promise<T>;
  put<T>(url: string, data?: unknown, config?: InternalAxiosRequestConfig): Promise<T>;
  patch<T>(url: string, data?: unknown, config?: InternalAxiosRequestConfig): Promise<T>;
  delete<T>(url: string, config?: InternalAxiosRequestConfig): Promise<T>;
  setAuth(userId: number, role: string): void;
  clearAuth(): void;
}

