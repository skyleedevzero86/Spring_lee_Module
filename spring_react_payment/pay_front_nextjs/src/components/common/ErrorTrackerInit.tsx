'use client';

import { useEffect } from 'react';
import { errorTracker } from '@/src/lib/monitoring/error-tracker';

export const ErrorTrackerInit = () => {
  useEffect(() => {
    errorTracker.init();
  }, []);

  return null;
};

