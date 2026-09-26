import {
    ShieldX,
} from 'lucide-react';

import {
    Link,
} from 'react-router-dom';

export default function UnauthorizedPage() {
    return (
        <div className="status-page">
            <ShieldX size={52} />

            <h1>Acceso restringido</h1>

            <p>
                Tu rol no tiene permisos para
                acceder a esta sección.
            </p>

            <Link
                className="primary-button"
                to="/dashboard"
            >
                Volver al dashboard
            </Link>
        </div>
    );
}