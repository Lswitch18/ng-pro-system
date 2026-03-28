import { useEffect, useState } from 'react';
import { api } from '../api';

interface Invoice {
    id: number; customerId: number; planId: number; status: string;
    baseAmount: number; overageAmount: number; totalAmount: number;
    dueDate: string; paidAt: string; referenceMonth: string; billingProcessId: string;
}

const statusBadge = (s: string) => {
    const m: Record<string, string> = { PAID: 'badge-success', PENDING: 'badge-warning', OVERDUE: 'badge-danger', CANCELLED: 'badge-muted' };
    const l: Record<string, string> = { PAID: 'Pago', PENDING: 'Pendente', OVERDUE: 'Vencido', CANCELLED: 'Cancelado' };
    return <span className={`badge ${m[s] || 'badge-muted'}`}>{l[s] || s}</span>;
};

const fmt = (v: number) => `R$ ${v.toFixed(2).replace('.', ',')}`;

export default function InvoicesPage() {
    const [invoices, setInvoices] = useState<Invoice[]>([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('ALL');

    const load = () => api.getInvoices().then(setInvoices).finally(() => setLoading(false));

    useEffect(() => { load(); }, []);

    const pay = async (id: number) => {
        await api.payInvoice(id);
        load();
    };

    const filtered = filter === 'ALL' ? invoices : invoices.filter(i => i.status === filter);

    if (loading) return <div className="loader">⬡ Carregando faturas...</div>;

    const total = filtered.reduce((s, i) => s + i.totalAmount, 0);
    const paid = filtered.filter(i => i.status === 'PAID').reduce((s, i) => s + i.totalAmount, 0);

    return (
        <div>
            <div className="topbar">
                <span className="topbar-title">Faturas</span>
                <div className="topbar-right" style={{ fontSize: 13, color: 'var(--text-muted)' }}>
                    Total filtrado: <strong style={{ color: 'var(--text)', marginLeft: 4 }}>{fmt(total)}</strong>
                    <span style={{ margin: '0 6px' }}>|</span>
                    Pago: <strong style={{ color: 'var(--success)', marginLeft: 4 }}>{fmt(paid)}</strong>
                </div>
            </div>
            <div className="content">
                <div className="section-header">
                    <select value={filter} onChange={e => setFilter(e.target.value)} style={{ width: 160 }}>
                        <option value="ALL">Todas</option>
                        <option value="PENDING">Pendentes</option>
                        <option value="PAID">Pagas</option>
                        <option value="OVERDUE">Vencidas</option>
                    </select>
                    <span style={{ color: 'var(--text-muted)', fontSize: 13 }}>{filtered.length} fatura(s)</span>
                </div>
                <div className="card" style={{ padding: 0 }}>
                    <div className="table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th><th>Cliente</th><th>Referência</th>
                                    <th>Base</th><th>Excedente</th><th>Total</th>
                                    <th>Vencimento</th><th>Status</th><th>Ação</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map(inv => (
                                    <tr key={inv.id}>
                                        <td style={{ fontFamily: 'monospace', color: 'var(--text-muted)' }}>#{inv.id}</td>
                                        <td>CLI-{inv.customerId}</td>
                                        <td><span className="badge badge-info">{inv.referenceMonth}</span></td>
                                        <td>{fmt(inv.baseAmount)}</td>
                                        <td style={{ color: inv.overageAmount > 0 ? 'var(--warning)' : 'var(--text-muted)' }}>
                                            {fmt(inv.overageAmount)}
                                        </td>
                                        <td style={{ fontWeight: 700 }}>{fmt(inv.totalAmount)}</td>
                                        <td style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                                            {inv.dueDate ? new Date(inv.dueDate).toLocaleDateString('pt-BR') : '—'}
                                        </td>
                                        <td>{statusBadge(inv.status)}</td>
                                        <td>
                                            {inv.status === 'PENDING' && (
                                                <button className="btn btn-success" style={{ padding: '4px 10px', fontSize: 11 }} onClick={() => pay(inv.id)}>
                                                    ✓ Pagar
                                                </button>
                                            )}
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
