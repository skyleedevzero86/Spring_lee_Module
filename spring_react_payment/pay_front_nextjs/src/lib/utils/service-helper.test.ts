import { ApiError } from '@/src/domain/types/error.types';
import {
  handleServiceCall,
  handleServiceCallWithPostProcess,
} from './service-helper';

describe('service-helper', () => {
  describe('handleServiceCall', () => {
    it('should return the result when API call succeeds', async () => {
      const mockApiCall = jest.fn().mockResolvedValue('success');
      const result = await handleServiceCall(
        mockApiCall,
        'TEST_ERROR',
        'Test error message'
      );

      expect(result).toBe('success');
      expect(mockApiCall).toHaveBeenCalledTimes(1);
    });

    it('should throw ApiError when API call throws ApiError', async () => {
      const apiError = new ApiError('EXISTING_ERROR', 400, 'Existing error');
      const mockApiCall = jest.fn().mockRejectedValue(apiError);

      await expect(
        handleServiceCall(mockApiCall, 'TEST_ERROR', 'Test error message')
      ).rejects.toThrow(apiError);
    });

    it('should wrap non-ApiError in ApiError', async () => {
      const mockApiCall = jest
        .fn()
        .mockRejectedValue(new Error('Network error'));

      await expect(
        handleServiceCall(mockApiCall, 'TEST_ERROR', 'Test error message')
      ).rejects.toThrow(ApiError);

      try {
        await handleServiceCall(
          mockApiCall,
          'TEST_ERROR',
          'Test error message'
        );
      } catch (error) {
        expect(error).toBeInstanceOf(ApiError);
        if (error instanceof ApiError) {
          expect(error.code).toBe('TEST_ERROR');
          expect(error.message).toBe('Test error message');
          expect(error.statusCode).toBe(500);
        }
      }
    });
  });

  describe('handleServiceCallWithPostProcess', () => {
    it('should call onSuccess with result and return processed value', async () => {
      const mockApiCall = jest.fn().mockResolvedValue(10);
      const mockOnSuccess = jest.fn().mockReturnValue(20);

      const result = await handleServiceCallWithPostProcess(
        mockApiCall,
        mockOnSuccess,
        'TEST_ERROR',
        'Test error message'
      );

      expect(result).toBe(20);
      expect(mockApiCall).toHaveBeenCalledTimes(1);
      expect(mockOnSuccess).toHaveBeenCalledWith(10);
    });

    it('should handle async onSuccess', async () => {
      const mockApiCall = jest.fn().mockResolvedValue(10);
      const mockOnSuccess = jest.fn().mockResolvedValue(20);

      const result = await handleServiceCallWithPostProcess(
        mockApiCall,
        mockOnSuccess,
        'TEST_ERROR',
        'Test error message'
      );

      expect(result).toBe(20);
      expect(mockOnSuccess).toHaveBeenCalledWith(10);
    });

    it('should throw ApiError when API call throws ApiError', async () => {
      const apiError = new ApiError('EXISTING_ERROR', 400, 'Existing error');
      const mockApiCall = jest.fn().mockRejectedValue(apiError);
      const mockOnSuccess = jest.fn();

      await expect(
        handleServiceCallWithPostProcess(
          mockApiCall,
          mockOnSuccess,
          'TEST_ERROR',
          'Test error message'
        )
      ).rejects.toThrow(apiError);

      expect(mockOnSuccess).not.toHaveBeenCalled();
    });

    it('should wrap non-ApiError in ApiError', async () => {
      const mockApiCall = jest
        .fn()
        .mockRejectedValue(new Error('Network error'));
      const mockOnSuccess = jest.fn();

      await expect(
        handleServiceCallWithPostProcess(
          mockApiCall,
          mockOnSuccess,
          'TEST_ERROR',
          'Test error message'
        )
      ).rejects.toThrow(ApiError);

      expect(mockOnSuccess).not.toHaveBeenCalled();
    });
  });
});

