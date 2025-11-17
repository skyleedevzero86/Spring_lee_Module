'use client';

import { useEffect } from 'react';

export function WebSocketErrorHandler() {
  useEffect(() => {
    if (process.env.NODE_ENV !== 'development') {
      return;
    }

    const originalError = console.error;

    console.error = (...args: any[]) => {
      try {
        const errorMessage = args[0]?.toString() || '';
        
        if (
          errorMessage.includes('WebSocket connection to') &&
          errorMessage.includes('_next/webpack-hmr') &&
          errorMessage.includes('failed')
        ) {
          return;
        }

        if (typeof originalError === 'function') {
          originalError.apply(console, args);
        } else {
          originalError(...args);
        }
      } catch (e) {
        try {
          originalError(...args);
        } catch (fallbackError) {
          console.warn('Error handler failed', fallbackError);
        }
      }
    };

    return () => {
      console.error = originalError;
    };
  }, []);

  return null;
}

