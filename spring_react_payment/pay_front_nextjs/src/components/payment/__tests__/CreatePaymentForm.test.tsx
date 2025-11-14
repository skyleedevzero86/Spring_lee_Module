import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CreatePaymentForm } from '../CreatePaymentForm';
import { usePayment } from '@/src/hooks/use-payment';
import { ApiError } from '@/src/domain/types/error.types';

jest.mock('@/src/hooks/use-payment');

const mockUsePayment = usePayment as jest.MockedFunction<typeof usePayment>;

describe('CreatePaymentForm', () => {
  const mockCreatePayment = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    Object.defineProperty(window, 'location', {
      value: {
        origin: 'http://localhost:3000',
        href: '',
      },
      writable: true,
    });

    mockUsePayment.mockReturnValue({
      loading: false,
      error: null,
      createPayment: mockCreatePayment,
      approvePayment: jest.fn(),
      getPaymentStatus: jest.fn(),
      getPaymentHistory: jest.fn(),
      getPaymentHistoryPage: jest.fn(),
      getPaymentDetail: jest.fn(),
      refundPayment: jest.fn(),
    });
  });

  it('결제 생성 폼 렌더링', () => {
    render(<CreatePaymentForm />);

    expect(screen.getByLabelText('주문번호')).toBeInTheDocument();
    expect(screen.getByLabelText('상품 설명')).toBeInTheDocument();
    expect(screen.getByLabelText('결제 금액')).toBeInTheDocument();
    expect(screen.getByLabelText('비과세 금액')).toBeInTheDocument();
    expect(screen.getByLabelText('결제 완료 URL')).toBeInTheDocument();
    expect(screen.getByLabelText('결제 취소 URL')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '결제 생성' })).toBeInTheDocument();
  });

  it('유효한 정보로 결제 생성 성공', async () => {
    const user = userEvent.setup({ delay: null });
    const mockResponse = {
      paymentId: 1,
      checkoutPage: 'http://checkout.example.com',
      orderNo: 'ORDER-123',
    };
    mockCreatePayment.mockResolvedValue(mockResponse);

    render(<CreatePaymentForm />);

    await user.type(screen.getByLabelText('주문번호'), 'ORDER-123');
    await user.type(screen.getByLabelText('상품 설명'), '테스트 상품');
    await user.type(screen.getByLabelText('결제 금액'), '10000');

    await user.click(screen.getByRole('button', { name: '결제 생성' }));

    await waitFor(() => {
      expect(mockCreatePayment).toHaveBeenCalled();
      const callArgs = mockCreatePayment.mock.calls[0][0];
      expect(callArgs.orderNo).toBe('ORDER-123');
      expect(callArgs.productDesc).toBe('테스트 상품');
      expect(callArgs.amount).toBe(10000);
      expect(callArgs.expiredTime).toBeDefined();
    });

    expect(window.location.href).toBe('http://checkout.example.com');
  });

  it('checkoutPage가 없을 때 성공 메시지 표시', async () => {
    const user = userEvent.setup({ delay: null });
    const mockResponse = {
      paymentId: 1,
      orderNo: 'ORDER-123',
    };
    mockCreatePayment.mockResolvedValue(mockResponse);

    render(<CreatePaymentForm />);

    await user.type(screen.getByLabelText('주문번호'), 'ORDER-123');
    await user.type(screen.getByLabelText('상품 설명'), '테스트 상품');
    await user.type(screen.getByLabelText('결제 금액'), '10000');

    await user.click(screen.getByRole('button', { name: '결제 생성' }));

    await waitFor(() => {
      expect(screen.getByText('결제가 생성되었습니다.')).toBeInTheDocument();
    });
  });

  it('결제 생성 실패 시 에러 메시지 표시', async () => {
    const user = userEvent.setup({ delay: null });
    const apiError = new ApiError('INVALID_AMOUNT', 400, '유효하지 않은 금액입니다.');
    mockCreatePayment.mockRejectedValue(apiError);

    mockUsePayment.mockReturnValue({
      loading: false,
      error: apiError,
      createPayment: mockCreatePayment,
      approvePayment: jest.fn(),
      getPaymentStatus: jest.fn(),
      getPaymentHistory: jest.fn(),
      getPaymentHistoryPage: jest.fn(),
      getPaymentDetail: jest.fn(),
      refundPayment: jest.fn(),
    });

    render(<CreatePaymentForm />);

    await user.type(screen.getByLabelText('주문번호'), 'ORDER-123');
    await user.type(screen.getByLabelText('상품 설명'), '테스트 상품');
    await user.type(screen.getByLabelText('결제 금액'), '10000');

    await user.click(screen.getByRole('button', { name: '결제 생성' }));

    await waitFor(() => {
      expect(screen.getByText('유효하지 않은 금액입니다.')).toBeInTheDocument();
    });
  });

  it('로딩 중일 때 버튼 비활성화', () => {
    mockUsePayment.mockReturnValue({
      loading: true,
      error: null,
      createPayment: mockCreatePayment,
      approvePayment: jest.fn(),
      getPaymentStatus: jest.fn(),
      getPaymentHistory: jest.fn(),
      getPaymentHistoryPage: jest.fn(),
      getPaymentDetail: jest.fn(),
      refundPayment: jest.fn(),
    });

    render(<CreatePaymentForm />);

    expect(screen.getByRole('button', { name: '결제 생성' })).toBeDisabled();
  });

  it('기본값이 올바르게 설정되어야 함', () => {
    render(<CreatePaymentForm />);

    const taxFreeInput = screen.getByLabelText('비과세 금액') as HTMLInputElement;
    expect(taxFreeInput.value).toBe('0');

    const retUrlInput = screen.getByLabelText('결제 완료 URL') as HTMLInputElement;
    expect(retUrlInput.value).toContain('/payments/success');

    const retCancelUrlInput = screen.getByLabelText('결제 취소 URL') as HTMLInputElement;
    expect(retCancelUrlInput.value).toContain('/payments/cancel');
  });
});

