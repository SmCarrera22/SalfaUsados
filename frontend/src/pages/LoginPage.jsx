import {
    CarFront,
    ShieldCheck,
    BarChart3,
} from 'lucide-react';

import { useAuth } from '../auth/AuthContext';

export default function LoginPage() {
    const { login } = useAuth();

    async function handleLogin() {
        try {
            await login();
        } catch (error) {
            console.error(
                'Error iniciando sesión con Microsoft:',
                error,
            );
        }
    }

    return (
        <div className="login-page">
            <section className="login-hero">
                <div className="login-brand">
                    <div className="brand-mark">
                        S
                    </div>

                    <span>Salfa360</span>
                </div>

                <div className="login-hero-content">
          <span className="eyebrow">
            Gestión inteligente
          </span>

                    <h1>
                        Control de inventario
                        en una sola plataforma.
                    </h1>

                    <p>
                        Consulta, administra y monitorea
                        el inventario de vehículos usados
                        de forma centralizada.
                    </p>

                    <div className="login-features">
                        <div>
                            <CarFront size={20} />
                            Inventario centralizado
                        </div>

                        <div>
                            <BarChart3 size={20} />
                            Información operacional
                        </div>

                        <div>
                            <ShieldCheck size={20} />
                            Acceso seguro por roles
                        </div>
                    </div>
                </div>
            </section>

            <section className="login-panel">
                <div className="login-card">
          <span className="eyebrow">
            Bienvenido
          </span>

                    <h2>Ingresa a Salfa360</h2>

                    <p>
                        Utiliza tu cuenta corporativa
                        Microsoft para continuar.
                    </p>

                    <button
                        className="primary-button login-button"
                        onClick={handleLogin}
                    >
                        <svg
                            width="20"
                            height="20"
                            viewBox="0 0 23 23"
                        >
                            <path
                                fill="#f35325"
                                d="M1 1h10v10H1z"
                            />
                            <path
                                fill="#81bc06"
                                d="M12 1h10v10H12z"
                            />
                            <path
                                fill="#05a6f0"
                                d="M1 12h10v10H1z"
                            />
                            <path
                                fill="#ffba08"
                                d="M12 12h10v10H12z"
                            />
                        </svg>

                        Continuar con Microsoft
                    </button>

                    <div className="login-security">
                        <ShieldCheck size={17} />

                        Autenticación protegida por
                        Microsoft Entra ID
                    </div>
                </div>
            </section>
        </div>
    );
}