import { motion } from 'framer-motion';
import { ArrowRight } from 'lucide-react';

export default function LandingFinalCTA() {
    return (
        <section style={{
            padding: 'var(--section-pad-lg)',
            background: 'radial-gradient(ellipse 60% 100% at 50% 50%, rgba(16,185,129,0.08), transparent)',
            textAlign: 'center'
        }}>
            <div className="landing-container">
                <motion.h2
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    className="font-display"
                    style={{ fontSize: '56px', fontWeight: 700, lineHeight: 1.1, marginBottom: '24px', letterSpacing: '-1px' }}
                >
                    Your Portfolio Isn't<br />
                    <span style={{ color: 'var(--land-accent-gold)' }}>Going to Track Itself.</span>
                </motion.h2>

                <motion.p
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: 0.1 }}
                    style={{ color: 'var(--land-text-secondary)', fontSize: '20px', maxWidth: '600px', margin: '0 auto 48px auto', lineHeight: 1.6 }}
                >
                    Join thousands of investors using MoneyMatters to make smarter, faster, and more profitable financial decisions.
                </motion.p>

                <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: 0.2 }}
                    style={{ display: 'flex', gap: '16px', justifyContent: 'center', marginBottom: '40px' }}
                >
                    <button className="land-btn land-btn-primary" style={{ height: '56px', fontSize: '18px', padding: '0 40px' }}>
                        Create Free Account <ArrowRight size={20} style={{ marginLeft: '8px' }} />
                    </button>
                    <button className="land-btn land-btn-ghost" style={{ height: '56px', fontSize: '18px', padding: '0 40px' }}>
                        Explore Calculators
                    </button>
                </motion.div>

                <motion.div
                    initial={{ opacity: 0 }}
                    whileInView={{ opacity: 1 }}
                    viewport={{ once: true }}
                    transition={{ delay: 0.4 }}
                    style={{ display: 'flex', gap: '24px', justifyContent: 'center', color: 'var(--land-text-muted)', fontSize: '14px' }}
                >
                    <span>✦ No credit card required</span>
                    <span>✦ Free forever</span>
                    <span>✦ 60-second setup</span>
                </motion.div>
            </div>
        </section>
    );
}
