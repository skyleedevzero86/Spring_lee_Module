import { ApiError } from '@/domain/types/error.types';
import {
  handleServiceCall,
  handleServiceCallWithPostProcess,
} from '../service-helper';

describe('service-helper', () => {
  describe('handleServiceCall', () => {
    it('API ?¸ì¶œ ?±ê³µ ??ê²°ê³¼ ë°˜í™˜', async () => {
      const mockApiCall = jest.fn().mockResolvedValue('success');
      const result = await handleServiceCall(
        mockApiCall,
        'TEST_ERROR',
        '?ŒìŠ¤???ëŸ¬ ë©”ì‹œì§€'
      );

      expect(result).toBe('success');
      expect(mockApiCall).toHaveBeenCalledTimes(1);
    });

    it('API ?¸ì¶œ??ApiErrorë¥??˜ì§ˆ ??ApiError ê·¸ë?ë¡??„íŒŒ', async () => {
      const apiError = new ApiError('EXISTING_ERROR', 400, 'ê¸°ì¡´ ?ëŸ¬');
      const mockApiCall = jest.fn().mockRejectedValue(apiError);

      await expect(
        handleServiceCall(mockApiCall, 'TEST_ERROR', '?ŒìŠ¤???ëŸ¬ ë©”ì‹œì§€')
      ).rejects.toThrow(apiError);
    });

    it('ApiErrorê°€ ?„ë‹Œ ?ëŸ¬ë¥?ApiErrorë¡??˜í•‘', async () => {
      const mockApiCall = jest
        .fn()
        .mockRejectedValue(new Error('?¤íŠ¸?Œí¬ ?¤ë¥˜'));

      await expect(
        handleServiceCall(mockApiCall, 'TEST_ERROR', '?ŒìŠ¤???ëŸ¬ ë©”ì‹œì§€')
      ).rejects.toThrow(ApiError);

      try {
        await handleServiceCall(
          mockApiCall,
          'TEST_ERROR',
          '?ŒìŠ¤???ëŸ¬ ë©”ì‹œì§€'
        );
      } catch (error) {
        expect(error).toBeInstanceOf(ApiError);
        if (error instanceof ApiError) {
          expect(error.code).toBe('TEST_ERROR');
          expect(error.message).toBe('?ŒìŠ¤???ëŸ¬ ë©”ì‹œì§€');
          expect(error.statusCode).toBe(500);
        }
      }
    });
  });

  describe('handleServiceCallWithPostProcess', () => {
    it('onSuccessë¥?ê²°ê³¼?€ ?¨ê»˜ ?¸ì¶œ?˜ê³  ì²˜ë¦¬??ê°?ë°˜í™˜', async () => {
      const mockApiCall = jest.fn().mockResolvedValue(10);
      const mockOnSuccess = jest.fn().mockReturnValue(20);

      const result = await handleServiceCallWithPostProcess(
        mockApiCall,
        mockOnSuccess,
        'TEST_ERROR',
        '?ŒìŠ¤???ëŸ¬ ë©”ì‹œì§€'
      );

      expect(result).toBe(20);
      expect(mockApiCall).toHaveBeenCalledTimes(1);
      expect(mockOnSuccess).toHaveBeenCalledWith(10);
    });

    it('ë¹„ë™ê¸?onSuccess ì²˜ë¦¬', async () => {
      const mockApiCall = jest.fn().mockResolvedValue(10);
      const mockOnSuccess = jest.fn().mockResolvedValue(20);

      const result = await handleServiceCallWithPostProcess(
        mockApiCall,
        mockOnSuccess,
        'TEST_ERROR',
        '?ŒìŠ¤???ëŸ¬ ë©”ì‹œì§€'
      );

      expect(result).toBe(20);
      expect(mockOnSuccess).toHaveBeenCalledWith(10);
    });

    it('API ?¸ì¶œ??ApiErrorë¥??˜ì§ˆ ??ApiError ?„íŒŒ', async () => {
      const apiError = new ApiError('EXISTING_ERROR', 400, 'ê¸°ì¡´ ?ëŸ¬');
      const mockApiCall = jest.fn().mockRejectedValue(apiError);
      const mockOnSuccess = jest.fn();

      await expect(
        handleServiceCallWithPostProcess(
          mockApiCall,
          mockOnSuccess,
          'TEST_ERROR',
          '?ŒìŠ¤???ëŸ¬ ë©”ì‹œì§€'
        )
      ).rejects.toThrow(apiError);

      expect(mockOnSuccess).not.toHaveBeenCalled();
    });

    it('ApiErrorê°€ ?„ë‹Œ ?ëŸ¬ë¥?ApiErrorë¡??˜í•‘', async () => {
      const mockApiCall = jest
        .fn()
        .mockRejectedValue(new Error('?¤íŠ¸?Œí¬ ?¤ë¥˜'));
      const mockOnSuccess = jest.fn();

      await expect(
        handleServiceCallWithPostProcess(
          mockApiCall,
          mockOnSuccess,
          'TEST_ERROR',
          '?ŒìŠ¤???ëŸ¬ ë©”ì‹œì§€'
        )
      ).rejects.toThrow(ApiError);

      expect(mockOnSuccess).not.toHaveBeenCalled();
    });
  });
});
