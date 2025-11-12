import { AxiosError } from 'axios';

interface ErrorResponse {
  message?: string;
  error?: string;
  status?: number;
}

export const handleApiError = (err: unknown): string => {
  if (err instanceof AxiosError) {
    const errorData = err.response?.data as ErrorResponse | undefined;
    if (errorData?.message) {
      return errorData.message;
    }
    if (errorData?.error) {
      return errorData.error;
    }
    if (err.response?.status === 400) {
      return '입력값이 올바르지 않습니다. 다시 확인해주세요.';
    }
    if (err.response?.status === 401) {
      return '인증이 필요합니다. 다시 로그인해주세요.';
    }
    if (err.response?.status === 403) {
      return '접근 권한이 없습니다.';
    }
    if (err.response?.status === 404) {
      return '요청한 리소스를 찾을 수 없습니다.';
    }
    if (err.response?.status === 500) {
      return '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
    }
    return `서버 오류가 발생했습니다. (상태 코드: ${err.response?.status || '알 수 없음'})`;
  }
  if (err instanceof Error) {
    return err.message;
  }
  return '알 수 없는 오류가 발생했습니다.';
};

