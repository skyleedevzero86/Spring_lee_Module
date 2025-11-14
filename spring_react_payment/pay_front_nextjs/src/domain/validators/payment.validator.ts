import { z } from 'zod';
import { containsSqlInjection, containsXss, validateUrl } from '@/lib/utils/security';

const orderNoPattern = /^[0-9a-zA-Z_\-:.^@]+$/;

const sqlInjectionRefine = (value: string) => !containsSqlInjection(value);
const xssRefine = (value: string) => !containsXss(value);
const urlRefine = (value: string) => validateUrl(value);

export const createPaymentSchema = z.object({
  orderNo: z
    .string()
    .min(1, 'ì£¼ë¬¸ë²ˆí˜¸???„ìˆ˜?…ë‹ˆ??')
    .max(50, 'ì£¼ë¬¸ë²ˆí˜¸??50???´í•˜?¬ì•¼ ?©ë‹ˆ??')
    .regex(orderNoPattern, 'ì£¼ë¬¸ë²ˆí˜¸???«ì, ?ë¬¸?? ?¹ìˆ˜ë¬¸ì _-:.^@ë§??¬ìš© ê°€?¥í•©?ˆë‹¤.')
    .refine(sqlInjectionRefine, 'ì£¼ë¬¸ë²ˆí˜¸???ˆìš©?˜ì? ?Šì? ë¬¸ìê°€ ?¬í•¨?˜ì–´ ?ˆìŠµ?ˆë‹¤.')
    .refine(xssRefine, 'ì£¼ë¬¸ë²ˆí˜¸???ˆìš©?˜ì? ?Šì? ë¬¸ìê°€ ?¬í•¨?˜ì–´ ?ˆìŠµ?ˆë‹¤.'),
  productDesc: z
    .string()
    .min(1, '?í’ˆ ?¤ëª…?€ ?„ìˆ˜?…ë‹ˆ??')
    .max(255, '?í’ˆ ?¤ëª…?€ 255???´í•˜?¬ì•¼ ?©ë‹ˆ??')
    .trim()
    .refine(sqlInjectionRefine, '?í’ˆ ?¤ëª…???ˆìš©?˜ì? ?Šì? ë¬¸ìê°€ ?¬í•¨?˜ì–´ ?ˆìŠµ?ˆë‹¤.')
    .refine(xssRefine, '?í’ˆ ?¤ëª…???ˆìš©?˜ì? ?Šì? ë¬¸ìê°€ ?¬í•¨?˜ì–´ ?ˆìŠµ?ˆë‹¤.'),
  amount: z
    .number()
    .min(1, 'ê²°ì œ ê¸ˆì•¡?€ 1???´ìƒ?´ì–´???©ë‹ˆ??')
    .max(1000000000, 'ê²°ì œ ê¸ˆì•¡?€ 10???ì„ ì´ˆê³¼?????†ìŠµ?ˆë‹¤.'),
  amountTaxFree: z
    .number()
    .min(0, 'ë¹„ê³¼??ê¸ˆì•¡?€ 0???´ìƒ?´ì–´???©ë‹ˆ??')
    .default(0),
  amountTaxable: z.number().min(0, 'ê³¼ì„¸ ê¸ˆì•¡?€ 0???´ìƒ?´ì–´???©ë‹ˆ??').optional(),
  amountVat: z.number().min(0, 'ë¶€ê°€?¸ëŠ” 0???´ìƒ?´ì–´???©ë‹ˆ??').optional(),
  amountServiceFee: z.number().min(0, 'ë´‰ì‚¬ë£ŒëŠ” 0???´ìƒ?´ì–´???©ë‹ˆ??').optional(),
  disposableCupDeposit: z.number().min(0, '?¼íšŒ?©ì»µ ë³´ì¦ê¸ˆì? 0???´ìƒ?´ì–´???©ë‹ˆ??').optional(),
  retUrl: z
    .string()
    .min(1, 'ê²°ì œ ?„ë£Œ URL?€ ?„ìˆ˜?…ë‹ˆ??')
    .max(255, 'ê²°ì œ ?„ë£Œ URL?€ 255???´í•˜?¬ì•¼ ?©ë‹ˆ??')
    .refine(urlRefine, '?¬ë°”ë¥?URL ?•ì‹???„ë‹™?ˆë‹¤.')
    .refine(sqlInjectionRefine, 'URL???ˆìš©?˜ì? ?Šì? ë¬¸ìê°€ ?¬í•¨?˜ì–´ ?ˆìŠµ?ˆë‹¤.')
    .refine(xssRefine, 'URL???ˆìš©?˜ì? ?Šì? ë¬¸ìê°€ ?¬í•¨?˜ì–´ ?ˆìŠµ?ˆë‹¤.'),
  retCancelUrl: z
    .string()
    .min(1, 'ê²°ì œ ì·¨ì†Œ URL?€ ?„ìˆ˜?…ë‹ˆ??')
    .max(255, 'ê²°ì œ ì·¨ì†Œ URL?€ 255???´í•˜?¬ì•¼ ?©ë‹ˆ??')
    .refine(urlRefine, '?¬ë°”ë¥?URL ?•ì‹???„ë‹™?ˆë‹¤.')
    .refine(sqlInjectionRefine, 'URL???ˆìš©?˜ì? ?Šì? ë¬¸ìê°€ ?¬í•¨?˜ì–´ ?ˆìŠµ?ˆë‹¤.')
    .refine(xssRefine, 'URL???ˆìš©?˜ì? ?Šì? ë¬¸ìê°€ ?¬í•¨?˜ì–´ ?ˆìŠµ?ˆë‹¤.'),
  retAppScheme: z.string().max(255, '???¤í‚´?€ 255???´í•˜?¬ì•¼ ?©ë‹ˆ??').optional(),
  autoExecute: z.boolean().optional(),
  resultCallback: z.string().max(500, 'ê²°ì œ ê²°ê³¼ ì½œë°± URL?€ 500???´í•˜?¬ì•¼ ?©ë‹ˆ??').optional(),
  callbackVersion: z.string().max(2, 'ì½œë°± ë²„ì „?€ 2???´í•˜?¬ì•¼ ?©ë‹ˆ??').optional(),
  expiredTime: z.string().optional(),
  enablePayMethods: z.string().max(100, 'ê²°ì œ?˜ë‹¨ êµ¬ë¶„?€ 100???´í•˜?¬ì•¼ ?©ë‹ˆ??').optional(),
  cashReceipt: z.boolean().optional(),
  cashReceiptTradeOption: z.string().max(10, '?„ê¸ˆ?ìˆ˜ì¦?ë°œê¸‰ ?€?…ì? 10???´í•˜?¬ì•¼ ?©ë‹ˆ??').optional(),
  cardOptions: z.unknown().optional(),
  installment: z.string().max(10, '? ë? ?œí•œ ?€?…ì? 10???´í•˜?¬ì•¼ ?©ë‹ˆ??').optional(),
});

export const approvePaymentSchema = z.object({
  payToken: z.string().max(30, 'ê²°ì œ ? í°?€ 30???´í•˜?¬ì•¼ ?©ë‹ˆ??').optional(),
  orderNo: z.string().max(50, 'ì£¼ë¬¸ë²ˆí˜¸??50???´í•˜?¬ì•¼ ?©ë‹ˆ??').optional(),
}).refine(
  (data) => (data.payToken && data.payToken.trim() !== '') || (data.orderNo && data.orderNo.trim() !== ''),
  {
    message: 'ê²°ì œ ? í° ?ëŠ” ì£¼ë¬¸ë²ˆí˜¸ ì¤??˜ë‚˜???„ìˆ˜?…ë‹ˆ??',
    path: ['payToken'],
  }
);

export const getPaymentStatusSchema = z.object({
  payToken: z.string().max(50, 'ê²°ì œ ? í°?€ 50???´í•˜?¬ì•¼ ?©ë‹ˆ??').optional(),
  orderNo: z.string().max(50, 'ì£¼ë¬¸ë²ˆí˜¸??50???´í•˜?¬ì•¼ ?©ë‹ˆ??').optional(),
}).refine(
  (data) => (data.payToken && data.payToken.trim() !== '') || (data.orderNo && data.orderNo.trim() !== ''),
  {
    message: 'ê²°ì œ ? í° ?ëŠ” ì£¼ë¬¸ë²ˆí˜¸ ì¤??˜ë‚˜???„ìˆ˜?…ë‹ˆ??',
    path: ['payToken'],
  }
);

export const refundPaymentSchema = z.object({
  refundNo: z
    .string()
    .min(1, '?˜ë¶ˆ ë²ˆí˜¸???„ìˆ˜?…ë‹ˆ??')
    .max(36, '?˜ë¶ˆ ë²ˆí˜¸??ìµœë? 36?ê¹Œì§€ ?…ë ¥ ê°€?¥í•©?ˆë‹¤.')
    .trim()
    .refine(sqlInjectionRefine, '?˜ë¶ˆ ë²ˆí˜¸???ˆìš©?˜ì? ?Šì? ë¬¸ìê°€ ?¬í•¨?˜ì–´ ?ˆìŠµ?ˆë‹¤.')
    .refine(xssRefine, '?˜ë¶ˆ ë²ˆí˜¸???ˆìš©?˜ì? ?Šì? ë¬¸ìê°€ ?¬í•¨?˜ì–´ ?ˆìŠµ?ˆë‹¤.'),
  reason: z
    .string()
    .max(255, '?˜ë¶ˆ ?¬ìœ ??ìµœë? 255?ê¹Œì§€ ?…ë ¥ ê°€?¥í•©?ˆë‹¤.')
    .refine((val) => !val || sqlInjectionRefine(val), '?˜ë¶ˆ ?¬ìœ ???ˆìš©?˜ì? ?Šì? ë¬¸ìê°€ ?¬í•¨?˜ì–´ ?ˆìŠµ?ˆë‹¤.')
    .refine((val) => !val || xssRefine(val), '?˜ë¶ˆ ?¬ìœ ???ˆìš©?˜ì? ?Šì? ë¬¸ìê°€ ?¬í•¨?˜ì–´ ?ˆìŠµ?ˆë‹¤.')
    .optional(),
  amount: z.number().min(1, '?˜ë¶ˆ ê¸ˆì•¡?€ 1???´ìƒ?´ì–´???©ë‹ˆ??').optional(),
  amountTaxFree: z.number().min(0, 'ë¹„ê³¼??ê¸ˆì•¡?€ 0???´ìƒ?´ì–´???©ë‹ˆ??').optional(),
  amountTaxable: z.number().min(0, 'ê³¼ì„¸ ê¸ˆì•¡?€ 0???´ìƒ?´ì–´???©ë‹ˆ??').optional(),
  amountVat: z.number().min(0, 'ë¶€ê°€?¸ëŠ” 0???´ìƒ?´ì–´???©ë‹ˆ??').optional(),
  amountServiceFee: z.number().min(0, 'ë´‰ì‚¬ë£ŒëŠ” 0???´ìƒ?´ì–´???©ë‹ˆ??').optional(),
  idempotent: z.boolean().optional(),
});

export type CreatePaymentFormData = z.infer<typeof createPaymentSchema>;
export type ApprovePaymentFormData = z.infer<typeof approvePaymentSchema>;
export type GetPaymentStatusFormData = z.infer<typeof getPaymentStatusSchema>;
export type RefundPaymentFormData = z.infer<typeof refundPaymentSchema>;

