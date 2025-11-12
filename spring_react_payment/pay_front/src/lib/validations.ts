import { z } from 'zod';

export const loginSchema = z.object({
  email: z.string().email('올바른 이메일 형식이 아닙니다.'),
  password: z.string().min(1, '비밀번호를 입력해주세요.'),
});

export const registerSchema = z.object({
  name: z.string().min(1, '이름을 입력해주세요.'),
  email: z.string().email('올바른 이메일 형식이 아닙니다.'),
  password: z.string().min(8, '비밀번호는 최소 8자 이상이어야 합니다.'),
});

export const paymentSchema = z.object({
  eventId: z.number().int().positive('이벤트 ID는 양수여야 합니다.'),
  amount: z.number().int().positive('결제 금액은 양수여야 합니다.').min(1000, '최소 결제 금액은 1,000원입니다.'),
  mobilePhone: z.string().regex(/^010\d{8}$/, '올바른 휴대폰 번호 형식이 아닙니다. (010-1234-5678)'),
});

export type LoginFormData = z.infer<typeof loginSchema>;
export type RegisterFormData = z.infer<typeof registerSchema>;
export type PaymentFormData = z.infer<typeof paymentSchema>;

