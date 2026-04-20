import { useState } from 'react';
import { ArrowLeft, RotateCcw } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Line } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Filler, Tooltip } from 'chart.js';
import Header from '../../components/Header';
import { calculatorsApi } from '../../services/api';
import ExplainButton from '../../components/ai/ExplainButton';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Filler, Tooltip);

const fmt = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 });

export default function SIPCalculator() {
    const [monthly, setMonthly] = useState(10000);
    const [rate, setRate] = useState(12);
    const [years, setYears] = useState(10);
    const [stepup, setStepup] = useState(10);
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);

    const calculate = async () => {
        setLoading(true);
        try {
            const res = await calculatorsApi.sipStepup({
                monthlySIPAmount: monthly, expectedReturnRate: rate,
                timePeriodYears: years, annualStepUpPercent: stepup,
            });
            setResult(res.data.data);
        } catch (e) {
            // Fallback client calculation
            let total = 0, invested = 0, m = monthly;
            const r = rate / 100 / 12;
            const yearlyData = [];
            for (let y = 1; y <= years; y++) {
                for (let mo = 0; mo < 12; mo++) {
                    invested += m;
                    total = (total + m) * (1 + r);
                }
                yearlyData.push({ year: y, invested, value: Math.round(total) });
                m = Math.round(m * (1 + stepup / 100));
            }
            setResult({ maturityValue: Math.round(total), totalAmountInvested: Math.round(invested), wealthGained: Math.round(total - invested), yearlyBreakdown: yearlyData });
        }
        setLoading(false);
    };

    const chartData = result ? {
        labels: (result.yearlyBreakdown || []).map(y => `Y${y.year}`),
        datasets: [
            { label: 'Value', data: (result.yearlyBreakdown || []).map(y => y.value || y.endValue), borderColor: '#00e5a0', backgroundColor: 'rgba(0,229,160,0.08)', fill: true, tension: 0.4, pointRadius: 3 },
            { label: 'Invested', data: (result.yearlyBreakdown || []).map(y => y.invested || y.totalInvested), borderColor: '#7c4dff', backgroundColor: 'rgba(124,77,255,0.05)', fill: true, tension: 0.4, pointRadius: 3 },
        ],
    } : null;

    return (
        <>
            <Header title="SIP Step-up Calculator" subtitle="Calculate returns with annual step-up"
                actions={<Link to="/calculators" className="btn btn-secondary"><ArrowLeft size={14} /> Back</Link>} />
            <div className="page-content">
                <div className="calc-layout">
                    <div className="calc-input-panel">
                        <div className="card">
                            <h3 style={{ marginBottom: 20 }}>Investment Parameters</h3>
                            {[
                                { label: 'Monthly SIP Amount', val: monthly, set: setMonthly, min: 500, max: 500000, step: 500, fmt: '₹' },
                                { label: 'Expected Return Rate', val: rate, set: setRate, min: 1, max: 30, step: 0.5, fmt: '%' },
                                { label: 'Time Period', val: years, set: setYears, min: 1, max: 40, step: 1, fmt: ' yrs' },
                                { label: 'Annual Step-up', val: stepup, set: setStepup, min: 0, max: 50, step: 1, fmt: '%' },
                            ].map(s => (
                                <div className="slider-group" key={s.label}>
                                    <div className="slider-header">
                                        <span className="slider-label">{s.label}</span>
                                        <span className="slider-value">{s.fmt === '₹' ? fmt(s.val) : s.val + s.fmt}</span>
                                    </div>
                                    <input type="range" className="range-slider" min={s.min} max={s.max} step={s.step} value={s.val} onChange={e => s.set(Number(e.target.value))} />
                                    <div className="slider-range"><span>{s.fmt === '₹' ? fmt(s.min) : s.min + s.fmt}</span><span>{s.fmt === '₹' ? fmt(s.max) : s.max + s.fmt}</span></div>
                                </div>
                            ))}
                            <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
                                <button className="btn btn-primary" style={{ flex: 1 }} onClick={calculate} disabled={loading}>
                                    {loading ? 'Calculating...' : 'Calculate Returns'}
                                </button>
                                <button className="btn btn-secondary" onClick={() => { setMonthly(10000); setRate(12); setYears(10); setStepup(10); setResult(null); }}>
                                    <RotateCcw size={14} /> Reset
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className="calc-result-panel">
                        {result ? (
                            <>
                                <div className="calc-result-hero">
                                    <div className="hero-label">Maturity Value</div>
                                    <div className="hero-value">{fmt(result.maturityValue)}</div>
                                    <div className="hero-sub">After {years} years with {stepup}% annual step-up</div>
                                </div>
                                <div className="result-row">
                                    <div className="result-card"><div className="result-label">Total Invested</div><div className="result-value">{fmt(result.totalAmountInvested)}</div></div>
                                    <div className="result-card"><div className="result-label">Wealth Gained</div><div className="result-value" style={{ color: '#00e5a0' }}>{fmt(result.wealthGained)}</div></div>
                                </div>
                                {chartData && (
                                    <div className="card">
                                        <h3 style={{ marginBottom: 16 }}>Growth Projection</h3>
                                        <div style={{ height: 260 }}>
                                            <Line data={chartData} options={{ responsive: true, maintainAspectRatio: false, plugins: { legend: { labels: { color: '#8899aa', font: { size: 11 } } } }, scales: { x: { grid: { display: false }, ticks: { color: '#5a6f83' } }, y: { grid: { color: 'rgba(255,255,255,0.04)' }, ticks: { color: '#5a6f83', callback: v => '₹' + (v / 100000).toFixed(0) + 'L' } } } }} />
                                        </div>
                                    </div>
                                )}
                                <ExplainButton
                                    type="SIP_STEPUP"
                                    inputs={{ monthlySIPAmount: monthly, expectedReturnRate: rate, timePeriodYears: years, annualStepUpPercent: stepup }}
                                    result={result}
                                />
                            </>
                        ) : (
                            <div className="card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 300 }}>
                                <p style={{ color: 'var(--text-muted)', fontSize: '1rem' }}>Adjust parameters and click Calculate</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
}
