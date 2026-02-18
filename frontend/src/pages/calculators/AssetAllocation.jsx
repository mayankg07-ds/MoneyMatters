import { useState } from 'react';
import { ArrowLeft, Plus, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Doughnut } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import Header from '../../components/Header';
import { calculatorsApi } from '../../services/api';

ChartJS.register(ArcElement, Tooltip, Legend);

const fmt = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 });
const COLORS = ['#00e5a0', '#7c4dff', '#ffb347', '#00bcd4', '#ff4c6a', '#36a2eb'];

const DEFAULT_ASSETS = ['Equity', 'Debt', 'Gold', 'Cash'];

export default function AssetAllocation() {
    const [holdings, setHoldings] = useState(
        DEFAULT_ASSETS.map(name => ({ assetName: name, currentValue: '' }))
    );
    const [targets, setTargets] = useState(
        [
            { assetName: 'Equity', targetPercentage: 60 },
            { assetName: 'Debt', targetPercentage: 25 },
            { assetName: 'Gold', targetPercentage: 10 },
            { assetName: 'Cash', targetPercentage: 5 },
        ]
    );
    const [freshInvestment, setFreshInvestment] = useState(0);
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);

    const addAsset = () => {
        const name = `Asset ${holdings.length + 1}`;
        setHoldings([...holdings, { assetName: name, currentValue: '' }]);
        setTargets([...targets, { assetName: name, targetPercentage: 0 }]);
    };

    const removeAsset = (idx) => {
        if (holdings.length <= 2) return;
        setHoldings(holdings.filter((_, i) => i !== idx));
        setTargets(targets.filter((_, i) => i !== idx));
    };

    const updateHolding = (idx, field, value) => {
        const updated = [...holdings];
        updated[idx] = { ...updated[idx], [field]: value };
        setHoldings(updated);
        if (field === 'assetName') {
            const updatedT = [...targets];
            updatedT[idx] = { ...updatedT[idx], assetName: value };
            setTargets(updatedT);
        }
    };

    const updateTarget = (idx, value) => {
        const updated = [...targets];
        updated[idx] = { ...updated[idx], targetPercentage: Number(value) };
        setTargets(updated);
    };

    const totalTarget = targets.reduce((s, t) => s + Number(t.targetPercentage || 0), 0);
    const totalCurrent = holdings.reduce((s, h) => s + Number(h.currentValue || 0), 0);

    const calculate = async () => {
        setLoading(true);
        try {
            const res = await calculatorsApi.assetAllocation({
                currentHoldings: holdings.map(h => ({ assetName: h.assetName, currentValue: Number(h.currentValue || 0) })),
                targetAllocations: targets.map(t => ({ assetName: t.assetName, targetPercentage: Number(t.targetPercentage) })),
                freshInvestment: Number(freshInvestment || 0),
            });
            setResult(res.data.data);
        } catch (e) {
            // Client-side fallback
            const total = totalCurrent + Number(freshInvestment || 0);
            const analyses = holdings.map((h, i) => {
                const cv = Number(h.currentValue || 0);
                const cp = total > 0 ? (cv / total) * 100 : 0;
                const tp = Number(targets[i].targetPercentage);
                const tv = (tp / 100) * total;
                return {
                    assetName: h.assetName, currentValue: cv,
                    currentPercentage: cp.toFixed(1), targetPercentage: tp,
                    drift: (cp - tp).toFixed(1), targetValue: tv.toFixed(0),
                    adjustmentNeeded: (tv - cv).toFixed(0),
                };
            });
            const actions = analyses.map(a => ({
                assetName: a.assetName,
                action: Number(a.adjustmentNeeded) > 100 ? 'BUY' : Number(a.adjustmentNeeded) < -100 ? 'SELL' : 'HOLD',
                amount: Math.abs(Number(a.adjustmentNeeded)),
                recommendation: Number(a.adjustmentNeeded) > 100 ? `Invest ${fmt(Math.abs(Number(a.adjustmentNeeded)))} more in ${a.assetName}` : Number(a.adjustmentNeeded) < -100 ? `Reduce ${a.assetName} by ${fmt(Math.abs(Number(a.adjustmentNeeded)))}` : `${a.assetName} is balanced`,
            }));
            setResult({
                totalPortfolioValue: total,
                assetAnalyses: analyses,
                rebalancingActions: actions,
                totalBuyAmount: actions.filter(a => a.action === 'BUY').reduce((s, a) => s + a.amount, 0),
                totalSellAmount: actions.filter(a => a.action === 'SELL').reduce((s, a) => s + a.amount, 0),
                balanced: analyses.every(a => Math.abs(Number(a.drift)) < 2),
            });
        }
        setLoading(false);
    };

    return (
        <>
            <Header title="Asset Allocation" subtitle="Rebalance your portfolio across asset classes"
                actions={<Link to="/calculators" className="btn btn-secondary"><ArrowLeft size={14} /> Back</Link>} />
            <div className="page-content">
                <div className="calc-layout">
                    <div className="calc-input-panel">
                        <div className="card">
                            <h3 style={{ marginBottom: 16 }}>Current Holdings</h3>
                            {holdings.map((h, i) => (
                                <div key={i} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr auto', gap: 8, marginBottom: 10, alignItems: 'end' }}>
                                    <div className="input-group" style={{ margin: 0 }}>
                                        {i === 0 && <label style={{ fontSize: '0.75rem' }}>Asset Class</label>}
                                        <input className="input-field" value={h.assetName} onChange={e => updateHolding(i, 'assetName', e.target.value)} />
                                    </div>
                                    <div className="input-group" style={{ margin: 0 }}>
                                        {i === 0 && <label style={{ fontSize: '0.75rem' }}>Current Value (₹)</label>}
                                        <input className="input-field" type="number" placeholder="0" value={h.currentValue} onChange={e => updateHolding(i, 'currentValue', e.target.value)} />
                                    </div>
                                    <button className="btn-icon" onClick={() => removeAsset(i)} style={{ borderColor: 'rgba(255,76,106,0.3)', color: '#ff4c6a', height: 38 }}><Trash2 size={14} /></button>
                                </div>
                            ))}
                            <button className="btn btn-secondary" style={{ width: '100%', marginBottom: 16 }} onClick={addAsset}><Plus size={14} /> Add Asset Class</button>

                            <h3 style={{ marginBottom: 12 }}>Target Allocation (%)</h3>
                            {targets.map((t, i) => (
                                <div key={i} className="slider-group">
                                    <div className="slider-header">
                                        <span className="slider-label">{t.assetName}</span>
                                        <span className="slider-value">{t.targetPercentage}%</span>
                                    </div>
                                    <input type="range" className="range-slider" min={0} max={100} step={1} value={t.targetPercentage} onChange={e => updateTarget(i, e.target.value)} />
                                </div>
                            ))}
                            <div style={{ fontSize: '0.82rem', color: totalTarget === 100 ? '#00e5a0' : '#ff4c6a', fontWeight: 600, marginBottom: 12 }}>
                                Total: {totalTarget}% {totalTarget !== 100 && '(must be 100%)'}
                            </div>

                            <div className="input-group">
                                <label>Fresh Investment (₹)</label>
                                <input className="input-field" type="number" value={freshInvestment} onChange={e => setFreshInvestment(e.target.value)} />
                            </div>

                            <button className="btn btn-primary" style={{ width: '100%', marginTop: 8 }} onClick={calculate} disabled={loading || totalTarget !== 100}>
                                {loading ? 'Calculating...' : 'Rebalance Portfolio'}
                            </button>
                        </div>
                    </div>

                    <div className="calc-result-panel">
                        {result ? (
                            <>
                                <div className="calc-result-hero">
                                    <div className="hero-label">Total Portfolio Value</div>
                                    <div className="hero-value">{fmt(result.totalPortfolioValue)}</div>
                                    <div className="hero-sub">{result.balanced || result.isBalanced ? '✅ Portfolio is balanced' : '⚠️ Rebalancing recommended'}</div>
                                </div>

                                <div className="two-col">
                                    <div className="card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 20 }}>
                                        <div style={{ width: 200, height: 200, position: 'relative' }}>
                                            <Doughnut data={{
                                                labels: (result.assetAnalyses || []).map(a => a.assetName),
                                                datasets: [{
                                                    data: (result.assetAnalyses || []).map(a => Number(a.targetValue || a.currentValue)),
                                                    backgroundColor: COLORS.slice(0, (result.assetAnalyses || []).length),
                                                    borderWidth: 0, cutout: '70%',
                                                }]
                                            }} options={{ responsive: true, maintainAspectRatio: true, plugins: { legend: { display: false } } }} />
                                            <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%,-50%)', textAlign: 'center' }}>
                                                <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Target</div>
                                                <div style={{ fontSize: '1rem', fontWeight: 700 }}>Allocation</div>
                                            </div>
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                                        {(result.assetAnalyses || []).map((a, i) => (
                                            <div className="result-card" key={i} style={{ padding: '12px 16px' }}>
                                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                                        <div style={{ width: 10, height: 10, borderRadius: '50%', background: COLORS[i] }}></div>
                                                        <span style={{ fontWeight: 500, fontSize: '0.85rem' }}>{a.assetName}</span>
                                                    </div>
                                                    <span style={{ fontSize: '0.82rem', fontWeight: 600 }}>{fmt(a.targetValue || a.currentValue)}</span>
                                                </div>
                                                <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: 4 }}>
                                                    Current: {Number(a.currentPercentage).toFixed(0)}% → Target: {Number(a.targetPercentage).toFixed(0)}%
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>

                                <div className="card">
                                    <div className="card-header"><h3>Rebalancing Actions</h3></div>
                                    <div className="data-table-wrapper">
                                        <table className="data-table">
                                            <thead><tr><th>Asset</th><th>Action</th><th>Amount</th><th>Recommendation</th></tr></thead>
                                            <tbody>
                                                {(result.rebalancingActions || []).map((a, i) => (
                                                    <tr key={i}>
                                                        <td style={{ fontWeight: 500 }}>{a.assetName}</td>
                                                        <td><span className={`badge badge-${a.action?.toLowerCase()}`}>{a.action}</span></td>
                                                        <td style={{ fontWeight: 600, color: a.action === 'BUY' ? '#00e5a0' : a.action === 'SELL' ? '#ff4c6a' : 'var(--text-secondary)' }}>{fmt(a.amount)}</td>
                                                        <td style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>{a.recommendation}</td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </>
                        ) : (
                            <div className="card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 300, flexDirection: 'column', gap: 16 }}>
                                <div style={{ width: 64, height: 64, borderRadius: '50%', background: 'rgba(255,179,71,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.5rem' }}>⚖️</div>
                                <p style={{ color: 'var(--text-muted)', fontSize: '1rem' }}>Set your holdings & targets, then click Rebalance</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
}
