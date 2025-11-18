import { PaymentStatus } from '@/domain/types/payment.types';

export const getStatusText = (status: string): string => {
  switch (status) {
    case PaymentStatus.COMPLETED:
      return '결제 완료';
    case PaymentStatus.APPROVED:
      return '결제 승인';
    case PaymentStatus.PENDING:
      return '결제 대기';
    case PaymentStatus.CANCELLED:
      return '결제 취소';
    case PaymentStatus.FAILED:
      return '결제 실패';
    default:
      return status || '알 수 없음';
  }
};

export const getStatusColor = (status: string): string => {
  switch (status) {
    case PaymentStatus.COMPLETED:
      return 'bg-green-100 text-green-800';
    case PaymentStatus.APPROVED:
      return 'bg-blue-100 text-blue-800';
    case PaymentStatus.PENDING:
      return 'bg-yellow-100 text-yellow-800';
    case PaymentStatus.CANCELLED:
      return 'bg-red-100 text-red-800';
    case PaymentStatus.FAILED:
      return 'bg-gray-100 text-gray-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

