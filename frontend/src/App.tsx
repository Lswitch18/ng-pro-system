import { useState } from 'react';
// Navigation via state, no router needed

import LoginPage from './pages/LoginPage';
import Dashboard from './pages/Dashboard';
import CustomersPage from './pages/CustomersPage';
import PlansPage from './pages/PlansPage';
import InvoicesPage from './pages/InvoicesPage';
import MonitoringPage from './pages/MonitoringPage';
import ProvisioningPage from './pages/ProvisioningPage';
import CollectionPage from './pages/CollectionPage';
import Sidebar from './components/Sidebar';
import './index.css';

export type Page = 'dashboard' | 'customers' | 'plans' | 'invoices' | 'monitoring' | 'provisioning' | 'collection';

function App() {
    const [token, setToken] = useState(localStorage.getItem('ng_token') || '');
    const [user, setUser] = useState(localStorage.getItem('ng_user') || '');
    const [role, setRole] = useState(localStorage.getItem('ng_role') || '');
    const [page, setPage] = useState<Page>('dashboard');

    const handleLogin = (tok: string, username: string, userRole: string) => {
        localStorage.setItem('ng_token', tok);
        localStorage.setItem('ng_user', username);
        localStorage.setItem('ng_role', userRole);
        setToken(tok);
        setUser(username);
        setRole(userRole);
    };

    const handleLogout = () => {
        localStorage.clear();
        setToken('');
    };

    if (!token) return <LoginPage onLogin={handleLogin} />;

    const renderPage = () => {
        switch (page) {
            case 'dashboard': return <Dashboard />;
            case 'customers': return <CustomersPage />;
            case 'plans': return <PlansPage />;
            case 'invoices': return <InvoicesPage />;
            case 'monitoring': return <MonitoringPage />;
            case 'provisioning': return <ProvisioningPage />;
            case 'collection': return <CollectionPage />;
            default: return <Dashboard />;
        }
    };

    return (
        <div className="layout">
            <Sidebar currentPage={page} onNavigate={setPage} user={user} role={role} onLogout={handleLogout} />
            <div className="main">
                {renderPage()}
            </div>
        </div>
    );
}

export default App;
