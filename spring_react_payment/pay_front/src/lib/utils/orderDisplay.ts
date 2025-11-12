export function getStatusDisplayName(status: string | null | undefined): string {
  if (!status) {
    return '-';
  }
  
  const statusMap: Record<string, string> = {
    'PENDING': '대기',
    'DONE': '완료',
    'ABORTED': '취소됨',
    'REFUND_REQUESTED': '환불 요청',
    'REFUNDED': '환불됨',
    'REFUND_FAILED': '환불 실패',
  };
  
  return statusMap[status] || status;
}

export function getPaymentMethodDisplayName(paymentMethod: string | null | undefined): string {
  if (!paymentMethod) {
    return '-';
  }
  
  const methodMap: Record<string, string> = {
    'CARD': '카드',
    'VIRTUAL_ACCOUNT': '가상계좌',
    'MOBILE': '휴대폰',
    'BANK_TRANSFER': '계좌이체',
    'EASY_PAY': '간편결제',
  };
  
  return methodMap[paymentMethod] || paymentMethod;
}

