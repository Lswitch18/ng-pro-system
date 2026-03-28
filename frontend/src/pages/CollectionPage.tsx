import { useEffect, useState } from 'react';
import { api } from '../api';

interface OverdueCustomer {
    id: number;
    name: string;
    email: string;
    phone: string;
    status: string;
    totalDebt: number;
    overdueInvoices: {
        id: number;
        amount: number;
        dueDate: string;
        referenceMonth: string;
    }[];
}

export default function CollectionPage() {
    const [customers, setCustomers] = useState<OverdueCustomer[]>([]);
    const [loading, setLoading] = useState(true);
    const [msg, setMsg] = useState('');
    const [filter, setFilter] = useState('');
    const [selected, setSelected] = useState<number[]>([]);
    const [sending, setSending] = useState(false);
    const [statusFilter, setStatusFilter] = useState('OVERDUE');
    const [whatsappStatus, setWhatsappStatus] = useState<{connected: boolean, status: string}>({connected: false, status: 'loading'});

    useEffect(() => {
        api.getWhatsAppStatus().then(setWhatsappStatus).catch(() => setWhatsappStatus({connected: false, status: 'error'}));
    }, []);

    const load = () => {
        setLoading(true);
        api.getOverdueCustomers(statusFilter !== 'ALL' ? statusFilter : undefined)
            .then(setCustomers)
            .finally(() => setLoading(false));
    };

    useEffect(() => { load(); }, [statusFilter]);

    const filtered = customers.filter(c => 
        c.name.toLowerCase().includes(filter.toLowerCase()) ||
        c.email.toLowerCase().includes(filter.toLowerCase()) ||
        c.id.toString().includes(filter)
    );

    const totalDebt = filtered.reduce((s, c) => s + c.totalDebt, 0);

    const toggleSelect = (id: number) => {
        setSelected(prev => 
            prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
        );
    };

    const toggleSelectAll = () => {
        if (selected.length === filtered.length) {
            setSelected([]);
        } else {
            setSelected(filtered.map(c => c.id));
        }
    };

    const sendEmails = async (action: 'email' | 'signal') => {
        if (selected.length === 0) {
            setMsg('Selecione pelo menos um cliente');
            return;
        }
        setSending(true);
        setMsg(action === 'email' ? 'Enviando e-mails...' : 'Enviando sinais...');
        try {
            const result = await api.sendOverdueNotifications(selected, action);
            setMsg(`✅ ${result.message}`);
        } catch (e: any) {
            setMsg(`❌ Erro: ${e.message}`);
        }
        setSending(false);
        setTimeout(() => setMsg(''), 5000);
    };

    const sendWhatsApp = async () => {
        if (selected.length === 0) {
            setMsg('Selecione pelo menos um cliente');
            return;
        }
        if (!whatsappStatus.connected) {
            setMsg('WhatsApp nao esta conectado');
            return;
        }

        setSending(true);
        setMsg('Enviando mensagens via WhatsApp...');

        const selectedCustomers = filtered.filter(c => selected.includes(c.id));
        const recipients = selectedCustomers
            .filter(c => c.phone)
            .map(c => ({
                phone: c.phone.replace(/\D/g, ''),
                amount: c.totalDebt.toFixed(2)
            }));

        if (recipients.length === 0) {
            setMsg('Nenhum cliente com telefone cadastrado');
            setSending(false);
            return;
        }

        try {
            const result = await api.sendBulkWhatsApp(recipients, 'collection');
            setMsg(`✅ ${result.sent || 0} mensagens WhatsApp enviadas`);
        } catch (e: any) {
            setMsg(`❌ Erro: ${e.message}`);
        }
        setSending(false);
        setTimeout(() => setMsg(''), 5000);
    };

    const suspendCustomers = async () => {
        if (selected.length === 0) {
            setMsg('Selecione pelo menos um cliente');
            return;
        }
        if (!confirm(`Suspender ${selected.length} clientes selecionados?`)) return;
        
        setSending(true);
        setMsg('Suspendendo clientes...');
        try {
            const result = await api.suspendOverdueCustomers(selected);
            setMsg(`✅ ${result.message}`);
            load();
        } catch (e: any) {
            setMsg(`❌ Erro: ${e.message}`);
        }
        setSending(false);
        setTimeout(() => setMsg(''), 5000);
    };

    const runDunningManual = async () => {
        setMsg('Iniciando Régua de Cobrança...');
        await api.runDunning();
        setMsg('✅ Dunning concluído. Inadimplentes suspensos.');
        load();
        setTimeout(() => setMsg(''), 3000);
    };

    return (
        <div>
            <div className="topbar">
                <span className="topbar-title">Cobrança e Inadimplência</span>
                <div className="topbar-right">
                    <button className="btn btn-warning" onClick={runDunningManual}>
                        ⚡ Executar Régua (Dunning)
                    </button>
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
                        <div className="stat-value">{filtered.length}</div>
                        <div className="stat-label">Clientes Inadimplentes</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value">R$ {totalDebt.toFixed(2)}</div>
                        <div className="stat-label">Total em Aberto</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value">{selected.length}</div>
                        <div className="stat-label">Selecionados</div>
                    </div>
                </div>

                <div className="card" style={{ marginBottom: 20, padding: 15 }}>
                    <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', alignItems: 'center' }}>
                        <input 
                            type="text" 
                            placeholder="Buscar por nome, email ou ID..." 
                            value={filter}
                            onChange={e => setFilter(e.target.value)}
                            style={{ flex: 1, minWidth: 200 }}
                        />
                        <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
                            <option value="OVERDUE">Inadimplentes</option>
                            <option value="SUSPENDED">Suspensos</option>
                            <option value="ALL">Todos</option>
                        </select>
                    </div>
                    <div style={{ display: 'flex', gap: 10, marginTop: 15, flexWrap: 'wrap', alignItems: 'center' }}>
                        <button 
                            className="btn btn-primary" 
                            onClick={() => sendEmails('email')}
                            disabled={sending || selected.length === 0}
                        >
                            📧 E-mail
                        </button>
                        <button 
                            className="btn btn-success" 
                            onClick={sendWhatsApp}
                            disabled={sending || selected.length === 0 || !whatsappStatus.connected}
                            title={whatsappStatus.connected ? '' : 'WhatsApp nao conectado'}
                        >
                            💬 WhatsApp
                        </button>
                        <button 
                            className="btn btn-warning" 
                            onClick={() => sendEmails('signal')}
                            disabled={sending || selected.length === 0}
                        >
                            📟 Sinal
                        </button>
                        <button 
                            className="btn btn-danger" 
                            onClick={suspendCustomers}
                            disabled={sending || selected.length === 0}
                        >
                            🚫 Suspender
                        </button>
                        <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 8, fontSize: 12 }}>
                            <span style={{ 
                                width: 8, height: 8, borderRadius: '50%', 
                                backgroundColor: whatsappStatus.connected ? '#22c55e' : '#ef4444' 
                            }} />
                            <span style={{ color: whatsappStatus.connected ? '#22c55e' : '#ef4444' }}>
                                WhatsApp {whatsappStatus.connected ? 'Conectado' : 'Desconectado'}
                            </span>
                        </div>
                    </div>
                </div>

                <div className="card" style={{ padding: 0 }}>
                    <div className="table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th style={{ width: 40 }}>
                                        <input 
                                            type="checkbox" 
                                            checked={selected.length === filtered.length && filtered.length > 0}
                                            onChange={toggleSelectAll}
                                        />
                                    </th>
                                    <th>ID</th>
                                    <th>Cliente</th>
                                    <th>Contato</th>
                                    <th>Faturas Vencidas</th>
                                    <th>Valor Total</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map(c => (
                                    <tr 
                                        key={c.id} 
                                        className={selected.includes(c.id) ? 'row-selected clickable-row' : 'clickable-row'}
                                        onClick={() => toggleSelect(c.id)}
                                        style={{ cursor: 'pointer' }}
                                    >
                                        <td onClick={e => e.stopPropagation()}>
                                            <input 
                                                type="checkbox" 
                                                checked={selected.includes(c.id)}
                                                onChange={() => toggleSelect(c.id)}
                                            />
                                        </td>
                                        <td>#{c.id}</td>
                                        <td>
                                            <strong>{c.name}</strong>
                                        </td>
                                        <td>
                                            <div style={{ fontSize: 12 }}>{c.email}</div>
                                            <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{c.phone}</div>
                                        </td>
                                        <td>
                                            {c.overdueInvoices.length}
                                            <span style={{ fontSize: 11, color: 'var(--text-muted)', marginLeft: 5 }}>
                                                ({c.overdueInvoices.map(i => i.referenceMonth).join(', ')})
                                            </span>
                                        </td>
                                        <td style={{ fontWeight: 700, color: 'var(--danger-color)' }}>
                                            R$ {c.totalDebt.toFixed(2)}
                                        </td>
                                        <td onClick={e => e.stopPropagation()}>
                                            <span className={`badge ${c.status === 'SUSPENDED' ? 'badge-danger' : 'badge-warning'}`}>
                                                {c.status}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                                {filtered.length === 0 && (
                                    <tr>
                                        <td colSpan={7} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
                                            {loading ? 'Carregando...' : 'Nenhum cliente inadimplente encontrado'}
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}
