import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Calculator, TrendingUp, Building2, PiggyBank, Landmark, DollarSign, ArrowRight, Banknote, Coins, ShieldCheck } from 'lucide-react';
import Header from '../components/Header';

const calculators = [
    { id: 'sip', title: 'SIP Step-up Calculator', desc: 'Calculate SIP returns with annual step-up in monthly investment amount.', icon: TrendingUp, color: '#00e5a0', bg: 'rgba(0,229,160,0.12)', cat: 'Investment', link: '/calculators/sip' },
    { id: 'retirement', title: 'Retirement Planner', desc: 'Plan your retirement corpus based on expenses, inflation & expected returns.', icon: PiggyBank, color: '#7c4dff', bg: 'rgba(124,77,255,0.12)', cat: 'Retirement', link: '/calculators/retirement' },
    { id: 'loan', title: 'Loan EMI Analyzer', desc: 'Calculate EMIs, total interest and view amortization schedule.', icon: Building2, color: '#00bcd4', bg: 'rgba(0,188,212,0.12)', cat: 'Loans', link: '/calculators/loan' },
    { id: 'asset', title: 'Asset Allocation', desc: 'Rebalance your portfolio across equity, debt & gold.', icon: DollarSign, color: '#ffb347', bg: 'rgba(255,179,71,0.12)', cat: 'Investment', link: '/calculators/asset-allocation' },
    { id: 'cashflow', title: 'Cashflow Planner', desc: 'Project monthly cashflows and savings over time.', icon: Landmark, color: '#36a2eb', bg: 'rgba(54,162,235,0.12)', cat: 'Tax', link: '/calculators/cashflow' },
    { id: 'swp', title: 'SWP Calculator', desc: 'Calculate systematic withdrawal plan for regular income from investments.', icon: Calculator, color: '#ff4c6a', bg: 'rgba(255,76,106,0.12)', cat: 'Retirement', link: '/calculators/swp' },
    { id: 'fd', title: 'FD Calculator', desc: 'Calculate Fixed Deposit maturity with compounding interest.', icon: Banknote, color: '#e91e63', bg: 'rgba(233,30,99,0.12)', cat: 'Savings', link: '/calculators/fd' },
    { id: 'rd', title: 'RD Calculator', desc: 'Calculate Recurring Deposit maturity with monthly deposits.', icon: Coins, color: '#4caf50', bg: 'rgba(76,175,80,0.12)', cat: 'Savings', link: '/calculators/rd' },
    { id: 'ppf', title: 'PPF Calculator', desc: 'Public Provident Fund calculator with tax-free returns.', icon: ShieldCheck, color: '#ff9800', bg: 'rgba(255,152,0,0.12)', cat: 'Tax', link: '/calculators/ppf' },
];

const categories = ['All', 'Investment', 'Retirement', 'Loans', 'Tax', 'Savings'];

export default function Calculators() {
    const [activeCat, setActiveCat] = useState('All');
    const filtered = activeCat === 'All' ? calculators : calculators.filter(c => c.cat === activeCat);

    return (
        <>
            <Header title="Financial Calculators" subtitle="Powerful tools to plan, analyze and optimize your finances" />
            <div className="page-content">
                <div className="pill-group">
                    {categories.map(c => (
                        <button key={c} className={`pill ${activeCat === c ? 'active' : ''}`} onClick={() => setActiveCat(c)}>{c}</button>
                    ))}
                </div>

                <div className="calc-grid">
                    {filtered.map(calc => (
                        <Link to={calc.link} key={calc.id} style={{ textDecoration: 'none' }}>
                            <div className="calc-card">
                                <div className="calc-icon" style={{ background: calc.bg, color: calc.color }}>
                                    <calc.icon size={22} />
                                </div>
                                <h3>{calc.title}</h3>
                                <p>{calc.desc}</p>
                                <div className="calc-footer">
                                    <span className="calc-category">{calc.cat}</span>
                                    <span className="calc-link">Calculate <ArrowRight size={14} /></span>
                                </div>
                            </div>
                        </Link>
                    ))}
                </div>

                <div className="promo-banner">
                    <div>
                        <h3>Need personalized advice?</h3>
                        <p>Our AI tools can help you build a custom financial plan.</p>
                    </div>
                    <button className="btn">Get Started</button>
                </div>
            </div>
        </>
    );
}
