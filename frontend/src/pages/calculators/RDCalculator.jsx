import { useState } from 'react';
import { ArrowLeft, RotateCcw } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Bar } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Tooltip, Legend } from 'chart.js';
import Header from '../../components/Header';

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const fmt = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 });

export default function RDCalculator() {
    const [monthly, setMonthly] = useState(5000);
    const [rate, setRate] = useState(7);
    const [tenure, setTenure] = useState(5);
    const [result, setResult] = useState(null);

    const calculate = () => {
        const n = tenure * 12;
        const r = rate / (12 * 100);
        let maturity = 0;
        const yearlyData = [];
        for (let m = 1; m <= n; m++) {
            maturity += monthly * Math.pow(1 + r, n - m + 1);
            if (m % 12 === 0) {
                yearlyData.push({ year: m / 12, value: Math.round(maturity), deposited: monthly * m });
            }
        }
        const totalDeposited = monthly * n;
        const interest = Math.round(maturity - totalDeposited);
        setResult({ maturity: Math.round(maturity), interest, totalDeposited, yearly: yearlyData });
    };

    const reset = () => { setMonthly(5000); setRate(7); setTenure(5); setResult(null); };

    const chartData = result ? {
        labels: result.yearly.map(y => `Year ${y.year}`),
        datasets: [
            { label: 'Deposited', data: result.yearly.map(y => y.deposited), backgroundColor: '#00bcd4', borderRadius: 6 },
            { label: 'Interest', data: result.yearly.map(y => y.value - y.deposited), backgroundColor: '#00e5a0', borderRadius: 6 },
        ],
    } : null;

    return (
        <>
            <Header title="RD Calculator" subtitle="Recurring Deposit maturity calculator" actions={<Link to="/calculators" className="btn btn-secondary"><ArrowLeft size={14} /> Back</Link>} />
            <div className="page-content">
                <div className="calc-layout">
                    <div className="card" style={{ padding: 28 }}>
                        <h3 style={{ marginBottom: 20 }}>RD Details</h3>
                        <div className="calc-field">
                            <label>Monthly Deposit: {fmt(monthly)}</label>
                            <input type="range" min="500" max="100000" step="500" value={monthly} onChange={e => setMonthly(+e.target.value)} className="slider" />
                        </div>
                        <div className="calc-field">
                            <label>Interest Rate: {rate}% p.a.</label>
                            <input type="range" min="1" max="12" step="0.1" value={rate} onChange={e => setRate(+e.target.value)} className="slider" />
                        </div>
                        <div className="calc-field">
                            <label>Tenure: {tenure} years</label>
                            <input type="range" min="1" max="15" step="1" value={tenure} onChange={e => setTenure(+e.target.value)} className="slider" />
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
                                    <div className="result-card"><div className="result-label">Total Interest</div><div className="result-value" style={{ color: '#00bcd4' }}>{fmt(result.interest)}</div></div>
                                </div>
                                <div className="result-row" style={{ marginTop: 12 }}>
                                    <div className="result-card"><div className="result-label">Total Deposited</div><div className="result-value">{fmt(result.totalDeposited)}</div></div>
                                    <div className="result-card"><div className="result-label">Returns Rate</div><div className="result-value">{((result.interest / result.totalDeposited) * 100).toFixed(2)}%</div></div>
                                </div>
                                <div style={{ marginTop: 24, height: 260 }}>
                                    <Bar data={chartData} options={{ responsive: true, maintainAspectRatio: false, plugins: { legend: { labels: { color: '#8899aa' } } }, scales: { x: { stacked: true, ticks: { color: '#5a6f83' }, grid: { display: false } }, y: { stacked: true, ticks: { color: '#5a6f83' }, grid: { color: 'rgba(255,255,255,0.04)' } } } }} />
                                </div>
                            </>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
}
