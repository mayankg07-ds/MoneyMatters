export default function LandingFooter() {
    return (
        <footer style={{
            borderTop: '1px solid var(--land-border-subtle)',
            padding: '80px 0 40px 0',
            background: 'var(--land-bg-primary)'
        }}>
            <div className="landing-container">
                <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr 1fr', gap: '40px', marginBottom: '80px' }}>

                    {/* Brand Col */}
                    <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
                            <span style={{ color: '#10b981', fontSize: '24px', lineHeight: 1 }}>◈</span>
                            <span className="font-display" style={{ fontWeight: 700, fontSize: '20px', color: '#fff', letterSpacing: '0.5px' }}>
                                MoneyMatters
                            </span>
                        </div>
                        <p style={{ color: 'var(--land-text-secondary)', fontSize: '14px' }}>
                            Smart Financial Intelligence
                        </p>
                    </div>

                    {/* Links 1 */}
                    <div>
                        <div style={{ color: '#fff', fontWeight: 600, marginBottom: '20px', fontSize: '14px' }}>Calculators</div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            {["SIP Calculator", "EMI / Loan", "Retirement Planner", "FD / RD / PPF"].map(l => (
                                <a key={l} href="#" style={{ color: 'var(--land-text-secondary)', fontSize: '14px', textDecoration: 'none' }}>{l}</a>
                            ))}
                        </div>
                    </div>

                    {/* Links 2 */}
                    <div>
                        <div style={{ color: '#fff', fontWeight: 600, marginBottom: '20px', fontSize: '14px' }}>Portfolio</div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            {["Holdings", "Transactions", "Analytics", "XIRR Tracker"].map(l => (
                                <a key={l} href="#" style={{ color: 'var(--land-text-secondary)', fontSize: '14px', textDecoration: 'none' }}>{l}</a>
                            ))}
                        </div>
                    </div>

                    {/* Links 3 */}
                    <div>
                        <div style={{ color: '#fff', fontWeight: 600, marginBottom: '20px', fontSize: '14px' }}>Company</div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            {["About", "GitHub", "Contact"].map(l => (
                                <a key={l} href="#" style={{ color: 'var(--land-text-secondary)', fontSize: '14px', textDecoration: 'none' }}>{l}</a>
                            ))}
                        </div>
                    </div>

                </div>

                {/* Copyright Bar */}
                <div style={{
                    borderTop: '1px solid var(--land-border-subtle)',
                    paddingTop: '32px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    color: 'var(--land-text-muted)',
                    fontSize: '13px'
                }}>
                    <div>© {new Date().getFullYear()} MoneyMatters · <a href="#" style={{ color: 'inherit', textDecoration: 'none' }}>Privacy Policy</a> · <a href="#" style={{ color: 'inherit', textDecoration: 'none' }}>Terms of Service</a></div>
                    <div>Built with precision. Designed for wealth.</div>
                </div>
            </div>
        </footer>
    );
}
