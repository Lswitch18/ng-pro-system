import { useEffect, useState } from 'react';
import { api } from '../api';

interface Plan {
    id: number; name: string; description: string; basePrice: number;
    speed: string; dataCapMb: number; familyPlan: boolean; active: boolean;
    tier1LimitMb: number; tier2PricePerMb: number;
}

const fmt = (v: number) => `R$ ${v.toFixed(2).replace('.', ',')}`;

export default function PlansPage() {
    const [plans, setPlans] = useState<Plan[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        api.getPlans().then(setPlans).finally(() => setLoading(false));
    }, []);

    if (loading) return <div className="loader">⬡ Carregando planos...</div>;

    return (
        <div>
            <div className="topbar"><span className="topbar-title">Planos</span></div>
            <div className="content">
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: 16 }}>
                    {plans.map(p => (
                        <div key={p.id} className="card" style={{ position: 'relative' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
                                <div>
                                    <div style={{ fontWeight: 700, fontSize: 16 }}>{p.name}</div>
                                    <div style={{ color: 'var(--text-muted)', fontSize: 12, marginTop: 2 }}>{p.description}</div>
                                </div>
                                {p.familyPlan && <span className="badge badge-info">Família</span>}
                            </div>
                            <div style={{ fontSize: 32, fontWeight: 800, color: 'var(--accent)', marginBottom: 12 }}>
                                {fmt(p.basePrice)}
                                <span style={{ fontSize: 13, color: 'var(--text-muted)', fontWeight: 400 }}>/mês</span>
                            </div>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 6, fontSize: 13 }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <span style={{ color: 'var(--text-muted)' }}>Velocidade</span>
                                    <span style={{ fontWeight: 600 }}>{p.speed}</span>
                                </div>
                                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <span style={{ color: 'var(--text-muted)' }}>Franquia</span>
                                    <span>{p.dataCapMb === 0 ? '∞ Ilimitado' : `${(p.dataCapMb / 1024).toFixed(0)} GB`}</span>
                                </div>
                                {p.tier1LimitMb > 0 && (
                                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                        <span style={{ color: 'var(--text-muted)' }}>Excedente</span>
                                        <span style={{ color: 'var(--warning)' }}>R$ {p.tier2PricePerMb.toFixed(4)}/MB</span>
                                    </div>
                                )}
                            </div>
                            <div style={{ marginTop: 12 }}>
                                <span className={`badge ${p.active ? 'badge-success' : 'badge-muted'}`}>
                                    {p.active ? '● Ativo' : '○ Inativo'}
                                </span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}
