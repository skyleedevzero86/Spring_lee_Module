import { useState, FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '@/lib/services/authService';
import type { RegisterRequest } from '@/types/api';
import styles from './RegisterPage.module.css';

export default function RegisterPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleRegister = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const request: RegisterRequest = { email, password, name };
      await authService.register(request);
      navigate('/login');
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.message || '회원가입에 실패했습니다.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>회원가입</h1>
      <form onSubmit={handleRegister} className={styles.form}>
        <div className={styles.field}>
          <label className={styles.label}>이름</label>
          <input
            type="text"
            className={styles.input}
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            disabled={loading}
          />
        </div>
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
            minLength={8}
            disabled={loading}
          />
          <p className={styles.hint}>비밀번호는 최소 8자 이상이어야 합니다.</p>
        </div>
        {error && <p className={styles.error}>{error}</p>}
        <button
          type="submit"
          disabled={loading}
          className={styles.submitButton}
        >
          {loading ? '가입 중...' : '회원가입'}
        </button>
        <div className={styles.footer}>
          <span className={styles.footerText}>이미 계정이 있으신가요? </span>
          <Link to="/login" className={styles.link}>
            로그인
          </Link>
        </div>
      </form>
    </div>
  );
}

