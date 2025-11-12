import { Link } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { useAuth } from '@/hooks/useAuth';
import { Button } from '@/components/ui/button';
import styles from './Header.module.css';

export default function Header() {
  const { user, isAuthenticated } = useAuthStore();
  const { logout } = useAuth();

  return (
    <header className={styles.header}>
      <div className={styles.container}>
        <Link to="/" className={styles.logo}>
          토스 결제
        </Link>
        <nav className={styles.nav}>
          {isAuthenticated() ? (
            <>
              <span className={styles.userName}>{user?.name}님</span>
              <Button variant="outline" size="sm" onClick={logout}>
                로그아웃
              </Button>
            </>
          ) : (
            <>
              <Link to="/login">
                <Button variant="default" size="sm">
                  로그인
                </Button>
              </Link>
              <Link to="/register">
                <Button variant="outline" size="sm">
                  회원가입
                </Button>
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}

