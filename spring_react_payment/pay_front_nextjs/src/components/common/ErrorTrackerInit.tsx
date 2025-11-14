'use client';

import { useEffect } from 'react';
import { errorTracker } from '@/lib/monitoring/error-tracker';

export const ErrorTrackerInit = () => {
  useEffect(() => {
    errorTracker.init();
  }, []);

  return null;
};

