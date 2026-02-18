import { useState } from 'react';
import { ArrowLeft } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Doughnut } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import Header from '../../components/Header';
import { calculatorsApi } from '../../services/api';

ChartJS.register(ArcElement, Tooltip, Legend);

const fmt = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 });

export default function LoanCalculator() {
    const [amount, setAmount] = useState(3000000);
    const [rate, setRate] = useState(8.5);
    const [tenure, setTenure] = useState(20);
    const [result, setResult] = useState(null);
    const [schedule, setSchedule] = useState([]);
    const [loading, setLoading] = useState(false);
    const [viewMode, setViewMode] = useState('yearly');

    const calculate = async () => {
        setLoading(true);
        try {
            const res = await calculatorsApi.loanAnalyze({ loanAmount: amount, annualInterestRate: rate, loanTermMonths: tenure * 12 });
            const d = res.data.data;
            setResult(d);
            setSchedule(d.amortizationSchedule || d.yearlyBreakdown || []);
        } catch (e) {
            const r = rate / 100 / 12, n = tenure * 12;
            const emi = amount * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
            const totalPay = emi * n;
            const totalInt = totalPay - amount;
            setResult({ monthlyEMI: Math.round(emi), totalPayment: Math.round(totalPay), totalInterest: Math.round(totalInt), loanAmount: amount });
            const sched = [];
            let bal = amount;
            for (let y = 1; y <= tenure; y++) {
                let yp = 0, yi = 0;
                for (let m = 0; m < 12 && bal > 0; m++) {
                    const intPart = bal * (rate / 100 / 12);
                    const prinPart = emi - intPart;
                    yp += prinPart; yi += intPart;
                    bal -= prinPart;
                }
                sched.push({ year: y, principal: Math.round(yp), interest: Math.round(yi), balance: Math.max(0, Math.round(bal)) });
            }
            setSchedule(sched);
        }
        setLoading(false);
    };

    const principal = result?.loanAmount || amount;
    const interest = result?.totalInterest || 0;

    return (
        <>
            <Header title="Loan EMI Analyzer" subtitle="Calculate EMI and amortization"
                actions={<Link to="/calculators" className="btn btn-secondary"><ArrowLeft size={14} /> Back</Link>} />
            <div className="page-content">
                <div className="calc-layout">
                    <div className="calc-input-panel">
                        <div className="card">
                            <h3 style={{ marginBottom: 20 }}>Loan Details</h3>
                            {[
                                { label: 'Loan Amount', val: amount, set: setAmount, min: 100000, max: 50000000, step: 100000, pre: true },
                                { label: 'Interest Rate', val: rate, set: setRate, min: 4, max: 20, step: 0.1, sfx: '%' },
                                { label: 'Loan Tenure', val: tenure, set: setTenure, min: 1, max: 30, step: 1, sfx: ' years' },
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
                            <button className="btn btn-primary" style={{ width: '100%', marginTop: 12 }} onClick={calculate} disabled={loading}>
                                {loading ? 'Analyzing...' : 'Analyze Loan'}
                            </button>
                        </div>
                    </div>

                    <div className="calc-result-panel">
                        {result ? (
                            <>
                                {/* Gradient EMI Card */}
                                <div style={{ background: 'linear-gradient(135deg, #00bcd4, #00e5a0)', borderRadius: 'var(--border-radius-lg)', padding: '36px 32px', textAlign: 'center', color: '#0a1929', position: 'relative', overflow: 'hidden' }}>
                                    <div style={{ position: 'absolute', top: -30, right: -30, width: 120, height: 120, borderRadius: '50%', background: 'rgba(255,255,255,0.1)' }}></div>
                                    <div style={{ position: 'absolute', bottom: -20, left: -20, width: 80, height: 80, borderRadius: '50%', background: 'rgba(255,255,255,0.08)' }}></div>
                                    <div style={{ fontSize: '0.85rem', fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, opacity: 0.8, marginBottom: 8 }}>Monthly EMI</div>
                                    <div style={{ fontFamily: 'Outfit', fontSize: '3rem', fontWeight: 800, marginBottom: 4 }}>{fmt(result.monthlyEMI)}</div>
                                    <div style={{ display: 'flex', justifyContent: 'center', gap: 32, marginTop: 16, fontSize: '0.85rem', fontWeight: 600 }}>
                                        <div><div style={{ opacity: 0.7, fontSize: '0.72rem', textTransform: 'uppercase', marginBottom: 2 }}>Total Interest</div>{fmt(interest)}</div>
                                        <div><div style={{ opacity: 0.7, fontSize: '0.72rem', textTransform: 'uppercase', marginBottom: 2 }}>Total Payment</div>{fmt(result.totalPayment)}</div>
                                    </div>
                                </div>

                                {/* Breakdown Row */}
                                <div className="two-col">
                                    <div className="card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 20 }}>
                                        <div style={{ width: 180, height: 180, position: 'relative' }}>
                                            <Doughnut data={{ labels: ['Principal', 'Interest'], datasets: [{ data: [principal, interest], backgroundColor: ['#00bcd4', '#7c4dff'], borderWidth: 0, cutout: '70%' }] }} options={{ responsive: true, maintainAspectRatio: true, plugins: { legend: { display: false } } }} />
                                            <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%,-50%)', textAlign: 'center' }}>
                                                <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Total</div>
                                                <div style={{ fontSize: '0.9rem', fontWeight: 700 }}>100%</div>
                                            </div>
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                                        <div className="result-card">
                                            <div className="result-label">Principal Loan</div>
                                            <div className="result-value">{fmt(principal)}</div>
                                            <div className="result-sub" style={{ color: '#00bcd4' }}>{(principal / (principal + interest) * 100).toFixed(1)}%</div>
                                        </div>
                                        <div className="result-card">
                                            <div className="result-label">Total Interest</div>
                                            <div className="result-value" style={{ color: '#7c4dff' }}>{fmt(interest)}</div>
                                            <div className="result-sub" style={{ color: '#7c4dff' }}>{(interest / (principal + interest) * 100).toFixed(1)}%</div>
                                        </div>
                                    </div>
                                </div>

                                {/* Amortization Schedule */}
                                {schedule.length > 0 && (
                                    <div className="card">
                                        <div className="card-header">
                                            <h3>Amortization Schedule</h3>
                                            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                                                <div className="tab-group">
                                                    <button className={`tab-btn ${viewMode === 'yearly' ? 'active' : ''}`} onClick={() => setViewMode('yearly')}>Yearly</button>
                                                    <button className={`tab-btn ${viewMode === 'monthly' ? 'active' : ''}`} onClick={() => setViewMode('monthly')}>Monthly</button>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="data-table-wrapper">
                                            <table className="data-table">
                                                <thead><tr><th>{viewMode === 'yearly' ? 'Year' : 'Month'}</th><th>Opening Balance</th><th>EMI</th><th style={{ color: '#00bcd4' }}>Principal</th><th style={{ color: '#7c4dff' }}>Interest</th><th>Closing Balance</th></tr></thead>
                                                <tbody>{schedule.slice(0, viewMode === 'yearly' ? tenure : 12).map((r, i) => (
                                                    <tr key={i}>
                                                        <td>{r.year || i + 1}</td>
                                                        <td>{fmt(i === 0 ? principal : schedule[i - 1]?.balance || schedule[i - 1]?.remainingBalance)}</td>
                                                        <td>{fmt(result.monthlyEMI * (viewMode === 'yearly' ? 12 : 1))}</td>
                                                        <td style={{ color: '#00bcd4', fontWeight: 600 }}>{fmt(r.principal || r.principalPaid)}</td>
                                                        <td style={{ color: '#7c4dff' }}>{fmt(r.interest || r.interestPaid)}</td>
                                                        <td>{fmt(r.balance || r.remainingBalance)}</td>
                                                    </tr>
                                                ))}</tbody>
                                            </table>
                                        </div>
                                    </div>
                                )}
                            </>
                        ) : (
                            <div className="card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 300, flexDirection: 'column', gap: 16 }}>
                                <div style={{ width: 64, height: 64, borderRadius: '50%', background: 'rgba(0,188,212,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.5rem' }}>🏦</div>
                                <p style={{ color: 'var(--text-muted)', fontSize: '1rem' }}>Set loan details and click Analyze</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
}
