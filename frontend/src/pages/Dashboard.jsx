import { useState, useEffect } from 'react';
import {
    Wallet, TrendingUp, ArrowUpRight, Percent,
    RefreshCw, Plus
} from 'lucide-react';
import { Line } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale, LinearScale, PointElement,
    LineElement, Filler, Tooltip,
} from 'chart.js';
import Header from '../components/Header';
import { holdingsApi, transactionsApi, analyticsApi } from '../services/api';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Filler, Tooltip);

const formatCurrency = (v) => {
    if (v == null) return '₹0';
    const n = Number(v);
    return '₹' + n.toLocaleString('en-IN', { maximumFractionDigits: 0 });
};

const formatPercent = (v) => {
    if (v == null) return '0%';
    return Number(v).toFixed(2) + '%';
};

export default function Dashboard() {
    const [summary, setSummary] = useState(null);
    const [holdings, setHoldings] = useState([]);
    const [transactions, setTransactions] = useState([]);
    const [analytics, setAnalytics] = useState(null);
    const [loading, setLoading] = useState(true);
    const userId = localStorage.getItem('userId') || '1';

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        setLoading(true);
        try {
            const [sumRes, txRes, anaRes] = await Promise.allSettled([
                holdingsApi.getSummary(userId),
                transactionsApi.getByUser(userId),
                analyticsApi.getByUser(userId),
            ]);
            if (sumRes.status === 'fulfilled') {
                setSummary(sumRes.value.data.data);
                setHoldings(sumRes.value.data.data?.holdings || []);
            }
            if (txRes.status === 'fulfilled') setTransactions(txRes.value.data.data || []);
            if (anaRes.status === 'fulfilled') setAnalytics(anaRes.value.data.data);
        } catch (e) {
            console.error(e);
        }
        setLoading(false);
    };

    const totalInvested = summary?.totalInvested || 0;
    const currentValue = summary?.totalCurrentValue || 0;
    const totalGain = summary?.totalUnrealizedGain || 0;
    const gainPercent = summary?.totalUnrealizedGainPercent || 0;
    const xirr = analytics?.xirr || 0;

    const chartData = {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
        datasets: [{
            data: [
                totalInvested * 0.7, totalInvested * 0.75, totalInvested * 0.8,
                totalInvested * 0.82, totalInvested * 0.88, totalInvested * 0.9,
                totalInvested * 0.85, totalInvested * 0.92, totalInvested * 0.95,
                totalInvested * 0.98, currentValue * 0.97, currentValue,
            ],
            fill: true,
            borderColor: '#00e5a0',
            backgroundColor: 'rgba(0, 229, 160, 0.08)',
            tension: 0.4,
            pointRadius: 0,
            borderWidth: 2,
        }],
    };

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false }, tooltip: { mode: 'index', intersect: false } },
        scales: {
            x: { grid: { display: false }, ticks: { color: '#5a6f83', font: { size: 11 } } },
            y: { display: false },
        },
    };

    const colorMap = {
        STOCK: '#36a2eb', MUTUAL_FUND: '#7c4dff', ETF: '#00bcd4',
        BOND: '#ffb347', GOLD: '#ffd700',
    };

    const iconColors = ['#36a2eb', '#ff6e40', '#7c4dff', '#00e5a0', '#ffb347', '#ff4c6a'];

    if (loading) {
        return (
            <>
                <Header title="Dashboard" />
                <div className="page-content"><div className="loading-spinner"><div className="spinner"></div></div></div>
            </>
        );
    }

    return (
        <>
            <Header
                title="Welcome back, Alex!"
                subtitle="Here's what's happening with your portfolio today."
                actions={
                    <>
                        <button className="btn btn-secondary" onClick={loadData}><RefreshCw size={14} /> Refresh</button>
                        <button className="btn btn-primary"><Plus size={14} /> Add Holding</button>
                    </>
                }
            />
            <div className="page-content">
                {/* KPI Cards */}
                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-header">
                            <span className="stat-label">Total Invested</span>
                            <div className="stat-icon" style={{ background: 'rgba(0,188,212,0.12)', color: '#00bcd4' }}>
                                <Wallet size={18} />
                            </div>
                        </div>
                        <div className="stat-value">{formatCurrency(totalInvested)}</div>
                        <div className="stat-sub">Since inception</div>
                    </div>

                    <div className="stat-card">
                        <div className="stat-header">
                            <span className="stat-label">Current Value</span>
                            <div className="stat-icon" style={{ background: 'rgba(0,229,160,0.12)', color: '#00e5a0' }}>
                                <TrendingUp size={18} />
                            </div>
                        </div>
                        <div className="stat-value">{formatCurrency(currentValue)}</div>
                        <div className="stat-sub">
                            <span className="stat-badge positive">↗ +1.2% Today</span>
                        </div>
                    </div>

                    <div className="stat-card">
                        <div className="stat-header">
                            <span className="stat-label">Total Gain</span>
                            <div className="stat-icon" style={{ background: totalGain >= 0 ? 'rgba(0,229,160,0.12)' : 'rgba(255,76,106,0.12)', color: totalGain >= 0 ? '#00e5a0' : '#ff4c6a' }}>
                                <ArrowUpRight size={18} />
                            </div>
                        </div>
                        <div className="stat-value" style={{ color: totalGain >= 0 ? '#00e5a0' : '#ff4c6a' }}>
                            {totalGain >= 0 ? '+' : ''}{formatCurrency(totalGain)}
                        </div>
                        <div className="stat-sub">Absolute Returns: {formatPercent(gainPercent)}</div>
                    </div>

                    <div className="stat-card">
                        <div className="stat-header">
                            <span className="stat-label">XIRR Rate</span>
                            <div className="stat-icon" style={{ background: 'rgba(124,77,255,0.12)', color: '#7c4dff' }}>
                                <Percent size={18} />
                            </div>
                        </div>
                        <div className="stat-value">{formatPercent(xirr)}</div>
                        <div className="stat-sub">Annualized Performance</div>
                    </div>
                </div>

                {/* Performance Chart */}
                <div className="card" style={{ marginBottom: 24 }}>
                    <div className="card-header">
                        <h3>Performance Overview</h3>
                        <div className="tab-group">
                            <button className="tab-btn">1W</button>
                            <button className="tab-btn active">1M</button>
                            <button className="tab-btn">6M</button>
                            <button className="tab-btn">1Y</button>
                            <button className="tab-btn">ALL</button>
                        </div>
                    </div>
                    <div style={{ height: 260 }}>
                        <Line data={chartData} options={chartOptions} />
                    </div>
                </div>

                {/* Holdings & Transactions */}
                <div className="two-col">
                    <div className="card">
                        <div className="card-header">
                            <h3>Top Holdings</h3>
                            <a href="/portfolio" className="btn-ghost">View All →</a>
                        </div>
                        <div className="data-table-wrapper">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Asset</th>
                                        <th>Invested</th>
                                        <th>Current</th>
                                        <th>Gain/Loss</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {holdings.length === 0 ? (
                                        <tr><td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 32 }}>No holdings yet. Add your first investment!</td></tr>
                                    ) : holdings.slice(0, 5).map((h, i) => (
                                        <tr key={h.id}>
                                            <td>
                                                <div className="asset-cell">
                                                    <div className="asset-icon" style={{ background: iconColors[i % iconColors.length] }}>
                                                        {h.assetSymbol?.[0] || '?'}
                                                    </div>
                                                    <div className="asset-info">
                                                        <div className="asset-name">{h.assetName}</div>
                                                        <div className="asset-sub">{h.assetSymbol} • {h.assetType}</div>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>{formatCurrency(h.totalInvested)}</td>
                                            <td>{formatCurrency(h.currentValue)}</td>
                                            <td className={h.unrealizedGain >= 0 ? 'gain-positive' : 'gain-negative'}>
                                                {h.unrealizedGain >= 0 ? '+' : ''}{formatCurrency(h.unrealizedGain)}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div className="card">
                        <div className="card-header">
                            <h3>Recent Transactions</h3>
                            <a href="/transactions" className="btn-ghost">View All →</a>
                        </div>
                        <div className="data-table-wrapper">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Date</th>
                                        <th>Asset</th>
                                        <th>Type</th>
                                        <th>Amount</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {transactions.length === 0 ? (
                                        <tr><td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 32 }}>No transactions yet.</td></tr>
                                    ) : transactions.slice(0, 5).map((tx) => (
                                        <tr key={tx.id}>
                                            <td style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                                                {new Date(tx.transactionDate).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}
                                            </td>
                                            <td>{tx.assetName || tx.assetSymbol}</td>
                                            <td>
                                                <span className={`badge badge-${tx.transactionType?.toLowerCase()}`}>
                                                    {tx.transactionType}
                                                </span>
                                            </td>
                                            <td style={{ fontWeight: 600 }}>{formatCurrency(tx.totalAmount)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                {/* Promo Banner */}
                <div className="promo-banner">
                    <div>
                        <h3>Optimize your portfolio</h3>
                        <p>Our AI-driven analysis suggests you could save ₹1,200 in fees.</p>
                    </div>
                    <button className="btn">Start Optimization</button>
                </div>
            </div>
        </>
    );
}
