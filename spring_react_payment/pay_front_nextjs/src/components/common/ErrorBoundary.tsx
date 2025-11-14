'use client';

import { Component, ReactNode } from 'react';
import { ApiError } from '@/domain/types/error.types';
import { logger } from '@/lib/logger/logger';
import { errorTracker } from '@/lib/monitoring/error-tracker';
import styles from './ErrorBoundary.module.css';

export interface ErrorBoundaryProps {
  children: ReactNode;
  fallback?: (error: Error) => ReactNode;
}

export interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    logger.error('?ëŸ¬ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤', {
      componentStack: errorInfo.componentStack,
      errorName: error.name,
    }, error);

    errorTracker.trackError(error, {
      componentStack: errorInfo.componentStack,
      errorBoundary: true,
    });
  }

  render() {
    if (this.state.hasError && this.state.error) {
      if (this.props.fallback) {
        return this.props.fallback(this.state.error);
      }

      const errorMessage =
        this.state.error instanceof ApiError
          ? this.state.error.message
          : '?ˆìƒì¹?ëª»í•œ ?¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤.';

      return (
        <div className={styles.container}>
          <div className={styles.content}>
            <h2 className={styles.title}>?¤ë¥˜ ë°œìƒ</h2>
            <p className={styles.message}>{errorMessage}</p>
            <button
              onClick={() => this.setState({ hasError: false, error: null })}
              className={styles.button}
            >
              ?¤ì‹œ ?œë„
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
