import { useState, FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '@/lib/services/authService';
import type { RegisterRequest } from '@/types/api';

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
    <div className="max-w-md mx-auto mt-20 p-6 border rounded">
      <h1 className="text-2xl font-bold mb-4 text-center">회원가입</h1>
      <form onSubmit={handleRegister} className="space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1">이름</label>
          <input
            type="text"
            className="w-full border px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            disabled={loading}
          />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">이메일</label>
          <input
            type="email"
            className="w-full border px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={loading}
          />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">비밀번호</label>
          <input
            type="password"
            className="w-full border px-3 py-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
            disabled={loading}
          />
          <p className="text-xs text-gray-500 mt-1">비밀번호는 최소 8자 이상이어야 합니다.</p>
        </div>
        {error && <p className="text-red-600 text-sm">{error}</p>}
        <button
          type="submit"
          disabled={loading}
          className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {loading ? '가입 중...' : '회원가입'}
        </button>
        <div className="text-center text-sm">
          <span className="text-gray-600">이미 계정이 있으신가요? </span>
          <Link to="/login" className="text-blue-600 hover:underline">
            로그인
          </Link>
        </div>
      </form>
    </div>
  );
}

