export interface Payment {
  id: number;
  orderNo: string;
  payToken?: string;
  checkoutPage?: string;
  productDesc: string;
  status: PaymentStatus;
  amount: number;
  createdAt: string;
  paidTs?: string;
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  FAILED = 'FAILED',
}

export interface CreatePaymentRequest {
  orderNo: string;
  productDesc: string;
  amount: number;
  amountTaxFree: number;
  amountTaxable?: number;
  amountVat?: number;
  amountServiceFee?: number;
  disposableCupDeposit?: number;
  retUrl: string;
  retCancelUrl: string;
  retAppScheme?: string;
  autoExecute?: boolean;
  resultCallback?: string;
  callbackVersion?: string;
  expiredTime?: string;
  enablePayMethods?: string;
  cashReceipt?: boolean;
  cashReceiptTradeOption?: string;
  cardOptions?: unknown;
  installment?: string;
}

export interface PaymentResponse {
  id: number;
  orderNo: string;
  payToken: string;
  checkoutPage: string;
  productDesc: string;
  status: string;
}

export interface ApprovePaymentRequest {
  payToken?: string;
  orderNo?: string;
}

export interface PaymentApprovalResponse {
  id: number;
  orderNo: string;
  payToken: string;
  status: string;
  mode?: string;
  approvalTime?: string;
  stateMsg?: string;
  amount?: number;
  discountedAmount?: number;
  paidAmount?: number;
  payMethod?: string;
  transactionId?: string;
}

export interface GetPaymentStatusRequest {
  payToken?: string;
  orderNo?: string;
}

export interface PaymentStatusResponse {
  payToken: string;
  orderNo: string;
  payStatus: string;
  payMethod?: string;
  mode?: string;
  amount?: number;
  discountedAmount?: number;
  paidAmount?: number;
  refundableAmount?: number;
  amountTaxFree?: number;
  amountTaxable?: number;
  amountVat?: number;
  createdTs?: string;
  paidTs?: string;
}

export interface PaymentHistoryResponse {
  id: number;
  orderNo: string;
  transactionId?: string;
  productDesc: string;
  amount: number;
  status: string;
  payMethod?: string;
  createdAt: string;
  paidTs?: string;
}

export interface PaymentDetailResponse {
  id: number;
  userId: number;
  orderNo: string;
  transactionId?: string;
  productDesc: string;
  amount: number;
  amountTaxFree: number;
  amountTaxable?: number;
  amountVat?: number;
  amountServiceFee?: number;
  disposableCupDeposit?: number;
  status: string;
  payMethod?: string;
  discountedAmount?: number;
  paidAmount?: number;
  paidTs?: string;
  mode?: string;
  approvalTime?: string;
  stateMsg?: string;
  card?: CardInfo;
  accountBankCode?: string;
  accountBankName?: string;
  accountNumber?: string;
  createdAt: string;
  updatedAt: string;
  expiredTime: string;
}

export interface CardInfo {
  noInterest?: boolean;
  spreadOut?: number;
  cardAuthorizationNo?: string;
  cardMethodType?: string;
  cardUserType?: string;
  cardNumber?: string;
  cardBinNumber?: string;
  cardNum4Print?: string;
  salesCheckLinkUrl?: string;
  cardCompanyName?: string;
  cardCompanyCode?: number;
}

export interface RefundPaymentRequest {
  refundNo: string;
  reason?: string;
  amount?: number;
  amountTaxFree?: number;
  amountTaxable?: number;
  amountVat?: number;
  amountServiceFee?: number;
  idempotent?: boolean;
}

export interface RefundPaymentResponse {
  paymentId: number;
  refundNo: string;
  refundableAmount: number;
  discountedAmount?: number;
  paidAmount?: number;
  refundedAmount: number;
  refundedDiscountAmount?: number;
  refundedPaidAmount?: number;
  approvalTime?: string;
  cashReceiptMgtKey?: string;
  payToken?: string;
  transactionId?: string;
  cardMethodType?: string;
  cardNumber?: string;
  cardUserType?: string;
  cardBinNumber?: string;
  cardNum4Print?: string;
  salesCheckLinkUrl?: string;
  accountBankCode?: string;
  accountBankName?: string;
  accountNumber?: string;
  status?: string;
}

export interface PageApiResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

