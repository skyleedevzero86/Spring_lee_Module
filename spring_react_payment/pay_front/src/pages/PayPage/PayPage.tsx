import { usePayment } from '@/hooks/usePayment';
import { Button } from '@/components/ui/button';
import { useAuthStore } from '@/store/authStore';
import styles from './PayPage.module.css';

export default function PayPage() {
  const paymentHook = usePayment();
  const { user } = useAuthStore();

  const handlePay = async () => {
    try {
      await paymentHook.requestPayment(
        2,
        50000,
        {
          name: user?.name || '홍길동',
          email: user?.email || 'hong@example.com',
          mobilePhone: '01012345678',
        }
      );
    } catch (err) {
    }
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>공연 예매</h1>

      <div className={styles.card}>
        <div className={styles.cardContent}>
          <h2 className={styles.cardTitle}>공연 예매 결제</h2>
          <p className={styles.cardDescription}>
            결제하기 버튼을 클릭하면 토스 페이먼츠 결제창이 열립니다.
          </p>
        </div>
      </div>

      <div className={styles.amountCard}>
        <div className={styles.amountText}>
          결제 금액: <span className={styles.amountValue}>50,000원</span>
        </div>
      </div>

      {paymentHook.error && (
        <div className={styles.errorMessage}>
          {paymentHook.error}
        </div>
      )}

      <Button
        onClick={handlePay}
        disabled={paymentHook.loading}
        className={styles.payButton}
        size="lg"
      >
        {paymentHook.loading ? '결제 준비 중...' : '결제하기'}
      </Button>
    </div>
  );
}

