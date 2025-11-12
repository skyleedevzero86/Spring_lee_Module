import styles from './FailPage.module.css';

export default function FailPage() {
  return (
    <div className={styles.container}>
      <p className={styles.errorText}>결제가 실패했습니다.</p>
      <p className={styles.message}>다시 시도해주세요.</p>
    </div>
  );
}

