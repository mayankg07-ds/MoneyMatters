import { useState } from 'react';
import { ArrowLeft } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Line, Doughnut } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, ArcElement, Filler, Tooltip, Legend } from 'chart.js';
import Header from '../../components/Header';
import { calculatorsApi } from '../../services/api';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, ArcElement, Filler, Tooltip, Legend);

const fmt = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 });

export default function SWPCalculator() {
    const [corpus, setCorpus] = useState(5000000);
    const [withdrawal, setWithdrawal] = useState(30000);
    const [rate, setRate] = useState(8);
    const [duration, setDuration] = useState(20);
    const [inflationAdj, setInflationAdj] = useState(false);
    const [inflation, setInflation] = useState(6);
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);

    const calculate = async () => {
        setLoading(true);
        try {
            const res = await calculatorsApi.swp({
                startingCorpus: corpus,
                monthlyWithdrawal: withdrawal,
                expectedAnnualReturnPercent: rate,
                durationYears: duration,
                inflationAdjusted: inflationAdj,
                inflationPercent: inflationAdj ? inflation : 0,
            });
            setResult(res.data.data);
        } catch (e) {
            // Client-side fallback
            const monthlyRate = rate / 100 / 12;
            const monthlyInflation = inflation / 100 / 12;
            let bal = corpus;
            let totalWithdrawn = 0;
            let totalReturns = 0;
            let mw = withdrawal;
            const yearly = [];
            let depleted = false;
            let effectiveMonths = duration * 12;

            for (let y = 1; y <= duration; y++) {
                const startBal = bal;
                let yReturns = 0, yWithdrawn = 0;
                for (let m = 0; m < 12; m++) {
                    if (bal <= 0) { depleted = true; effectiveMonths = (y - 1) * 12 + m; break; }
                    const ret = bal * monthlyRate;
                    yReturns += ret;
                    bal += ret;
                    const w = Math.min(mw, bal);
                    bal -= w;
                    yWithdrawn += w;
                    totalWithdrawn += w;
                    totalReturns += ret;
                    if (inflationAdj) mw = mw * (1 + monthlyInflation);
                }
                yearly.push({
                    year: y, startingCorpus: Math.round(startBal), totalReturns: Math.round(yReturns),
                    totalWithdrawals: Math.round(yWithdrawn), endingCorpus: Math.max(0, Math.round(bal)),
                    avgMonthlyWithdrawal: Math.round(yWithdrawn / 12), corpusGrowing: bal > startBal,
                });
                if (depleted) break;
                if (inflationAdj) mw = withdrawal * Math.pow(1 + inflation / 100, y);
            }

            const wdRate = ((withdrawal * 12) / corpus * 100).toFixed(2);
            const isSustainable = !depleted;
            setResult({
                startingCorpus: corpus,
                initialMonthlyWithdrawal: withdrawal,
                finalCorpusValue: Math.max(0, Math.round(bal)),
                totalWithdrawn: Math.round(totalWithdrawn),
                totalReturnsEarned: Math.round(totalReturns),
                effectiveDurationMonths: depleted ? effectiveMonths : duration * 12,
                isSustainable,
                sustainabilityMessage: isSustainable ? `Your corpus will last the full ${duration} years with ${fmt(Math.round(bal))} remaining.` : `Your corpus will be depleted in ${Math.floor(effectiveMonths / 12)} years ${effectiveMonths % 12} months.`,
                withdrawalRate: wdRate,
                safeWithdrawalRate: 4,
                yearlySummary: yearly,
            });
        }
        setLoading(false);
    };

    const chartData = result?.yearlySummary ? {
        labels: result.yearlySummary.map(y => `Y${y.year}`),
        datasets: [
            {
                label: 'Corpus Balance', data: result.yearlySummary.map(y => y.endingCorpus),
                borderColor: '#00e5a0', backgroundColor: 'rgba(0,229,160,0.08)', fill: true, tension: 0.4, pointRadius: 3,
            },
        ],
    } : null;

    const wdVsReturn = result ? {
        labels: ['Total Withdrawn', 'Returns Earned', 'Final Corpus'],
        datasets: [{
            data: [Number(result.totalWithdrawn), Number(result.totalReturnsEarned), Math.max(0, Number(result.finalCorpusValue))],
            backgroundColor: ['#ff4c6a', '#00e5a0', '#7c4dff'],
            borderWidth: 0, cutout: '65%',
        }]
    } : null;

    return (
        <>
            <Header title="SWP Calculator" subtitle="Systematic Withdrawal Plan for regular income"
                actions={<Link to="/calculators" className="btn btn-secondary"><ArrowLeft size={14} /> Back</Link>} />
            <div className="page-content">
                <div className="calc-layout">
                    <div className="calc-input-panel">
                        <div className="card">
                            <h3 style={{ marginBottom: 20 }}>Withdrawal Parameters</h3>
                            {[
                                { label: 'Starting Corpus', val: corpus, set: setCorpus, min: 100000, max: 100000000, step: 100000, pre: true },
                                { label: 'Monthly Withdrawal', val: withdrawal, set: setWithdrawal, min: 5000, max: 500000, step: 1000, pre: true },
                                { label: 'Expected Return', val: rate, set: setRate, min: 1, max: 20, step: 0.5, sfx: '%' },
                                { label: 'Duration', val: duration, set: setDuration, min: 1, max: 50, step: 1, sfx: ' years' },
                            ].map(s => (
                                <div className="slider-group" key={s.label}>
                                    <div className="slider-header">
                                        <span className="slider-label">{s.label}</span>
                                        <span className="slider-value">{s.pre ? fmt(s.val) : s.val + s.sfx}</span>
                                    </div>
                                    <input type="range" className="range-slider" min={s.min} max={s.max} step={s.step} value={s.val} onChange={e => s.set(Number(e.target.value))} />
                                    <div className="slider-range"><span>{s.pre ? fmt(s.min) : s.min + s.sfx}</span><span>{s.pre ? fmt(s.max) : s.max + s.sfx}</span></div>
                                </div>
                            ))}

                            <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '14px 0', borderTop: '1px solid var(--border-subtle)', marginTop: 12 }}>
                                <label style={{ position: 'relative', width: 44, height: 24, flexShrink: 0 }}>
                                    <input type="checkbox" checked={inflationAdj} onChange={e => setInflationAdj(e.target.checked)} style={{ opacity: 0, width: 0, height: 0 }} />
                                    <span style={{ position: 'absolute', inset: 0, background: inflationAdj ? 'var(--accent-primary)' : 'var(--bg-tertiary)', borderRadius: 12, cursor: 'pointer', transition: '0.3s' }}>
                                        <span style={{ position: 'absolute', top: 3, left: inflationAdj ? 23 : 3, width: 18, height: 18, borderRadius: '50%', background: '#fff', transition: '0.3s' }}></span>
                                    </span>
                                </label>
                                <span style={{ fontSize: '0.88rem' }}>Adjust withdrawals for inflation</span>
                            </div>

                            {inflationAdj && (
                                <div className="slider-group">
                                    <div className="slider-header">
                                        <span className="slider-label">Inflation Rate</span>
                                        <span className="slider-value">{inflation}%</span>
                                    </div>
                                    <input type="range" className="range-slider" min={0} max={15} step={0.5} value={inflation} onChange={e => setInflation(Number(e.target.value))} />
                                </div>
                            )}

                            <button className="btn btn-primary" style={{ width: '100%', marginTop: 12 }} onClick={calculate} disabled={loading}>
                                {loading ? 'Calculating...' : 'Calculate SWP'}
                            </button>
                        </div>
                    </div>

                    <div className="calc-result-panel">
                        {result ? (
                            <>
                                {/* Sustainability Hero */}
                                <div style={{
                                    background: result.isSustainable ? 'linear-gradient(135deg, #00bcd4, #00e5a0)' : 'linear-gradient(135deg, #ff4c6a, #ffb347)',
                                    borderRadius: 'var(--border-radius-lg)', padding: '32px 28px', textAlign: 'center', color: '#0a1929', position: 'relative', overflow: 'hidden'
                                }}>
                                    <div style={{ position: 'absolute', top: -30, right: -30, width: 120, height: 120, borderRadius: '50%', background: 'rgba(255,255,255,0.1)' }}></div>
                                    <div style={{ position: 'absolute', bottom: -20, left: -20, width: 80, height: 80, borderRadius: '50%', background: 'rgba(255,255,255,0.08)' }}></div>
                                    <div style={{ fontSize: '0.8rem', fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, opacity: 0.8, marginBottom: 6 }}>
                                        {result.isSustainable ? '✅ Sustainable Plan' : '⚠️ Corpus Will Deplete'}
                                    </div>
                                    <div style={{ fontFamily: 'Outfit', fontSize: '1rem', fontWeight: 500, lineHeight: 1.6, maxWidth: 380, margin: '0 auto' }}>
                                        {result.sustainabilityMessage}
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'center', gap: 32, marginTop: 16, fontSize: '0.82rem', fontWeight: 600 }}>
                                        <div>
                                            <div style={{ opacity: 0.7, fontSize: '0.7rem', textTransform: 'uppercase' }}>Withdrawal Rate</div>
                                            {Number(result.withdrawalRate).toFixed(1)}%
                                        </div>
                                        <div>
                                            <div style={{ opacity: 0.7, fontSize: '0.7rem', textTransform: 'uppercase' }}>Safe Rate (4% Rule)</div>
                                            {Number(result.safeWithdrawalRate).toFixed(1)}%
                                        </div>
                                    </div>
                                </div>

                                <div className="result-row" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
                                    <div className="result-card">
                                        <div className="result-label">Total Withdrawn</div>
                                        <div className="result-value" style={{ color: '#ff4c6a' }}>{fmt(result.totalWithdrawn)}</div>
                                    </div>
                                    <div className="result-card">
                                        <div className="result-label">Returns Earned</div>
                                        <div className="result-value" style={{ color: '#00e5a0' }}>{fmt(result.totalReturnsEarned)}</div>
                                    </div>
                                    <div className="result-card">
                                        <div className="result-label">Final Corpus</div>
                                        <div className="result-value" style={{ color: '#7c4dff' }}>{fmt(result.finalCorpusValue)}</div>
                                    </div>
                                </div>

                                <div className="two-col">
                                    {chartData && (
                                        <div className="card">
                                            <h3 style={{ marginBottom: 16 }}>Corpus Over Time</h3>
                                            <div style={{ height: 220 }}>
                                                <Line data={chartData} options={{
                                                    responsive: true, maintainAspectRatio: false,
                                                    plugins: { legend: { display: false } },
                                                    scales: {
                                                        x: { grid: { display: false }, ticks: { color: '#5a6f83' } },
                                                        y: { grid: { color: 'rgba(255,255,255,0.04)' }, ticks: { color: '#5a6f83', callback: v => v >= 10000000 ? '₹' + (v / 10000000).toFixed(1) + 'Cr' : '₹' + (v / 100000).toFixed(0) + 'L' } }
                                                    }
                                                }} />
                                            </div>
                                        </div>
                                    )}
                                    {wdVsReturn && (
                                        <div className="card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: 12 }}>
                                            <h3>Money Flow</h3>
                                            <div style={{ width: 170, height: 170 }}>
                                                <Doughnut data={wdVsReturn} options={{ responsive: true, maintainAspectRatio: true, plugins: { legend: { labels: { color: '#8899aa', font: { size: 10 } }, position: 'bottom' } } }} />
                                            </div>
                                        </div>
                                    )}
                                </div>

                                {/* Yearly Summary Table */}
                                <div className="card">
                                    <div className="card-header"><h3>Yearly Summary</h3></div>
                                    <div className="data-table-wrapper">
                                        <table className="data-table">
                                            <thead><tr><th>Year</th><th>Opening</th><th>Returns</th><th>Withdrawn</th><th>Closing</th><th>Status</th></tr></thead>
                                            <tbody>
                                                {(result.yearlySummary || []).map(y => (
                                                    <tr key={y.year}>
                                                        <td>{y.year}</td>
                                                        <td>{fmt(y.startingCorpus)}</td>
                                                        <td style={{ color: '#00e5a0' }}>+{fmt(y.totalReturns)}</td>
                                                        <td style={{ color: '#ff4c6a' }}>-{fmt(y.totalWithdrawals)}</td>
                                                        <td style={{ fontWeight: 600 }}>{fmt(y.endingCorpus)}</td>
                                                        <td><span className={`badge badge-${y.corpusGrowing ? 'buy' : 'sell'}`}>{y.corpusGrowing ? 'Growing' : 'Shrinking'}</span></td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </>
                        ) : (
                            <div className="card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 300, flexDirection: 'column', gap: 16 }}>
                                <div style={{ width: 64, height: 64, borderRadius: '50%', background: 'rgba(255,76,106,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.5rem' }}>💎</div>
                                <p style={{ color: 'var(--text-muted)', fontSize: '1rem' }}>Set parameters and click Calculate SWP</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
}
