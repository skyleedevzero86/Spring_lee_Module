import { BrowserRouter, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import PayPage from './pages/PayPage';
import PaySuccessPage from './pages/PaySuccessPage';
import FailPage from './pages/FailPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/pay" element={<PayPage />} />
        <Route path="/pay/fail" element={<FailPage />} />
        <Route path="/success" element={<PaySuccessPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
