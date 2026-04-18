import { useState, useEffect } from 'react';
import { TrendingUp, BarChart3, Building2, Clock, Download } from 'lucide-react';
import { Doughnut } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import Header from '../components/Header';
import { analyticsApi } from '../services/api';

ChartJS.register(ArcElement, Tooltip, Legend);

const fmt = (v) => {
    if (v == null) return '₹0';
    const n = Number(v);
    if (Math.abs(n) >= 100000) return '₹' + (n / 100000).toFixed(1) + 'L';
    return '₹' + n.toLocaleString('en-IN', { maximumFractionDigits: 0 });
};

export default function Analytics() {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [range, setRange] = useState('ALL');
    useEffect(() => {
        (async () => {
            try { const r = await analyticsApi.getByUser(); setData(r.data.data); } catch (e) { console.error(e); }
            setLoading(false);
        })();
    }, []);

    const xirr = data?.xirr || 0, cagr = data?.cagr || 0;
    const absRet = data?.absoluteReturn || data?.totalGain || 0;
    const days = data?.investmentDurationDays || 0;
    const assets = data?.assetWiseAnalytics || [];
    const gainers = data?.topGainers || [];
    const losers = data?.topLosers || [];
    const performers = [...gainers.map(g => ({ ...g, t: 'g' })), ...losers.map(l => ({ ...l, t: 'l' }))];
    const dc = ['#00e5a0', '#7c4dff', '#ffb347', '#ff4c6a', '#00bcd4', '#36a2eb'];

    if (loading) return (<><Header title="Financial Analytics" /><div className="page-content"><div className="loading-spinner"><div className="spinner"></div></div></div></>);

    return (
        <>
            <Header title="Financial Analytics" subtitle="Portfolio performance and asset breakdown"
                actions={<div className="tab-group">{['1Y', '3Y', '5Y', 'ALL'].map(r => <button key={r} className={`tab-btn ${range === r ? 'active' : ''}`} onClick={() => setRange(r)}>{r}</button>)}</div>} />
            <div className="page-content">
                <div className="stats-grid">
                    {[
                        { label: 'XIRR', val: Number(xirr).toFixed(2) + '%', sub: 'Internal Rate of Return', ic: TrendingUp, bg: 'rgba(0,229,160,0.12)', c: '#00e5a0' },
                        { label: 'CAGR', val: Number(cagr).toFixed(2) + '%', sub: 'Compounded Growth', ic: BarChart3, bg: 'rgba(0,188,212,0.12)', c: '#00bcd4' },
                        { label: 'Abs. Return', val: fmt(absRet), sub: 'Total Absolute Return', ic: Building2, bg: 'rgba(124,77,255,0.12)', c: '#7c4dff' },
                        { label: 'Days Invested', val: days.toLocaleString(), sub: 'Since Inception', ic: Clock, bg: 'rgba(255,179,71,0.12)', c: '#ffb347' },
                    ].map((s, i) => (
                        <div className="stat-card" key={i}>
                            <div className="stat-header"><span className="stat-label">{s.label}</span><div className="stat-icon" style={{ background: s.bg, color: s.c }}><s.ic size={18} /></div></div>
                            <div className="stat-value">{s.val}</div><div className="stat-sub">{s.sub}</div>
                        </div>
                    ))}
                </div>

                <div className="two-col" style={{ marginBottom: 24 }}>
                    <div className="card">
                        <div className="card-header"><h3>Asset Allocation</h3></div>
                        <div style={{ height: 260, display: 'flex', alignItems: 'center', justifyContent: 'center', position: 'relative' }}>
                            {assets.length > 0 ? (<><Doughnut data={{ labels: assets.map(a => a.assetType?.replace('_', ' ')), datasets: [{ data: assets.map(a => Number(a.allocation || 0)), backgroundColor: dc.slice(0, assets.length), borderWidth: 0, cutout: '72%' }] }} options={{ responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } } }} />
                                <div style={{ position: 'absolute', textAlign: 'center' }}><div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>PORTFOLIO</div><div style={{ fontSize: '1.75rem', fontWeight: 700, fontFamily: 'Outfit' }}>100%</div></div></>
                            ) : <p style={{ color: 'var(--text-muted)' }}>No data</p>}
                        </div>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 16, marginTop: 16 }}>
                            {assets.map((a, i) => <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 8 }}><div style={{ width: 10, height: 10, borderRadius: '50%', background: dc[i] }}></div><span style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>{a.assetType?.replace('_', ' ')}</span><span style={{ fontSize: '0.82rem', fontWeight: 600 }}>{Number(a.allocation || 0).toFixed(0)}%</span></div>)}
                        </div>
                    </div>

                    <div className="card">
                        <div className="card-header"><h3>Top Performers</h3></div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 20, marginTop: 8 }}>
                            {performers.length === 0 ? <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: 40 }}>No data</p> :
                                performers.slice(0, 5).map((p, i) => {
                                    const pct = Math.abs(Number(p.gainPercent || 0));
                                    return (<div key={i}><div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                                        <span style={{ fontWeight: 500, fontSize: '0.9rem' }}>{p.assetName || p.assetSymbol}</span>
                                        <span style={{ fontWeight: 600, color: p.t === 'g' ? '#00e5a0' : '#ff4c6a', fontSize: '0.85rem' }}>{p.t === 'g' ? '+' : '-'}{pct.toFixed(1)}%</span>
                                    </div><div className="progress-bar"><div className="progress-fill" style={{ width: `${Math.min(pct * 2, 100)}%`, background: p.t === 'g' ? '#00e5a0' : '#ff4c6a' }}></div></div></div>);
                                })
                            }
                        </div>
                    </div>
                </div>

                <div className="card">
                    <div className="card-header"><h3>Asset-wise Performance</h3><button className="btn btn-ghost"><Download size={14} /> CSV</button></div>
                    <div className="data-table-wrapper">
                        <table className="data-table"><thead><tr><th>Type</th><th>Invested</th><th>Current</th><th>Gain</th><th>%</th></tr></thead>
                            <tbody>{assets.length === 0 ? <tr><td colSpan="5" style={{ textAlign: 'center', padding: 32, color: 'var(--text-muted)' }}>No data</td></tr> :
                                assets.map((a, i) => <tr key={i}><td><div className="asset-cell"><div className="asset-icon" style={{ background: dc[i] }}><TrendingUp size={14} /></div><span style={{ fontWeight: 500 }}>{a.assetType?.replace('_', ' ')}</span></div></td><td>{fmt(a.invested)}</td><td>{fmt(a.currentValue)}</td><td className={Number(a.gain) >= 0 ? 'gain-positive' : 'gain-negative'}>{Number(a.gain) >= 0 ? '+' : ''}{fmt(a.gain)}</td><td><span className={`stat-badge ${Number(a.gainPercent) >= 0 ? 'positive' : 'negative'}`}>{Number(a.gainPercent || 0).toFixed(1)}%</span></td></tr>)
                            }</tbody></table>
                    </div>
                </div>
            </div>
        </>
    );
}
