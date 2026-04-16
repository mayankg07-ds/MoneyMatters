import { useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { useUser, useClerk } from '@clerk/react';
import './Sidebar.css';
import {
    LayoutDashboard,
    PieChart,
    Calculator,
    BarChart3,
    ArrowLeftRight,
    Settings,
    LogOut,
    Menu,
    X,
} from 'lucide-react';

const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/portfolio', label: 'Portfolio', icon: PieChart },
    { path: '/calculators', label: 'Calculators', icon: Calculator },
    { path: '/analytics', label: 'Analytics', icon: BarChart3 },
    { path: '/transactions', label: 'Transactions', icon: ArrowLeftRight },
];

export default function Sidebar() {
    const location = useLocation();
    const { signOut } = useClerk();
    const { user } = useUser();
    const [mobileOpen, setMobileOpen] = useState(false);
    const userName = user?.fullName || user?.firstName || 'User';
    const initials = userName.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();

    const isActive = (path) => {
        if (path === '/calculators') return location.pathname.startsWith('/calculators');
        return location.pathname === path;
    };

    const handleLogout = () => {
        signOut({ redirectUrl: '/login' });
    };

    const closeMobile = () => setMobileOpen(false);

    return (
        <>
            {/* Mobile hamburger toggle */}
            <button className="sidebar-toggle" onClick={() => setMobileOpen(!mobileOpen)} aria-label="Toggle menu">
                {mobileOpen ? <X size={22} /> : <Menu size={22} />}
            </button>

            {/* Overlay for mobile */}
            {mobileOpen && <div className="sidebar-overlay" onClick={closeMobile}></div>}

            <aside className={`sidebar ${mobileOpen ? 'sidebar-open' : ''}`}>
                <div className="sidebar-brand">
                    <NavLink to="/dashboard" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%', textDecoration: 'none', gap: '8px' }} onClick={closeMobile}>
                        <span style={{ color: '#10b981', fontSize: '24px', lineHeight: 1 }}>◈</span>
                        <span className="font-display" style={{ fontWeight: 700, fontSize: '20px', color: '#fff', letterSpacing: '0.5px' }}>
                            MoneyMatters
                        </span>
                    </NavLink>
                </div>

                <nav className="sidebar-nav">
                    {navItems.map(({ path, label, icon: Icon }) => (
                        <NavLink
                            key={path}
                            to={path}
                            className={`nav-link ${isActive(path) ? 'active' : ''}`}
                            onClick={closeMobile}
                        >
                            <Icon size={18} />
                            <span>{label}</span>
                        </NavLink>
                    ))}
                </nav>

                <div className="sidebar-footer">
                    <NavLink to="/settings" className="nav-link" onClick={closeMobile}>
                        <Settings size={18} />
                        <span>Settings</span>
                    </NavLink>
                    <button className="nav-link logout-btn" onClick={handleLogout}>
                        <LogOut size={18} />
                        <span>Logout</span>
                    </button>

                    <div className="sidebar-user">
                        <div className="user-avatar">{initials}</div>
                        <div className="user-info">
                            <span className="user-name">{userName}</span>
                            <span className="user-role">Pro Investor</span>
                        </div>
                    </div>
                </div>
            </aside>
        </>
    );
}
