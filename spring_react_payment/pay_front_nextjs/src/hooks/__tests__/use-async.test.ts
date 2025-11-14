import { renderHook, waitFor } from '@testing-library/react';
import { useAsync } from '../use-async';
import { ApiError } from '@/domain/types/error.types';

describe('useAsync', () => {
  it('null ?°ì´?? false ë¡œë”©, null ?ëŸ¬ë¡?ì´ˆê¸°??, () => {
    const { result } = renderHook(() =>
      useAsync(() => Promise.resolve('test'))
    );

    expect(result.current.data).toBeNull();
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBeNull();
  });

  it('execute ?¸ì¶œ ??ë¡œë”©??trueë¡??¤ì •', async () => {
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

  it('ë¹„ë™ê¸??¨ìˆ˜ ?±ê³µ ???°ì´???¤ì •', async () => {
    const mockAsyncFn = jest.fn().mockResolvedValue('success');

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    await result.current.execute();

    await waitFor(() => {
      expect(result.current.data).toBe('success');
      expect(result.current.error).toBeNull();
      expect(result.current.loading).toBe(false);
    });
  });

  it('ë¹„ë™ê¸??¨ìˆ˜ê°€ ApiErrorë¥??˜ì§ˆ ???ëŸ¬ ?¤ì •', async () => {
    const apiError = new ApiError('TEST_ERROR', 400, '?ŒìŠ¤???ëŸ¬');
    const mockAsyncFn = jest.fn().mockRejectedValue(apiError);

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    await expect(result.current.execute()).rejects.toThrow(apiError);

    await waitFor(() => {
      expect(result.current.error).toBe(apiError);
      expect(result.current.loading).toBe(false);
    });
  });

  it('ApiErrorê°€ ?„ë‹Œ ?ëŸ¬ë¥?ApiErrorë¡??˜í•‘', async () => {
    const mockAsyncFn = jest
      .fn()
      .mockRejectedValue(new Error('?¤íŠ¸?Œí¬ ?¤ë¥˜'));

    const { result } = renderHook(() => useAsync(mockAsyncFn));

    await expect(result.current.execute()).rejects.toThrow(ApiError);

    await waitFor(() => {
      expect(result.current.error).toBeInstanceOf(ApiError);
      expect(result.current.loading).toBe(false);
    });
  });

  it('onSuccess ì½œë°±???œê³µ?˜ë©´ ?¸ì¶œ', async () => {
    const mockOnSuccess = jest.fn();
    const mockAsyncFn = jest.fn().mockResolvedValue('success');

    const { result } = renderHook(() =>
      useAsync(mockAsyncFn, { onSuccess: mockOnSuccess })
    );

    await result.current.execute();

    expect(mockOnSuccess).toHaveBeenCalledWith('success');
  });

  it('onError ì½œë°±???œê³µ?˜ë©´ ?¸ì¶œ', async () => {
    const apiError = new ApiError('TEST_ERROR', 400, '?ŒìŠ¤???ëŸ¬');
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

  it('reset ?¸ì¶œ ???íƒœ ì´ˆê¸°??, async () => {
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

  it('?¸ìê°€ ?ˆëŠ” ?¨ìˆ˜ ì²˜ë¦¬', async () => {
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
