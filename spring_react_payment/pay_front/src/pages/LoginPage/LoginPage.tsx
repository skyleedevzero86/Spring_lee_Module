import { useState, FormEvent } from 'react';
import { useAuth } from '@/hooks/useAuth';
import styles from './LoginPage.module.css';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login, loading, error } = useAuth();

  const handleLogin = async (e: FormEvent) => {
    e.preventDefault();
    try {
      await login({ email, password });
    } catch (err) {
    }
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>로그인</h1>
      <form onSubmit={handleLogin} className={styles.form}>
        <div className={styles.field}>
          <label className={styles.label}>이메일</label>
          <input
            type="email"
            className={styles.input}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={loading}
          />
        </div>
        <div className={styles.field}>
          <label className={styles.label}>비밀번호</label>
          <input
            type="password"
            className={styles.input}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={loading}
          />
        </div>
        {error && <p className={styles.error}>{error}</p>}
        <button
          type="submit"
          disabled={loading}
          className={styles.submitButton}
        >
          {loading ? '로그인 중...' : '로그인'}
        </button>
      </form>
    </div>
  );
}

