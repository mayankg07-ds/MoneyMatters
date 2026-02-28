import { motion } from 'framer-motion';
import { LineChart, Home, Briefcase, Landmark, RefreshCw, FileText, ArrowRight } from 'lucide-react';

const calculators = [
    {
        icon: <LineChart size={24} color="var(--land-accent-green)" />,
        name: "SIP Step-Up Calculator",
        desc: "Plan your wealth with increasing monthly SIPs that grow with your income.",
        formula: "FVA = PMT × [(1+r)ⁿ-1]/r"
    },
    {
        icon: <Home size={24} color="var(--land-accent-green)" />,
        name: "Loan Analyzer (Smart EMI)",
        desc: "Know every rupee of your loan — EMI, amortization, total interest, and more.",
        formula: "EMI = P[r(1+r)ⁿ] / [(1+r)ⁿ-1]"
    },
    {
        icon: <Briefcase size={24} color="var(--land-accent-green)" />,
        name: "Retirement Planner",
        desc: "Model your retirement corpus with inflation-adjusted goals.",
        formula: "PVA = PMT × [1-(1+r)⁻ⁿ]/r"
    },
    {
        icon: <Landmark size={24} color="var(--land-accent-green)" />,
        name: "FD Calculator",
        desc: "See exactly what your fixed deposit earns with quarterly compounding.",
        formula: "A = P(1+r/n)^(nt)"
    },
    {
        icon: <RefreshCw size={24} color="var(--land-accent-green)" />,
        name: "RD Calculator",
        desc: "Calculate recurring deposit maturity with monthly compounding.",
        formula: "FVA = PMT[(1+r)ⁿ-1]/r"
    },
    {
        icon: <FileText size={24} color="var(--land-accent-green)" />,
        name: "PPF Calculator",
        desc: "Model your PPF maturity with the Section 80C tax advantage.",
        formula: "Fixed 7.1% p.a. Compounded Yrly"
    }
];

export default function LandingCalculators() {
    return (
        <section id="calculators" style={{ padding: 'var(--section-pad-md)' }}>
            <div className="landing-container">
                <div style={{ textAlign: 'center', marginBottom: '64px' }}>
                    <div style={{ fontSize: '13px', color: 'var(--land-accent-gold)', letterSpacing: '0.1em', textTransform: 'uppercase', marginBottom: '16px', fontWeight: 600 }}>
                        — Our Tools —
                    </div>
                    <motion.h2
                        initial={{ opacity: 0, y: 20 }}
                        whileInView={{ opacity: 1, y: 0 }}
                        viewport={{ once: true }}
                        className="font-display"
                        style={{ fontSize: '48px', fontWeight: 700, lineHeight: 1.1, marginBottom: '24px' }}
                    >
                        Six Calculators.<br />
                        Infinite Clarity.
                    </motion.h2>
                    <motion.p
                        initial={{ opacity: 0, y: 20 }}
                        whileInView={{ opacity: 1, y: 0 }}
                        viewport={{ once: true }}
                        transition={{ delay: 0.1 }}
                        style={{ color: 'var(--land-text-secondary)', fontSize: '18px', maxWidth: '520px', margin: '0 auto', lineHeight: 1.6 }}
                    >
                        Every financial decision deserves precision. We built the tools to give you exactly that.
                    </motion.p>
                </div>

                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))',
                    gap: '24px'
                }}>
                    {calculators.map((calc, i) => (
                        <motion.div
                            key={i}
                            initial={{ opacity: 0, y: 30 }}
                            whileInView={{ opacity: 1, y: 0 }}
                            viewport={{ once: true }}
                            transition={{ delay: i * 0.08, duration: 0.5 }}
                            className="calc-card"
                            style={{
                                background: 'var(--land-bg-card)',
                                border: '1px solid var(--land-border-subtle)',
                                borderRadius: '16px',
                                padding: '32px 28px',
                                position: 'relative',
                                overflow: 'hidden',
                                transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
                                cursor: 'pointer'
                            }}
                            onMouseOver={(e) => {
                                e.currentTarget.style.background = 'rgba(16,185,129,0.05)';
                                e.currentTarget.style.borderColor = 'rgba(16,185,129,0.25)';
                                e.currentTarget.style.transform = 'translateY(-4px)';
                                e.currentTarget.style.boxShadow = '0 20px 40px rgba(0,0,0,0.4), 0 0 30px rgba(16,185,129,0.08)';
                                e.currentTarget.querySelector('.glow-corner').style.opacity = '1';
                            }}
                            onMouseOut={(e) => {
                                e.currentTarget.style.background = 'var(--land-bg-card)';
                                e.currentTarget.style.borderColor = 'var(--land-border-subtle)';
                                e.currentTarget.style.transform = 'translateY(0)';
                                e.currentTarget.style.boxShadow = 'none';
                                e.currentTarget.querySelector('.glow-corner').style.opacity = '0';
                            }}
                        >
                            {/* Corner Glow */}
                            <div className="glow-corner" style={{
                                position: 'absolute',
                                top: 0, right: 0,
                                width: '120px', height: '120px',
                                background: 'radial-gradient(circle, rgba(16,185,129,0.12), transparent 70%)',
                                opacity: 0,
                                transition: 'opacity 0.4s ease',
                                pointerEvents: 'none'
                            }} />

                            <div style={{
                                width: '48px', height: '48px',
                                background: 'rgba(16,185,129,0.1)',
                                borderRadius: '12px',
                                display: 'flex', alignItems: 'center', justifyContent: 'center',
                                marginBottom: '24px'
                            }}>
                                {calc.icon}
                            </div>

                            <h3 className="font-display" style={{ fontSize: '20px', fontWeight: 600, marginBottom: '12px', color: '#fff' }}>
                                {calc.name}
                            </h3>

                            <p style={{ color: 'var(--land-text-secondary)', fontSize: '15px', lineHeight: 1.6, marginBottom: '24px', minHeight: '48px' }}>
                                {calc.desc}
                            </p>

                            <div className="font-mono" style={{
                                fontSize: '12px',
                                color: 'rgba(16,185,129,0.7)',
                                padding: '10px 14px',
                                background: 'rgba(16,185,129,0.05)',
                                borderRadius: '6px',
                                borderLeft: '2px solid rgba(16,185,129,0.3)',
                                marginBottom: '24px',
                                letterSpacing: '0.05em'
                            }}>
                                {calc.formula}
                            </div>

                            <a href="#" style={{
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '8px',
                                color: 'var(--land-accent-green)',
                                fontSize: '14px',
                                fontWeight: 600,
                                textDecoration: 'none'
                            }}>
                                Calculate <ArrowRight size={16} />
                            </a>
                        </motion.div>
                    ))}
                </div>
            </div>
        </section>
    );
}
