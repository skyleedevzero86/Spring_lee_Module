import { ApiError } from '@/src/domain/types/error.types';
import styles from './ErrorMessage.module.css';
import type { ErrorMessageProps } from './ErrorMessage.types';

export const ErrorMessage = ({ error, className = '' }: ErrorMessageProps) => {
  if (!error) return null;

  const message = error instanceof ApiError ? error.message : error.message;

  return (
    <div
      className={`${styles.container} ${className}`}
      role="alert"
    >
      <p className={styles.title}>오류</p>
      <p className={styles.message}>{message}</p>
    </div>
  );
};
