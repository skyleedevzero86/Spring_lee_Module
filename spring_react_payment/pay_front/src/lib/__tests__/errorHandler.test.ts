import { AxiosError } from 'axios';
import { handleApiError } from '../errorHandler';

describe('handleApiError', () => {
  it('AxiosError의 response.data.message를 반환해야 함', () => {
    // given
    const errorMessage = '서버에서 발생한 오류 메시지';
    const error = new AxiosError('Request failed');
    error.response = {
      data: { message: errorMessage },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as any,
    };

    // when
    const result = handleApiError(error);

    // then
    expect(result).toBe(errorMessage);
  });

  it('AxiosError의 response.data.error를 반환해야 함', () => {
    // given
    const errorMessage = '에러 발생';
    const error = new AxiosError('Request failed');
    error.response = {
      data: { error: errorMessage },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as any,
    };

    // when
    const result = handleApiError(error);

    // then
    expect(result).toBe(errorMessage);
  });

  it('401 상태 코드일 때 인증 오류 메시지를 반환해야 함', () => {
    // given
    const error = new AxiosError('Request failed');
    error.response = {
      data: {},
      status: 401,
      statusText: 'Unauthorized',
      headers: {},
      config: {} as any,
    };

    // when
    const result = handleApiError(error);

    // then
    expect(result).toBe('인증이 필요합니다. 다시 로그인해주세요.');
  });

  it('403 상태 코드일 때 권한 오류 메시지를 반환해야 함', () => {
    // given
    const error = new AxiosError('Request failed');
    error.response = {
      data: {},
      status: 403,
      statusText: 'Forbidden',
      headers: {},
      config: {} as any,
    };

    // when
    const result = handleApiError(error);

    // then
    expect(result).toBe('접근 권한이 없습니다.');
  });

  it('404 상태 코드일 때 리소스 없음 메시지를 반환해야 함', () => {
    // given
    const error = new AxiosError('Request failed');
    error.response = {
      data: {},
      status: 404,
      statusText: 'Not Found',
      headers: {},
      config: {} as any,
    };

    // when
    const result = handleApiError(error);

    // then
    expect(result).toBe('요청한 리소스를 찾을 수 없습니다.');
  });

  it('500 상태 코드일 때 서버 오류 메시지를 반환해야 함', () => {
    // given
    const error = new AxiosError('Request failed');
    error.response = {
      data: {},
      status: 500,
      statusText: 'Internal Server Error',
      headers: {},
      config: {} as any,
    };

    // when
    const result = handleApiError(error);

    // then
    expect(result).toBe('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
  });

  it('기타 상태 코드일 때 일반 서버 오류 메시지를 반환해야 함', () => {
    // given
    const error = new AxiosError('Request failed');
    error.response = {
      data: {},
      status: 503,
      statusText: 'Service Unavailable',
      headers: {},
      config: {} as any,
    };

    // when
    const result = handleApiError(error);

    // then
    expect(result).toBe('서버 오류가 발생했습니다.');
  });

  it('Error 인스턴스일 때 에러 메시지를 반환해야 함', () => {
    // given
    const errorMessage = '일반 에러 메시지';
    const error = new Error(errorMessage);

    // when
    const result = handleApiError(error);

    // then
    expect(result).toBe(errorMessage);
  });

  it('알 수 없는 타입의 에러일 때 기본 메시지를 반환해야 함', () => {
    // given
    const error = '문자열 에러';

    // when
    const result = handleApiError(error);

    // then
    expect(result).toBe('알 수 없는 오류가 발생했습니다.');
  });

  it('response가 없는 AxiosError일 때 기본 메시지를 반환해야 함', () => {
    // given
    const error = new AxiosError('Network Error');

    // when
    const result = handleApiError(error);

    // then
    expect(result).toBe('서버 오류가 발생했습니다.');
  });
});


