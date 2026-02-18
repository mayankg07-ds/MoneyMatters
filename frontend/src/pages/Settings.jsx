import Header from '../components/Header';
import { Settings as SettingsIcon, User, Bell, Shield, Palette, Globe } from 'lucide-react';

export default function Settings() {
    return (
        <>
            <Header title="Settings" subtitle="Manage your account and preferences" />
            <div className="page-content">
                <div className="two-col">
                    <div className="card">
                        <div className="card-header"><h3><User size={18} style={{ marginRight: 8, verticalAlign: 'middle' }} />Profile</h3></div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 20, marginBottom: 24 }}>
                            <div style={{ width: 72, height: 72, borderRadius: '50%', background: 'linear-gradient(135deg, #ff8a65, #ff6e40)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.5rem', fontWeight: 700, color: '#fff' }}>AM</div>
                            <div>
                                <div style={{ fontSize: '1.25rem', fontWeight: 600, color: 'var(--text-heading)' }}>Alex Morgan</div>
                                <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>alex.morgan@email.com</div>
                                <span className="stat-badge positive" style={{ marginTop: 6 }}>Pro Investor</span>
                            </div>
                        </div>
                        <div className="input-group"><label>Full Name</label><input className="input-field" defaultValue="Alex Morgan" /></div>
                        <div className="input-group"><label>Email</label><input className="input-field" defaultValue="alex.morgan@email.com" /></div>
                        <div className="input-group"><label>Phone</label><input className="input-field" defaultValue="+91 98765 43210" /></div>
                        <button className="btn btn-primary" style={{ marginTop: 8 }}>Save Changes</button>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
                        <div className="card">
                            <div className="card-header"><h3><Bell size={18} style={{ marginRight: 8, verticalAlign: 'middle' }} />Notifications</h3></div>
                            {['Price Alerts', 'Portfolio Updates', 'Transaction Confirmations', 'Weekly Reports'].map((item) => (
                                <div key={item} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 0', borderBottom: '1px solid var(--border-subtle)' }}>
                                    <span style={{ fontSize: '0.9rem' }}>{item}</span>
                                    <label style={{ position: 'relative', width: 44, height: 24 }}>
                                        <input type="checkbox" defaultChecked style={{ opacity: 0, width: 0, height: 0 }} />
                                        <span style={{ position: 'absolute', inset: 0, background: 'var(--accent-primary)', borderRadius: 12, cursor: 'pointer', transition: '0.3s' }}></span>
                                    </label>
                                </div>
                            ))}
                        </div>

                        <div className="card">
                            <div className="card-header"><h3><Palette size={18} style={{ marginRight: 8, verticalAlign: 'middle' }} />Appearance</h3></div>
                            <div className="input-group"><label>Theme</label>
                                <select className="select-field"><option>Dark Mode</option><option>Light Mode</option><option>System</option></select>
                            </div>
                            <div className="input-group"><label>Currency</label>
                                <select className="select-field"><option>₹ INR - Indian Rupee</option><option>$ USD - US Dollar</option><option>€ EUR - Euro</option></select>
                            </div>
                        </div>

                        <div className="card">
                            <div className="card-header"><h3><Shield size={18} style={{ marginRight: 8, verticalAlign: 'middle' }} />Security</h3></div>
                            <button className="btn btn-secondary" style={{ width: '100%', marginBottom: 12 }}>Change Password</button>
                            <button className="btn btn-secondary" style={{ width: '100%' }}>Enable Two-Factor Auth</button>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}
