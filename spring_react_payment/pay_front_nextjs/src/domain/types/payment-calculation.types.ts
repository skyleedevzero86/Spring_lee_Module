export interface PaymentAmountBreakdown {
  subtotal: number;
  discountAmount: number;
  taxableAmount: number;
  taxFreeAmount: number;
  vatAmount: number;
  serviceFeeAmount: number;
  disposableCupDeposit: number;
  totalAmount: number;
}

export interface Discount {
  type: 'PERCENTAGE' | 'FIXED' | 'CASHBACK';
  value: number;
  maxAmount?: number;
  minPurchaseAmount?: number;
}

export interface TaxCalculation {
  taxFreeAmount: number;
  taxableAmount: number;
  vatRate: number;
  vatAmount: number;
}

export interface RefundCalculation {
  refundableAmount: number;
  refundableTaxFree: number;
  refundableTaxable: number;
  refundableVat: number;
  refundableServiceFee: number;
  refundedAmount: number;
  remainingAmount: number;
}

export interface PaymentValidationResult {
  isValid: boolean;
  errors: string[];
  warnings: string[];
}

export interface RefundRequest {
  paymentId: number;
  refundAmount?: number;
  refundTaxFree?: number;
  refundTaxable?: number;
  refundVat?: number;
  refundServiceFee?: number;
  reason?: string;
}

