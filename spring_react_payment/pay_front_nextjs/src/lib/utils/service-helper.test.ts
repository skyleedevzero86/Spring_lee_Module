import { ApiError } from '@/src/domain/types/error.types';
import {
  handleServiceCall,
  handleServiceCallWithPostProcess,
} from './service-helper';

describe('service-helper', () => {
  describe('handleServiceCall', () => {
    it('API 호출 성공 시 결과 반환', async () => {
      const mockApiCall = jest.fn().mockResolvedValue('success');
      const result = await handleServiceCall(
        mockApiCall,
        'TEST_ERROR',
        '테스트 에러 메시지'
      );

      expect(result).toBe('success');
      expect(mockApiCall).toHaveBeenCalledTimes(1);
    });

    it('API 호출이 ApiError를 던질 때 ApiError 그대로 전파', async () => {
      const apiError = new ApiError('EXISTING_ERROR', 400, '기존 에러');
      const mockApiCall = jest.fn().mockRejectedValue(apiError);

      await expect(
        handleServiceCall(mockApiCall, 'TEST_ERROR', '테스트 에러 메시지')
      ).rejects.toThrow(apiError);
    });

    it('ApiError가 아닌 에러를 ApiError로 래핑', async () => {
      const mockApiCall = jest
        .fn()
        .mockRejectedValue(new Error('네트워크 오류'));

      await expect(
        handleServiceCall(mockApiCall, 'TEST_ERROR', '테스트 에러 메시지')
      ).rejects.toThrow(ApiError);

      try {
        await handleServiceCall(
          mockApiCall,
          'TEST_ERROR',
          '테스트 에러 메시지'
        );
      } catch (error) {
        expect(error).toBeInstanceOf(ApiError);
        if (error instanceof ApiError) {
          expect(error.code).toBe('TEST_ERROR');
          expect(error.message).toBe('테스트 에러 메시지');
          expect(error.statusCode).toBe(500);
        }
      }
    });
  });

  describe('handleServiceCallWithPostProcess', () => {
    it('onSuccess를 결과와 함께 호출하고 처리된 값 반환', async () => {
      const mockApiCall = jest.fn().mockResolvedValue(10);
      const mockOnSuccess = jest.fn().mockReturnValue(20);

      const result = await handleServiceCallWithPostProcess(
        mockApiCall,
        mockOnSuccess,
        'TEST_ERROR',
        '테스트 에러 메시지'
      );

      expect(result).toBe(20);
      expect(mockApiCall).toHaveBeenCalledTimes(1);
      expect(mockOnSuccess).toHaveBeenCalledWith(10);
    });

    it('비동기 onSuccess 처리', async () => {
      const mockApiCall = jest.fn().mockResolvedValue(10);
      const mockOnSuccess = jest.fn().mockResolvedValue(20);

      const result = await handleServiceCallWithPostProcess(
        mockApiCall,
        mockOnSuccess,
        'TEST_ERROR',
        '테스트 에러 메시지'
      );

      expect(result).toBe(20);
      expect(mockOnSuccess).toHaveBeenCalledWith(10);
    });

    it('API 호출이 ApiError를 던질 때 ApiError 전파', async () => {
      const apiError = new ApiError('EXISTING_ERROR', 400, '기존 에러');
      const mockApiCall = jest.fn().mockRejectedValue(apiError);
      const mockOnSuccess = jest.fn();

      await expect(
        handleServiceCallWithPostProcess(
          mockApiCall,
          mockOnSuccess,
          'TEST_ERROR',
          '테스트 에러 메시지'
        )
      ).rejects.toThrow(apiError);

      expect(mockOnSuccess).not.toHaveBeenCalled();
    });

    it('ApiError가 아닌 에러를 ApiError로 래핑', async () => {
      const mockApiCall = jest
        .fn()
        .mockRejectedValue(new Error('네트워크 오류'));
      const mockOnSuccess = jest.fn();

      await expect(
        handleServiceCallWithPostProcess(
          mockApiCall,
          mockOnSuccess,
          'TEST_ERROR',
          '테스트 에러 메시지'
        )
      ).rejects.toThrow(ApiError);

      expect(mockOnSuccess).not.toHaveBeenCalled();
    });
  });
});
