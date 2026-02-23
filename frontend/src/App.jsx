import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { ToastProvider } from './components/Toast';
import Sidebar from './components/Sidebar';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Portfolio from './pages/Portfolio';
import Analytics from './pages/Analytics';
import Transactions from './pages/Transactions';
import Calculators from './pages/Calculators';
import SIPCalculator from './pages/calculators/SIPCalculator';
import RetirementPlanner from './pages/calculators/RetirementPlanner';
import LoanCalculator from './pages/calculators/LoanCalculator';
import AssetAllocation from './pages/calculators/AssetAllocation';
import CashflowPlanner from './pages/calculators/CashflowPlanner';
import SWPCalculator from './pages/calculators/SWPCalculator';
import FDCalculator from './pages/calculators/FDCalculator';
import RDCalculator from './pages/calculators/RDCalculator';
import PPFCalculator from './pages/calculators/PPFCalculator';
import Settings from './pages/Settings';

function ProtectedRoute({ children }) {
  const userId = localStorage.getItem('userId');
  if (!userId) return <Navigate to="/login" replace />;
  return children;
}

function AppLayout() {
  const location = useLocation();
  if (location.pathname === '/login' || location.pathname === '/register') return null;

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <Routes>
          <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
          <Route path="/portfolio" element={<ProtectedRoute><Portfolio /></ProtectedRoute>} />
          <Route path="/analytics" element={<ProtectedRoute><Analytics /></ProtectedRoute>} />
          <Route path="/transactions" element={<ProtectedRoute><Transactions /></ProtectedRoute>} />
          <Route path="/calculators" element={<ProtectedRoute><Calculators /></ProtectedRoute>} />
          <Route path="/calculators/sip" element={<ProtectedRoute><SIPCalculator /></ProtectedRoute>} />
          <Route path="/calculators/retirement" element={<ProtectedRoute><RetirementPlanner /></ProtectedRoute>} />
          <Route path="/calculators/loan" element={<ProtectedRoute><LoanCalculator /></ProtectedRoute>} />
          <Route path="/calculators/asset-allocation" element={<ProtectedRoute><AssetAllocation /></ProtectedRoute>} />
          <Route path="/calculators/cashflow" element={<ProtectedRoute><CashflowPlanner /></ProtectedRoute>} />
          <Route path="/calculators/swp" element={<ProtectedRoute><SWPCalculator /></ProtectedRoute>} />
          <Route path="/calculators/fd" element={<ProtectedRoute><FDCalculator /></ProtectedRoute>} />
          <Route path="/calculators/rd" element={<ProtectedRoute><RDCalculator /></ProtectedRoute>} />
          <Route path="/calculators/ppf" element={<ProtectedRoute><PPFCalculator /></ProtectedRoute>} />
          <Route path="/settings" element={<ProtectedRoute><Settings /></ProtectedRoute>} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </div>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/*" element={<AppLayout />} />
        </Routes>
      </ToastProvider>
    </BrowserRouter>
  );
}
