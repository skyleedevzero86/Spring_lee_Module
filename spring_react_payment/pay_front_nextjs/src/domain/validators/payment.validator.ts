import { z } from 'zod';
import { containsSqlInjection, containsXss, validateUrl } from '@/lib/utils';

const orderNoPattern = /^[0-9a-zA-Z_\-:.^@]+$/;

const sqlInjectionRefine = (value: string) => !containsSqlInjection(value);
const xssRefine = (value: string) => !containsXss(value);
const urlRefine = (value: string) => validateUrl(value);

export const createPaymentSchema = z.object({
  orderNo: z
    .string()
    .min(1, '주문번호는 필수입니다')
    .max(50, '주문번호는 50자 이하여야 합니다')
    .regex(orderNoPattern, '주문번호는 숫자, 영문자, 특수문자 _-:.^@만 사용 가능합니다.')
    .refine(sqlInjectionRefine, '주문번호에 사용할 수 없는 문자가 포함되어 있습니다.')
    .refine(xssRefine, '주문번호에 사용할 수 없는 문자가 포함되어 있습니다.'),
  productDesc: z
    .string()
    .min(1, '상품 명은 필수입니다')
    .max(255, '상품 명은 255자 이하여야 합니다')
    .trim()
    .refine(sqlInjectionRefine, '상품 명에 사용할 수 없는 문자가 포함되어 있습니다.')
    .refine(xssRefine, '상품 명에 사용할 수 없는 문자가 포함되어 있습니다.'),
  amount: z
    .number()
    .min(1, '결제 금액은 1원 이상이어야 합니다')
    .max(1000000000, '결제 금액은 10억을 초과할 수 없습니다.'),
  amountTaxFree: z
    .number()
    .min(0, '비과세 금액은 0원 이상이어야 합니다'),
  amountTaxable: z.number().min(0, '과세 금액은 0원 이상이어야 합니다').optional(),
  amountVat: z.number().min(0, '부가세는 0원 이상이어야 합니다').optional(),
  amountServiceFee: z.number().min(0, '봉사료는 0원 이상이어야 합니다').optional(),
  disposableCupDeposit: z.number().min(0, '일회용컵 보증금은 0원 이상이어야 합니다').optional(),
  retAppScheme: z.string().max(255, '앱 스킴은 255자 이하여야 합니다').optional(),
  autoExecute: z.boolean().optional(),
  resultCallback: z.string().max(500, '결제 결과 콜백 URL은 500자 이하여야 합니다').optional(),
  callbackVersion: z.string().max(2, '콜백 버전은 2자 이하여야 합니다').optional(),
  expiredTime: z.string().optional(),
  enablePayMethods: z.string().max(100, '결제수단 구분은 100자 이하여야 합니다').optional(),
  cashReceipt: z.boolean().optional(),
  cashReceiptTradeOption: z.string().max(10, '현금영수증 발급 구분은 10자 이하여야 합니다').optional(),
  cardOptions: z.unknown().optional(),
  installment: z.string().max(10, '할부 개월 수는 10자 이하여야 합니다').optional(),
});

export const approvePaymentSchema = z.object({
  payToken: z.string().max(30, '결제 토큰은 30자 이하여야 합니다').optional(),
  orderNo: z.string().max(50, '주문번호는 50자 이하여야 합니다').optional(),
}).refine(
  (data) => (data.payToken && data.payToken.trim() !== '') || (data.orderNo && data.orderNo.trim() !== ''),
  {
    message: '결제 토큰 또는 주문번호 중 하나는 필수입니다',
    path: ['payToken'],
  }
);

export const getPaymentStatusSchema = z.object({
  payToken: z.string().max(50, '결제 토큰은 50자 이하여야 합니다').optional(),
  orderNo: z.string().max(50, '주문번호는 50자 이하여야 합니다').optional(),
}).refine(
  (data) => (data.payToken && data.payToken.trim() !== '') || (data.orderNo && data.orderNo.trim() !== ''),
  {
    message: '결제 토큰 또는 주문번호 중 하나는 필수입니다',
    path: ['payToken'],
  }
);

export const refundPaymentSchema = z.object({
  refundNo: z
    .string()
    .min(1, '환불 번호는 필수입니다')
    .max(36, '환불 번호는 최대 36자까지 입력 가능합니다.')
    .trim()
    .refine(sqlInjectionRefine, '환불 번호에 사용할 수 없는 문자가 포함되어 있습니다.')
    .refine(xssRefine, '환불 번호에 사용할 수 없는 문자가 포함되어 있습니다.'),
  reason: z
    .string()
    .max(255, '환불 사유는 최대 255자까지 입력 가능합니다.')
    .refine((val) => !val || sqlInjectionRefine(val), '환불 사유에 사용할 수 없는 문자가 포함되어 있습니다.')
    .refine((val) => !val || xssRefine(val), '환불 사유에 사용할 수 없는 문자가 포함되어 있습니다.')
    .optional(),
  amount: z.number().min(1, '환불 금액은 1원 이상이어야 합니다').optional(),
  amountTaxFree: z.number().min(0, '비과세 금액은 0원 이상이어야 합니다').optional(),
  amountTaxable: z.number().min(0, '과세 금액은 0원 이상이어야 합니다').optional(),
  amountVat: z.number().min(0, '부가세는 0원 이상이어야 합니다').optional(),
  amountServiceFee: z.number().min(0, '봉사료는 0원 이상이어야 합니다').optional(),
  idempotent: z.boolean().optional(),
});

export type CreatePaymentFormData = z.infer<typeof createPaymentSchema>;
export type ApprovePaymentFormData = z.infer<typeof approvePaymentSchema>;
export type GetPaymentStatusFormData = z.infer<typeof getPaymentStatusSchema>;
export type RefundPaymentFormData = z.infer<typeof refundPaymentSchema>;
