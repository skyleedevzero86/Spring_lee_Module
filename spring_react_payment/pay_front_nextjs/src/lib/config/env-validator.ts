import { z } from 'zod';

const envSchema = z.object({
  NEXT_PUBLIC_API_BASE_URL: z.string().url().optional(),
  NEXT_PUBLIC_ERROR_TRACKING_ENABLED: z
    .string()
    .transform((val) => val === 'true')
    .optional()
    .default('false'),
  NEXT_PUBLIC_PERFORMANCE_MONITORING_ENABLED: z
    .string()
    .transform((val) => val === 'true')
    .optional()
    .default('false'),
  NODE_ENV: z.enum(['development', 'production', 'test']).default('development'),
});

type Env = z.infer<typeof envSchema>;

function validateEnv(): Env {
  try {
    return envSchema.parse({
      NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL,
      NEXT_PUBLIC_ERROR_TRACKING_ENABLED: process.env.NEXT_PUBLIC_ERROR_TRACKING_ENABLED,
      NEXT_PUBLIC_PERFORMANCE_MONITORING_ENABLED: process.env.NEXT_PUBLIC_PERFORMANCE_MONITORING_ENABLED,
      NODE_ENV: process.env.NODE_ENV,
    });
  } catch (error) {
    if (error instanceof z.ZodError) {
      const missingVars = error.errors.map((err) => err.path.join('.')).join(', ');
      throw new Error(`환경 변수 검증 실패: ${missingVars}`);
    }
    throw error;
  }
}

export const env = validateEnv();




