import './LandingTicker.css';

const TICKER_ITEMS = [
    "SIP Calculator",
    "Home Loan EMI",
    "XIRR Tracker",
    "Retirement Planner",
    "Portfolio Analytics",
    "FD Calculator",
    "Step-Up SIP",
    "Capital Gains (FIFO)",
    "PPF Calculator"
];

export default function LandingTicker() {
    return (
        <div className="ticker-wrapper" style={{
            background: 'rgba(16,185,129,0.06)',
            borderTop: '1px solid rgba(16,185,129,0.15)',
            borderBottom: '1px solid rgba(16,185,129,0.15)',
            padding: '14px 0',
            overflow: 'hidden',
            display: 'flex',
            whiteSpace: 'nowrap'
        }}>
            <div className="ticker-track font-mono">
                {/* Duplicate items for seamless loop */}
                {[...TICKER_ITEMS, ...TICKER_ITEMS, ...TICKER_ITEMS].map((item, index) => (
                    <span key={index} className="ticker-item">
                        <span style={{ color: 'var(--land-accent-gold)', margin: '0 12px 0 24px' }}>✦</span>
                        {item}
                        <span style={{ color: 'rgba(16,185,129,0.3)', margin: '0 0 0 24px' }}>·</span>
                    </span>
                ))}
            </div>
        </div>
    );
}
