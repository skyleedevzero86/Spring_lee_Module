import { z } from 'zod';
import { containsSqlInjection, containsXss } from '@/lib/utils';

const sqlInjectionRefine = (value: string) => !containsSqlInjection(value);
const xssRefine = (value: string) => !containsXss(value);

export const registerMemberSchema = z.object({
  email: z
    .string()
    .min(1, '이메일은 필수입니다')
    .email('올바른 이메일 형식이 아닙니다.')
    .max(255, '이메일은 255자 이하여야 합니다')
    .refine(sqlInjectionRefine, '이메일에 사용할 수 없는 문자가 포함되어 있습니다.')
    .refine(xssRefine, '이메일에 사용할 수 없는 문자가 포함되어 있습니다.'),
  password: z
    .string()
    .min(8, '비밀번호는 최소 8자 이상이어야 합니다')
    .max(100, '비밀번호는 100자 이하여야 합니다')
    .refine(sqlInjectionRefine, '비밀번호에 사용할 수 없는 문자가 포함되어 있습니다.')
    .refine(xssRefine, '비밀번호에 사용할 수 없는 문자가 포함되어 있습니다.'),
  name: z
    .string()
    .min(1, '이름은 필수입니다')
    .max(50, '이름은 50자 이하여야 합니다')
    .trim()
    .refine(sqlInjectionRefine, '이름에 사용할 수 없는 문자가 포함되어 있습니다.')
    .refine(xssRefine, '이름에 사용할 수 없는 문자가 포함되어 있습니다.'),
});

export const resetPasswordSchema = z.object({
  email: z
    .string()
    .min(1, '이메일은 필수입니다')
    .email('올바른 이메일 형식이 아닙니다.')
    .max(255, '이메일은 255자 이하여야 합니다')
    .refine(sqlInjectionRefine, '이메일에 사용할 수 없는 문자가 포함되어 있습니다.')
    .refine(xssRefine, '이메일에 사용할 수 없는 문자가 포함되어 있습니다.'),
  newPassword: z
    .string()
    .min(8, '비밀번호는 최소 8자 이상이어야 합니다')
    .max(100, '비밀번호는 100자 이하여야 합니다')
    .refine(sqlInjectionRefine, '비밀번호에 사용할 수 없는 문자가 포함되어 있습니다.')
    .refine(xssRefine, '비밀번호에 사용할 수 없는 문자가 포함되어 있습니다.'),
});

export type RegisterMemberFormData = z.infer<typeof registerMemberSchema>;
export type ResetPasswordFormData = z.infer<typeof resetPasswordSchema>;
