import { useState } from 'react';
import { ArrowLeft, RotateCcw } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Line } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Filler, Tooltip, Legend } from 'chart.js';
import Header from '../../components/Header';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Filler, Tooltip, Legend);

const fmt = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 });

const PPF_RATE = 7.1; // current govt rate

export default function PPFCalculator() {
    const [annual, setAnnual] = useState(150000);
    const [tenure, setTenure] = useState(15);
    const [result, setResult] = useState(null);

    const calculate = () => {
        const r = PPF_RATE / 100;
        let balance = 0;
        const yearly = [];
        for (let y = 1; y <= tenure; y++) {
            balance = (balance + annual) * (1 + r);
            yearly.push({ year: y, balance: Math.round(balance), deposited: annual * y, interest: Math.round(balance - annual * y) });
        }
        setResult({ maturity: Math.round(balance), totalDeposited: annual * tenure, interest: Math.round(balance - annual * tenure), yearly });
    };

    const reset = () => { setAnnual(150000); setTenure(15); setResult(null); };

    const chartData = result ? {
        labels: result.yearly.map(y => `Year ${y.year}`),
        datasets: [
            { label: 'Balance', data: result.yearly.map(y => y.balance), borderColor: '#00e5a0', backgroundColor: 'rgba(0,229,160,0.08)', fill: true, tension: 0.4, pointRadius: 3, borderWidth: 2 },
            { label: 'Deposited', data: result.yearly.map(y => y.deposited), borderColor: '#7c4dff', backgroundColor: 'rgba(124,77,255,0.08)', fill: true, tension: 0.4, pointRadius: 0, borderWidth: 2, borderDash: [6, 4] },
        ],
    } : null;

    return (
        <>
            <Header title="PPF Calculator" subtitle="Public Provident Fund calculator" actions={<Link to="/calculators" className="btn btn-secondary"><ArrowLeft size={14} /> Back</Link>} />
            <div className="page-content">
                <div className="calc-layout">
                    <div className="card" style={{ padding: 28 }}>
                        <h3 style={{ marginBottom: 20 }}>PPF Details</h3>
                        <div style={{ padding: '10px 14px', borderRadius: 'var(--border-radius-sm)', background: 'rgba(0,229,160,0.08)', border: '1px solid rgba(0,229,160,0.2)', marginBottom: 20, fontSize: '0.85rem', color: '#00e5a0' }}>
                            Current PPF Rate: {PPF_RATE}% p.a. (Govt. of India)
                        </div>
                        <div className="calc-field">
                            <label>Annual Investment: {fmt(annual)}</label>
                            <input type="range" min="500" max="150000" step="500" value={annual} onChange={e => setAnnual(+e.target.value)} className="slider" />
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                                <span>₹500</span><span>₹1,50,000 (max)</span>
                            </div>
                        </div>
                        <div className="calc-field">
                            <label>Tenure: {tenure} years</label>
                            <input type="range" min="15" max="50" step="5" value={tenure} onChange={e => setTenure(+e.target.value)} className="slider" />
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                                <span>15 yrs (min)</span><span>50 yrs (with ext.)</span>
                            </div>
                        </div>
                        <div style={{ display: 'flex', gap: 12, marginTop: 16 }}>
                            <button className="btn btn-primary" onClick={calculate} style={{ flex: 1 }}>Calculate</button>
                            <button className="btn btn-secondary" onClick={reset}><RotateCcw size={14} /> Reset</button>
                        </div>
                    </div>

                    <div className="card" style={{ padding: 28 }}>
                        {!result ? (
                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 300, color: 'var(--text-muted)' }}>
                                <p>Adjust values and click Calculate</p>
                            </div>
                        ) : (
                            <>
                                <h3 style={{ marginBottom: 16 }}>Results</h3>
                                <div className="result-row">
                                    <div className="result-card"><div className="result-label">Maturity Amount</div><div className="result-value" style={{ color: '#00e5a0' }}>{fmt(result.maturity)}</div></div>
                                    <div className="result-card"><div className="result-label">Total Interest</div><div className="result-value" style={{ color: '#7c4dff' }}>{fmt(result.interest)}</div></div>
                                </div>
                                <div className="result-row" style={{ marginTop: 12 }}>
                                    <div className="result-card"><div className="result-label">Total Deposited</div><div className="result-value">{fmt(result.totalDeposited)}</div></div>
                                    <div className="result-card"><div className="result-label">Wealth Gain</div><div className="result-value">{((result.interest / result.totalDeposited) * 100).toFixed(1)}%</div></div>
                                </div>
                                <div style={{ marginTop: 24, height: 260 }}>
                                    <Line data={chartData} options={{ responsive: true, maintainAspectRatio: false, plugins: { legend: { labels: { color: '#8899aa' } } }, scales: { x: { ticks: { color: '#5a6f83' }, grid: { display: false } }, y: { ticks: { color: '#5a6f83', callback: v => '₹' + (v / 100000).toFixed(0) + 'L' }, grid: { color: 'rgba(255,255,255,0.04)' } } } }} />
                                </div>
                                <div style={{ marginTop: 16, fontSize: '0.82rem', color: 'var(--text-secondary)', background: 'var(--bg-surface)', padding: 14, borderRadius: 'var(--border-radius-sm)' }}>
                                    <strong>Tax Benefit:</strong> PPF investment qualifies for deduction under Section 80C (up to ₹1.5L/year). Interest and maturity amount are fully tax-free (EEE status).
                                </div>
                            </>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
}
