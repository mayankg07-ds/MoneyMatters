import { useState } from 'react';
import { ArrowLeft, RotateCcw } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Bar } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Tooltip, Legend } from 'chart.js';
import Header from '../../components/Header';

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const fmt = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 });

export default function FDCalculator() {
    const [principal, setPrincipal] = useState(100000);
    const [rate, setRate] = useState(7);
    const [tenure, setTenure] = useState(5);
    const [compounding, setCompounding] = useState('quarterly');
    const [result, setResult] = useState(null);

    const freqMap = { monthly: 12, quarterly: 4, 'half-yearly': 2, yearly: 1 };

    const calculate = () => {
        const n = freqMap[compounding];
        const maturity = principal * Math.pow(1 + rate / (100 * n), n * tenure);
        const interest = maturity - principal;

        const yearly = [];
        for (let y = 1; y <= tenure; y++) {
            const val = principal * Math.pow(1 + rate / (100 * n), n * y);
            yearly.push({ year: y, value: Math.round(val), interest: Math.round(val - principal) });
        }
        setResult({ maturity: Math.round(maturity), interest: Math.round(interest), yearly });
    };

    const reset = () => { setPrincipal(100000); setRate(7); setTenure(5); setCompounding('quarterly'); setResult(null); };

    const chartData = result ? {
        labels: result.yearly.map(y => `Year ${y.year}`),
        datasets: [
            { label: 'Principal', data: result.yearly.map(() => principal), backgroundColor: '#7c4dff', borderRadius: 6 },
            { label: 'Interest', data: result.yearly.map(y => y.interest), backgroundColor: '#00e5a0', borderRadius: 6 },
        ],
    } : null;

    return (
        <>
            <Header title="FD Calculator" subtitle="Fixed Deposit maturity calculator" actions={<Link to="/calculators" className="btn btn-secondary"><ArrowLeft size={14} /> Back</Link>} />
            <div className="page-content">
                <div className="calc-layout">
                    <div className="card" style={{ padding: 28 }}>
                        <h3 style={{ marginBottom: 20 }}>FD Details</h3>
                        <div className="calc-field">
                            <label>Principal Amount: {fmt(principal)}</label>
                            <input type="range" min="10000" max="10000000" step="10000" value={principal} onChange={e => setPrincipal(+e.target.value)} className="slider" />
                        </div>
                        <div className="calc-field">
                            <label>Interest Rate: {rate}% p.a.</label>
                            <input type="range" min="1" max="15" step="0.1" value={rate} onChange={e => setRate(+e.target.value)} className="slider" />
                        </div>
                        <div className="calc-field">
                            <label>Tenure: {tenure} years</label>
                            <input type="range" min="1" max="30" step="1" value={tenure} onChange={e => setTenure(+e.target.value)} className="slider" />
                        </div>
                        <div className="calc-field">
                            <label>Compounding</label>
                            <select className="input-field select-field" value={compounding} onChange={e => setCompounding(e.target.value)}>
                                <option value="monthly">Monthly</option>
                                <option value="quarterly">Quarterly</option>
                                <option value="half-yearly">Half-Yearly</option>
                                <option value="yearly">Yearly</option>
                            </select>
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
                                    <div className="result-card"><div className="result-label">Principal</div><div className="result-value">{fmt(principal)}</div></div>
                                    <div className="result-card"><div className="result-label">Effective Yield</div><div className="result-value">{((result.maturity / principal - 1) * 100).toFixed(2)}%</div></div>
                                </div>
                                <div style={{ marginTop: 24, height: 260 }}>
                                    <Bar data={chartData} options={{ responsive: true, maintainAspectRatio: false, plugins: { legend: { labels: { color: '#8899aa' } } }, scales: { x: { ticks: { color: '#5a6f83' }, grid: { display: false } }, y: { ticks: { color: '#5a6f83' }, grid: { color: 'rgba(255,255,255,0.04)' } } } }} />
                                </div>
                            </>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
}
