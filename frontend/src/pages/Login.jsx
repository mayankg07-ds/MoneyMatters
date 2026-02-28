import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as Dialog from '@radix-ui/react-dialog';
import { Mail, Lock, X } from 'lucide-react';
import { useToast } from '../components/Toast';

import LandingNavbar from '../components/landing/LandingNavbar';
import LandingHero from '../components/landing/LandingHero';
import LandingTicker from '../components/landing/LandingTicker';
import LandingCalculators from '../components/landing/LandingCalculators';
import LandingPortfolio from '../components/landing/LandingPortfolio';
import LandingHowItWorks from '../components/landing/LandingHowItWorks';
import LandingStatsBar from '../components/landing/LandingStatsBar';
import LandingFinalCTA from '../components/landing/LandingFinalCTA';
import LandingFooter from '../components/landing/LandingFooter';

import './Landing.css';

export default function Login() {
    const navigate = useNavigate();
    const toast = useToast();

    // Login Form State
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);

    // Dialog State
    const [open, setOpen] = useState(false);

    const handleLoginSubmit = (e) => {
        e.preventDefault();
        const errs = {};
        if (!email.trim()) errs.email = 'Email is required';
        if (!password) errs.password = 'Password is required';
        setErrors(errs);
        if (Object.keys(errs).length > 0) return;

        setLoading(true);
        setTimeout(() => {
            localStorage.setItem('userId', '1');
            localStorage.setItem('userName', 'Alex Morgan');
            toast.success('Welcome back!');
            setLoading(false);
            setOpen(false);
            navigate('/dashboard');
        }, 600);
    };

    return (
        <div className="landing-page-wrapper">
            <LandingNavbar onSignInClick={() => setOpen(true)} />

            <main>
                <LandingHero />
                <LandingTicker />
                <LandingCalculators />
                <LandingPortfolio />
                <LandingHowItWorks />
                <LandingStatsBar />
                <LandingFinalCTA />
            </main>

            <LandingFooter />

            {/* --- RADIX UI LOGIN DIALOG --- */}
            <Dialog.Root open={open} onOpenChange={setOpen}>
                <Dialog.Portal>
                    <Dialog.Overlay style={{
                        background: 'rgba(6, 9, 16, 0.9)',
                        backdropFilter: 'blur(8px)',
                        position: 'fixed',
                        inset: 0,
                        zIndex: 999,
                    }} />
                    <Dialog.Content style={{
                        background: 'var(--land-bg-secondary)',
                        border: '1px solid var(--land-border-subtle)',
                        borderRadius: '20px',
                        boxShadow: 'var(--land-shadow-card)',
                        position: 'fixed',
                        top: '50%',
                        left: '50%',
                        transform: 'translate(-50%, -50%)',
                        width: '90vw',
                        maxWidth: '440px',
                        padding: '40px',
                        zIndex: 1000,
                        color: '#f0f4f8'
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                            <Dialog.Title className="font-display" style={{ fontSize: '24px', fontWeight: 600, margin: 0 }}>
                                Sign in to your account
                            </Dialog.Title>
                            <Dialog.Close asChild>
                                <button style={{ background: 'transparent', border: 'none', color: 'var(--land-text-secondary)', cursor: 'pointer' }}>
                                    <X size={20} />
                                </button>
                            </Dialog.Close>
                        </div>
                        <Dialog.Description style={{ color: 'var(--land-text-secondary)', fontSize: '14px', marginBottom: '32px' }}>
                            Enter your details below to access your dashboard.
                        </Dialog.Description>

                        {/* Trial Hint */}
                        <div
                            onClick={() => { setEmail('demo@moneymatters.com'); setPassword('demo123'); }}
                            style={{
                                background: 'rgba(16,185,129,0.05)',
                                border: '1px solid rgba(16,185,129,0.2)',
                                borderRadius: '12px',
                                padding: '16px',
                                cursor: 'pointer',
                                marginBottom: '24px',
                                transition: 'background 0.2s',
                                fontSize: '13px'
                            }}
                            onMouseOver={(e) => e.currentTarget.style.background = 'rgba(16,185,129,0.1)'}
                            onMouseOut={(e) => e.currentTarget.style.background = 'rgba(16,185,129,0.05)'}
                        >
                            <div style={{ color: 'var(--land-accent-green)', fontWeight: 600, marginBottom: '8px' }}>🚀 Trial Access</div>
                            <div className="font-mono" style={{ color: '#fff', fontSize: '12px', opacity: 0.8, marginBottom: '4px' }}>Email: demo@moneymatters.com</div>
                            <div className="font-mono" style={{ color: '#fff', fontSize: '12px', opacity: 0.8 }}>Pass: demo123</div>
                            <div style={{ color: 'var(--land-text-secondary)', fontSize: '11px', marginTop: '8px' }}>(Click to auto-fill)</div>
                        </div>

                        <form onSubmit={handleLoginSubmit}>
                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '8px', color: 'var(--land-text-secondary)' }}>Email Address</label>
                                <div style={{ position: 'relative' }}>
                                    <Mail size={16} style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--land-text-secondary)' }} />
                                    <input
                                        type="email"
                                        placeholder="name@company.com"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        style={{
                                            width: '100%',
                                            background: 'rgba(255,255,255,0.03)',
                                            border: `1px solid ${errors.email ? '#ef4444' : 'rgba(255,255,255,0.1)'}`,
                                            borderRadius: '8px',
                                            padding: '12px 16px 12px 44px',
                                            color: '#fff',
                                            fontSize: '15px',
                                            outline: 'none',
                                            transition: 'border-color 0.2s'
                                        }}
                                        onFocus={(e) => e.target.style.borderColor = 'var(--land-accent-green)'}
                                        onBlur={(e) => e.target.style.borderColor = errors.email ? '#ef4444' : 'rgba(255,255,255,0.1)'}
                                    />
                                </div>
                                {errors.email && <div style={{ color: '#ef4444', fontSize: '12px', marginTop: '6px' }}>{errors.email}</div>}
                            </div>

                            <div style={{ marginBottom: '32px' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                                    <label style={{ fontSize: '14px', fontWeight: 500, color: 'var(--land-text-secondary)' }}>Password</label>
                                    <a href="#" style={{ fontSize: '13px', color: 'var(--land-accent-green)', textDecoration: 'none' }}>Forgot password?</a>
                                </div>
                                <div style={{ position: 'relative' }}>
                                    <Lock size={16} style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--land-text-secondary)' }} />
                                    <input
                                        type="password"
                                        placeholder="••••••••"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        style={{
                                            width: '100%',
                                            background: 'rgba(255,255,255,0.03)',
                                            border: `1px solid ${errors.password ? '#ef4444' : 'rgba(255,255,255,0.1)'}`,
                                            borderRadius: '8px',
                                            padding: '12px 16px 12px 44px',
                                            color: '#fff',
                                            fontSize: '15px',
                                            outline: 'none',
                                            transition: 'border-color 0.2s'
                                        }}
                                        onFocus={(e) => e.target.style.borderColor = 'var(--land-accent-green)'}
                                        onBlur={(e) => e.target.style.borderColor = errors.password ? '#ef4444' : 'rgba(255,255,255,0.1)'}
                                    />
                                </div>
                                {errors.password && <div style={{ color: '#ef4444', fontSize: '12px', marginTop: '6px' }}>{errors.password}</div>}
                            </div>

                            <button
                                type="submit"
                                disabled={loading}
                                className="land-btn land-btn-primary"
                                style={{ width: '100%', height: '48px', fontSize: '16px', display: 'flex', justifyContent: 'center' }}
                            >
                                {loading ? 'Signing In...' : 'Sign In'}
                            </button>
                        </form>
                    </Dialog.Content>
                </Dialog.Portal>
            </Dialog.Root>

        </div>
    );
}
