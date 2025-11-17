'use client';

import { useEffect } from 'react';

export function WebSocketErrorHandler() {
  useEffect(() => {
    if (process.env.NODE_ENV !== 'development') {
      return;
    }

    const originalError = console.error;

    console.error = (...args: any[]) => {
      const errorMessage = args[0]?.toString() || '';
      
      if (
        errorMessage.includes('WebSocket connection to') &&
        errorMessage.includes('_next/webpack-hmr') &&
        errorMessage.includes('failed')
      ) {
        return;
      }

      originalError.apply(console, args);
    };

    return () => {
      console.error = originalError;
    };
  }, []);

  return null;
}

