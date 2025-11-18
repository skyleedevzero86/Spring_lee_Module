import { ApiError } from '@/domain/types/error.types';

export interface RetryOptions {
  maxRetries?: number;
  initialDelay?: number;
  maxDelay?: number;
  backoffMultiplier?: number;
  retryableStatusCodes?: number[];
  retryableErrorCodes?: string[];
}

const DEFAULT_OPTIONS: Required<RetryOptions> = {
  maxRetries: 3,
  initialDelay: 1000,
  maxDelay: 10000,
  backoffMultiplier: 2,
  retryableStatusCodes: [408, 500, 502, 503, 504], // 429는 제외 (재시도하지 않음)
  retryableErrorCodes: ['NETWORK_ERROR', 'TIMEOUT_ERROR'],
};

function calculateDelay(
  attempt: number,
  initialDelay: number,
  maxDelay: number,
  backoffMultiplier: number
): number {
  const delay = initialDelay * Math.pow(backoffMultiplier, attempt);
  return Math.min(delay, maxDelay);
}

function shouldRetry(
  error: unknown,
  retryableStatusCodes: number[],
  retryableErrorCodes: string[]
): boolean {
  if (error instanceof ApiError) {
    const nonRetryableErrorCodes = ['C002', 'VALIDATION_ERROR', 'AUTHENTICATION_ERROR', 'AUTHORIZATION_ERROR'];
    if (nonRetryableErrorCodes.includes(error.code)) {
      return false;
    }

    // 429 Rate Limit 에러는 재시도하지 않음 (사용자에게 즉시 알려야 함)
    if (error.statusCode === 429) {
      return false;
    }

    if (error.statusCode >= 400 && error.statusCode < 500) {
      if (error.statusCode === 408) {
        return true;
      }
      return false;
    }

    if (retryableErrorCodes.includes(error.code)) {
      return true;
    }
    if (retryableStatusCodes.includes(error.statusCode)) {
      return true;
    }
  }
  return false;
}

function getRetryDelay(
  error: unknown,
  attempt: number,
  initialDelay: number,
  maxDelay: number,
  backoffMultiplier: number
): number {
  if (error instanceof ApiError && error.statusCode === 429 && error.retryAfter) {
    return Math.min(error.retryAfter * 1000 + 100, maxDelay);
  }
  
  return calculateDelay(attempt, initialDelay, maxDelay, backoffMultiplier);
}

export async function withRetry<T>(
  fn: () => Promise<T>,
  options: RetryOptions = {}
): Promise<T> {
  const config = { ...DEFAULT_OPTIONS, ...options };
  let lastError: unknown;

  for (let attempt = 0; attempt <= config.maxRetries; attempt++) {
    try {
      return await fn();
    } catch (error) {
      lastError = error;

      if (attempt === config.maxRetries) {
        break;
      }

      if (!shouldRetry(error, config.retryableStatusCodes, config.retryableErrorCodes)) {
        throw error;
      }

      const delay = getRetryDelay(
        error,
        attempt,
        config.initialDelay,
        config.maxDelay,
        config.backoffMultiplier
      );

      await new Promise((resolve) => setTimeout(resolve, delay));
    }
  }

  throw lastError;
}
