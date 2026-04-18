import { useState, useEffect } from 'react';
import {
    Building2, TrendingUp, Wallet, ArrowUpRight,
    Search, Plus, RefreshCw, MoreVertical,
    Edit2, Trash2, X, ArrowUp, ArrowDown,
} from 'lucide-react';
import Header from '../components/Header';
import { holdingsApi } from '../services/api';
import { useToast } from '../components/Toast';
import nifty500 from '../assets/nifty500.json';

const formatCurrency = (v) => {
    if (v == null) return '₹0';
    return '₹' + Number(v).toLocaleString('en-IN', { maximumFractionDigits: 2 });
};

const ASSET_TYPES = ['ALL', 'STOCK', 'MUTUAL_FUND', 'ETF', 'BOND', 'GOLD'];
const iconColors = ['#36a2eb', '#ff6e40', '#7c4dff', '#00e5a0', '#ffb347', '#ff4c6a', '#00bcd4'];

export default function Portfolio() {
    const [holdings, setHoldings] = useState([]);
    const [summary, setSummary] = useState(null);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [filterType, setFilterType] = useState('ALL');
    const [currentPage, setCurrentPage] = useState(1);
    const [showModal, setShowModal] = useState(false);
    const [editingHolding, setEditingHolding] = useState(null);
    const [sortKey, setSortKey] = useState(null);
    const [sortDir, setSortDir] = useState('asc');
    const toast = useToast();
    const [form, setForm] = useState({
        assetType: 'STOCK', assetName: '', assetSymbol: '',
        exchange: 'NSE', quantity: '', avgBuyPrice: '', purchaseDate: '',
    });
    const [showSuggestions, setShowSuggestions] = useState(false);
    const [suggestions, setSuggestions] = useState([]);
    const [activeField, setActiveField] = useState(null);
    
    const perPage = 6;

    useEffect(() => { loadData(); }, []);

    const loadData = async () => {
        setLoading(true);
        try {
            const [hRes, sRes] = await Promise.allSettled([
                holdingsApi.getByUser(),
                holdingsApi.getSummary(),
            ]);
            if (hRes.status === 'fulfilled') setHoldings(hRes.value.data.data || []);
            if (sRes.status === 'fulfilled') setSummary(sRes.value.data.data);
        } catch (e) { console.error(e); }
        setLoading(false);
    };

    const filtered = holdings.filter((h) => {
        const matchSearch = h.assetName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            h.assetSymbol?.toLowerCase().includes(searchTerm.toLowerCase());
        const matchType = filterType === 'ALL' || h.assetType === filterType;
        return matchSearch && matchType;
    });

    const sorted = [...filtered].sort((a, b) => {
        if (!sortKey) return 0;
        let va = a[sortKey], vb = b[sortKey];
        if (typeof va === 'string') { va = va.toLowerCase(); vb = (vb || '').toLowerCase(); }
        if (va < vb) return sortDir === 'asc' ? -1 : 1;
        if (va > vb) return sortDir === 'asc' ? 1 : -1;
        return 0;
    });

    const totalPages = Math.ceil(sorted.length / perPage);
    const paged = sorted.slice((currentPage - 1) * perPage, currentPage * perPage);

    const toggleSort = (key) => {
        if (sortKey === key) setSortDir(d => d === 'asc' ? 'desc' : 'asc');
        else { setSortKey(key); setSortDir('asc'); }
    };

    const SortIcon = ({ col }) => (
        <span className={`sort-icon ${sortKey === col ? 'active' : ''}`}>
            {sortKey === col ? (sortDir === 'asc' ? <ArrowUp size={12} /> : <ArrowDown size={12} />) : '⇅'}
        </span>
    );

    const handleRefreshAll = async () => {
        try { await holdingsApi.refreshAll(); loadData(); toast.success('Prices refreshed'); } catch (e) { console.error(e); toast.error('Failed to refresh prices'); }
    };

    const openAdd = () => {
        setEditingHolding(null);
        setForm({ assetType: 'STOCK', assetName: '', assetSymbol: '', exchange: 'NSE', quantity: '', avgBuyPrice: '', purchaseDate: '' });
        setShowModal(true);
    };

    const openEdit = (h) => {
        setEditingHolding(h);
        setForm({
            assetType: h.assetType, assetName: h.assetName,
            assetSymbol: h.assetSymbol, exchange: h.exchange, quantity: h.quantity,
            avgBuyPrice: h.avgBuyPrice, purchaseDate: h.purchaseDate || '',
        });
        setShowModal(true);
    };

    const handleSearchInput = (e, field) => {
        const val = e.target.value;
        setForm({ ...form, [field]: val });
        setActiveField(field);
        
        if (val.length > 0) {
            const lower=val.toLowerCase();
            const filtered = nifty500.filter(item => 
                (item.Symbol && item.Symbol.toLowerCase().includes(lower)) || 
                (item['Company Name'] && item['Company Name'].toLowerCase().includes(lower))
            ).slice(0, 10);
            
            setSuggestions(filtered);
            setShowSuggestions(true);
        } else {
            setShowSuggestions(false);
            setSuggestions([]);
        }
    };

    const handleSuggestionClick = (s) => {
        setForm({
            ...form,
            assetSymbol: s.Symbol,
            assetName: s['Company Name'],
            exchange: 'NSE'
        });
        setShowSuggestions(false);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const payload = { ...form, quantity: Number(form.quantity), avgBuyPrice: Number(form.avgBuyPrice) };
        try {
            if (editingHolding) {
                await holdingsApi.update(editingHolding.id, payload);
            } else {
                await holdingsApi.create(payload);
            }
            setShowModal(false);
            toast.success(editingHolding ? 'Holding updated!' : 'Holding added!');
            loadData();
        } catch (e) { console.error(e); toast.error('Error saving holding'); }
    };

    const handleDelete = async (id) => {
        if (!confirm('Delete this holding?')) return;
        try { await holdingsApi.delete(id); toast.success('Holding deleted'); loadData(); } catch (e) { console.error(e); toast.error('Failed to delete'); }
    };

    const totalInvested = summary?.totalInvested || 0;
    const currentValue = summary?.totalCurrentValue || 0;
    const totalGain = summary?.totalUnrealizedGain || 0;
    const gainPercent = summary?.totalUnrealizedGainPercent || 0;

    return (
        <>
            <Header
                title="Portfolio Holdings"
                subtitle="Manage your asset allocation and performance"
                actions={
                    <>
                        <button className="btn btn-secondary" onClick={handleRefreshAll}><RefreshCw size={14} /> Refresh Prices</button>
                    </>
                }
            />
            <div className="page-content">
                {/* Stats */}
                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-header">
                            <span className="stat-label">Total Portfolio Value</span>
                            <div className="stat-icon" style={{ background: 'rgba(0,188,212,0.12)', color: '#00bcd4' }}><Building2 size={18} /></div>
                        </div>
                        <div className="stat-value">{formatCurrency(currentValue)}</div>
                        <span className="stat-badge positive">↑ {formatCurrency(Math.abs(totalGain))} today</span>
                    </div>
                    <div className="stat-card">
                        <div className="stat-header">
                            <span className="stat-label">Total Profit</span>
                            <div className="stat-icon" style={{ background: totalGain >= 0 ? 'rgba(0,229,160,0.12)' : 'rgba(255,76,106,0.12)', color: totalGain >= 0 ? '#00e5a0' : '#ff4c6a' }}><TrendingUp size={18} /></div>
                        </div>
                        <div className="stat-value" style={{ color: totalGain >= 0 ? '#00e5a0' : '#ff4c6a' }}>
                            {totalGain >= 0 ? '+' : ''}{formatCurrency(totalGain)}
                        </div>
                        <div className="stat-sub">All Time</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-header">
                            <span className="stat-label">Invested Capital</span>
                            <div className="stat-icon" style={{ background: 'rgba(124,77,255,0.12)', color: '#7c4dff' }}><Wallet size={18} /></div>
                        </div>
                        <div className="stat-value">{formatCurrency(totalInvested)}</div>
                        <div className="stat-sub">Principal</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-header">
                            <span className="stat-label">Returns</span>
                            <div className="stat-icon" style={{ background: 'rgba(0,229,160,0.12)', color: '#00e5a0' }}><ArrowUpRight size={18} /></div>
                        </div>
                        <div className="stat-value">{gainPercent >= 0 ? '+' : ''}{Number(gainPercent).toFixed(2)}%</div>
                        <div className="stat-sub">Overall return</div>
                    </div>
                </div>

                {/* Filter Bar */}
                <div className="filter-bar">
                    <div className="search-box">
                        <Search size={16} className="icon" />
                        <input
                            placeholder="Search holdings..."
                            value={searchTerm}
                            onChange={(e) => { setSearchTerm(e.target.value); setCurrentPage(1); }}
                        />
                    </div>
                    <select className="select-field" style={{ width: 180 }} value={filterType} onChange={(e) => { setFilterType(e.target.value); setCurrentPage(1); }}>
                        {ASSET_TYPES.map((t) => <option key={t} value={t}>{t === 'ALL' ? 'All Asset Types' : t.replace('_', ' ')}</option>)}
                    </select>
                    <button className="btn btn-primary" onClick={openAdd} id="add-holding-btn"><Plus size={16} /> Add Holding</button>
                </div>

                {/* Holdings Table */}
                <div className="card">
                    {loading ? (
                        <div className="loading-spinner"><div className="spinner"></div></div>
                    ) : (
                        <>
                            <div className="data-table-wrapper">
                                <table className="data-table" id="holdings-table">
                                    <thead>
                                        <tr>
                                            <th className="sortable-header" onClick={() => toggleSort('assetSymbol')}>Symbol / Name <SortIcon col="assetSymbol" /></th>
                                            <th className="sortable-header" onClick={() => toggleSort('quantity')}>Quantity <SortIcon col="quantity" /></th>
                                            <th className="sortable-header" onClick={() => toggleSort('avgBuyPrice')}>Avg Buy Price <SortIcon col="avgBuyPrice" /></th>
                                            <th className="sortable-header" onClick={() => toggleSort('currentPrice')}>Current Price <SortIcon col="currentPrice" /></th>
                                            <th className="sortable-header" onClick={() => toggleSort('currentValue')}>Current Value <SortIcon col="currentValue" /></th>
                                            <th className="sortable-header" onClick={() => toggleSort('unrealizedGain')}>Total Gain/Loss <SortIcon col="unrealizedGain" /></th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {paged.length === 0 ? (
                                            <tr><td colSpan="7" style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No holdings found. Click "Add Holding" to start.</td></tr>
                                        ) : paged.map((h, i) => {
                                            const gain = h.unrealizedGain || 0;
                                            const gainPct = h.unrealizedGainPercent || 0;
                                            return (
                                                <tr key={h.id}>
                                                    <td>
                                                        <div className="asset-cell">
                                                            <div className="asset-icon" style={{ background: iconColors[i % iconColors.length] }}>
                                                                {h.assetSymbol?.[0]}
                                                            </div>
                                                            <div className="asset-info">
                                                                <div className="asset-name">{h.assetSymbol}</div>
                                                                <div className="asset-sub">{h.assetName}</div>
                                                            </div>
                                                        </div>
                                                    </td>
                                                    <td>{Number(h.quantity).toFixed(2)}</td>
                                                    <td>{formatCurrency(h.avgBuyPrice)}</td>
                                                    <td>{formatCurrency(h.currentPrice)}</td>
                                                    <td>{formatCurrency(h.currentValue)}</td>
                                                    <td>
                                                        <div className={gain >= 0 ? 'gain-positive' : 'gain-negative'}>
                                                            {gain >= 0 ? '+' : ''}{formatCurrency(gain)}
                                                            <div style={{ fontSize: '0.75rem' }}>{gain >= 0 ? '+' : ''}{Number(gainPct).toFixed(2)}%</div>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <div style={{ display: 'flex', gap: 6 }}>
                                                            <button className="btn-icon" onClick={() => openEdit(h)} title="Edit"><Edit2 size={14} /></button>
                                                            <button className="btn-icon" onClick={() => handleDelete(h.id)} title="Delete" style={{ borderColor: 'rgba(255,76,106,0.3)', color: '#ff4c6a' }}><Trash2 size={14} /></button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            );
                                        })}
                                    </tbody>
                                </table>
                            </div>

                            {totalPages > 1 && (
                                <div className="pagination">
                                    <div className="page-info">Showing <span>{(currentPage - 1) * perPage + 1}</span> to <span>{Math.min(currentPage * perPage, filtered.length)}</span> of <span>{filtered.length}</span> results</div>
                                    <div className="page-buttons">
                                        <button className="page-btn" disabled={currentPage === 1} onClick={() => setCurrentPage(p => p - 1)}>‹</button>
                                        {Array.from({ length: totalPages }, (_, i) => (
                                            <button key={i + 1} className={`page-btn ${currentPage === i + 1 ? 'active' : ''}`} onClick={() => setCurrentPage(i + 1)}>{i + 1}</button>
                                        ))}
                                        <button className="page-btn" disabled={currentPage === totalPages} onClick={() => setCurrentPage(p => p + 1)}>›</button>
                                    </div>
                                </div>
                            )}
                        </>
                    )}
                </div>

                {/* Add/Edit Modal */}
                {showModal && (
                    <div className="modal-overlay" onClick={() => setShowModal(false)}>
                        <div className="modal" onClick={(e) => e.stopPropagation()}>
                            <div className="modal-header">
                                <h2>{editingHolding ? 'Edit Holding' : 'Add New Holding'}</h2>
                                <button className="btn-icon" onClick={() => setShowModal(false)}><X size={18} /></button>
                            </div>
                            <form onSubmit={handleSubmit}>
                                <div className="input-group">
                                    <label>Asset Type</label>
                                    <select className="select-field" value={form.assetType} onChange={e => {
                                        const newType = e.target.value;
                                        if (newType === 'GOLD') {
                                            setForm({ ...form, assetType: newType, assetName: 'Gold', assetSymbol: 'GOLD', exchange: 'NONE' });
                                        } else {
                                            setForm({ ...form, assetType: newType });
                                        }
                                    }}>
                                        {['STOCK', 'MUTUAL_FUND', 'ETF', 'BOND', 'GOLD'].map(t => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}
                                    </select>
                                </div>
                                {form.assetType !== 'GOLD' && (
                                    <>
                                        <div className="input-group autocomplete-container">
                                    <label>Asset Name</label>
                                    <input 
                                        className="input-field" 
                                        placeholder="e.g. Reliance Industries" 
                                        value={form.assetName} 
                                        onChange={e => handleSearchInput(e, 'assetName')} 
                                        onFocus={() => {
                                            setActiveField('assetName');
                                            if(suggestions.length > 0) setShowSuggestions(true);
                                        }}
                                        onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
                                        required 
                                    />
                                    {showSuggestions && activeField === 'assetName' && suggestions.length > 0 && (
                                        <ul className="autocomplete-dropdown">
                                            {suggestions.map(s => (
                                                <li key={s.Symbol} className="autocomplete-item" onMouseDown={() => handleSuggestionClick(s)}>
                                                    <span className="autocomplete-item-symbol">{s.Symbol}</span>
                                                    <span className="autocomplete-item-name">{s['Company Name']}</span>
                                                </li>
                                            ))}
                                        </ul>
                                    )}
                                </div>
                                <div className="input-group autocomplete-container">
                                    <label>Symbol</label>
                                    <input 
                                        className="input-field" 
                                        placeholder="e.g. RELIANCE" 
                                        value={form.assetSymbol} 
                                        onChange={e => handleSearchInput(e, 'assetSymbol')} 
                                        onFocus={() => {
                                            setActiveField('assetSymbol');
                                            if(suggestions.length > 0) setShowSuggestions(true);
                                        }}
                                        onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
                                        required 
                                    />
                                    {showSuggestions && activeField === 'assetSymbol' && suggestions.length > 0 && (
                                        <ul className="autocomplete-dropdown">
                                            {suggestions.map(s => (
                                                <li key={s.Symbol} className="autocomplete-item" onMouseDown={() => handleSuggestionClick(s)}>
                                                    <span className="autocomplete-item-symbol">{s.Symbol}</span>
                                                    <span className="autocomplete-item-name">{s['Company Name']}</span>
                                                </li>
                                            ))}
                                        </ul>
                                    )}
                                        </div>
                                    </>
                                )}
                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                                    {form.assetType !== 'GOLD' && (
                                        <div className="input-group">
                                        <label>Exchange</label>
                                        <select className="select-field" value={form.exchange} onChange={e => setForm({ ...form, exchange: e.target.value })}>
                                            <option value="NSE">NSE</option>
                                            <option value="BSE">BSE</option>
                                        </select>
                                    </div>
                                    )}
                                    <div className="input-group" style={form.assetType === 'GOLD' ? { gridColumn: '1 / -1' } : {}}>
                                        <label>Purchase Date</label>
                                        <input className="input-field" type="date" value={form.purchaseDate} onChange={e => setForm({ ...form, purchaseDate: e.target.value })} />
                                    </div>
                                </div>
                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                                    <div className="input-group">
                                        <label>Quantity</label>
                                        <input className="input-field" type="number" step="0.01" placeholder="10" value={form.quantity} onChange={e => setForm({ ...form, quantity: e.target.value })} required />
                                    </div>
                                    <div className="input-group">
                                        <label>Avg Buy Price (₹)</label>
                                        <input className="input-field" type="number" step="0.01" placeholder="2800.50" value={form.avgBuyPrice} onChange={e => setForm({ ...form, avgBuyPrice: e.target.value })} required />
                                    </div>
                                </div>
                                <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
                                    <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowModal(false)}>Cancel</button>
                                    <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>{editingHolding ? 'Update Holding' : 'Add Holding'}</button>
                                </div>
                            </form>
                        </div>
                    </div>
                )}
            </div>
        </>
    );
}
