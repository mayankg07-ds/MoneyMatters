import { Search, Bell } from 'lucide-react';
import './Header.css';

export default function Header({ title, subtitle, actions }) {
    return (
        <header className="top-header">
            <div className="header-left">
                <h1 className="header-title">{title}</h1>
                {subtitle && <p className="header-subtitle">{subtitle}</p>}
            </div>
            <div className="header-right">
                {actions && <div className="header-actions">{actions}</div>}
                <div className="header-search">
                    <Search size={16} />
                    <input type="text" placeholder="Search assets, transactions..." />
                </div>
                <button className="notification-btn" id="notification-bell">
                    <Bell size={18} />
                    <span className="notif-dot"></span>
                </button>
                <div className="header-avatar">
                    <span>AM</span>
                </div>
            </div>
        </header>
    );
}
