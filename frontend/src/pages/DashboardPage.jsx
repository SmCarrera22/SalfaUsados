import {
    useEffect,
    useMemo,
    useState,
} from 'react';

import {
    CarFront,
    CircleCheck,
    Wrench,
    Bookmark,
    TrendingUp,
    AlertCircle,
} from 'lucide-react';

import {
    Link,
} from 'react-router-dom';

import {
    vehicleApi,
} from '../api/apiClient';

import {
    useAuth,
} from '../auth/AuthContext';

import VehicleStatusBadge
    from '../components/vehicles/VehicleStatusBadge';

const ROLE_LABELS = {
    ADMIN: 'Administrador',
    OPERATOR: 'Operador',
    ANALYST: 'Analista',
};

export default function DashboardPage() {
    const {
        account,
        roles,
    } = useAuth();

    const [vehicles, setVehicles] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState('');

    const firstName =
        account?.name?.split(' ')[0] ??
        'Usuario';

    const role =
        roles[0] ?? 'USER';

    useEffect(() => {
        async function loadDashboard() {
            try {
                const data =
                    await vehicleApi.getAll();

                setVehicles(
                    Array.isArray(data)
                        ? data
                        : [],
                );
            } catch (err) {
                console.error(
                    'Error cargando dashboard:',
                    err,
                );

                setError(
                    err.message ??
                    'No fue posible cargar los indicadores.',
                );
            } finally {
                setLoading(false);
            }
        }

        loadDashboard();
    }, []);

    const metrics =
        useMemo(
            () => ({
                total:
                vehicles.length,

                available:
                vehicles.filter(
                    (vehicle) =>
                        vehicle.status ===
                        'AVAILABLE',
                ).length,

                workshop:
                vehicles.filter(
                    (vehicle) =>
                        vehicle.status ===
                        'WORKSHOP',
                ).length,

                reserved:
                vehicles.filter(
                    (vehicle) =>
                        vehicle.status ===
                        'RESERVED',
                ).length,
            }),
            [vehicles],
        );

    const recentVehicles =
        useMemo(
            () =>
                [...vehicles]
                    .sort(
                        (a, b) =>
                            new Date(
                                b.updatedAt ??
                                b.createdAt ??
                                0,
                            ) -
                            new Date(
                                a.updatedAt ??
                                a.createdAt ??
                                0,
                            ),
                    )
                    .slice(0, 5),
            [vehicles],
        );

    return (
        <div>
            <section className="page-heading">
                <div>
          <span className="eyebrow">
            RESUMEN GENERAL
          </span>

                    <h1>
                        Hola, {firstName}
                    </h1>

                    <p>
                        Revisa el estado general del
                        inventario de Salfa Usados.
                    </p>
                </div>

                <div className="role-pill">
                    {ROLE_LABELS[role] ?? role}
                </div>
            </section>

            {error && (
                <div className="alert alert-error dashboard-alert">
                    <AlertCircle size={19} />

                    <div>
                        <strong>
                            Información no disponible
                        </strong>

                        <span>{error}</span>
                    </div>
                </div>
            )}

            <section className="stats-grid">
                <StatCard
                    icon={<CarFront />}
                    label="Inventario total"
                    value={
                        loading
                            ? '...'
                            : metrics.total
                    }
                    helper="Vehículos registrados"
                />

                <StatCard
                    icon={<CircleCheck />}
                    label="Disponibles"
                    value={
                        loading
                            ? '...'
                            : metrics.available
                    }
                    helper="Listos para venta"
                />

                <StatCard
                    icon={<Wrench />}
                    label="En taller"
                    value={
                        loading
                            ? '...'
                            : metrics.workshop
                    }
                    helper="En proceso"
                />

                <StatCard
                    icon={<Bookmark />}
                    label="Reservados"
                    value={
                        loading
                            ? '...'
                            : metrics.reserved
                    }
                    helper="Con reserva activa"
                />
            </section>

            <section className="dashboard-grid">
                <article className="content-card">
                    <div className="card-header">
                        <div>
                            <h3>
                                Actividad reciente
                            </h3>

                            <p>
                                Últimos vehículos actualizados
                            </p>
                        </div>

                        <Link
                            to="/vehicles"
                            className="card-link"
                        >
                            Ver inventario
                        </Link>
                    </div>

                    {loading ? (
                        <div className="loading-state dashboard-loading">
                            <div className="spinner" />
                            Cargando...
                        </div>
                    ) : recentVehicles.length === 0 ? (
                        <div className="empty-state">
                            <CarFront size={34} />

                            <strong>
                                Sin vehículos registrados
                            </strong>

                            <p>
                                El inventario todavía no
                                contiene registros.
                            </p>
                        </div>
                    ) : (
                        <div className="recent-list">
                            {recentVehicles.map(
                                (vehicle) => (
                                    <div
                                        className="recent-vehicle"
                                        key={vehicle.id}
                                    >
                                        <div className="vehicle-icon">
                                            <CarFront size={18} />
                                        </div>

                                        <div className="recent-vehicle-info">
                                            <strong>
                                                {vehicle.brand}{' '}
                                                {vehicle.model}
                                            </strong>

                                            <span>
                        {vehicle.plate}
                                                {' · '}
                                                {vehicle.branch}
                      </span>
                                        </div>

                                        <VehicleStatusBadge
                                            status={
                                                vehicle.status
                                            }
                                        />
                                    </div>
                                ),
                            )}
                        </div>
                    )}
                </article>

                <article className="content-card">
                    <div className="card-header">
                        <div>
                            <h3>Tu acceso</h3>

                            <p>
                                Permisos de la sesión
                            </p>
                        </div>

                        <TrendingUp size={21} />
                    </div>

                    <div className="access-summary">
                        <span>Rol activo</span>

                        <strong>
                            {ROLE_LABELS[role] ??
                                role}
                        </strong>

                        <span>Identidad</span>

                        <strong>
                            Microsoft Entra ID
                        </strong>

                        <span>API</span>

                        <strong>
                            Salfa360 BFF
                        </strong>

                        <span>Estado</span>

                        <strong className="success-text">
                            Autenticado
                        </strong>
                    </div>
                </article>
            </section>
        </div>
    );
}

function StatCard({
                      icon,
                      label,
                      value,
                      helper,
                  }) {
    return (
        <article className="stat-card">
            <div className="stat-icon">
                {icon}
            </div>

            <div>
                <span>{label}</span>
                <strong>{value}</strong>
                <small>{helper}</small>
            </div>
        </article>
    );
}