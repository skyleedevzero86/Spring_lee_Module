import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAuth } from '@/hooks/useAuth';
import { loginSchema, type LoginFormData } from '@/lib/validations';
import styles from './LoginPage.module.css';

export default function LoginPage() {
  const { login, loading, error } = useAuth();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    await login(data);
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>로그인</h1>
      <form onSubmit={handleSubmit(onSubmit)} className={styles.form}>
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

