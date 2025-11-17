'use client';

import { useEffect } from 'react';
import { errorTracker } from '@/lib';

export const ErrorTrackerInit = () => {
  useEffect(() => {
    errorTracker.init();
  }, []);

  return null;
};

