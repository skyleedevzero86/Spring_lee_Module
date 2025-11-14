'use client';

import { useEffect } from 'react';
import { performanceMonitor } from '@/src/lib/monitoring/performance-monitor';
import { errorTracker } from '@/src/lib/monitoring/error-tracker';

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

