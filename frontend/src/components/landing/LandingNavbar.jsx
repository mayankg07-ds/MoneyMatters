import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { ArrowRight } from 'lucide-react';
import GradientText from '../GradientText';
import '../Sidebar.css'; // Just for the brand icon fallback if needed
import '../../pages/Landing.css';

export default function LandingNavbar({ clerkSignInSlot }) {
    const [scrolled, setScrolled] = useState(false);

    useEffect(() => {
        const handleScroll = () => setScrolled(window.scrollY > 80);
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    return (
        <motion.nav
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.5 }}
            style={{
                position: 'fixed',
                top: 0,
                left: 0,
                right: 0,
                height: '68px',
                zIndex: 100,
                display: 'flex',
                alignItems: 'center',
                padding: '0 40px',
                background: scrolled ? 'rgba(6, 9, 16, 0.85)' : 'transparent',
                backdropFilter: scrolled ? 'blur(20px)' : 'none',
                WebkitBackdropFilter: scrolled ? 'blur(20px)' : 'none',
                borderBottom: scrolled ? '1px solid rgba(255,255,255,0.06)' : '1px solid transparent',
                transition: 'all 0.3s ease'
            }}
        >
            <div style={{ display: 'flex', alignItems: 'center', flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                    {/* Logo ◈ symbol in #10b981 + "MoneyMatters" */}
                    <span style={{ color: '#10b981', fontSize: '24px', lineHeight: 1 }}>◈</span>
                    <span className="font-display" style={{ fontWeight: 700, fontSize: '20px', color: '#fff', letterSpacing: '0.5px' }}>
                        MoneyMatters
                    </span>
                </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '32px' }} className="nav-links-container">
                {['Calculators', 'Portfolio', 'Analytics', 'Docs'].map(link => (
                    <a
                        key={link}
                        href={`#${link.toLowerCase()}`}
                        style={{
                            fontSize: '14px',
                            color: 'var(--land-text-secondary)',
                            textDecoration: 'none',
                            transition: 'color 0.2s ease',
                            position: 'relative',
                            fontWeight: 500
                        }}
                        onMouseOver={(e) => e.target.style.color = '#fff'}
                        onMouseOut={(e) => e.target.style.color = 'var(--land-text-secondary)'}
                    >
                        {link}
                    </a>
                ))}
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', flex: 1, justifyContent: 'flex-end' }}>
                {/* Clerk-powered auth slot — renders SignInButton or UserButton */}
                {clerkSignInSlot ?? (
                    <button
                        className="land-btn land-btn-primary"
                        style={{ height: '40px', padding: '0 20px', fontSize: '14px', display: 'flex', gap: '6px' }}
                    >
                        Get Started <ArrowRight size={16} />
                    </button>
                )}
            </div>
        </motion.nav>
    );
}
