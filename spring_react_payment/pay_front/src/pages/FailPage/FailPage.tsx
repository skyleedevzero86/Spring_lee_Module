import { useSearchParams, Link } from 'react-router-dom';
import styles from './FailPage.module.css';

export default function FailPage() {
  const [searchParams] = useSearchParams();
  const errorCode = searchParams.get('code');
  const errorMessage = searchParams.get('message');
  const orderId = searchParams.get('orderId');

  const getErrorMessage = () => {
    if (errorMessage) {
      return errorMessage;
    }
    if (errorCode) {
      const errorMessages: Record<string, string> = {
        '24': '결제가 취소되었습니다.',
        'USER_CANCEL': '사용자가 결제를 취소했습니다.',
        'INVALID_CARD': '유효하지 않은 카드 정보입니다.',
        'INSUFFICIENT_FUNDS': '잔액이 부족합니다.',
        'CARD_EXPIRED': '카드 유효기간이 만료되었습니다.',
      };
      return errorMessages[errorCode] || `결제 오류가 발생했습니다. (코드: ${errorCode})`;
    }
    return '결제가 실패했습니다.';
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.errorTitle}>결제 실패</h1>
      <p className={styles.errorText}>{getErrorMessage()}</p>
      {orderId && (
        <p className={styles.orderInfo}>주문번호: {orderId}</p>
      )}
      <div className={styles.buttonGroup}>
        <Link to="/pay" className={styles.retryButton}>
          다시 시도하기
        </Link>
        <Link to="/" className={styles.homeButton}>
          홈으로 돌아가기
        </Link>
      </div>
    </div>
  );
}

