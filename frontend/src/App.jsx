import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Login from './pages/Login';
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
import Settings from './pages/Settings';

function AppLayout() {
  const location = useLocation();
  const isLogin = location.pathname === '/login';

  if (isLogin) return <Login />;

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <Routes>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/portfolio" element={<Portfolio />} />
          <Route path="/analytics" element={<Analytics />} />
          <Route path="/transactions" element={<Transactions />} />
          <Route path="/calculators" element={<Calculators />} />
          <Route path="/calculators/sip" element={<SIPCalculator />} />
          <Route path="/calculators/retirement" element={<RetirementPlanner />} />
          <Route path="/calculators/loan" element={<LoanCalculator />} />
          <Route path="/calculators/asset-allocation" element={<AssetAllocation />} />
          <Route path="/calculators/cashflow" element={<CashflowPlanner />} />
          <Route path="/calculators/swp" element={<SWPCalculator />} />
          <Route path="/settings" element={<Settings />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </div>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/*" element={<AppLayout />} />
      </Routes>
    </BrowserRouter>
  );
}
