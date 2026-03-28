import { useEffect, useState } from 'react';
import { api } from '../api';

interface NasSession {
    sessionId: string;
    username: string;
    ip: string;
    nasIp: string;
    startTime: string;
    status: string;
}

export default function ProvisioningPage() {
    const [sessions, setSessions] = useState<NasSession[]>([]);
    const [nasStatus, setNasStatus] = useState<any>(null);
    const [loading, setLoading] = useState(true);
    const [msg, setMsg] = useState('');
    const [testUser, setTestUser] = useState('');
    const [testPass, setTestPass] = useState('');

    const load = () => {
        setLoading(true);
        Promise.all([
            fetch('http://localhost:4003/api/nas/status').then(r => r.json()),
            fetch('http://localhost:4003/api/nas/sessions').then(r => r.json())
        ]).then(([status, sessionsData]) => {
            setNasStatus(status);
            setSessions(sessionsData.sessions || []);
        }).catch(() => {
            setNasStatus({ online: false, error: 'NAS unavailable' });
        }).finally(() => setLoading(false));
    };

    useEffect(() => { load(); }, []);

    const testAuth = async () => {
        if (!testUser || !testPass) {
            setMsg('Preencha usuário e senha');
            return;
        }
        setMsg('Testando autenticação...');
        
        try {
            const res = await fetch('http://localhost:4003/api/nas/test-auth', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: testUser, password: testPass })
            });
            const data = await res.json();
            
            if (data.success) {
                setMsg('✅ Autenticação OK - Usuário liberado!');
            } else {
                setMsg('❌ Falha na autenticação');
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

    if (loading) return <div className="loader">⬡ Carregando provisionamento...</div>;

    return (
        <div>
            <div className="topbar">
                <span className="topbar-title">Provisionamento e NAS</span>
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
                        <div className="stat-label">Sessões Ativas</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value">{nasStatus?.nasIp || 'N/A'}</div>
                        <div className="stat-label">IP do NAS</div>
                    </div>
                </div>

                <div className="card" style={{ marginBottom: 20, padding: 15 }}>
                    <h3 style={{ marginBottom: 15 }}>Testar Autenticação RADIUS</h3>
                    <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                        <input 
                            type="text" 
                            placeholder="Usuário" 
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

                <div className="card" style={{ padding: 0 }}>
                    <div className="table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th>Sessão</th>
                                    <th>Usuário</th>
                                    <th>IP Atribuído</th>
                                    <th>NAS</th>
                                    <th>Início</th>
                                    <th>Ações</th>
                                </tr>
                            </thead>
                            <tbody>
                                {sessions.map(s => (
                                    <tr key={s.sessionId}>
                                        <td style={{ fontFamily: 'monospace', fontSize: 11 }}>{s.sessionId.substring(0, 8)}...</td>
                                        <td><strong>{s.username}</strong></td>
                                        <td>{s.ip}</td>
                                        <td>{s.nasIp}</td>
                                        <td style={{ fontSize: 12 }}>{new Date(s.startTime).toLocaleString()}</td>
                                        <td>
                                            <div style={{ display: 'flex', gap: 5 }}>
                                                <button 
                                                    className="btn btn-warning" 
                                                    style={{ padding: '4px 8px', fontSize: 11 }}
                                                    onClick={() => disconnectUser(s.username)}
                                                >
                                                    Desconectar
                                                </button>
                                                <button 
                                                    className="btn btn-danger" 
                                                    style={{ padding: '4px 8px', fontSize: 11 }}
                                                    onClick={() => kickUser(s.username)}
                                                >
                                                    Kick
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                                {sessions.length === 0 && (
                                    <tr>
                                        <td colSpan={6} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
                                            Nenhuma sessão ativa
                                        </td>
                                    </tr>
                                )}
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
        </div>
    );
}
