import {
    Link,
} from 'react-router-dom';

export default function NotFoundPage() {
    return (
        <div className="status-page">
      <span className="status-code">
        404
      </span>

            <h1>Página no encontrada</h1>

            <p>
                La ruta solicitada no existe
                en Salfa360.
            </p>

            <Link
                className="primary-button"
                to="/dashboard"
            >
                Ir al dashboard
            </Link>
        </div>
    );
}