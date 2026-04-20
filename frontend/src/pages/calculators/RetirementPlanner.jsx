import { useState } from 'react';
import { ArrowLeft, RotateCcw } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Bar } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Tooltip, Legend } from 'chart.js';
import Header from '../../components/Header';
import { calculatorsApi } from '../../services/api';
import ExplainButton from '../../components/ai/ExplainButton';

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const fmt = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 });

export default function RetirementPlanner() {
    const [age, setAge] = useState(30);
    const [retireAge, setRetireAge] = useState(60);
    const [lifeExp, setLifeExp] = useState(85);
    const [monthlyExp, setMonthlyExp] = useState(50000);
    const [inflation, setInflation] = useState(6);
    const [preRoi, setPreRoi] = useState(12);
    const [postRoi, setPostRoi] = useState(8);
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);

    const calculate = async () => {
        setLoading(true);
        try {
            const res = await calculatorsApi.retirement({
                currentAge: age, retirementAge: retireAge, lifeExpectancy: lifeExp,
                monthlyExpense: monthlyExp, expectedInflation: inflation,
                preRetirementReturn: preRoi, postRetirementReturn: postRoi,
            });
            setResult(res.data.data);
        } catch (e) {
            // Fallback
            const yearsToRetire = retireAge - age;
            const retirementYears = lifeExp - retireAge;
            const futureMonthly = monthlyExp * Math.pow(1 + inflation / 100, yearsToRetire);
            const futureAnnual = futureMonthly * 12;
            const realPostRate = (postRoi - inflation) / 100;
            const corpus = realPostRate > 0 ? futureAnnual * (1 - Math.pow(1 + realPostRate, -retirementYears)) / realPostRate : futureAnnual * retirementYears;
            const monthlySaving = corpus / ((Math.pow(1 + preRoi / 100 / 12, yearsToRetire * 12) - 1) / (preRoi / 100 / 12));
            setResult({ requiredCorpus: Math.round(corpus), monthlyExpenseAtRetirement: Math.round(futureMonthly), monthlySavingRequired: Math.round(monthlySaving), futureAnnualExpense: Math.round(futureAnnual) });
        }
        setLoading(false);
    };

    const sliders = [
        { label: 'Current Age', val: age, set: setAge, min: 18, max: 55, step: 1, sfx: ' yrs' },
        { label: 'Retirement Age', val: retireAge, set: setRetireAge, min: 40, max: 70, step: 1, sfx: ' yrs' },
        { label: 'Life Expectancy', val: lifeExp, set: setLifeExp, min: 60, max: 100, step: 1, sfx: ' yrs' },
        { label: 'Monthly Expense', val: monthlyExp, set: setMonthlyExp, min: 10000, max: 500000, step: 5000, sfx: '', pre: '₹' },
        { label: 'Inflation Rate', val: inflation, set: setInflation, min: 2, max: 15, step: 0.5, sfx: '%' },
        { label: 'Pre-Retirement Return', val: preRoi, set: setPreRoi, min: 4, max: 20, step: 0.5, sfx: '%' },
        { label: 'Post-Retirement Return', val: postRoi, set: setPostRoi, min: 4, max: 15, step: 0.5, sfx: '%' },
    ];

    return (
        <>
            <Header title="Retirement Planner" subtitle="Plan your retirement corpus"
                actions={<Link to="/calculators" className="btn btn-secondary"><ArrowLeft size={14} /> Back</Link>} />
            <div className="page-content">
                <div className="calc-layout">
                    <div className="calc-input-panel">
                        <div className="card">
                            <h3 style={{ marginBottom: 20 }}>Parameters</h3>
                            {sliders.map(s => (
                                <div className="slider-group" key={s.label}>
                                    <div className="slider-header">
                                        <span className="slider-label">{s.label}</span>
                                        <span className="slider-value">{s.pre ? fmt(s.val) : s.val + s.sfx}</span>
                                    </div>
                                    <input type="range" className="range-slider" min={s.min} max={s.max} step={s.step} value={s.val} onChange={e => s.set(Number(e.target.value))} />
                                    <div className="slider-range"><span>{s.pre ? fmt(s.min) : s.min + s.sfx}</span><span>{s.pre ? fmt(s.max) : s.max + s.sfx}</span></div>
                                </div>
                            ))}


                            <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
                                <button className="btn btn-primary" style={{ flex: 1 }} onClick={calculate} disabled={loading}>
                                    {loading ? 'Planning...' : 'Plan Retirement'}
                                </button>
                                <button className="btn btn-secondary" onClick={() => { setAge(30); setRetireAge(60); setLifeExp(85); setMonthlyExp(50000); setInflation(6); setPreRoi(12); setPostRoi(8); setResult(null); }}>
                                    <RotateCcw size={14} /> Reset
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className="calc-result-panel">
                        {result ? (
                            <>
                                {/* Hero corpus card with monthly income badge */}
                                <div className="calc-result-hero" style={{ position: 'relative' }}>
                                    <div className="hero-label">Required Retirement Corpus</div>
                                    <div className="hero-value">{fmt(result.requiredCorpus)}</div>
                                    <div className="hero-sub">Amount needed at age {retireAge} to sustain lifestyle until {lifeExp}</div>
                                    <div style={{ position: 'absolute', top: 16, right: 16, background: 'rgba(0,229,160,0.15)', border: '1px solid rgba(0,229,160,0.3)', borderRadius: 'var(--border-radius-sm)', padding: '8px 14px', textAlign: 'right' }}>
                                        <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Monthly Income Needed</div>
                                        <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#00e5a0' }}>{fmt(result.monthlyExpenseAtRetirement || result.requiredCorpus / ((lifeExp - retireAge) * 12))}</div>
                                        <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)' }}>Inflation adjusted</div>
                                    </div>
                                </div>

                                {/* Current vs Future Expense */}
                                <div className="result-row">
                                    <div className="result-card">
                                        <div className="result-label">Current Monthly Expense</div>
                                        <div className="result-value">{fmt(monthlyExp)}</div>
                                        <div style={{ height: 4, background: 'var(--border-subtle)', borderRadius: 2, marginTop: 12 }}><div style={{ height: '100%', width: '40%', background: 'var(--text-muted)', borderRadius: 2 }}></div></div>
                                    </div>
                                    <div className="result-card">
                                        <div className="result-label">Future Monthly Expense</div>
                                        <div className="result-value" style={{ color: '#ff4c6a' }}>{fmt(result.monthlyExpenseAtRetirement)}</div>
                                        <div style={{ fontSize: '0.72rem', color: '#ff4c6a', marginTop: 4 }}>↗ Due to inflation</div>
                                        <div style={{ height: 4, background: 'var(--border-subtle)', borderRadius: 2, marginTop: 8 }}><div style={{ height: '100%', width: '100%', background: '#ff4c6a', borderRadius: 2 }}></div></div>
                                    </div>
                                </div>

                                {/* Corpus Projection Chart */}
                                <div className="card">
                                    <h3 style={{ marginBottom: 16 }}>Corpus Projection</h3>
                                    <div style={{ height: 280 }}>
                                        <Bar data={(() => {
                                            const labels = [];
                                            const accData = [];
                                            const wdData = [];
                                            const yearsToRetire = retireAge - age;
                                            for (let a = age; a <= lifeExp; a += 5) {
                                                labels.push(`Age ${a}`);
                                                if (a <= retireAge) {
                                                    const yrs = a - age;
                                                    const saving = result.monthlySavingRequired || 0;
                                                    const accum = saving * ((Math.pow(1 + preRoi / 100 / 12, yrs * 12) - 1) / (preRoi / 100 / 12));
                                                    accData.push(Math.round(accum));
                                                    wdData.push(0);
                                                } else {
                                                    accData.push(0);
                                                    const remaining = lifeExp - a;
                                                    const total = lifeExp - retireAge;
                                                    wdData.push(Math.round(result.requiredCorpus * (remaining / total)));
                                                }
                                            }
                                            return {
                                                labels, datasets: [
                                                    { label: 'Accumulation', data: accData, backgroundColor: 'rgba(0,188,212,0.7)', borderRadius: 4 },
                                                    { label: 'Withdrawal', data: wdData, backgroundColor: 'rgba(0,229,160,0.6)', borderRadius: 4 },
                                                ]
                                            };
                                        })()} options={{ responsive: true, maintainAspectRatio: false, plugins: { legend: { labels: { color: '#8899aa', font: { size: 11 } } } }, scales: { x: { grid: { display: false }, ticks: { color: '#5a6f83' } }, y: { grid: { color: 'rgba(255,255,255,0.04)' }, ticks: { color: '#5a6f83', callback: v => v >= 10000000 ? '₹' + (v / 10000000).toFixed(1) + 'Cr' : '₹' + (v / 100000).toFixed(0) + 'L' } } } }} />
                                    </div>
                                </div>

                                {/* How to Achieve section */}
                                <div className="card" style={{ borderLeft: '3px solid #00bcd4' }}>
                                    <h3 style={{ marginBottom: 16 }}>How to Achieve This Corpus?</h3>
                                    <div className="result-row">
                                        <div className="result-card">
                                            <div className="result-label" style={{ textTransform: 'uppercase', fontSize: '0.7rem', letterSpacing: '0.5px' }}>Monthly SIP Required</div>
                                            <div style={{ display: 'flex', alignItems: 'baseline', gap: 6, marginTop: 8 }}>
                                                <div className="result-value" style={{ color: '#00e5a0' }}>{fmt(result.monthlySavingRequired)}</div>
                                                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>/mo</span>
                                            </div>
                                            <div className="result-sub">Assuming {preRoi}% returns</div>
                                        </div>
                                        <div className="result-card">
                                            <div className="result-label" style={{ textTransform: 'uppercase', fontSize: '0.7rem', letterSpacing: '0.5px' }}>Lumpsum Investment</div>
                                            <div style={{ display: 'flex', alignItems: 'baseline', gap: 6, marginTop: 8 }}>
                                                <div className="result-value" style={{ color: '#00bcd4' }}>{fmt(Math.round(result.requiredCorpus / Math.pow(1 + preRoi / 100, retireAge - age)))}</div>
                                                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>today</span>
                                            </div>
                                            <div className="result-sub">One-time investment</div>
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', gap: 12, marginTop: 16 }}>
                                        <Link to="/calculators/sip" className="btn btn-primary" style={{ flex: 1 }}>Start SIP →</Link>
                                        <button className="btn btn-secondary" style={{ flex: 1 }}>Talk to Advisor</button>
                                    </div>
                                </div>
                                <ExplainButton
                                    type="RETIREMENT"
                                    inputs={{ currentAge: age, retirementAge: retireAge, lifeExpectancy: lifeExp, monthlyExpense: monthlyExp, expectedInflation: inflation, preRetirementReturn: preRoi, postRetirementReturn: postRoi }}
                                    result={result}
                                />
                            </>
                        ) : (
                            <div className="card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 300, flexDirection: 'column', gap: 16 }}>
                                <div style={{ width: 64, height: 64, borderRadius: '50%', background: 'rgba(124,77,255,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.5rem' }}>🏖️</div>
                                <p style={{ color: 'var(--text-muted)', fontSize: '1rem' }}>Set parameters and click Plan Retirement</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
}
