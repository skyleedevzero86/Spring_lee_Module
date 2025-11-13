'use client';

import { useRouter } from 'next/navigation';
import { Button } from './Button';

interface BackButtonProps {
  className?: string;
  children?: React.ReactNode;
}

export const BackButton = ({ className, children }: BackButtonProps) => {
  const router = useRouter();

  const handleBack = () => {
    if (typeof window !== 'undefined' && window.history.length > 1) {
      router.back();
    } else {
      router.push('/');
    }
  };

  return (
    <Button
      variant="outline"
      size="sm"
      onClick={handleBack}
      className={className}
    >
      {children || '← 이전 페이지'}
    </Button>
  );
};

