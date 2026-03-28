import { useEffect, useState } from 'react';
import { api } from '../api';

interface Stats {
    totalCustomers: number;
    activeCustomers: number;
    suspendedCustomers: number;
    blockedCustomers: number;
    paidRevenue: number;
    pendingRevenue: number;
    pendingInvoices: number;
    overdueInvoices: number;
    totalPlans: number;
    lockserverStatus: string;
    lockserverActiveLocks: number;
}

export default function Dashboard() {
    const [stats, setStats] = useState<Stats | null>(null);
    const [loading, setLoading] = useState(true);
    const [billing, setBilling] = useState('');

    const load = async () => {
        try {
            setStats(await api.getStats());
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { load(); const t = setInterval(load, 15000); return () => clearInterval(t); }, []);

    const handleBilling = async () => {
        setBilling('running');
        try {
            const r = await api.runBilling();
            setBilling(`✅ ${r.invoicesGenerated} faturas geradas`);
            load();
        } catch {
            setBilling('❌ Erro ao executar billing');
        }
        setTimeout(() => setBilling(''), 5000);
    };

    const fmt = (v: number) => `R$ ${v.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`;

    if (loading) return (
        <div>
            <div className="topbar"><span className="topbar-title">Dashboard</span></div>
            <div className="loader">⬡ Carregando...</div>
        </div>
    );

    const s = stats!;
    const activeRate = s.totalCustomers ? Math.round((s.activeCustomers / s.totalCustomers) * 100) : 0;

    return (
        <div>
            <div className="topbar">
                <span className="topbar-title">Dashboard Executivo</span>
                <div className="topbar-right">
                    <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                        <span className="pulse" />LIVE
                    </span>
                    <button className="btn btn-primary" onClick={handleBilling} disabled={billing === 'running'}>
                        ⚡ {billing === 'running' ? 'Processando...' : 'Executar Billing'}
                    </button>
                </div>
            </div>
            {billing && billing !== 'running' && (
                <div style={{ margin: '12px 32px 0', padding: '10px 16px', background: 'var(--bg-card)', borderRadius: 8, border: '1px solid var(--border)', fontSize: 13 }}>
                    {billing}
                </div>
            )}
            <div className="content">
                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-icon">👥</div>
                        <div className="stat-value">{s.totalCustomers}</div>
                        <div className="stat-label">Total Clientes</div>
                        <div className="stat-sub">↑ {s.activeCustomers} ativos</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-icon">💰</div>
                        <div className="stat-value" style={{ fontSize: 24 }}>{fmt(s.paidRevenue)}</div>
                        <div className="stat-label">Receita Paga</div>
                        <div className="stat-sub">Faturas liquidadas</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-icon">⏳</div>
                        <div className="stat-value" style={{ fontSize: 24 }}>{fmt(s.pendingRevenue)}</div>
                        <div className="stat-label">A Receber</div>
                        <div className="stat-sub">{s.pendingInvoices} faturas pendentes</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-icon">📋</div>
                        <div className="stat-value">{s.totalPlans}</div>
                        <div className="stat-label">Planos Ativos</div>
                        <div className="stat-sub">ISP / Telecom</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-icon">🔒</div>
                        <div className="stat-value">{s.lockserverActiveLocks}</div>
                        <div className="stat-label">Lockserver</div>
                        <div className="stat-sub">
                            <span className="pulse" />{s.lockserverStatus}
                        </div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-icon">🚫</div>
                        <div className="stat-value">{s.overdueInvoices}</div>
                        <div className="stat-label">Faturas Vencidas</div>
                        <div className="stat-sub">{s.blockedCustomers} bloqueados</div>
                    </div>
                </div>

                {/* Business Funnel */}
                <div className="card" style={{ marginBottom: 16 }}>
                    <div className="card-title">Funil de Billing Enterprise (Ciclo de Vida)</div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 20, gap: 12 }}>
                        {[
                            { label: '1. Venda', desc: 'Leads/Contratos', value: s.totalCustomers, color: '#6366f1' },
                            { label: '2. Provisionamento', desc: 'Ativos no Radius', value: s.activeCustomers, color: '#3b82f6' },
                            { label: '3. Mediação', desc: 'CDRs Processados', value: 'Live', color: '#06b6d4' },
                            { label: '4. Faturamento', desc: 'Faturas Geradas', value: s.pendingInvoices + s.overdueInvoices, color: '#8b5cf6' },
                            { label: '5. Arrecadação', desc: 'Receita Líquida', value: fmt(s.paidRevenue), color: 'var(--success)' },
                        ].map((step, i) => (
                            <div key={i} style={{ flex: 1, padding: 16, background: 'var(--bg-deep)', borderRadius: 12, border: '1px solid rgba(56,139,253,0.1)', textAlign: 'center', position: 'relative' }}>
                                <div style={{ fontSize: 11, color: step.color, fontWeight: 700, marginBottom: 4 }}>{step.label}</div>
                                <div style={{ fontSize: 18, fontWeight: 800 }}>{step.value}</div>
                                <div style={{ fontSize: 10, color: 'var(--text-muted)', marginTop: 2 }}>{step.desc}</div>
                                {i < 4 && <div style={{ position: 'absolute', right: -10, top: '40%', fontSize: 12, color: 'var(--border)', zIndex: 1 }}>→</div>}
                            </div>
                        ))}
                    </div>
                </div>

                {/* Customer health */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                    <div className="card">
                        <div className="card-title">Saúde da Base — Clientes</div>
                        <div style={{ marginTop: 16 }}>
                            {[
                                { label: 'Ativos', value: s.activeCustomers, color: 'var(--success)', pct: activeRate },
                                { label: 'Suspensos', value: s.suspendedCustomers, color: 'var(--warning)', pct: s.totalCustomers ? Math.round((s.suspendedCustomers / s.totalCustomers) * 100) : 0 },
                                { label: 'Bloqueados', value: s.blockedCustomers, color: 'var(--danger)', pct: s.totalCustomers ? Math.round((s.blockedCustomers / s.totalCustomers) * 100) : 0 },
                            ].map(row => (
                                <div key={row.label} style={{ marginBottom: 16 }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 4 }}>
                                        <span style={{ color: 'var(--text-dim)' }}>{row.label}</span>
                                        <span style={{ color: row.color, fontWeight: 700 }}>{row.value} ({row.pct}%)</span>
                                    </div>
                                    <div className="progress-bar">
                                        <div className="progress-fill" style={{ width: `${row.pct}%`, background: row.color }} />
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="card">
                        <div className="card-title">Status de Arrecadação</div>
                        <div style={{ marginTop: 16 }}>
                            {[
                                { label: 'Receita Paga (Conciliada)', value: s.paidRevenue, total: s.paidRevenue + s.pendingRevenue, color: 'var(--success)' },
                                { label: 'A Receber (Em Aberto)', value: s.pendingRevenue, total: s.paidRevenue + s.pendingRevenue, color: 'var(--warning)' },
                            ].map(row => {
                                const pct = row.total > 0 ? Math.round((row.value / row.total) * 100) : 0;
                                return (
                                    <div key={row.label} style={{ marginBottom: 16 }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 4 }}>
                                            <span style={{ color: 'var(--text-dim)' }}>{row.label}</span>
                                            <span style={{ color: row.color, fontWeight: 700 }}>{fmt(row.value)}</span>
                                        </div>
                                        <div className="progress-bar">
                                            <div className="progress-fill" style={{ width: `${pct}%`, background: row.color }} />
                                        </div>
                                    </div>
                                );
                            })}
                            <div style={{ marginTop: 16, padding: '12px 16px', background: 'var(--bg-deep)', borderRadius: 8, fontSize: 13 }}>
                                <div style={{ color: 'var(--text-muted)' }}>Total Previsto</div>
                                <div style={{ fontSize: 22, fontWeight: 800, color: 'var(--text)', marginTop: 4 }}>
                                    {fmt(s.paidRevenue + s.pendingRevenue)}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
