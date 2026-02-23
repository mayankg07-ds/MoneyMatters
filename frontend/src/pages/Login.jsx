import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Mail, Lock, ShieldCheck, KeyRound } from 'lucide-react';
import { useToast } from '../components/Toast';
import GradientText from '../components/GradientText';

export default function Login() {
    const navigate = useNavigate();
    const toast = useToast();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);

    const handleSubmit = (e) => {
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
            navigate('/dashboard');
        }, 600);
    };

    return (
        <div className="login-page" style={{ backgroundImage: 'url("/src/assets/WolfofWallStreet.png")', backgroundSize: 'cover', backgroundPosition: 'center' }}>
            <div className="login-overlay"></div>
            <div className="login-logo">
                <GradientText
                    colors={["#0676bc", "#003cc7", "#bdc2d0"]}
                    animationSpeed={2}
                    showBorder={true}
                    className="login-brand-text"
                >
                    Money Matters
                </GradientText>
                <p>Smart wealth management</p>
            </div>

            <div className="login-card">
                <h2>Sign in to your account</h2>
                <p className="subtitle">Enter your details below to access your dashboard.</p>

                {/* Trial Credentials Hint */}
                <div
                    className="trial-hint"
                    onClick={() => { setEmail('demo@moneymatters.com'); setPassword('demo123'); }}
                    title="Click to auto-fill"
                >
                    <div className="hint-title">🚀 Trial Access</div>
                    <div className="hint-code">
                        <span>Email: demo@moneymatters.com</span>
                        <span>Pass: demo123</span>
                    </div>
                    <div className="hint-sub">(Click to auto-fill)</div>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="input-group">
                        <label>Email Address</label>
                        <div className="input-with-icon">
                            <Mail size={16} className="icon" />
                            <input
                                id="login-email"
                                type="email"
                                className={`input-field ${errors.email ? 'input-error' : ''}`}
                                placeholder="name@company.com"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                            />
                        </div>
                        {errors.email && <span className="field-error">{errors.email}</span>}
                    </div>

                    <div className="input-group">
                        <div className="password-row">
                            <label>Password</label>
                            <a href="#" className="forgot-link">Forgot password?</a>
                        </div>
                        <div className="input-with-icon">
                            <Lock size={16} className="icon" />
                            <input
                                id="login-password"
                                type="password"
                                className={`input-field ${errors.password ? 'input-error' : ''}`}
                                placeholder="••••••••"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                            />
                        </div>
                        {errors.password && <span className="field-error">{errors.password}</span>}
                    </div>

                    <div className="remember-row">
                        <input type="checkbox" id="remember-me" />
                        <label htmlFor="remember-me">Keep me logged in</label>
                    </div>

                    <button type="submit" className="btn-login" id="sign-in-btn" disabled={loading}>
                        {loading ? 'Signing In...' : 'Sign In'}
                    </button>
                </form>

                <div className="divider">or continue with</div>

                <div className="social-buttons">
                    <button className="social-btn" id="google-login">
                        <svg width="18" height="18" viewBox="0 0 24 24"><path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4" /><path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" /><path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" /><path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" /></svg>
                        Google
                    </button>
                    <button className="social-btn" id="apple-login">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="#333"><path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.8-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z" /></svg>
                        Apple
                    </button>
                </div>
            </div>

            <div className="login-footer">
                <p>New to MoneyMatters? <Link to="/register">Create an account</Link></p>
            </div>

            <div className="login-trust">
                <div className="badge-item">
                    <ShieldCheck size={14} />
                    Bank-Level Security
                </div>
                <div className="badge-item">
                    <KeyRound size={14} />
                    256-Bit AES Encryption
                </div>
            </div>

            <div className="login-links">
                <a href="#">Privacy Policy</a>
                <a href="#">Terms of Service</a>
                <a href="#">Contact Us</a>
            </div>
        </div>
    );
}
