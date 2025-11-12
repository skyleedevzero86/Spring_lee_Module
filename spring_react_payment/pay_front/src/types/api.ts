export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  message?: string;
  data?: {
    userId: number;
    email: string;
    name: string;
    role: string;
    token: string;
  };
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
}

export interface RegisterResponse {
  message?: string;
  data?: {
    userId: number;
    email: string;
    name: string;
  };
}

export interface OrderResponse {
  id: number;
  orderId: string;
  orderName: string;
  memberId: number;
  amount: number;
  paymentMethod: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface PurchaseInitRequest {
  eventId: number;
  amount: number;
}

export interface PurchaseInitResponse {
  data: {
    purchaseUUID: string;
  };
}

export interface PurchaseConfirmRequest {
  paymentKey: string;
  orderId: string;
  orderName: string;
  amount: number;
}

export interface PaymentRequestParams {
  method: 'CARD' | 'VIRTUAL_ACCOUNT' | 'MOBILE' | 'BANK_TRANSFER';
  amount: {
    currency: string;
    value: number;
  };
  orderId: string;
  orderName: string;
  successUrl: string;
  failUrl: string;
  customerName: string;
  customerEmail: string;
  customerMobilePhone: string;
}

