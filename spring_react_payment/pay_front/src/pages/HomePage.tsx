import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { useAuthStore } from '@/store/authStore';
import styles from './HomePage.module.css';

export default function HomePage() {
  const { isAuthenticated, isAdmin, user } = useAuthStore();

  return (
    <div className={styles.container}>
      <main className={styles.main}>
        {!isAuthenticated() ? (
          <>
            <div className={styles.header}>
              <h1 className={styles.title}>토스 페이먼츠 결제 시스템</h1>
              <p className={styles.subtitle}>
                안전하고 간편한 결제 서비스를 제공합니다
              </p>
            </div>

            <div className={styles.guideSection}>
              <h2 className={styles.guideTitle}>토스 결제 사용 방법</h2>
              <div className={styles.guideContent}>
                <div className={styles.guideStep}>
                  <div className={styles.stepNumber}>1</div>
                  <div className={styles.stepContent}>
                    <h3 className={styles.stepTitle}>회원가입 및 로그인</h3>
                    <p className={styles.stepDescription}>
                      먼저 회원가입을 진행하고 로그인해주세요. 
                      이메일과 비밀번호로 간편하게 가입할 수 있습니다.
                    </p>
                  </div>
                </div>
                <div className={styles.guideStep}>
                  <div className={styles.stepNumber}>2</div>
                  <div className={styles.stepContent}>
                    <h3 className={styles.stepTitle}>결제하기</h3>
                    <p className={styles.stepDescription}>
                      로그인 후 결제하기 버튼을 클릭하여 결제를 진행하세요. 
                      이벤트 ID와 결제 금액을 입력하면 주문이 생성됩니다.
                    </p>
                  </div>
                </div>
                <div className={styles.guideStep}>
                  <div className={styles.stepNumber}>3</div>
                  <div className={styles.stepContent}>
                    <h3 className={styles.stepTitle}>결제 승인</h3>
                    <p className={styles.stepDescription}>
                      토스 페이먼츠 결제 위젯을 통해 결제를 완료하세요. 
                      카드 정보를 입력하고 결제를 승인하면 주문이 완료됩니다.
                    </p>
                  </div>
                </div>
                <div className={styles.guideStep}>
                  <div className={styles.stepNumber}>4</div>
                  <div className={styles.stepContent}>
                    <h3 className={styles.stepTitle}>결제 내역 확인</h3>
                    <p className={styles.stepDescription}>
                      내 결제 내역에서 모든 결제 정보를 확인할 수 있습니다. 
                      필요시 환불도 신청할 수 있습니다.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </>
        ) : (
          <>
            <div className={styles.welcomeSection}>
              <h1 className={styles.welcomeTitle}>
                {user?.name}님, 환영합니다!
              </h1>
              {isAdmin() ? (
                <div className={styles.userInfo}>
                  <p className={styles.userRole}>관리자 계정</p>
                  <p className={styles.userDescription}>
                    관리자 계정으로 로그인하셨습니다. 전체 결제 내역을 조회하고 
                    모든 주문을 관리할 수 있습니다.
                  </p>
                </div>
              ) : (
                <div className={styles.userInfo}>
                  <p className={styles.userRole}>일반 사용자</p>
                  <p className={styles.userDescription}>
                    결제하기를 통해 새로운 주문을 생성하거나, 
                    내 결제 내역에서 이전 주문을 확인할 수 있습니다.
                  </p>
                </div>
              )}
            </div>

            <div className={styles.buttonGroup}>
              <Link to="/pay">
                <Button variant="default" size="lg">
                  결제하기
                </Button>
              </Link>
              <Link to="/orders">
                <Button variant="outline" size="lg">
                  내 결제 내역
                </Button>
              </Link>
              {isAdmin() && (
                <Link to="/admin">
                  <Button variant="outline" size="lg">
                    전체 결제 내역
                  </Button>
                </Link>
              )}
            </div>
          </>
        )}
      </main>
    </div>
  );
}

