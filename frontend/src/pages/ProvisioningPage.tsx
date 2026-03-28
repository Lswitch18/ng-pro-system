import { useEffect, useState, useMemo } from 'react';
import { api } from '../api';

interface Customer {
    id: number; name: string; email: string; status: string; phone: string;
}

interface NasSession {
    sessionId: string;
    username: string;
    ip: string;
    nasIp: string;
    startTime: string;
    status: string;
    bytesIn?: number;
    bytesOut?: number;
    acctSessionTime?: number;
}

export default function ProvisioningPage() {
    const [customers, setCustomers] = useState<Customer[]>([]);
    const [sessions, setSessions] = useState<NasSession[]>([]);
    const [nasStatus, setNasStatus] = useState<any>(null);
    const [loading, setLoading] = useState(true);
    const [msg, setMsg] = useState('');
    const [testUser, setTestUser] = useState('');
    const [testPass, setTestPass] = useState('');
    
    const [search, setSearch] = useState('');
    const [filterStatus, setFilterStatus] = useState('all');
    const [autoRefresh, setAutoRefresh] = useState(true);
    const [refreshInterval, setRefreshInterval] = useState(30);
    const [selectedSession, setSelectedSession] = useState<NasSession | null>(null);
    const [actionLoading, setActionLoading] = useState<number | null>(null);

    const load = async () => {
        setLoading(true);
        
        try {
            const [cust, nas, sess] = await Promise.all([
                api.getCustomers(),
                fetch('http://localhost:4003/api/nas/status').then(r => r.json()).catch(() => ({ online: false })),
                fetch('http://localhost:4003/api/nas/sessions').then(r => r.json()).catch(() => ({ sessions: [] }))
            ]);
            
            setCustomers(cust);
            setNasStatus(nas);
            setSessions(sess.sessions || []);
        } catch (e) {
            console.error('Error loading:', e);
        }
        
        setLoading(false);
    };

    useEffect(() => { load(); }, []);

    useEffect(() => {
        if (!autoRefresh) return;
        const interval = setInterval(load, refreshInterval * 1000);
        return () => clearInterval(interval);
    }, [autoRefresh, refreshInterval]);

    const filteredCustomers = useMemo(() => {
        return customers.filter(c => {
            const matchesSearch = search === '' || 
                c.name.toLowerCase().includes(search.toLowerCase()) ||
                c.email.toLowerCase().includes(search.toLowerCase()) ||
                c.id.toString().includes(search);
            
            const matchesStatus = filterStatus === 'all' || 
                (filterStatus === 'connected' && sessions.some(s => s.username === c.email.split('@')[0])) ||
                (filterStatus === 'online' && c.status === 'ACTIVE' && !sessions.some(s => s.username === c.email.split('@')[0])) ||
                (filterStatus === 'suspended' && c.status === 'SUSPENDED') ||
                (filterStatus === 'blocked' && c.status === 'BLOCKED');
            
            return matchesSearch && matchesStatus;
        });
    }, [customers, sessions, search, filterStatus]);

    const getCustomerStatus = (customer: Customer) => {
        const session = sessions.find(s => s.username === customer.email.split('@')[0]);
        if (session) return { state: 'CONNECTED', ip: session.ip, sessionId: session.sessionId, session };
        if (customer.status === 'ACTIVE') return { state: 'ONLINE', ip: null, sessionId: null, session: null };
        if (customer.status === 'SUSPENDED') return { state: 'SUSPENDED', ip: null, sessionId: null, session: null };
        if (customer.status === 'BLOCKED') return { state: 'BLOCKED', ip: null, sessionId: null, session: null };
        return { state: 'DISABLED', ip: null, sessionId: null, session: null };
    };

    const testAuth = async () => {
        if (!testUser || !testPass) {
            setMsg('Preencha usuario e senha');
            return;
        }
        setMsg('Testando autenticacao...');
        
        try {
            const res = await fetch('http://localhost:4003/api/nas/test-auth', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: testUser, password: testPass })
            });
            const data = await res.json();
            
            if (data.success) {
                setMsg('✅ Autenticacao OK - Usuario liberado!');
            } else {
                setMsg('❌ Falha na autenticacao');
            }
        } catch (e) {
            setMsg('❌ Erro ao testar');
        }
        
        setTimeout(() => setMsg(''), 3000);
    };

    const disconnectUser = async (username: string) => {
        if (!confirm(`Desconectar ${username}?`)) return;
        
        try {
            await fetch('http://localhost:4003/api/nas/disconnect', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username })
            });
            setMsg(`✅ ${username} desconectado`);
            load();
        } catch (e) {
            setMsg('❌ Erro ao desconectar');
        }
        
        setTimeout(() => setMsg(''), 3000);
    };

    const kickUser = async (username: string) => {
        if (!confirm(`Expulsar ${username} do sistema?`)) return;
        
        try {
            await fetch('http://localhost:4003/api/nas/kick', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username })
            });
            setMsg(`✅ ${username} expulso`);
            load();
        } catch (e) {
            setMsg('❌ Erro ao expelir');
        }
        
        setTimeout(() => setMsg(''), 3000);
    };

    const updateCustomerStatus = async (customerId: number, newStatus: string) => {
        const statusMap: Record<string, string> = {
            'activate': 'ACTIVE',
            'suspend': 'SUSPENDED',
            'block': 'BLOCKED'
        };
        
        setActionLoading(customerId);
        try {
            await api.updateCustomerStatus(customerId, statusMap[newStatus]);
            setMsg(`✅ Cliente ${statusMap[newStatus] === 'ACTIVE' ? 'ativado' : statusMap[newStatus] === 'SUSPENDED' ? 'suspendido' : 'bloqueado'} com sucesso!`);
            load();
        } catch (e) {
            setMsg('❌ Erro ao atualizar status');
        }
        setActionLoading(null);
        setTimeout(() => setMsg(''), 3000);
    };

    const getStatusColor = (state: string) => {
        if (state === 'CONNECTED') return '#22c55e';
        if (state === 'ONLINE') return '#3b82f6';
        if (state === 'SUSPENDED') return '#f59e0b';
        if (state === 'BLOCKED') return '#ef4444';
        return '#6b7280';
    };

    const formatBytes = (bytes: number) => {
        if (!bytes) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    };

    const formatTime = (seconds: number) => {
        if (!seconds) return '0s';
        const h = Math.floor(seconds / 3600);
        const m = Math.floor((seconds % 3600) / 60);
        const s = seconds % 60;
        if (h > 0) return `${h}h ${m}m`;
        if (m > 0) return `${m}m ${s}s`;
        return `${s}s`;
    };

    if (loading) return <div className="loader">⬡ Carregando provisionamento...</div>;

    return (
        <div>
            <div className="topbar">
                <span className="topbar-title">Provisionamento (RADIUS/NAS)</span>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <label style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 12, cursor: 'pointer' }}>
                        <input 
                            type="checkbox" 
                            checked={autoRefresh} 
                            onChange={e => setAutoRefresh(e.target.checked)}
                            style={{ width: 16, height: 16 }}
                        />
                        Auto-refresh
                    </label>
                    {autoRefresh && (
                        <select 
                            value={refreshInterval} 
                            onChange={e => setRefreshInterval(Number(e.target.value))}
                            style={{ padding: '4px 8px', fontSize: 12, borderRadius: 4 }}
                        >
                            <option value={10}>10s</option>
                            <option value={30}>30s</option>
                            <option value={60}>1min</option>
                        </select>
                    )}
                    <span style={{ 
                        width: 8, 
                        height: 8, 
                        borderRadius: '50%', 
                        background: autoRefresh ? '#22c55e' : '#6b7280',
                        animation: autoRefresh ? 'pulse 2s infinite' : 'none'
                    }} />
                </div>
            </div>
            
            <div className="content">
                {msg && (
                    <div className={`toast ${msg.includes('✅') ? 'toast-success' : msg.includes('❌') ? 'toast-error' : 'toast-warning'}`}>
                        {msg}
                    </div>
                )}
                
                <div className="stats-grid" style={{ marginBottom: 20 }}>
                    <div className="stat-card">
                        <div className="stat-value" style={{ color: nasStatus?.online ? '#22c55e' : '#ef4444' }}>
                            {nasStatus?.online ? 'ONLINE' : 'OFFLINE'}
                        </div>
                        <div className="stat-label">Status NAS</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value">{sessions.length}</div>
                        <div className="stat-label">Sessoes Ativas</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value">{customers.filter(c => c.status === 'ACTIVE').length}</div>
                        <div className="stat-label">Clientes Ativos</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value" style={{ color: '#f59e0b' }}>{customers.filter(c => c.status === 'SUSPENDED').length}</div>
                        <div className="stat-label">Suspensos</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value" style={{ color: '#ef4444' }}>{customers.filter(c => c.status === 'BLOCKED').length}</div>
                        <div className="stat-label">Bloqueados</div>
                    </div>
                </div>

                <div className="card" style={{ marginBottom: 20, padding: 15 }}>
                    <h3 style={{ marginBottom: 15 }}>Testar Autenticacao RADIUS</h3>
                    <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                        <input 
                            type="text" 
                            placeholder="Usuario" 
                            value={testUser}
                            onChange={e => setTestUser(e.target.value)}
                            style={{ flex: 1, minWidth: 150 }}
                        />
                        <input 
                            type="password" 
                            placeholder="Senha" 
                            value={testPass}
                            onChange={e => setTestPass(e.target.value)}
                            style={{ flex: 1, minWidth: 150 }}
                        />
                        <button className="btn btn-primary" onClick={testAuth}>
                            🔐 Testar
                        </button>
                        <button className="btn btn-secondary" onClick={load}>
                            🔄 Atualizar
                        </button>
                    </div>
                </div>

                <div className="card" style={{ marginBottom: 20, padding: 15 }}>
                    <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', alignItems: 'center' }}>
                        <input 
                            type="text" 
                            placeholder="Buscar por nome, email ou ID..." 
                            value={search}
                            onChange={e => setSearch(e.target.value)}
                            style={{ flex: 1, minWidth: 200 }}
                        />
                        <select 
                            value={filterStatus} 
                            onChange={e => setFilterStatus(e.target.value)}
                            style={{ padding: '8px 12px', borderRadius: 6, minWidth: 150 }}
                        >
                            <option value="all">Todos</option>
                            <option value="connected">Conectados</option>
                            <option value="online">Online (Ativos)</option>
                            <option value="suspended">Suspensos</option>
                            <option value="blocked">Bloqueados</option>
                        </select>
                        <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                            {filteredCustomers.length} de {customers.length} clientes
                        </span>
                    </div>
                </div>

                <div className="card" style={{ padding: 0 }}>
                    <div className="table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Cliente</th>
                                    <th>Estado</th>
                                    <th>IP</th>
                                    <th>Sessao</th>
                                    <th>Acoes</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredCustomers.map(c => {
                                    const status = getCustomerStatus(c);
                                    return (
                                        <tr key={c.id}>
                                            <td>CLI-{c.id}</td>
                                            <td>
                                                <div style={{ fontWeight: 600 }}>{c.name}</div>
                                                <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{c.email}</div>
                                            </td>
                                            <td>
                                                <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                                    <span className="pulse" style={{ background: getStatusColor(status.state) }} />
                                                    <span style={{ color: getStatusColor(status.state), fontWeight: 700 }}>
                                                        {status.state}
                                                    </span>
                                                </span>
                                            </td>
                                            <td style={{ fontFamily: 'monospace' }}>{status.ip || '-'}</td>
                                            <td style={{ fontSize: 11, fontFamily: 'monospace' }}>
                                                {status.sessionId ? (
                                                    <button 
                                                        style={{ 
                                                            background: 'none', 
                                                            border: 'none', 
                                                            color: '#3b82f6', 
                                                            cursor: 'pointer',
                                                            textDecoration: 'underline'
                                                        }}
                                                        onClick={() => setSelectedSession(status.session)}
                                                    >
                                                        {status.sessionId.substring(0, 8)}...
                                                    </button>
                                                ) : '-'}
                                            </td>
                                            <td>
                                                <div style={{ display: 'flex', gap: 5, flexWrap: 'wrap' }}>
                                                    {status.state === 'CONNECTED' && (
                                                        <>
                                                            <button 
                                                                className="btn btn-warning" 
                                                                style={{ padding: '4px 8px', fontSize: 11 }}
                                                                onClick={() => disconnectUser(c.email.split('@')[0])}
                                                            >
                                                                Desconectar
                                                            </button>
                                                            <button 
                                                                className="btn btn-danger" 
                                                                style={{ padding: '4px 8px', fontSize: 11 }}
                                                                onClick={() => kickUser(c.email.split('@')[0])}
                                                            >
                                                                Expulsar
                                                            </button>
                                                        </>
                                                    )}
                                                    {c.status !== 'ACTIVE' && (
                                                        <button 
                                                            className="btn btn-primary" 
                                                            style={{ padding: '4px 8px', fontSize: 11 }}
                                                            onClick={() => updateCustomerStatus(c.id, 'activate')}
                                                            disabled={actionLoading === c.id}
                                                        >
                                                            {actionLoading === c.id ? '...' : 'Ativar'}
                                                        </button>
                                                    )}
                                                    {c.status === 'ACTIVE' && status.state !== 'CONNECTED' && (
                                                        <button 
                                                            className="btn btn-warning" 
                                                            style={{ padding: '4px 8px', fontSize: 11 }}
                                                            onClick={() => updateCustomerStatus(c.id, 'suspend')}
                                                            disabled={actionLoading === c.id}
                                                        >
                                                            {actionLoading === c.id ? '...' : 'Suspender'}
                                                        </button>
                                                    )}
                                                    {c.status !== 'BLOCKED' && (
                                                        <button 
                                                            className="btn btn-danger" 
                                                            style={{ padding: '4px 8px', fontSize: 11 }}
                                                            onClick={() => updateCustomerStatus(c.id, 'block')}
                                                            disabled={actionLoading === c.id}
                                                        >
                                                            {actionLoading === c.id ? '...' : 'Bloquear'}
                                                        </button>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                </div>

                {!nasStatus?.online && (
                    <div className="card" style={{ marginTop: 20, backgroundColor: 'rgba(239,68,68,0.1)', borderColor: 'rgba(239,68,68,0.3)' }}>
                        <p style={{ color: '#ef4444', margin: 0 }}>
                            ⚠️ NAS Offline. Inicie o container com: docker-compose up -d nas-simulator
                        </p>
                    </div>
                )}
            </div>

            {selectedSession && (
                <div className="modal-overlay" onClick={() => setSelectedSession(null)}>
                    <div className="modal" onClick={e => e.stopPropagation()}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                            <h3>Detalhes da Sessao</h3>
                            <button 
                                onClick={() => setSelectedSession(null)}
                                style={{ background: 'none', border: 'none', fontSize: 20, cursor: 'pointer' }}
                            >
                                ×
                            </button>
                        </div>
                        <div style={{ display: 'grid', gap: 10 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <span style={{ color: 'var(--text-muted)' }}>Session ID:</span>
                                <span style={{ fontFamily: 'monospace' }}>{selectedSession.sessionId}</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <span style={{ color: 'var(--text-muted)' }}>Username:</span>
                                <span>{selectedSession.username}</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <span style={{ color: 'var(--text-muted)' }}>IP Address:</span>
                                <span style={{ fontFamily: 'monospace' }}>{selectedSession.ip}</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <span style={{ color: 'var(--text-muted)' }}>NAS IP:</span>
                                <span style={{ fontFamily: 'monospace' }}>{selectedSession.nasIp}</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <span style={{ color: 'var(--text-muted)' }}>Start Time:</span>
                                <span>{new Date(selectedSession.startTime).toLocaleString()}</span>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <span style={{ color: 'var(--text-muted)' }}>Status:</span>
                                <span style={{ color: '#22c55e', fontWeight: 700 }}>{selectedSession.status}</span>
                            </div>
                            {selectedSession.bytesIn !== undefined && (
                                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <span style={{ color: 'var(--text-muted)' }}>Bytes In:</span>
                                    <span>{formatBytes(selectedSession.bytesIn)}</span>
                                </div>
                            )}
                            {selectedSession.bytesOut !== undefined && (
                                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <span style={{ color: 'var(--text-muted)' }}>Bytes Out:</span>
                                    <span>{formatBytes(selectedSession.bytesOut)}</span>
                                </div>
                            )}
                            {selectedSession.acctSessionTime !== undefined && (
                                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <span style={{ color: 'var(--text-muted)' }}>Duration:</span>
                                    <span>{formatTime(selectedSession.acctSessionTime)}</span>
                                </div>
                            )}
                        </div>
                        <div style={{ marginTop: 20, display: 'flex', gap: 10 }}>
                            <button 
                                className="btn btn-warning"
                                onClick={() => {
                                    disconnectUser(selectedSession.username);
                                    setSelectedSession(null);
                                }}
                            >
                                Desconectar
                            </button>
                            <button 
                                className="btn btn-secondary"
                                onClick={() => setSelectedSession(null)}
                            >
                                Fechar
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
