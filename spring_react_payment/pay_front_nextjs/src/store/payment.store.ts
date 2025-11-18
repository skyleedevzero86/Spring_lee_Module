import { create } from 'zustand';
import type {
  PaymentHistoryResponse,
  PaymentDetailResponse,
  PageApiResponse,
} from '@/domain/types/payment.types';

interface PaymentState {
  paymentHistory: PaymentHistoryResponse[];
  paymentDetail: PaymentDetailResponse | null;
  paymentHistoryPage: PageApiResponse<PaymentHistoryResponse> | null;
  setPaymentHistory: (history: PaymentHistoryResponse[]) => void;
  setPaymentDetail: (detail: PaymentDetailResponse | null) => void;
  setPaymentHistoryPage: (page: PageApiResponse<PaymentHistoryResponse> | null) => void;
  clearPaymentHistory: () => void;
  clearPaymentDetail: () => void;
  clearPaymentHistoryPage: () => void;
}

export const usePaymentStore = create<PaymentState>((set) => ({
  paymentHistory: [],
  paymentDetail: null,
  paymentHistoryPage: null,
  setPaymentHistory: (history) => set({ paymentHistory: history }),
  setPaymentDetail: (detail) => set({ paymentDetail: detail }),
  setPaymentHistoryPage: (page) => set({ paymentHistoryPage: page }),
  clearPaymentHistory: () => set({ paymentHistory: [] }),
  clearPaymentDetail: () => set({ paymentDetail: null }),
  clearPaymentHistoryPage: () => set({ paymentHistoryPage: null }),
}));




