import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { usePayment } from '@/hooks/usePayment';
import { Button } from '@/components/ui/button';
import { useAuthStore } from '@/store/authStore';
import { paymentSchema, type PaymentFormData } from '@/lib/validations';
import styles from './PayPage.module.css';

export default function PayPage() {
  const paymentHook = usePayment();
  const { user } = useAuthStore();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<PaymentFormData>({
    resolver: zodResolver(paymentSchema),
    defaultValues: {
      eventId: 1,
      amount: 50000,
      mobilePhone: '',
    },
  });

  const onSubmit = async (data: PaymentFormData) => {
    if (!user?.name || !user?.email) {
      return;
    }

    await paymentHook.requestPayment(
      data.eventId,
      data.amount,
      {
        name: user.name,
        email: user.email,
        mobilePhone: data.mobilePhone,
      }
    );
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>공연 예매</h1>

      <div className={styles.card}>
        <div className={styles.cardContent}>
          <h2 className={styles.cardTitle}>공연 예매 결제</h2>
          <p className={styles.cardDescription}>
            결제 정보를 입력하고 결제하기 버튼을 클릭하면 토스 페이먼츠 결제창이 열립니다.
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className={styles.form}>
        <div className={styles.field}>
          <label className={styles.label}>이벤트 ID</label>
          <input
            type="number"
            className={styles.input}
            {...register('eventId', { valueAsNumber: true })}
            disabled={paymentHook.loading}
          />
          {errors.eventId && (
            <p className={styles.error}>{errors.eventId.message}</p>
          )}
        </div>

        <div className={styles.field}>
          <label className={styles.label}>결제 금액 (원)</label>
          <input
            type="number"
            className={styles.input}
            {...register('amount', { valueAsNumber: true })}
            disabled={paymentHook.loading}
          />
          {errors.amount && (
            <p className={styles.error}>{errors.amount.message}</p>
          )}
        </div>

        <div className={styles.field}>
          <label className={styles.label}>휴대폰 번호 (01012345678)</label>
          <input
            type="text"
            className={styles.input}
            placeholder="01012345678"
            {...register('mobilePhone')}
            disabled={paymentHook.loading}
          />
          {errors.mobilePhone && (
            <p className={styles.error}>{errors.mobilePhone.message}</p>
          )}
        </div>

        {paymentHook.error && (
          <div className={styles.errorMessage}>
            {paymentHook.error}
          </div>
        )}

        <Button
          type="submit"
          disabled={paymentHook.loading}
          className={styles.payButton}
          size="lg"
        >
          {paymentHook.loading ? '결제 준비 중...' : '결제하기'}
        </Button>
      </form>
    </div>
  );
}

