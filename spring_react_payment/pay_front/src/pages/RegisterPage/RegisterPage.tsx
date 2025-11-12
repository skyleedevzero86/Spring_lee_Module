import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { authService } from '@/lib/services/authService';
import { registerSchema, type RegisterFormData } from '@/lib/validations';
import { handleApiError } from '@/lib/errorHandler';
import styles from './RegisterPage.module.css';

export default function RegisterPage() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: RegisterFormData) => {
    setLoading(true);
    setError(null);

    try {
      await authService.register(data);
      navigate('/login');
    } catch (err: unknown) {
      setError(handleApiError(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>회원가입</h1>
      <form onSubmit={handleSubmit(onSubmit)} className={styles.form}>
        <div className={styles.field}>
          <label className={styles.label}>이름</label>
          <input
            type="text"
            className={styles.input}
            {...register('name')}
            disabled={loading}
          />
          {errors.name && (
            <p className={styles.error}>{errors.name.message}</p>
          )}
        </div>
        <div className={styles.field}>
          <label className={styles.label}>이메일</label>
          <input
            type="email"
            className={styles.input}
            {...register('email')}
            disabled={loading}
          />
          {errors.email && (
            <p className={styles.error}>{errors.email.message}</p>
          )}
        </div>
        <div className={styles.field}>
          <label className={styles.label}>비밀번호</label>
          <input
            type="password"
            className={styles.input}
            {...register('password')}
            disabled={loading}
          />
          {errors.password && (
            <p className={styles.error}>{errors.password.message}</p>
          )}
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

