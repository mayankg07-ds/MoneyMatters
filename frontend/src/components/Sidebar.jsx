import { NavLink, useLocation } from 'react-router-dom';
import {
    LayoutDashboard,
    PieChart,
    Calculator,
    BarChart3,
    ArrowLeftRight,
    Settings,
    LogOut,
} from 'lucide-react';
import './Sidebar.css';

const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/portfolio', label: 'Portfolio', icon: PieChart },
    { path: '/calculators', label: 'Calculators', icon: Calculator },
    { path: '/analytics', label: 'Analytics', icon: BarChart3 },
    { path: '/transactions', label: 'Transactions', icon: ArrowLeftRight },
];

export default function Sidebar() {
    const location = useLocation();

    const isActive = (path) => {
        if (path === '/calculators') {
            return location.pathname.startsWith('/calculators');
        }
        return location.pathname === path;
    };

    return (
        <aside className="sidebar">
            <div className="sidebar-brand">
                <div className="brand-icon">
                    <span>M</span>
                </div>
                <div className="brand-text">
                    <span className="brand-name">Money</span>
                    <span className="brand-name bold">Matters</span>
                </div>
            </div>

            <nav className="sidebar-nav">
                {navItems.map(({ path, label, icon: Icon }) => (
                    <NavLink
                        key={path}
                        to={path}
                        className={`nav-link ${isActive(path) ? 'active' : ''}`}
                    >
                        <Icon size={18} />
                        <span>{label}</span>
                    </NavLink>
                ))}
            </nav>

            <div className="sidebar-footer">
                <NavLink to="/settings" className="nav-link">
                    <Settings size={18} />
                    <span>Settings</span>
                </NavLink>

                <div className="sidebar-user">
                    <div className="user-avatar">AM</div>
                    <div className="user-info">
                        <span className="user-name">Alex Morgan</span>
                        <span className="user-role">Pro Investor</span>
                    </div>
                </div>
            </div>
        </aside>
    );
}
