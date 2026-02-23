import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Mail, Lock, User, ShieldCheck, KeyRound } from 'lucide-react';
import { useToast } from '../components/Toast';
import GradientText from '../components/GradientText';

export default function Register() {
    const navigate = useNavigate();
    const toast = useToast();
    const [form, setForm] = useState({ name: '', email: '', password: '', confirm: '' });
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);

    const validate = () => {
        const e = {};
        if (!form.name.trim()) e.name = 'Name is required';
        if (!form.email.trim()) e.email = 'Email is required';
        else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = 'Invalid email format';
        if (!form.password) e.password = 'Password is required';
        else if (form.password.length < 6) e.password = 'Password must be at least 6 characters';
        if (form.password !== form.confirm) e.confirm = 'Passwords do not match';
        return e;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        const v = validate();
        setErrors(v);
        if (Object.keys(v).length > 0) return;

        setLoading(true);
        // Mock registration
        setTimeout(() => {
            localStorage.setItem('userId', '1');
            localStorage.setItem('userName', form.name);
            toast.success('Account created successfully!');
            navigate('/dashboard');
        }, 800);
    };

    const upd = (k, v) => { setForm(p => ({ ...p, [k]: v })); setErrors(p => ({ ...p, [k]: '' })); };

    return (
        <div className="login-page">
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
                <h2>Create your account</h2>
                <p className="subtitle">Fill in the details to get started.</p>

                <form onSubmit={handleSubmit}>
                    <div className="input-group">
                        <label>Full Name <span style={{ color: 'var(--color-danger)' }}>*</span></label>
                        <div className="input-with-icon">
                            <User size={16} className="icon" />
                            <input
                                id="register-name"
                                type="text"
                                className={`input-field ${errors.name ? 'input-error' : ''}`}
                                placeholder="Alex Morgan"
                                value={form.name}
                                onChange={(e) => upd('name', e.target.value)}
                            />
                        </div>
                        {errors.name && <span className="field-error">{errors.name}</span>}
                    </div>

                    <div className="input-group">
                        <label>Email Address <span style={{ color: 'var(--color-danger)' }}>*</span></label>
                        <div className="input-with-icon">
                            <Mail size={16} className="icon" />
                            <input
                                id="register-email"
                                type="email"
                                className={`input-field ${errors.email ? 'input-error' : ''}`}
                                placeholder="name@company.com"
                                value={form.email}
                                onChange={(e) => upd('email', e.target.value)}
                            />
                        </div>
                        {errors.email && <span className="field-error">{errors.email}</span>}
                    </div>

                    <div className="input-group">
                        <label>Password <span style={{ color: 'var(--color-danger)' }}>*</span></label>
                        <div className="input-with-icon">
                            <Lock size={16} className="icon" />
                            <input
                                id="register-password"
                                type="password"
                                className={`input-field ${errors.password ? 'input-error' : ''}`}
                                placeholder="••••••••"
                                value={form.password}
                                onChange={(e) => upd('password', e.target.value)}
                            />
                        </div>
                        {errors.password && <span className="field-error">{errors.password}</span>}
                    </div>

                    <div className="input-group">
                        <label>Confirm Password <span style={{ color: 'var(--color-danger)' }}>*</span></label>
                        <div className="input-with-icon">
                            <Lock size={16} className="icon" />
                            <input
                                id="register-confirm"
                                type="password"
                                className={`input-field ${errors.confirm ? 'input-error' : ''}`}
                                placeholder="••••••••"
                                value={form.confirm}
                                onChange={(e) => upd('confirm', e.target.value)}
                            />
                        </div>
                        {errors.confirm && <span className="field-error">{errors.confirm}</span>}
                    </div>

                    <button type="submit" className="btn-login" id="register-btn" disabled={loading}>
                        {loading ? 'Creating Account...' : 'Create Account'}
                    </button>
                </form>
            </div>

            <div className="login-footer">
                <p>Already have an account? <Link to="/login">Sign in</Link></p>
            </div>

            <div className="login-trust">
                <div className="badge-item"><ShieldCheck size={14} /> Bank-Level Security</div>
                <div className="badge-item"><KeyRound size={14} /> 256-Bit AES Encryption</div>
            </div>

            <div className="login-links">
                <a href="#">Privacy Policy</a>
                <a href="#">Terms of Service</a>
                <a href="#">Contact Us</a>
            </div>
        </div>
    );
}
