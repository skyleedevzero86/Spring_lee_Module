import { useState, useCallback, useRef } from 'react';
import { ApiError } from '@/src/domain/types/error.types';

interface UseAsyncOptions<T> {
  onSuccess?: (data: T) => void | Promise<void>;
  onError?: (error: ApiError) => void;
  immediate?: boolean;
}

interface UseAsyncReturn<T, Args extends unknown[] = []> {
  data: T | null;
  loading: boolean;
  error: ApiError | null;
  execute: (...args: Args) => Promise<T | undefined>;
  reset: () => void;
}

export function useAsync<T, Args extends unknown[] = []>(
  asyncFunction: (...args: Args) => Promise<T>,
  options: UseAsyncOptions<T> = {}
): UseAsyncReturn<T, Args> {
  const { onSuccess, onError, immediate = false } = options;
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);
  const abortControllerRef = useRef<AbortController | null>(null);

  const execute = useCallback(
    async (...args: Args): Promise<T | undefined> => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }

      const abortController = new AbortController();
      abortControllerRef.current = abortController;

      setLoading(true);
      setError(null);

      try {
        const result = await asyncFunction(...args);

        if (abortController.signal.aborted) {
          return undefined;
        }

        setData(result);
        await onSuccess?.(result);
        return result;
      } catch (err) {
        if (abortController.signal.aborted) {
          return undefined;
        }

        const apiError =
          err instanceof ApiError
            ? err
            : new ApiError(
                'UNKNOWN_ERROR',
                500,
                '알 수 없는 오류가 발생했습니다.'
              );
        setError(apiError);
        onError?.(apiError);
        throw apiError;
      } finally {
        if (!abortController.signal.aborted) {
          setLoading(false);
        }
      }
    },
    [asyncFunction, onSuccess, onError]
  );

  const reset = useCallback(() => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    setData(null);
    setError(null);
    setLoading(false);
  }, []);

  return {
    data,
    loading,
    error,
    execute,
    reset,
  };
}

