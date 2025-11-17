'use client';

import { Component, ReactNode } from 'react';
import { ApiError } from '@/domain/types/error.types';
import { logger, errorTracker } from '@/lib';
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
    logger.error('에러가 발생했습니다', {
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
          : '예상치 못한 오류가 발생했습니다.';

      return (
        <div className={styles.container}>
          <div className={styles.content}>
            <h2 className={styles.title}>에러 발생</h2>
            <p className={styles.message}>{errorMessage}</p>
            <button
              onClick={() => this.setState({ hasError: false, error: null })}
              className={styles.button}
            >
              다시 시도
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
