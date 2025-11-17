'use client';

import { useEffect } from 'react';
import { performanceMonitor, errorTracker } from '@/lib';

export function MonitoringProvider({ children }: { children: React.ReactNode }) {
  useEffect(() => {
    performanceMonitor.init();
    errorTracker.init();

    return () => {
      errorTracker.flush();
    };
  }, []);

  return <>{children}</>;
}

