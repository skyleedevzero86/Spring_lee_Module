import styles from './LoadingSpinner.module.css';
import { cn } from '@/lib/utils';

export interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

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
        aria-label="로딩 �?
      >
        <span className={styles.srOnly}>로딩 �?..</span>
      </div>
    </div>
  );
};
