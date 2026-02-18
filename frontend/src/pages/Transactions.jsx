import { useState, useEffect } from 'react';
import { Search, Plus, Download, Wallet, TrendingUp, DollarSign, X } from 'lucide-react';
import Header from '../components/Header';
import { transactionsApi } from '../services/api';

const fmt = (v) => {
    if (v == null) return '₹0';
    return '₹' + Number(v).toLocaleString('en-IN', { maximumFractionDigits: 2 });
};

export default function Transactions() {
    const [txs, setTxs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [typeFilter, setTypeFilter] = useState('ALL');
    const [page, setPage] = useState(1);
    const [showModal, setShowModal] = useState(false);
    const [form, setForm] = useState({
        userId: 1, transactionType: 'BUY', assetType: 'STOCK', assetName: '',
        assetSymbol: '', exchange: 'NSE', quantity: '', pricePerUnit: '',
        charges: '0', transactionDate: '', notes: '',
    });
    const perPage = 8;
    const uid = localStorage.getItem('userId') || '1';

    useEffect(() => { load(); }, []);

    const load = async () => {
        setLoading(true);
        try {
            const r = await transactionsApi.getByUser(uid);
            setTxs(r.data.data || []);
        } catch (e) { console.error(e); }
        setLoading(false);
    };

    const filtered = txs.filter(t => {
        const ms = (t.assetName || '').toLowerCase().includes(search.toLowerCase()) ||
            (t.assetSymbol || '').toLowerCase().includes(search.toLowerCase());
        const mt = typeFilter === 'ALL' || t.transactionType === typeFilter;
        return ms && mt;
    });

    const totalPages = Math.ceil(filtered.length / perPage);
    const paged = filtered.slice((page - 1) * perPage, page * perPage);

    const totalInvested = txs.filter(t => t.transactionType === 'BUY').reduce((s, t) => s + Number(t.totalAmount || 0), 0);
    const totalSold = txs.filter(t => t.transactionType === 'SELL').reduce((s, t) => s + Number(t.totalAmount || 0), 0);
    const dividends = txs.filter(t => t.transactionType === 'DIVIDEND').reduce((s, t) => s + Number(t.totalAmount || 0), 0);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await transactionsApi.record({
                ...form, userId: Number(uid),
                quantity: Number(form.quantity), pricePerUnit: Number(form.pricePerUnit),
                charges: Number(form.charges || 0),
            });
            setShowModal(false);
            load();
        } catch (e) { console.error(e); alert('Error recording transaction'); }
    };

    const handleDelete = async (id) => {
        if (!confirm('Delete this transaction?')) return;
        try { await transactionsApi.delete(id); load(); } catch (e) { console.error(e); }
    };

    return (
        <>
            <Header title="Transaction History" subtitle="Complete record of your trades" />
            <div className="page-content">
                <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
                    <div className="stat-card">
                        <div className="stat-header"><span className="stat-label">Total Bought</span><div className="stat-icon" style={{ background: 'rgba(0,229,160,0.12)', color: '#00e5a0' }}><Wallet size={18} /></div></div>
                        <div className="stat-value">{fmt(totalInvested)}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-header"><span className="stat-label">Total Sold</span><div className="stat-icon" style={{ background: 'rgba(255,76,106,0.12)', color: '#ff4c6a' }}><TrendingUp size={18} /></div></div>
                        <div className="stat-value">{fmt(totalSold)}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-header"><span className="stat-label">Dividends</span><div className="stat-icon" style={{ background: 'rgba(0,188,212,0.12)', color: '#00bcd4' }}><DollarSign size={18} /></div></div>
                        <div className="stat-value">{fmt(dividends)}</div>
                    </div>
                </div>

                <div className="filter-bar">
                    <div className="search-box"><Search size={16} className="icon" /><input placeholder="Search transactions..." value={search} onChange={e => { setSearch(e.target.value); setPage(1); }} /></div>
                    <select className="select-field" style={{ width: 160 }} value={typeFilter} onChange={e => { setTypeFilter(e.target.value); setPage(1); }}>
                        <option value="ALL">All Types</option>
                        <option value="BUY">Buy</option>
                        <option value="SELL">Sell</option>
                        <option value="DIVIDEND">Dividend</option>
                        <option value="SIP">SIP</option>
                    </select>
                    <button className="btn btn-secondary"><Download size={14} /> Export</button>
                    <button className="btn btn-primary" onClick={() => setShowModal(true)} id="add-tx-btn"><Plus size={16} /> New Transaction</button>
                </div>

                <div className="card">
                    {loading ? <div className="loading-spinner"><div className="spinner"></div></div> : (
                        <>
                            <div className="data-table-wrapper">
                                <table className="data-table" id="transactions-table">
                                    <thead><tr><th>Date</th><th>Type</th><th>Asset</th><th>Qty</th><th>Price</th><th>Charges</th><th>Net Amount</th><th></th></tr></thead>
                                    <tbody>
                                        {paged.length === 0 ? <tr><td colSpan="8" style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No transactions found.</td></tr> :
                                            paged.map(tx => (
                                                <tr key={tx.id}>
                                                    <td style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>{new Date(tx.transactionDate).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}</td>
                                                    <td><span className={`badge badge-${tx.transactionType?.toLowerCase()}`}>{tx.transactionType}</span></td>
                                                    <td><div className="asset-info"><div className="asset-name">{tx.assetSymbol}</div><div className="asset-sub">{tx.assetName}</div></div></td>
                                                    <td>{Number(tx.quantity).toFixed(2)}</td>
                                                    <td>{fmt(tx.pricePerUnit)}</td>
                                                    <td style={{ color: 'var(--text-muted)' }}>{fmt(tx.charges)}</td>
                                                    <td style={{ fontWeight: 600 }}>{fmt(tx.totalAmount)}</td>
                                                    <td><button className="btn-icon" onClick={() => handleDelete(tx.id)} style={{ borderColor: 'rgba(255,76,106,0.3)', color: '#ff4c6a' }}>×</button></td>
                                                </tr>
                                            ))}
                                    </tbody>
                                </table>
                            </div>
                            {totalPages > 1 && (
                                <div className="pagination">
                                    <div className="page-info">Showing <span>{(page - 1) * perPage + 1}</span>-<span>{Math.min(page * perPage, filtered.length)}</span> of <span>{filtered.length}</span></div>
                                    <div className="page-buttons">
                                        <button className="page-btn" disabled={page === 1} onClick={() => setPage(p => p - 1)}>‹</button>
                                        {Array.from({ length: totalPages }, (_, i) => <button key={i + 1} className={`page-btn ${page === i + 1 ? 'active' : ''}`} onClick={() => setPage(i + 1)}>{i + 1}</button>)}
                                        <button className="page-btn" disabled={page === totalPages} onClick={() => setPage(p => p + 1)}>›</button>
                                    </div>
                                </div>
                            )}
                        </>
                    )}
                </div>

                {showModal && (
                    <div className="modal-overlay" onClick={() => setShowModal(false)}>
                        <div className="modal" onClick={e => e.stopPropagation()}>
                            <div className="modal-header"><h2>New Transaction</h2><button className="btn-icon" onClick={() => setShowModal(false)}><X size={18} /></button></div>
                            <form onSubmit={handleSubmit}>
                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                                    <div className="input-group"><label>Type</label><select className="select-field" value={form.transactionType} onChange={e => setForm({ ...form, transactionType: e.target.value })}><option value="BUY">Buy</option><option value="SELL">Sell</option><option value="DIVIDEND">Dividend</option><option value="SIP">SIP</option></select></div>
                                    <div className="input-group"><label>Asset Type</label><select className="select-field" value={form.assetType} onChange={e => setForm({ ...form, assetType: e.target.value })}>{['STOCK', 'MUTUAL_FUND', 'ETF', 'BOND', 'GOLD'].map(t => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}</select></div>
                                </div>
                                <div className="input-group"><label>Asset Name</label><input className="input-field" value={form.assetName} onChange={e => setForm({ ...form, assetName: e.target.value })} required /></div>
                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                                    <div className="input-group"><label>Symbol</label><input className="input-field" value={form.assetSymbol} onChange={e => setForm({ ...form, assetSymbol: e.target.value })} required /></div>
                                    <div className="input-group"><label>Exchange</label><select className="select-field" value={form.exchange} onChange={e => setForm({ ...form, exchange: e.target.value })}><option value="NSE">NSE</option><option value="BSE">BSE</option></select></div>
                                </div>
                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 12 }}>
                                    <div className="input-group"><label>Quantity</label><input className="input-field" type="number" step="0.01" value={form.quantity} onChange={e => setForm({ ...form, quantity: e.target.value })} required /></div>
                                    <div className="input-group"><label>Price</label><input className="input-field" type="number" step="0.01" value={form.pricePerUnit} onChange={e => setForm({ ...form, pricePerUnit: e.target.value })} required /></div>
                                    <div className="input-group"><label>Charges</label><input className="input-field" type="number" step="0.01" value={form.charges} onChange={e => setForm({ ...form, charges: e.target.value })} /></div>
                                </div>
                                <div className="input-group"><label>Date</label><input className="input-field" type="date" value={form.transactionDate} onChange={e => setForm({ ...form, transactionDate: e.target.value })} required /></div>
                                <div className="input-group"><label>Notes</label><input className="input-field" value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} /></div>
                                <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
                                    <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowModal(false)}>Cancel</button>
                                    <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>Record</button>
                                </div>
                            </form>
                        </div>
                    </div>
                )}
            </div>
        </>
    );
}
