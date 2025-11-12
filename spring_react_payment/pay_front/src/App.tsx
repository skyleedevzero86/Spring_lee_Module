import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, useNavigate } from 'react-router-dom';
import Header from './components/Header';
import ProtectedRoute from './components/ProtectedRoute';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import PayPage from './pages/PayPage';
import PaySuccessPage from './pages/PaySuccessPage';
import FailPage from './pages/FailPage';
import MyOrdersPage from './pages/MyOrdersPage';
import AdminDashboardPage from './pages/AdminDashboardPage';

function AppRoutes() {
  const navigate = useNavigate();

  useEffect(() => {
    const handleRedirect = (event: CustomEvent<{ path: string }>) => {
      navigate(event.detail.path, { replace: true });
    };

    window.addEventListener('auth:redirect', handleRedirect as EventListener);
    return () => {
      window.removeEventListener('auth:redirect', handleRedirect as EventListener);
    };
  }, [navigate]);

  return (
    <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/pay"
          element={
            <ProtectedRoute>
              <PayPage />
            </ProtectedRoute>
          }
        />
        <Route path="/pay/fail" element={<FailPage />} />
        <Route
          path="/success"
          element={
            <ProtectedRoute>
              <PaySuccessPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders"
          element={
            <ProtectedRoute>
              <MyOrdersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <ProtectedRoute requireAdmin>
              <AdminDashboardPage />
            </ProtectedRoute>
          }
        />
    </Routes>
  );
}

function App() {
  return (
    <BrowserRouter>
      <Header />
      <AppRoutes />
    </BrowserRouter>
  );
}

export default App;
