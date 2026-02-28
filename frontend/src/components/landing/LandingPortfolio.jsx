import { motion } from 'framer-motion';
import { CheckCircle2 } from 'lucide-react';

const TerminalMockup = () => (
    <motion.div
        initial={{ rotate: -1.5 }}
        whileHover={{ rotate: 0 }}
        transition={{ duration: 0.6, ease: "easeOut" }}
        style={{
            background: '#0d1421',
            border: '1px solid rgba(16,185,129,0.2)',
            borderRadius: '16px',
            padding: '24px',
            fontFamily: 'var(--font-mono)',
            fontSize: '13px',
            boxShadow: '0 0 60px rgba(16,185,129,0.08), 0 40px 80px rgba(0,0,0,0.5)',
            color: 'var(--land-text-secondary)',
            cursor: 'default'
        }}
    >
        <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid rgba(255,255,255,0.06)', paddingBottom: '16px', marginBottom: '16px' }}>
            <span style={{ color: '#fff', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{ color: 'var(--land-accent-green)', fontSize: '16px' }}>◈</span> Portfolio Overview
            </span>
            <span>Feb 2026</span>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '24px' }}>
            <div>
                <div style={{ marginBottom: '4px' }}>Current Value</div>
                <div style={{ color: '#fff', fontSize: '18px' }}>₹14,82,340</div>
            </div>
            <div>
                <div style={{ marginBottom: '4px' }}>Total Invested</div>
                <div style={{ color: '#fff', fontSize: '18px' }}>₹11,00,000</div>
            </div>
            <div>
                <div style={{ marginBottom: '4px' }}>Absolute Return</div>
                <div style={{ color: 'var(--land-accent-green)' }}>+34.76% ↑</div>
            </div>
            <div>
                <div style={{ marginBottom: '4px' }}>XIRR</div>
                <div style={{ color: '#fff' }}>22.4%</div>
            </div>
        </div>

        <div style={{ borderTop: '1px solid rgba(255,255,255,0.06)', paddingTop: '16px', marginBottom: '24px' }}>
            <div style={{ color: '#fff', marginBottom: '16px', letterSpacing: '0.1em' }}>HOLDINGS</div>
            {[
                { n: 'RELIANCE', q: '×50', v: '₹2,87,500', r: '+18.2%' },
                { n: 'HDFC BANK', q: '×100', v: '₹1,66,200', r: '+12.5%' },
                { n: 'INFY', q: '×200', v: '₹3,68,400', r: '+42.1%' },
                { n: 'NIFTY50 ETF', q: '×500', v: '₹1,24,500', r: '+9.8%' }
            ].map((h, i) => (
                <div key={i} style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1.5fr 1fr', gap: '8px', marginBottom: '12px' }}>
                    <span style={{ color: '#fff' }}>{h.n}</span>
                    <span>{h.q}</span>
                    <span>{h.v}</span>
                    <span style={{ color: 'var(--land-accent-green)', textAlign: 'right' }}>{h.r} ↑</span>
                </div>
            ))}
        </div>

        <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid rgba(255,255,255,0.06)', paddingTop: '16px' }}>
            <button style={{ background: 'transparent', border: '1px solid rgba(255,255,255,0.1)', color: '#fff', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer', fontFamily: 'inherit' }}>
                [Add Transaction]
            </button>
            <button style={{ background: 'rgba(16,185,129,0.1)', border: '1px solid rgba(16,185,129,0.3)', color: 'var(--land-accent-green)', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer', fontFamily: 'inherit' }}>
                [Export Report →]
            </button>
        </div>
    </motion.div>
);

export default function LandingPortfolio() {
    const features = [
        "Real-time portfolio valuation",
        "XIRR & CAGR performance tracking",
        "FIFO-based capital gains calculator",
        "Transaction history with P&L breakdown",
        "Asset allocation analytics",
        "Export-ready reports"
    ];

    return (
        <section id="portfolio" style={{ padding: 'var(--section-pad-md)' }}>
            <div className="landing-container" style={{ display: 'flex', alignItems: 'center', gap: '80px' }}>

                {/* Left Copy */}
                <motion.div
                    style={{ width: '45%' }}
                    initial={{ opacity: 0, x: -40 }}
                    whileInView={{ opacity: 1, x: 0 }}
                    viewport={{ once: true }}
                    transition={{ duration: 0.7 }}
                >
                    <div style={{ fontSize: '13px', color: 'var(--land-text-secondary)', letterSpacing: '0.1em', textTransform: 'uppercase', marginBottom: '16px', fontWeight: 600 }}>
                        — Beyond Calculators —
                    </div>
                    <h2 className="font-display" style={{ fontSize: '48px', fontWeight: 700, lineHeight: 1.1, marginBottom: '24px' }}>
                        Track Every Rupee.<br />
                        Own Every Decision.
                    </h2>
                    <p style={{ color: 'var(--land-text-secondary)', fontSize: '18px', lineHeight: 1.7, marginBottom: '40px' }}>
                        MoneyMatters isn't just a calculator — it's your complete financial intelligence platform. Track live holdings, record transactions, calculate XIRR returns, and analyse capital gains with FIFO precision. Everything your CA does. Now in your hands.
                    </p>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                        {features.map((feat, i) => (
                            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '12px', color: '#fff', fontSize: '16px' }}>
                                <CheckCircle2 size={20} color="var(--land-accent-green)" />
                                {feat}
                            </div>
                        ))}
                    </div>
                </motion.div>

                {/* Right Terminal Mockup */}
                <motion.div
                    style={{ width: '55%', perspective: '1000px' }}
                    initial={{ opacity: 0, scale: 0.95 }}
                    whileInView={{ opacity: 1, scale: 1 }}
                    viewport={{ once: true }}
                    transition={{ duration: 0.7, delay: 0.2 }}
                >
                    <TerminalMockup />
                </motion.div>

            </div>
        </section>
    );
}
