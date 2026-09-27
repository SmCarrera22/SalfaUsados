import {
    Bell,
} from 'lucide-react';

import { useAuth } from '../../auth/AuthContext';

const ROLE_LABELS = {
    ADMIN: 'Administrador',
    OPERATOR: 'Operador',
    ANALYST: 'Analista',
};

export default function Header() {
    const {
        account,
        roles,
    } = useAuth();

    const role =
        roles[0] ?? 'USER';

    const displayName =
        account?.name ??
        account?.username ??
        'Usuario';

    const initials = displayName
        .split(' ')
        .slice(0, 2)
        .map((part) => part[0])
        .join('')
        .toUpperCase();

    return (
        <header className="topbar">
            <div>
                <h2>Gestión de Usados</h2>
                <p>
                    Plataforma operacional Salfa360
                </p>
            </div>

            <div className="topbar-actions">
                <button
                    className="icon-button"
                    aria-label="Notificaciones"
                >
                    <Bell size={20} />
                </button>

                <div className="user-profile">
                    <div className="user-avatar">
                        {initials}
                    </div>

                    <div className="user-info">
                        <strong>{displayName}</strong>

                        <span>
              {ROLE_LABELS[role] ?? role}
            </span>
                    </div>
                </div>
            </div>
        </header>
    );
}