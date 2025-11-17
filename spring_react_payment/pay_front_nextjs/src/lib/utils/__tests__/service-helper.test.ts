import { ApiError } from '@/domain/types/error.types';
import {
  handleServiceCall,
  handleServiceCallWithPostProcess,
} from '../service-helper';

describe('service-helper', () => {
  describe('handleServiceCall', () => {
    it('API ?�출 ?�공 ??결과 반환', async () => {
      const mockApiCall = jest.fn().mockResolvedValue('success');
      const result = await handleServiceCall(
        mockApiCall,
        'TEST_ERROR',
        '?�스???�러 메시지'
      );

      expect(result).toBe('success');
      expect(mockApiCall).toHaveBeenCalledTimes(1);
    });

    it('API ?�출??ApiError�??�질 ??ApiError 그�?�??�파', async () => {
      const apiError = new ApiError('EXISTING_ERROR', 400, '기존 ?�러');
      const mockApiCall = jest.fn().mockRejectedValue(apiError);

      await expect(
        handleServiceCall(mockApiCall, 'TEST_ERROR', '?�스???�러 메시지')
      ).rejects.toThrow(apiError);
    });

    it('ApiError가 ?�닌 ?�러�?ApiError�??�핑', async () => {
      const mockApiCall = jest
        .fn()
        .mockRejectedValue(new Error('?�트?�크 ?�류'));

      await expect(
        handleServiceCall(mockApiCall, 'TEST_ERROR', '?�스???�러 메시지')
      ).rejects.toThrow(ApiError);

      try {
        await handleServiceCall(
          mockApiCall,
          'TEST_ERROR',
          '?�스???�러 메시지'
        );
      } catch (error) {
        expect(error).toBeInstanceOf(ApiError);
        if (error instanceof ApiError) {
          expect(error.code).toBe('TEST_ERROR');
          expect(error.message).toBe('?�스???�러 메시지');
          expect(error.statusCode).toBe(500);
        }
      }
    });
  });

  describe('handleServiceCallWithPostProcess', () => {
    it('onSuccess�?결과?� ?�께 ?�출?�고 처리??�?반환', async () => {
      const mockApiCall = jest.fn().mockResolvedValue(10);
      const mockOnSuccess = jest.fn().mockReturnValue(20);

      const result = await handleServiceCallWithPostProcess(
        mockApiCall,
        mockOnSuccess,
        'TEST_ERROR',
        '?�스???�러 메시지'
      );

      expect(result).toBe(20);
      expect(mockApiCall).toHaveBeenCalledTimes(1);
      expect(mockOnSuccess).toHaveBeenCalledWith(10);
    });

    it('비동�?onSuccess 처리', async () => {
      const mockApiCall = jest.fn().mockResolvedValue(10);
      const mockOnSuccess = jest.fn().mockResolvedValue(20);

      const result = await handleServiceCallWithPostProcess(
        mockApiCall,
        mockOnSuccess,
        'TEST_ERROR',
        '?�스???�러 메시지'
      );

      expect(result).toBe(20);
      expect(mockOnSuccess).toHaveBeenCalledWith(10);
    });

    it('API ?�출??ApiError�??�질 ??ApiError ?�파', async () => {
      const apiError = new ApiError('EXISTING_ERROR', 400, '기존 ?�러');
      const mockApiCall = jest.fn().mockRejectedValue(apiError);
      const mockOnSuccess = jest.fn();

      await expect(
        handleServiceCallWithPostProcess(
          mockApiCall,
          mockOnSuccess,
          'TEST_ERROR',
          '?�스???�러 메시지'
        )
      ).rejects.toThrow(apiError);

      expect(mockOnSuccess).not.toHaveBeenCalled();
    });

    it('ApiError가 ?�닌 ?�러�?ApiError�??�핑', async () => {
      const mockApiCall = jest
        .fn()
        .mockRejectedValue(new Error('?�트?�크 ?�류'));
      const mockOnSuccess = jest.fn();

      await expect(
        handleServiceCallWithPostProcess(
          mockApiCall,
          mockOnSuccess,
          'TEST_ERROR',
          '?�스???�러 메시지'
        )
      ).rejects.toThrow(ApiError);

      expect(mockOnSuccess).not.toHaveBeenCalled();
    });
  });
});
