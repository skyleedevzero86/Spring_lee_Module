import { containsSqlInjection, containsXss, sanitizeInput, validateUrl } from './security';

export interface ValidationResult {
  isValid: boolean;
  errors: string[];
}

export class InputValidator {
  static validateString(value: unknown, fieldName: string, options?: {
    required?: boolean;
    minLength?: number;
    maxLength?: number;
    pattern?: RegExp;
    checkSqlInjection?: boolean;
    checkXss?: boolean;
  }): ValidationResult {
    const errors: string[] = [];
    const {
      required = false,
      minLength,
      maxLength,
      pattern,
      checkSqlInjection = true,
      checkXss = true,
    } = options || {};

    if (value === null || value === undefined) {
      if (required) {
        errors.push(`${fieldName}은(는) 필수입니다.`);
      }
      return { isValid: errors.length === 0, errors };
    }

    if (typeof value !== 'string') {
      errors.push(`${fieldName}은(는) 문자열이어야 합니다.`);
      return { isValid: false, errors };
    }

    const stringValue = value.trim();

    if (required && stringValue.length === 0) {
      errors.push(`${fieldName}은(는) 필수입니다.`);
    }

    if (minLength !== undefined && stringValue.length < minLength) {
      errors.push(`${fieldName}은(는) 최소 ${minLength}자 이상이어야 합니다.`);
    }

    if (maxLength !== undefined && stringValue.length > maxLength) {
      errors.push(`${fieldName}은(는) 최대 ${maxLength}자 이하여야 합니다.`);
    }

    if (pattern && !pattern.test(stringValue)) {
      errors.push(`${fieldName}의 형식이 올바르지 않습니다.`);
    }

    if (checkSqlInjection && containsSqlInjection(stringValue)) {
      errors.push(`${fieldName}에 허용되지 않은 문자가 포함되어 있습니다.`);
    }

    if (checkXss && containsXss(stringValue)) {
      errors.push(`${fieldName}에 허용되지 않은 문자가 포함되어 있습니다.`);
    }

    return { isValid: errors.length === 0, errors };
  }

  static validateNumber(value: unknown, fieldName: string, options?: {
    required?: boolean;
    min?: number;
    max?: number;
    integer?: boolean;
  }): ValidationResult {
    const errors: string[] = [];
    const {
      required = false,
      min,
      max,
      integer = false,
    } = options || {};

    if (value === null || value === undefined) {
      if (required) {
        errors.push(`${fieldName}은(는) 필수입니다.`);
      }
      return { isValid: errors.length === 0, errors };
    }

    if (typeof value !== 'number' || isNaN(value)) {
      errors.push(`${fieldName}은(는) 숫자여야 합니다.`);
      return { isValid: false, errors };
    }

    if (integer && !Number.isInteger(value)) {
      errors.push(`${fieldName}은(는) 정수여야 합니다.`);
    }

    if (min !== undefined && value < min) {
      errors.push(`${fieldName}은(는) ${min} 이상이어야 합니다.`);
    }

    if (max !== undefined && value > max) {
      errors.push(`${fieldName}은(는) ${max} 이하여야 합니다.`);
    }

    return { isValid: errors.length === 0, errors };
  }

  static validateUrl(value: unknown, fieldName: string, required = false): ValidationResult {
    const errors: string[] = [];

    if (value === null || value === undefined) {
      if (required) {
        errors.push(`${fieldName}은(는) 필수입니다.`);
      }
      return { isValid: errors.length === 0, errors };
    }

    if (typeof value !== 'string') {
      errors.push(`${fieldName}은(는) 문자열이어야 합니다.`);
      return { isValid: false, errors };
    }

    if (!validateUrl(value)) {
      errors.push(`${fieldName}은(는) 올바른 URL 형식이어야 합니다.`);
    }

    return { isValid: errors.length === 0, errors };
  }

  static sanitizeAndValidate<T extends Record<string, unknown>>(
    data: T,
    schema: Record<keyof T, {
      required?: boolean;
      type: 'string' | 'number' | 'url';
      minLength?: number;
      maxLength?: number;
      min?: number;
      max?: number;
      pattern?: RegExp;
    }>
  ): { data: T; errors: string[] } {
    const errors: string[] = [];
    const sanitized = { ...data };

    for (const [key, config] of Object.entries(schema)) {
      const value = sanitized[key as keyof T];
      let result: ValidationResult;

      if (config.type === 'string') {
        const stringValue = typeof value === 'string' ? sanitizeInput(value) : value;
        sanitized[key as keyof T] = stringValue as T[keyof T];
        result = this.validateString(stringValue, key, {
          required: config.required,
          minLength: config.minLength,
          maxLength: config.maxLength,
          pattern: config.pattern,
        });
      } else if (config.type === 'number') {
        result = this.validateNumber(value, key, {
          required: config.required,
          min: config.min,
          max: config.max,
        });
      } else if (config.type === 'url') {
        result = this.validateUrl(value, key, config.required);
      } else {
        continue;
      }

      if (!result.isValid) {
        errors.push(...result.errors);
      }
    }

    return { data: sanitized, errors };
  }
}

