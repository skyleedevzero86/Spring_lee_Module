import { ApiError } from '@/src/domain/types/error.types';

export interface ErrorMessageProps {
  error: ApiError | Error | null;
  className?: string;
}

