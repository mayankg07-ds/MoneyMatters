import { motion } from 'framer-motion';
import { ArrowRight, PlayCircle } from 'lucide-react';
import wolfImage from '../../assets/WolfofWallStreet.png';

const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
        opacity: 1,
        transition: { staggerChildren: 0.15, delayChildren: 0.2 }
    }
};

const itemVariants = {
    hidden: { opacity: 0, y: 30 },
    visible: { opacity: 1, y: 0, transition: { duration: 0.7, ease: [0.16, 1, 0.3, 1] } }
};

export default function LandingHero() {
    return (
        <section style={{
            padding: 'var(--section-pad-lg)',
            paddingTop: '160px', /* Extra for navbar */
            background: `
        radial-gradient(ellipse 80% 60% at 10% 50%, rgba(16,185,129,0.07), transparent),
        radial-gradient(ellipse 60% 80% at 90% 20%, rgba(99,102,241,0.06), transparent),
        #060910
      `,
            minHeight: '100vh',
            display: 'flex',
            alignItems: 'center'
        }}>
            <div className="landing-container" style={{ display: 'flex', gap: '40px', alignItems: 'center' }}>

                {/* LEFT SIDE: Copy */}
                <motion.div
                    style={{ width: '55%', zIndex: 10 }}
                    variants={containerVariants}
                    initial="hidden"
                    whileInView="visible"
                    viewport={{ once: true }}
                >
                    <motion.div variants={itemVariants} style={{ marginBottom: '24px' }}>
                        <span style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '8px',
                            padding: '6px 16px',
                            borderRadius: '100px',
                            background: 'rgba(255,255,255,0.03)',
                            border: '1px solid rgba(255,255,255,0.08)',
                            fontSize: '13px',
                            color: 'var(--land-text-secondary)',
                            letterSpacing: '0.5px'
                        }}>
                            <span style={{ color: 'var(--land-accent-gold)' }}>✦</span> Smart Financial Intelligence — Built for India
                        </span>
                    </motion.div>

                    <motion.h1 variants={itemVariants} className="font-display" style={{
                        fontSize: '72px',
                        lineHeight: 1.05,
                        fontWeight: 700,
                        marginBottom: '24px',
                        letterSpacing: '-1.5px'
                    }}>
                        YOUR WEALTH,<br />
                        <span className="shimmer-text">PRECISELY</span><br />
                        CALCULATED.
                    </motion.h1>

                    <motion.p variants={itemVariants} style={{
                        fontSize: '18px',
                        color: 'var(--land-text-secondary)',
                        maxWidth: '480px',
                        lineHeight: 1.7,
                        marginBottom: '40px'
                    }}>
                        India's most comprehensive financial intelligence platform.
                        SIP planners, loan analyzers, retirement models — all in one
                        place. Built for investors who mean business.
                    </motion.p>

                    <motion.div variants={itemVariants} style={{ display: 'flex', gap: '16px', marginBottom: '48px' }}>
                        <button className="land-btn land-btn-primary" style={{ height: '52px', fontSize: '16px', padding: '0 32px' }}>
                            Start Calculating <ArrowRight size={18} style={{ marginLeft: '8px' }} />
                        </button>
                        <button className="land-btn land-btn-ghost" style={{ height: '52px', fontSize: '16px', padding: '0 32px' }}>
                            ◎ Watch Demo
                        </button>
                    </motion.div>

                    <motion.div variants={itemVariants} style={{
                        display: 'flex',
                        borderTop: '1px solid var(--land-border-subtle)',
                        paddingTop: '24px',
                        gap: '40px'
                    }}>
                        {[
                            { val: '12,000+', lbl: 'Users' },
                            { val: '₹847 Cr+', lbl: 'Tracked' },
                            { val: '99.8%', lbl: 'Accuracy' }
                        ].map((stat, i) => (
                            <div key={i}>
                                <div className="font-display" style={{ fontSize: '22px', fontWeight: 700, color: '#fff', marginBottom: '4px' }}>{stat.val}</div>
                                <div style={{ fontSize: '12px', color: 'var(--land-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.1em' }}>{stat.lbl}</div>
                            </div>
                        ))}
                    </motion.div>
                </motion.div>

                {/* RIGHT SIDE: Painting */}
                <motion.div
                    style={{ width: '45%', position: 'relative', perspective: '1000px' }}
                    initial={{ opacity: 0, x: 40 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 1, delay: 0.4, ease: "easeOut" }}
                >
                    {/* Main Painting Container */}
                    <motion.div
                        style={{
                            borderRadius: '20px',
                            border: '2px solid rgba(240, 180, 41, 0.4)',
                            boxShadow: '0 0 60px rgba(240,180,41,0.15), 0 40px 80px rgba(0,0,0,0.6)',
                            overflow: 'hidden',
                            height: '520px',
                            position: 'relative',
                            transformOrigin: 'center center'
                        }}
                        initial={{ rotate: 2 }}
                        whileHover={{ rotate: 0, scale: 1.02, transition: { duration: 0.6, ease: "easeOut" } }}
                    >
                        <img
                            src={wolfImage}
                            alt="The Wolf of Wall Street"
                            style={{
                                width: '100%',
                                height: '100%',
                                objectFit: 'cover',
                                objectPosition: 'center 15%',
                                filter: 'contrast(1.05) saturate(1.1)'
                            }}
                        />
                        {/* Dark gradient overlay to blend bottom */}
                        <div style={{
                            position: 'absolute', inset: 0,
                            background: 'linear-gradient(to top, rgba(6,9,16,0.8), transparent 40%)'
                        }}></div>
                    </motion.div>

                    {/* Floating Card */}
                    <motion.div
                        style={{
                            position: 'absolute',
                            bottom: '40px',
                            left: '-40px',
                            background: 'rgba(6,9,16,0.85)',
                            backdropFilter: 'blur(20px)',
                            WebkitBackdropFilter: 'blur(20px)',
                            border: '1px solid rgba(16,185,129,0.3)',
                            borderRadius: '16px',
                            padding: '16px 20px',
                            boxShadow: '0 20px 40px rgba(0,0,0,0.4)',
                            zIndex: 20
                        }}
                        animate={{ y: [0, -8, 0] }}
                        transition={{ duration: 4, repeat: Infinity, ease: 'easeInOut' }}
                    >
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px', fontSize: '13px', color: 'var(--land-text-secondary)' }}>
                            📈 Portfolio Today
                        </div>
                        <div style={{ fontSize: '28px', fontWeight: 700, color: 'var(--land-accent-green)', letterSpacing: '-0.5px', marginBottom: '8px' }}>
                            ₹ 4,20,847 <span style={{ fontSize: '16px', background: 'rgba(16,185,129,0.1)', padding: '2px 6px', borderRadius: '4px' }}>↑ +18.4%</span>
                        </div>
                        <div className="font-mono" style={{ fontSize: '11px', color: 'var(--land-text-secondary)' }}>
                            XIRR: 22.3% <span style={{ color: 'rgba(255,255,255,0.2)', margin: '0 8px' }}>|</span> CAGR: 19.1%
                        </div>
                    </motion.div>

                    {/* Small Badge */}
                    <motion.div
                        style={{
                            position: 'absolute',
                            top: '20px',
                            right: '-20px',
                            background: 'rgba(6,9,16,0.85)',
                            backdropFilter: 'blur(10px)',
                            border: '1px solid rgba(255,255,255,0.1)',
                            borderRadius: '100px',
                            padding: '8px 16px',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px',
                            fontSize: '12px',
                            fontWeight: 600,
                            boxShadow: '0 10px 20px rgba(0,0,0,0.3)'
                        }}
                    >
                        🔒 Bank-Level Security
                    </motion.div>

                </motion.div>
            </div>
        </section>
    );
}
