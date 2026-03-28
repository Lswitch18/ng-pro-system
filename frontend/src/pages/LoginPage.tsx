import { useState } from 'react';
import { api } from '../api';

interface Props {
    onLogin: (token: string, username: string, role: string) => void;
}

export default function LoginPage({ onLogin }: Props) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        console.log('Attempting login with:', username);
        try {
            const data = await api.login(username, password);
            onLogin(data.token, data.username, data.role);
        } catch (err: any) {
            console.error('Login failed:', err);
            setError('Usuário ou senha incorretos. Verifique os dados e tente novamente.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-page">
            <div className="login-card">
                <div className="login-logo">
                    <h1>ng-pro</h1>
                    <p>Enterprise Billing Ecosystem</p>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label className="form-label">Usuário</label>
                        <input
                            value={username}
                            onChange={e => setUsername(e.target.value)}
                            placeholder="Nome de usuário"
                            required
                            autoFocus
                        />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Senha</label>
                        <div style={{ position: 'relative' }}>
                            <input
                                type={showPassword ? 'text' : 'password'}
                                value={password}
                                onChange={e => setPassword(e.target.value)}
                                placeholder="••••••••"
                                required
                                style={{ paddingRight: '40px' }}
                            />
                            <button
                                type="button"
                                onClick={() => setShowPassword(!showPassword)}
                                style={{
                                    position: 'absolute',
                                    right: '12px',
                                    top: '50%',
                                    transform: 'translateY(-50%)',
                                    background: 'none',
                                    border: 'none',
                                    color: 'var(--text-muted)',
                                    cursor: 'pointer',
                                    fontSize: '18px',
                                    padding: '4px'
                                }}
                                title={showPassword ? "Ocultar senha" : "Mostrar senha"}
                            >
                                {showPassword ? '👁️‍🗨️' : '👁️'}
                            </button>
                        </div>
                    </div>
                    <button
                        type="submit"
                        className="btn btn-primary"
                        style={{ width: '100%', justifyContent: 'center', padding: '12px', marginTop: '8px', fontSize: '15px' }}
                        disabled={loading}
                    >
                        {loading ? 'Autenticando...' : '→ Entrar'}
                    </button>
                    {error && <p className="login-error">{error}</p>}
                </form>
                <p style={{ textAlign: 'center', marginTop: 20, fontSize: 11, color: 'var(--text-muted)' }}>
                    Powered by Spring Boot 3 + JWT
                </p>
            </div>
        </div>
    );
}
