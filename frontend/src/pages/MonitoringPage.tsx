import { useEffect, useState } from 'react';
import { api } from '../api';

interface Stats {
    lockserverStatus: string;
    lockserverActiveLocks: number;
    totalCustomers: number;
    activeCustomers: number;
    pendingInvoices: number;
}

const events = [
    { time: '11:54:01', type: 'USAGE', msg: 'Customer [3] DATA usage: 512.0 MB processed' },
    { time: '11:53:44', type: 'LOCK', msg: 'Lock ACQUIRED for GLOBAL_BILLING_RUN by MANUAL_BILLING_1743' },
    { time: '11:53:44', type: 'BILLING', msg: 'STARTING Enterprise Billing Run: MANUAL_BILLING_1743' },
    { time: '11:53:45', type: 'BILLING', msg: 'Computing tiered pricing for Customer [1]: base R$129.90 | overage R$0.00' },
    { time: '11:53:45', type: 'BILLING', msg: 'Computing tiered pricing for Customer [3]: base R$199.90 | overage R$24.00' },
    { time: '11:53:45', type: 'SAP', msg: '[ERP/SAP] Syncing invoice for joao.silva@email.com: R$129.90' },
    { time: '11:53:45', type: 'SAP', msg: '[ERP/SAP] Syncing invoice for carlos.f@email.com: R$223.90' },
    { time: '11:53:45', type: 'LOCK', msg: 'Lock RELEASED for GLOBAL_BILLING_RUN by MANUAL_BILLING_1743' },
    { time: '11:53:46', type: 'RADIUS', msg: '[RADIUS/AAA] Customer [4] ACCESS=false (SUSPENDED)' },
    { time: '11:52:10', type: 'USAGE', msg: 'Customer [6] DATA usage: 310.0 MB processed' },
    { time: '11:51:00', type: 'RADIUS', msg: '[RADIUS/AAA] Customer [1] ACCESS=true (ACTIVE)' },
];

const typeColor: Record<string, string> = {
    USAGE: 'var(--accent)',
    LOCK: 'var(--accent2)',
    BILLING: 'var(--success)',
    SAP: 'var(--warning)',
    RADIUS: '#22d3ee',
};

export default function MonitoringPage() {
    const [stats, setStats] = useState<Stats | null>(null);

    useEffect(() => {
        api.getStats().then(setStats);
        const t = setInterval(() => api.getStats().then(setStats), 10000);
        return () => clearInterval(t);
    }, []);

    return (
        <div>
            <div className="topbar">
                <span className="topbar-title">Monitoramento</span>
                <div className="topbar-right">
                    <span style={{ fontSize: 12, color: 'var(--success)' }}>
                        <span className="pulse" />Sistema Operacional
                    </span>
                </div>
            </div>
            <div className="content">
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16, marginBottom: 24 }}>
                    <div className="stat-card">
                        <div className="stat-icon">🔒</div>
                        <div className="stat-value">{stats?.lockserverActiveLocks ?? '—'}</div>
                        <div className="stat-label">Locks Ativos</div>
                        <div className="stat-sub"><span className="pulse" />{stats?.lockserverStatus ?? 'RUNNING'}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-icon">📡</div>
                        <div className="stat-value">{stats?.activeCustomers ?? '—'}</div>
                        <div className="stat-label">Sessões Radius Ativas</div>
                        <div className="stat-sub">AAA Auth</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-icon">⚡</div>
                        <div className="stat-value">{stats?.pendingInvoices ?? '—'}</div>
                        <div className="stat-label">Faturas na Fila</div>
                        <div className="stat-sub">CDR / Kinesis sim</div>
                    </div>
                </div>

                <div className="card">
                    <div className="card-title">Log de Eventos do Sistema — Tempo Real</div>
                    <div style={{ marginTop: 16, fontFamily: 'monospace', fontSize: 12 }}>
                        {events.map((e, i) => (
                            <div key={i} style={{
                                display: 'flex', gap: 12, padding: '8px 0',
                                borderBottom: '1px solid rgba(56,139,253,0.06)',
                                alignItems: 'flex-start'
                            }}>
                                <span style={{ color: 'var(--text-muted)', minWidth: 60 }}>{e.time}</span>
                                <span style={{
                                    color: typeColor[e.type] || 'var(--text)',
                                    minWidth: 70, fontWeight: 700, fontSize: 10,
                                    background: `${typeColor[e.type]}18`,
                                    padding: '1px 6px', borderRadius: 4
                                }}>{e.type}</span>
                                <span style={{ color: 'var(--text-dim)' }}>{e.msg}</span>
                            </div>
                        ))}
                    </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginTop: 16 }}>
                    <div className="card">
                        <div className="card-title">Integrations Hub</div>
                        <div style={{ marginTop: 12 }}>
                            {[
                                { name: 'SAP S/4HANA', status: 'CONNECTED', latency: '42ms' },
                                { name: 'RADIUS/AAA', status: 'CONNECTED', latency: '8ms' },
                                { name: 'WhatsApp', status: 'CONNECTED', latency: '180ms' },
                                { name: 'Kinesis CDR', status: 'SIMULATED', latency: '—' },
                            ].map(s => (
                                <div key={s.name} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 0', borderBottom: '1px solid rgba(56,139,253,0.06)', fontSize: 13 }}>
                                    <span style={{ fontWeight: 600 }}>{s.name}</span>
                                    <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                                        <span style={{ color: 'var(--text-muted)', fontSize: 11 }}>{s.latency}</span>
                                        <span className={`badge ${s.status === 'CONNECTED' ? 'badge-success' : 'badge-info'}`}>{s.status}</span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                    <div className="card">
                        <div className="card-title">Lockserver — Recursos</div>
                        <div style={{ marginTop: 12 }}>
                            {[
                                { resource: 'GLOBAL_BILLING_RUN', state: 'FREE' },
                                { resource: 'INVOICE_GEN_CLI_3', state: 'FREE' },
                                { resource: 'CDR_PROCESSOR', state: 'FREE' },
                            ].map(r => (
                                <div key={r.resource} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid rgba(56,139,253,0.06)', fontSize: 12, fontFamily: 'monospace' }}>
                                    <span style={{ color: 'var(--text-dim)' }}>{r.resource}</span>
                                    <span className={`badge ${r.state === 'FREE' ? 'badge-success' : 'badge-danger'}`}>{r.state}</span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
