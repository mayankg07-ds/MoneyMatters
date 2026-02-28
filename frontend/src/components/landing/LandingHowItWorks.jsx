import { motion } from 'framer-motion';

export default function LandingHowItWorks() {
    const steps = [
        {
            num: '①',
            title: 'Sign In to Your MoneyMatters Account',
            desc: 'Create your free account in under 60 seconds. No credit card. No surprises.'
        },
        {
            num: '②',
            title: 'Input Your Numbers',
            desc: 'Enter principal, rates, tenure, step-up percentages, and all relevant parameters. Our engine handles the rest.'
        },
        {
            num: '③',
            title: 'Get Instant Clarity',
            desc: 'Your full financial picture — charts, projections, and actionable insights delivered instantly.'
        }
    ];

    return (
        <section style={{ padding: 'var(--section-pad-md)' }}>
            <div className="landing-container">
                <div style={{ position: 'relative' }}>

                    {/* Connecting Dashed Line (Desktop hidden on mobile via CSS usually, but we'll absolute position it) */}
                    <div style={{
                        position: 'absolute',
                        top: '32px',
                        left: '10%',
                        right: '10%',
                        height: '2px',
                        borderTop: '2px dashed rgba(16,185,129,0.3)',
                        zIndex: 0
                    }} />

                    {/* Desktop Steps Grid */}
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(3, 1fr)',
                        gap: '40px',
                        position: 'relative',
                        zIndex: 1
                    }}>
                        {steps.map((step, i) => (
                            <motion.div
                                key={i}
                                initial={{ opacity: 0, y: 30 }}
                                whileInView={{ opacity: 1, y: 0 }}
                                viewport={{ once: true }}
                                transition={{ delay: i * 0.2, duration: 0.6 }}
                                style={{
                                    background: 'var(--land-bg-primary)', // To cover the dashed line behind
                                    padding: '16px',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    gap: '16px'
                                }}
                            >
                                <div style={{
                                    fontSize: '13px',
                                    color: 'var(--land-text-secondary)',
                                    letterSpacing: '0.1em',
                                    textTransform: 'uppercase'
                                }}>
                                    Step {i + 1}
                                </div>
                                <div style={{
                                    fontSize: '48px',
                                    color: 'var(--land-accent-green)',
                                    lineHeight: 1,
                                    fontFamily: 'var(--font-display)',
                                    background: 'var(--land-bg-primary)',
                                    display: 'inline-block',
                                }}>
                                    {step.num}
                                </div>
                                <h3 className="font-display" style={{ fontSize: '22px', fontWeight: 600, color: '#fff', marginTop: '8px' }}>
                                    {step.title}
                                </h3>
                                <p style={{ color: 'var(--land-text-secondary)', fontSize: '15px', lineHeight: 1.6 }}>
                                    {step.desc}
                                </p>
                            </motion.div>
                        ))}
                    </div>

                </div>
            </div>
        </section>
    );
}
