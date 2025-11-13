type LogLevel = 'debug' | 'info' | 'warn' | 'error';

interface LogContext {
  [key: string]: unknown;
}

class Logger {
  private isDevelopment = process.env.NODE_ENV === 'development';

  private formatMessage(level: LogLevel, message: string, context?: LogContext): string {
    const timestamp = new Date().toISOString();
    const logEntry = {
      timestamp,
      level,
      message,
      ...context,
    };
    return JSON.stringify(logEntry);
  }

  private log(level: LogLevel, message: string, context?: LogContext, error?: Error): void {
    if (!this.isDevelopment && level === 'debug') {
      return;
    }

    const formattedMessage = this.formatMessage(level, message, context);
    const logContext = error
      ? { ...context, error: { name: error.name, message: error.message, stack: error.stack } }
      : context;

    switch (level) {
      case 'debug':
        console.debug(formattedMessage, logContext);
        break;
      case 'info':
        console.info(formattedMessage, logContext);
        break;
      case 'warn':
        console.warn(formattedMessage, logContext);
        break;
      case 'error':
        console.error(formattedMessage, logContext);
        break;
    }

    if (level === 'error' && typeof window !== 'undefined') {
      this.sendToErrorTracking(message, logContext, error);
    }
  }

  private sendToErrorTracking(
    message: string,
    context?: LogContext,
    error?: Error
  ): void {
    if (typeof window === 'undefined') return;

    try {
      const errorData = {
        message,
        context,
        error: error
          ? {
              name: error.name,
              message: error.message,
              stack: error.stack,
            }
          : undefined,
        url: window.location.href,
        userAgent: navigator.userAgent,
        timestamp: new Date().toISOString(),
      };

      if (process.env.NEXT_PUBLIC_ERROR_TRACKING_ENABLED === 'true') {
        fetch('/api/logs/error', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(errorData),
        }).catch(() => {});
      }
    } catch {
    }
  }

  debug(message: string, context?: LogContext): void {
    this.log('debug', message, context);
  }

  info(message: string, context?: LogContext): void {
    this.log('info', message, context);
  }

  warn(message: string, context?: LogContext): void {
    this.log('warn', message, context);
  }

  error(message: string, context?: LogContext, error?: Error): void {
    this.log('error', message, context, error);
  }
}

export const logger = new Logger();

