import { useEffect, useState } from 'react';
import { api } from '../api';

interface Customer {
    id: number; name: string; email: string; phone: string; cpfCnpj: string;
    status: string; planId: number; city: string; state: string; monthlyUsageMb: number;
}

const statusBadge = (s: string) => {
    const m: Record<string, string> = { ACTIVE: 'badge-success', SUSPENDED: 'badge-warning', BLOCKED: 'badge-danger' };
    const l: Record<string, string> = { ACTIVE: 'Ativo', SUSPENDED: 'Suspenso', BLOCKED: 'Bloqueado' };
    return <span className={`badge ${m[s] || 'badge-muted'}`}>{l[s] || s}</span>;
};

export default function CustomersPage() {
    const [customers, setCustomers] = useState<Customer[]>([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [filter, setFilter] = useState('ALL');

    const load = async () => {
        try { setCustomers(await api.getCustomers()); }
        finally { setLoading(false); }
    };

    useEffect(() => { load(); }, []);

    const changeStatus = async (id: number, status: string) => {
        await api.updateCustomerStatus(id, status);
        load();
    };

    const filtered = customers.filter(c => {
        const matchSearch = c.name.toLowerCase().includes(search.toLowerCase()) ||
            c.email.toLowerCase().includes(search.toLowerCase());
        const matchFilter = filter === 'ALL' || c.status === filter;
        return matchSearch && matchFilter;
    });

    if (loading) return <div className="loader">⬡ Carregando clientes...</div>;

    return (
        <div>
            <div className="topbar">
                <span className="topbar-title">Clientes</span>
            </div>
            <div className="content">
                <div className="section-header">
                    <div style={{ display: 'flex', gap: 10 }}>
                        <input value={search} onChange={e => setSearch(e.target.value)} placeholder="🔍 Buscar cliente..." style={{ width: 240 }} />
                        <select value={filter} onChange={e => setFilter(e.target.value)} style={{ width: 150 }}>
                            <option value="ALL">Todos</option>
                            <option value="ACTIVE">Ativos</option>
                            <option value="SUSPENDED">Suspensos</option>
                            <option value="BLOCKED">Bloqueados</option>
                        </select>
                    </div>
                    <span style={{ color: 'var(--text-muted)', fontSize: 13 }}>{filtered.length} cliente(s)</span>
                </div>
                <div className="card" style={{ padding: 0 }}>
                    <div className="table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th><th>Nome</th><th>Email</th><th>Telefone</th>
                                    <th>Cidade/UF</th><th>Uso (MB)</th><th>Status</th><th>Ações</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map(c => (
                                    <tr key={c.id}>
                                        <td style={{ color: 'var(--text-muted)', fontFamily: 'monospace' }}>#{c.id}</td>
                                        <td style={{ fontWeight: 600 }}>{c.name}</td>
                                        <td style={{ color: 'var(--text-dim)' }}>{c.email}</td>
                                        <td>{c.phone}</td>
                                        <td>{c.city}/{c.state}</td>
                                        <td style={{ fontFamily: 'monospace' }}>{c.monthlyUsageMb.toLocaleString()}</td>
                                        <td>{statusBadge(c.status)}</td>
                                        <td>
                                            <div style={{ display: 'flex', gap: 6 }}>
                                                {c.status !== 'ACTIVE' && (
                                                    <button className="btn btn-success" style={{ padding: '4px 10px', fontSize: 11 }} onClick={() => changeStatus(c.id, 'ACTIVE')}>Ativar</button>
                                                )}
                                                {c.status === 'ACTIVE' && (
                                                    <button className="btn btn-warning" style={{ padding: '4px 10px', fontSize: 11 }} onClick={() => changeStatus(c.id, 'SUSPENDED')}>Suspender</button>
                                                )}
                                                {c.status !== 'BLOCKED' && (
                                                    <button className="btn btn-danger" style={{ padding: '4px 10px', fontSize: 11 }} onClick={() => changeStatus(c.id, 'BLOCKED')}>Bloquear</button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}
