import styles from './styles/LoadingSpinner.module.css';
import { cn } from '@/src/lib/utils/classname';
import type { LoadingSpinnerProps } from './types/LoadingSpinner.types';

export const LoadingSpinner = ({ size = 'md', className = '' }: LoadingSpinnerProps) => {
  const sizeClass = {
    sm: styles.spinnerSmall,
    md: styles.spinnerMedium,
    lg: styles.spinnerLarge,
  }[size];

  return (
    <div className={cn(styles.container, className)}>
      <div
        className={cn(styles.spinner, sizeClass)}
        role="status"
        aria-label="로딩 중"
      >
        <span className={styles.srOnly}>로딩 중...</span>
      </div>
    </div>
  );
};
