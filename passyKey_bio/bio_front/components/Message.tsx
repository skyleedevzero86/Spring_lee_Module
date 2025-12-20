'use client';

import { useEffect, useState } from 'react';

interface MessageProps {
  message: string;
  type: 'success' | 'error' | '';
  onClose?: () => void;
}

export default function Message({ message, type, onClose }: MessageProps) {
  const [visible, setVisible] = useState(!!message);

  useEffect(() => {
    if (message) {
      setVisible(true);
      const timer = setTimeout(() => {
        setVisible(false);
        onClose?.();
      }, 5000);
      return () => clearTimeout(timer);
    } else {
      setVisible(false);
    }
  }, [message, onClose]);

  if (!visible || !message) return null;

  return (
    <div className={`message ${type}`}>
      {message}
    </div>
  );
}

