import { renderHook, waitFor } from '@testing-library/react';
import { useAsync } from '../use-async';
import { ApiError } from '@/domain/types/error.types';

describe('useAsync', () => {
  it('null ?�이?? false 로딩, null ?�러�?초기??, () => {
    const { result } = renderHook(() =>
      useAsync(() => Promise.resolve('test'))
    );

    expect(result.current.data).toBeNull();
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBeNull();
  });

  it('execute ?�출 ??로딩??true�??�정', async () => {
    const mockAsyncFn = jest.fn(
      () => new Promise((resolve) => setTimeout(() => resolve('success'), 100))
    );

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    const executePromise = result.current.execute();

    await waitFor(() => {
      expect(result.current.loading).toBe(true);
    });

    await executePromise;
    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });
  });

  it('비동�??�수 ?�공 ???�이???�정', async () => {
    const mockAsyncFn = jest.fn().mockResolvedValue('success');

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    await result.current.execute();

    await waitFor(() => {
      expect(result.current.data).toBe('success');
      expect(result.current.error).toBeNull();
      expect(result.current.loading).toBe(false);
    });
  });

  it('비동�??�수가 ApiError�??�질 ???�러 ?�정', async () => {
    const apiError = new ApiError('TEST_ERROR', 400, '?�스???�러');
    const mockAsyncFn = jest.fn().mockRejectedValue(apiError);

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    await expect(result.current.execute()).rejects.toThrow(apiError);

    await waitFor(() => {
      expect(result.current.error).toBe(apiError);
      expect(result.current.loading).toBe(false);
    });
  });

  it('ApiError가 ?�닌 ?�러�?ApiError�??�핑', async () => {
    const mockAsyncFn = jest
      .fn()
      .mockRejectedValue(new Error('?�트?�크 ?�류'));

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    await expect(result.current.execute()).rejects.toThrow(ApiError);

    await waitFor(() => {
      expect(result.current.error).toBeInstanceOf(ApiError);
      expect(result.current.loading).toBe(false);
    });
  });

  it('onSuccess 콜백???�공?�면 ?�출', async () => {
    const mockOnSuccess = jest.fn();
    const mockAsyncFn = jest.fn().mockResolvedValue('success');

    const { result } = renderHook(() =>
      useAsync(mockAsyncFn, { onSuccess: mockOnSuccess })
    );

    await result.current.execute();

    expect(mockOnSuccess).toHaveBeenCalledWith('success');
  });

  it('onError 콜백???�공?�면 ?�출', async () => {
    const apiError = new ApiError('TEST_ERROR', 400, '?�스???�러');
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

  it('reset ?�출 ???�태 초기??, async () => {
    const mockAsyncFn = jest.fn().mockResolvedValue('success');

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    await result.current.execute();

    await waitFor(() => {
      expect(result.current.data).toBe('success');
    });

    result.current.reset();

    await waitFor(() => {
      expect(result.current.data).toBeNull();
      expect(result.current.error).toBeNull();
      expect(result.current.loading).toBe(false);
    });
  });

  it('?�자가 ?�는 ?�수 처리', async () => {
    const mockAsyncFn = jest.fn((arg1: string, arg2: number) =>
      Promise.resolve(`${arg1}-${arg2}`)
    );

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    await result.current.execute('test', 123);

    expect(mockAsyncFn).toHaveBeenCalledWith('test', 123);
    
    await waitFor(() => {
      expect(result.current.data).toBe('test-123');
    });
  });
});
