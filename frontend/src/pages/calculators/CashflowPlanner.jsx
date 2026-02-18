import { useState } from 'react';
import { ArrowLeft, Plus, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Bar } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Tooltip, Legend } from 'chart.js';
import Header from '../../components/Header';
import { calculatorsApi } from '../../services/api';

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const fmt = (v) => '₹' + Number(v || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 });

export default function CashflowPlanner() {
    const [incomes, setIncomes] = useState([
        { name: 'Salary', monthlyAmount: 80000, category: 'Fixed' },
        { name: 'Freelance', monthlyAmount: 15000, category: 'Variable' },
    ]);
    const [expenses, setExpenses] = useState([
        { name: 'Rent', monthlyAmount: 20000, category: 'Fixed' },
        { name: 'Groceries', monthlyAmount: 8000, category: 'Variable' },
        { name: 'Utilities', monthlyAmount: 3000, category: 'Fixed' },
        { name: 'Entertainment', monthlyAmount: 5000, category: 'Discretionary' },
    ]);
    const [projectionYears, setProjectionYears] = useState(10);
    const [incomeGrowth, setIncomeGrowth] = useState(8);
    const [expenseGrowth, setExpenseGrowth] = useState(6);
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);

    const addItem = (type) => {
        if (type === 'income') setIncomes([...incomes, { name: '', monthlyAmount: '', category: 'Fixed' }]);
        else setExpenses([...expenses, { name: '', monthlyAmount: '', category: 'Fixed' }]);
    };

    const removeItem = (type, idx) => {
        if (type === 'income') { if (incomes.length <= 1) return; setIncomes(incomes.filter((_, i) => i !== idx)); }
        else { if (expenses.length <= 1) return; setExpenses(expenses.filter((_, i) => i !== idx)); }
    };

    const updateItem = (type, idx, field, value) => {
        const list = type === 'income' ? [...incomes] : [...expenses];
        list[idx] = { ...list[idx], [field]: field === 'monthlyAmount' ? value : value };
        if (type === 'income') setIncomes(list); else setExpenses(list);
    };

    const totalIncome = incomes.reduce((s, i) => s + Number(i.monthlyAmount || 0), 0);
    const totalExpense = expenses.reduce((s, e) => s + Number(e.monthlyAmount || 0), 0);
    const netCashflow = totalIncome - totalExpense;

    const calculate = async () => {
        setLoading(true);
        try {
            const res = await calculatorsApi.cashflow({
                incomes: incomes.map(i => ({ name: i.name, monthlyAmount: Number(i.monthlyAmount || 0), category: i.category })),
                expenses: expenses.map(e => ({ name: e.name, monthlyAmount: Number(e.monthlyAmount || 0), category: e.category })),
                projectionYears,
                expectedIncomeGrowthPercent: incomeGrowth,
                expectedExpenseGrowthPercent: expenseGrowth,
            });
            setResult(res.data.data);
        } catch (e) {
            // Client-side fallback
            const projs = [];
            let cumSavings = 0;
            for (let y = 1; y <= projectionYears; y++) {
                const mi = totalIncome * Math.pow(1 + incomeGrowth / 100, y - 1);
                const me = totalExpense * Math.pow(1 + expenseGrowth / 100, y - 1);
                const net = mi - me;
                const annual = net * 12;
                cumSavings += annual;
                projs.push({
                    year: y, monthlyIncome: Math.round(mi), monthlyExpense: Math.round(me),
                    monthlyNetCashflow: Math.round(net), annualIncome: Math.round(mi * 12),
                    annualExpense: Math.round(me * 12), annualSavings: Math.round(annual),
                    savingsRate: mi > 0 ? ((net / mi) * 100).toFixed(1) : 0,
                    cumulativeSavings: Math.round(cumSavings),
                });
            }
            setResult({
                currentMonthlyIncome: totalIncome, currentMonthlyExpense: totalExpense,
                currentNetCashflow: netCashflow, currentSavingsRate: totalIncome > 0 ? ((netCashflow / totalIncome) * 100).toFixed(1) : 0,
                totalSavingsOverPeriod: Math.round(cumSavings),
                averageSavingsRate: projs.length > 0 ? (projs.reduce((s, p) => s + Number(p.savingsRate), 0) / projs.length).toFixed(1) : 0,
                projections: projs,
            });
        }
        setLoading(false);
    };

    const chartData = result?.projections ? {
        labels: result.projections.map(p => `Y${p.year}`),
        datasets: [
            { label: 'Income', data: result.projections.map(p => p.annualIncome), backgroundColor: 'rgba(0,229,160,0.7)', borderRadius: 4 },
            { label: 'Expense', data: result.projections.map(p => p.annualExpense), backgroundColor: 'rgba(255,76,106,0.6)', borderRadius: 4 },
            { label: 'Savings', data: result.projections.map(p => p.annualSavings), backgroundColor: 'rgba(124,77,255,0.6)', borderRadius: 4 },
        ]
    } : null;

    const renderItemsList = (items, type) => (
        <>
            {items.map((item, i) => (
                <div key={i} style={{ display: 'grid', gridTemplateColumns: '1fr 100px 90px auto', gap: 6, marginBottom: 8, alignItems: 'end' }}>
                    <div className="input-group" style={{ margin: 0 }}>
                        {i === 0 && <label style={{ fontSize: '0.72rem' }}>Name</label>}
                        <input className="input-field" value={item.name} onChange={e => updateItem(type, i, 'name', e.target.value)} placeholder="Name" />
                    </div>
                    <div className="input-group" style={{ margin: 0 }}>
                        {i === 0 && <label style={{ fontSize: '0.72rem' }}>₹/month</label>}
                        <input className="input-field" type="number" value={item.monthlyAmount} onChange={e => updateItem(type, i, 'monthlyAmount', e.target.value)} />
                    </div>
                    <div className="input-group" style={{ margin: 0 }}>
                        {i === 0 && <label style={{ fontSize: '0.72rem' }}>Type</label>}
                        <select className="select-field" value={item.category} onChange={e => updateItem(type, i, 'category', e.target.value)} style={{ padding: '8px 6px', fontSize: '0.78rem' }}>
                            <option value="Fixed">Fixed</option>
                            <option value="Variable">Variable</option>
                            <option value="Discretionary">Disc.</option>
                        </select>
                    </div>
                    <button className="btn-icon" onClick={() => removeItem(type, i)} style={{ borderColor: 'rgba(255,76,106,0.3)', color: '#ff4c6a', height: 38, width: 38 }}><Trash2 size={13} /></button>
                </div>
            ))}
            <button className="btn btn-secondary" style={{ width: '100%', marginBottom: 4, fontSize: '0.82rem', padding: '6px 12px' }} onClick={() => addItem(type)}><Plus size={13} /> Add {type === 'income' ? 'Income' : 'Expense'}</button>
        </>
    );

    return (
        <>
            <Header title="Cashflow Planner" subtitle="Project monthly cashflows and savings over time"
                actions={<Link to="/calculators" className="btn btn-secondary"><ArrowLeft size={14} /> Back</Link>} />
            <div className="page-content">
                <div className="calc-layout">
                    <div className="calc-input-panel">
                        <div className="card">
                            <h3 style={{ marginBottom: 12, color: '#00e5a0' }}>💰 Income Sources</h3>
                            {renderItemsList(incomes, 'income')}
                            <div style={{ fontSize: '0.82rem', fontWeight: 600, color: '#00e5a0', textAlign: 'right', margin: '8px 0 20px' }}>
                                Total: {fmt(totalIncome)}/mo
                            </div>

                            <h3 style={{ marginBottom: 12, color: '#ff4c6a' }}>💸 Expenses</h3>
                            {renderItemsList(expenses, 'expense')}
                            <div style={{ fontSize: '0.82rem', fontWeight: 600, color: '#ff4c6a', textAlign: 'right', margin: '8px 0 20px' }}>
                                Total: {fmt(totalExpense)}/mo
                            </div>

                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10, margin: '12px 0' }}>
                                <div className="slider-group">
                                    <div className="slider-header">
                                        <span className="slider-label">Years</span>
                                        <span className="slider-value">{projectionYears}</span>
                                    </div>
                                    <input type="range" className="range-slider" min={1} max={30} value={projectionYears} onChange={e => setProjectionYears(Number(e.target.value))} />
                                </div>
                                <div className="slider-group">
                                    <div className="slider-header">
                                        <span className="slider-label">Inc. Growth</span>
                                        <span className="slider-value">{incomeGrowth}%</span>
                                    </div>
                                    <input type="range" className="range-slider" min={0} max={30} step={0.5} value={incomeGrowth} onChange={e => setIncomeGrowth(Number(e.target.value))} />
                                </div>
                                <div className="slider-group">
                                    <div className="slider-header">
                                        <span className="slider-label">Exp. Growth</span>
                                        <span className="slider-value">{expenseGrowth}%</span>
                                    </div>
                                    <input type="range" className="range-slider" min={0} max={20} step={0.5} value={expenseGrowth} onChange={e => setExpenseGrowth(Number(e.target.value))} />
                                </div>
                            </div>

                            <button className="btn btn-primary" style={{ width: '100%', marginTop: 8 }} onClick={calculate} disabled={loading}>
                                {loading ? 'Projecting...' : 'Project Cashflow'}
                            </button>
                        </div>
                    </div>

                    <div className="calc-result-panel">
                        {result ? (
                            <>
                                {/* Net Cashflow Hero */}
                                <div style={{ background: netCashflow >= 0 ? 'linear-gradient(135deg, #00bcd4, #00e5a0)' : 'linear-gradient(135deg, #ff4c6a, #ffb347)', borderRadius: 'var(--border-radius-lg)', padding: '32px 28px', textAlign: 'center', color: '#0a1929', position: 'relative', overflow: 'hidden' }}>
                                    <div style={{ position: 'absolute', top: -30, right: -30, width: 120, height: 120, borderRadius: '50%', background: 'rgba(255,255,255,0.1)' }}></div>
                                    <div style={{ fontSize: '0.8rem', fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, opacity: 0.8, marginBottom: 6 }}>Net Monthly Cashflow</div>
                                    <div style={{ fontFamily: 'Outfit', fontSize: '2.8rem', fontWeight: 800 }}>{fmt(result.currentNetCashflow)}</div>
                                    <div style={{ display: 'flex', justifyContent: 'center', gap: 32, marginTop: 14, fontSize: '0.82rem', fontWeight: 600 }}>
                                        <div><div style={{ opacity: 0.7, fontSize: '0.7rem', textTransform: 'uppercase' }}>Savings Rate</div>{Number(result.currentSavingsRate).toFixed(1)}%</div>
                                        <div><div style={{ opacity: 0.7, fontSize: '0.7rem', textTransform: 'uppercase' }}>Total Savings ({projectionYears}yr)</div>{fmt(result.totalSavingsOverPeriod)}</div>
                                    </div>
                                </div>

                                <div className="result-row">
                                    <div className="result-card">
                                        <div className="result-label">Monthly Income</div>
                                        <div className="result-value" style={{ color: '#00e5a0' }}>{fmt(result.currentMonthlyIncome)}</div>
                                    </div>
                                    <div className="result-card">
                                        <div className="result-label">Monthly Expense</div>
                                        <div className="result-value" style={{ color: '#ff4c6a' }}>{fmt(result.currentMonthlyExpense)}</div>
                                    </div>
                                </div>

                                {chartData && (
                                    <div className="card">
                                        <h3 style={{ marginBottom: 16 }}>Income vs Expenses vs Savings</h3>
                                        <div style={{ height: 280 }}>
                                            <Bar data={chartData} options={{
                                                responsive: true, maintainAspectRatio: false,
                                                plugins: { legend: { labels: { color: '#8899aa', font: { size: 11 } } } },
                                                scales: {
                                                    x: { grid: { display: false }, ticks: { color: '#5a6f83' } },
                                                    y: { grid: { color: 'rgba(255,255,255,0.04)' }, ticks: { color: '#5a6f83', callback: v => v >= 1000000 ? '₹' + (v / 100000).toFixed(0) + 'L' : '₹' + (v / 1000).toFixed(0) + 'K' } }
                                                }
                                            }} />
                                        </div>
                                    </div>
                                )}

                                {/* Yearly Breakdown Table */}
                                <div className="card">
                                    <div className="card-header"><h3>Yearly Projection</h3></div>
                                    <div className="data-table-wrapper">
                                        <table className="data-table">
                                            <thead><tr><th>Year</th><th>Monthly Inc.</th><th>Monthly Exp.</th><th>Net/mo</th><th>Annual Savings</th><th>Savings Rate</th></tr></thead>
                                            <tbody>
                                                {(result.projections || []).map(p => (
                                                    <tr key={p.year}>
                                                        <td>{p.year}</td>
                                                        <td style={{ color: '#00e5a0' }}>{fmt(p.monthlyIncome)}</td>
                                                        <td style={{ color: '#ff4c6a' }}>{fmt(p.monthlyExpense)}</td>
                                                        <td style={{ fontWeight: 600 }}>{fmt(p.monthlyNetCashflow)}</td>
                                                        <td style={{ fontWeight: 600, color: '#7c4dff' }}>{fmt(p.annualSavings)}</td>
                                                        <td>{Number(p.savingsRate).toFixed(1)}%</td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </>
                        ) : (
                            <div className="card" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 300, flexDirection: 'column', gap: 16 }}>
                                <div style={{ width: 64, height: 64, borderRadius: '50%', background: 'rgba(54,162,235,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.5rem' }}>📊</div>
                                <p style={{ color: 'var(--text-muted)', fontSize: '1rem' }}>Add incomes & expenses, then click Project Cashflow</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
}
