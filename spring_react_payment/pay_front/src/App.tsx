import { BrowserRouter, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import PayPage from './pages/PayPage';
import PaySuccessPage from './pages/PaySuccessPage';
import FailPage from './pages/FailPage';
import MyOrdersPage from './pages/MyOrdersPage';
import AdminDashboardPage from './pages/AdminDashboardPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/pay" element={<PayPage />} />
        <Route path="/pay/fail" element={<FailPage />} />
        <Route path="/success" element={<PaySuccessPage />} />
        <Route path="/orders" element={<MyOrdersPage />} />
        <Route path="/admin" element={<AdminDashboardPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
