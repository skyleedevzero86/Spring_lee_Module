import { renderHook, waitFor } from '@testing-library/react';
import { useAsync } from './use-async';
import { ApiError } from '@/src/domain/types/error.types';

describe('useAsync', () => {
  it('should initialize with null data, false loading, and null error', () => {
    const { result } = renderHook(() =>
      useAsync(() => Promise.resolve('test'))
    );

    expect(result.current.data).toBeNull();
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBeNull();
  });

  it('should set loading to true when execute is called', async () => {
    const mockAsyncFn = jest.fn(
      () => new Promise((resolve) => setTimeout(() => resolve('success'), 100))
    );

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    const executePromise = result.current.execute();

    expect(result.current.loading).toBe(true);

    await executePromise;
    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });
  });

  it('should set data when async function succeeds', async () => {
    const mockAsyncFn = jest.fn().mockResolvedValue('success');

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    await result.current.execute();

    expect(result.current.data).toBe('success');
    expect(result.current.error).toBeNull();
    expect(result.current.loading).toBe(false);
  });

  it('should set error when async function throws ApiError', async () => {
    const apiError = new ApiError('TEST_ERROR', 400, 'Test error');
    const mockAsyncFn = jest.fn().mockRejectedValue(apiError);

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    await expect(result.current.execute()).rejects.toThrow(apiError);

    await waitFor(() => {
      expect(result.current.error).toBe(apiError);
      expect(result.current.loading).toBe(false);
    });
  });

  it('should wrap non-ApiError in ApiError', async () => {
    const mockAsyncFn = jest
      .fn()
      .mockRejectedValue(new Error('Network error'));

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    await expect(result.current.execute()).rejects.toThrow(ApiError);

    await waitFor(() => {
      expect(result.current.error).toBeInstanceOf(ApiError);
      expect(result.current.loading).toBe(false);
    });
  });

  it('should call onSuccess callback when provided', async () => {
    const mockOnSuccess = jest.fn();
    const mockAsyncFn = jest.fn().mockResolvedValue('success');

    const { result } = renderHook(() =>
      useAsync(mockAsyncFn, { onSuccess: mockOnSuccess })
    );

    await result.current.execute();

    expect(mockOnSuccess).toHaveBeenCalledWith('success');
  });

  it('should call onError callback when provided', async () => {
    const apiError = new ApiError('TEST_ERROR', 400, 'Test error');
    const mockOnError = jest.fn();
    const mockAsyncFn = jest.fn().mockRejectedValue(apiError);

    const { result } = renderHook(() =>
      useAsync(mockAsyncFn, { onError: mockOnError })
    );

    await expect(result.current.execute()).rejects.toThrow();

    await waitFor(() => {
      expect(mockOnError).toHaveBeenCalledWith(apiError);
    });
  });

  it('should reset state when reset is called', async () => {
    const mockAsyncFn = jest.fn().mockResolvedValue('success');

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    await result.current.execute();

    expect(result.current.data).toBe('success');

    result.current.reset();

    expect(result.current.data).toBeNull();
    expect(result.current.error).toBeNull();
    expect(result.current.loading).toBe(false);
  });

  it('should handle function with arguments', async () => {
    const mockAsyncFn = jest.fn((arg1: string, arg2: number) =>
      Promise.resolve(`${arg1}-${arg2}`)
    );

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    await result.current.execute('test', 123);

    expect(mockAsyncFn).toHaveBeenCalledWith('test', 123);
    expect(result.current.data).toBe('test-123');
  });
});

