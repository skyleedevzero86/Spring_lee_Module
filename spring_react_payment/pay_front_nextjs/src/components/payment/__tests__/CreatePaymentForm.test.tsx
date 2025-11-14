import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CreatePaymentForm } from '../CreatePaymentForm';
import { usePayment } from '@/hooks/use-payment';
import { ApiError } from '@/domain/types/error.types';

jest.mock('@/hooks/use-payment');

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

  it('결제 ?�성 ???�더�?, () => {
    render(<CreatePaymentForm />);

    expect(screen.getByLabelText('주문번호')).toBeInTheDocument();
    expect(screen.getByLabelText('?�품 ?�명')).toBeInTheDocument();
    expect(screen.getByLabelText('결제 금액')).toBeInTheDocument();
    expect(screen.getByLabelText('비과??금액')).toBeInTheDocument();
    expect(screen.getByLabelText('결제 ?�료 URL')).toBeInTheDocument();
    expect(screen.getByLabelText('결제 취소 URL')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '결제 ?�성' })).toBeInTheDocument();
  });

  it('?�효???�보�?결제 ?�성 ?�공', async () => {
    const user = userEvent.setup({ delay: null });
    const mockResponse = {
      paymentId: 1,
      checkoutPage: 'http://checkout.example.com',
      orderNo: 'ORDER-123',
    };
    mockCreatePayment.mockResolvedValue(mockResponse);

    render(<CreatePaymentForm />);

    await user.type(screen.getByLabelText('주문번호'), 'ORDER-123');
    await user.type(screen.getByLabelText('?�품 ?�명'), '?�스???�품');
    await user.type(screen.getByLabelText('결제 금액'), '10000');

    await user.click(screen.getByRole('button', { name: '결제 ?�성' }));

    await waitFor(() => {
      expect(mockCreatePayment).toHaveBeenCalled();
      const callArgs = mockCreatePayment.mock.calls[0][0];
      expect(callArgs.orderNo).toBe('ORDER-123');
      expect(callArgs.productDesc).toBe('?�스???�품');
      expect(callArgs.amount).toBe(10000);
      expect(callArgs.expiredTime).toBeDefined();
    });

    expect(window.location.href).toBe('http://checkout.example.com');
  });

  it('checkoutPage가 ?�을 ???�공 메시지 ?�시', async () => {
    const user = userEvent.setup({ delay: null });
    const mockResponse = {
      paymentId: 1,
      orderNo: 'ORDER-123',
    };
    mockCreatePayment.mockResolvedValue(mockResponse);

    render(<CreatePaymentForm />);

    await user.type(screen.getByLabelText('주문번호'), 'ORDER-123');
    await user.type(screen.getByLabelText('?�품 ?�명'), '?�스???�품');
    await user.type(screen.getByLabelText('결제 금액'), '10000');

    await user.click(screen.getByRole('button', { name: '결제 ?�성' }));

    await waitFor(() => {
      expect(screen.getByText('결제가 ?�성?�었?�니??')).toBeInTheDocument();
    });
  });

  it('결제 ?�성 ?�패 ???�러 메시지 ?�시', async () => {
    const user = userEvent.setup({ delay: null });
    const apiError = new ApiError('INVALID_AMOUNT', 400, '?�효?��? ?��? 금액?�니??');
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
    await user.type(screen.getByLabelText('?�품 ?�명'), '?�스???�품');
    await user.type(screen.getByLabelText('결제 금액'), '10000');

    await user.click(screen.getByRole('button', { name: '결제 ?�성' }));

    await waitFor(() => {
      expect(screen.getByText('?�효?��? ?��? 금액?�니??')).toBeInTheDocument();
    });
  });

  it('로딩 중일 ??버튼 비활?�화', () => {
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

    expect(screen.getByRole('button', { name: '결제 ?�성' })).toBeDisabled();
  });

  it('기본값이 ?�바르게 ?�정?�어????, () => {
    render(<CreatePaymentForm />);

    const taxFreeInput = screen.getByLabelText('비과??금액') as HTMLInputElement;
    expect(taxFreeInput.value).toBe('0');

    const retUrlInput = screen.getByLabelText('결제 ?�료 URL') as HTMLInputElement;
    expect(retUrlInput.value).toContain('/payments/success');

    const retCancelUrlInput = screen.getByLabelText('결제 취소 URL') as HTMLInputElement;
    expect(retCancelUrlInput.value).toContain('/payments/cancel');
  });
});

