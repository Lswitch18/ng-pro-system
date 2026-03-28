import type { Page } from '../App';

const navItems: { id: Page; icon: string; label: string }[] = [
    { id: 'dashboard', icon: '⬡', label: 'Dashboard' },
    { id: 'customers', icon: '👥', label: 'Clientes' },
    { id: 'provisioning', icon: '📡', label: 'Provisionamento' },
    { id: 'plans', icon: '📋', label: 'Planos' },
    { id: 'invoices', icon: '💰', label: 'Faturamento' },
    { id: 'collection', icon: '🏦', label: 'Arrecadação' },
    { id: 'monitoring', icon: '🔒', label: 'Monitoramento' },
];

interface Props {
    currentPage: Page;
    onNavigate: (p: Page) => void;
    user: string;
    role: string;
    onLogout: () => void;
}

export default function Sidebar({ currentPage, onNavigate, user, role, onLogout }: Props) {
    return (
        <nav className="sidebar">
            <div className="sidebar-brand">
                <h1>ng-pro</h1>
                <p>Enterprise Billing</p>
            </div>

            {navItems.map(item => (
                <button
                    key={item.id}
                    className={`nav-item ${currentPage === item.id ? 'active' : ''}`}
                    onClick={() => onNavigate(item.id)}
                >
                    <span className="nav-icon">{item.icon}</span>
                    {item.label}
                </button>
            ))}

            <div className="sidebar-footer">
                <span>{user}</span>
                <div style={{ color: 'var(--accent)', fontSize: '11px', marginBottom: 8 }}>{role}</div>
                <button className="btn btn-ghost" style={{ width: '100%', fontSize: '12px', padding: '6px 10px' }} onClick={onLogout}>
                    Sair
                </button>
            </div>
        </nav>
    );
}
