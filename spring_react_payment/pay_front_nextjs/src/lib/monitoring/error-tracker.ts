interface ErrorReport {
  message: string;
  stack?: string;
  url: string;
  userAgent: string;
  timestamp: string;
  userId?: number;
  context?: Record<string, unknown>;
}

class ErrorTracker {
  private errorQueue: ErrorReport[] = [];
  private readonly MAX_QUEUE_SIZE = 50;
  private readonly BATCH_SIZE = 10;

  init(): void {
    if (typeof window === 'undefined') return;

    window.addEventListener('error', (event) => {
      this.trackError({
        message: event.message,
        stack: event.error?.stack,
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno,
      });
    });

    window.addEventListener('unhandledrejection', (event) => {
      this.trackError({
        message: event.reason?.message || 'ì²˜ë¦¬?˜ì? ?Šì? Promise ê±°ë?',
        stack: event.reason?.stack,
      });
    });
  }

  trackError(
    error: Error | string,
    context?: Record<string, unknown>
  ): void {
    if (typeof window === 'undefined') return;

    const errorReport: ErrorReport = {
      message: typeof error === 'string' ? error : error.message,
      stack: typeof error === 'string' ? undefined : error.stack,
      url: window.location.href,
      userAgent: navigator.userAgent,
      timestamp: new Date().toISOString(),
      context,
    };

    const userId = this.getUserId();
    if (userId) {
      errorReport.userId = userId;
    }

    this.errorQueue.push(errorReport);

    if (this.errorQueue.length > this.MAX_QUEUE_SIZE) {
      this.errorQueue.shift();
    }

    if (process.env.NEXT_PUBLIC_ERROR_TRACKING_ENABLED === 'true') {
      this.sendError(errorReport);
    }
  }

  private getUserId(): number | null {
    if (typeof window === 'undefined') return null;
    try {
      const { TokenManager } = require('@/lib/utils/token-manager');
      return TokenManager.getUserId();
    } catch {
      return null;
    }
  }

  private sendError(errorReport: ErrorReport): void {
    if (typeof window === 'undefined') return;

    try {
      fetch('/api/logs/error', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(errorReport),
      }).catch(() => {});
    } catch {
    }
  }

  flush(): void {
    if (this.errorQueue.length === 0) return;

    const batch = this.errorQueue.splice(0, this.BATCH_SIZE);

    if (process.env.NEXT_PUBLIC_ERROR_TRACKING_ENABLED === 'true') {
      batch.forEach((error) => this.sendError(error));
    }
  }

  getErrors(): ErrorReport[] {
    return [...this.errorQueue];
  }

  clearErrors(): void {
    this.errorQueue = [];
  }
}

export const errorTracker = new ErrorTracker();

