import { ApiError } from '@/domain/types/error.types';

export async function handleServiceCall<T>(
  apiCall: () => Promise<T>,
  errorCode: string,
  errorMessage: string
): Promise<T> {
  try {
    return await apiCall();
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError(errorCode, 500, errorMessage);
  }
}

export async function handleServiceCallWithPostProcess<T, R = T>(
  apiCall: () => Promise<T>,
  onSuccess: (result: T) => R | Promise<R>,
  errorCode: string,
  errorMessage: string
): Promise<R> {
  try {
    const result = await apiCall();
    return await onSuccess(result);
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError(errorCode, 500, errorMessage);
  }
}

