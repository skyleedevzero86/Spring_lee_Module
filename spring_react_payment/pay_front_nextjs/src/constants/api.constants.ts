export const API_ENDPOINTS = {
  MEMBERS: {
    BASE: '/api/v1/members',
    REGISTER: '/api/v1/members',
    FIND_BY_EMAIL: (email: string) => `/api/v1/members/email/${encodeURIComponent(email)}`,
    FIND_BY_ID: (id: number) => `/api/v1/members/${id}`,
    SEARCH_BY_NAME: (name: string) => `/api/v1/members/search/name?name=${encodeURIComponent(name)}`,
    SEARCH_BY_EMAIL: (email: string) => `/api/v1/members/search/email?email=${encodeURIComponent(email)}`,
    SEARCH_ALL: '/api/v1/members/search/all',
    SEARCH_BY_NAME_PAGE: (name: string, page: number, size: number) =>
      `/api/v1/members/search/name/page?name=${encodeURIComponent(name)}&page=${page}&size=${size}`,
    SEARCH_BY_EMAIL_PAGE: (email: string, page: number, size: number) =>
      `/api/v1/members/search/email/page?email=${encodeURIComponent(email)}&page=${page}&size=${size}`,
    SEARCH_ALL_PAGE: (page: number, size: number) =>
      `/api/v1/members/search/all/page?page=${page}&size=${size}`,
    RESET_PASSWORD: '/api/v1/members/reset-password',
  },
  PAYMENTS: {
    BASE: '/api/v1/payments',
    CREATE: '/api/v1/payments',
    APPROVE: '/api/v1/payments/approve',
    STATUS: '/api/v1/payments/status',
    HISTORY: '/api/v1/payments',
    HISTORY_PAGE: (page: number, size: number) => `/api/v1/payments/page?page=${page}&size=${size}`,
    DETAIL: (id: number) => `/api/v1/payments/${id}`,
    REFUND: (id: number) => `/api/v1/payments/${id}/refund`,
  },
} as const;

export const API_TIMEOUT = 30000;

export const STORAGE_KEYS = {
  USER_ID: 'userId',
  USER_ROLE: 'userRole',
  MEMBER_STORAGE: 'member-storage',
} as const;

