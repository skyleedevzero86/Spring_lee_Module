import { env } from './env-validator';

export const appConfig = {
  api: {
    baseUrl: env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080',
    timeout: 30000,
  },
  monitoring: {
    errorTracking: env.NEXT_PUBLIC_ERROR_TRACKING_ENABLED,
    performanceMonitoring: env.NEXT_PUBLIC_PERFORMANCE_MONITORING_ENABLED,
  },
  environment: env.NODE_ENV,
  isDevelopment: env.NODE_ENV === 'development',
  isProduction: env.NODE_ENV === 'production',
  isTest: env.NODE_ENV === 'test',
} as const;



