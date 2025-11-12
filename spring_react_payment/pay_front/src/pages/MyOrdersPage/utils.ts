import { paymentService } from '@/lib/services/paymentService';

export const downloadReceipt = async (orderId: string): Promise<void> => {
  const blob = await paymentService.downloadReceipt(orderId);
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `영수증_${orderId}.pdf`;
  document.body.appendChild(a);
  a.click();
  window.URL.revokeObjectURL(url);
  document.body.removeChild(a);
};

