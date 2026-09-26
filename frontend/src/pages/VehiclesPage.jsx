import {
    useEffect,
    useMemo,
    useState,
} from 'react';

import {
    CarFront,
    Plus,
    Search,
    Pencil,
    Trash2,
    RefreshCw,
    AlertCircle,
    SlidersHorizontal,
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

const STATUS_OPTIONS = [
    {
        value: '',
        label: 'Todos los estados',
    },
    {
        value: 'AVAILABLE',
        label: 'Disponible',
    },
    {
        value: 'RESERVED',
        label: 'Reservado',
    },
    {
        value: 'WORKSHOP',
        label: 'En taller',
    },
    {
        value: 'SOLD',
        label: 'Vendido',
    },
];

export default function VehiclesPage() {
    const {
        isAdmin,
        isOperator,
    } = useAuth();

    const [vehicles, setVehicles] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState('');

    const [search, setSearch] =
        useState('');

    const [statusFilter, setStatusFilter] =
        useState('');

    const [deletingId, setDeletingId] =
        useState(null);

    const canCreateOrEdit =
        isAdmin || isOperator;

    async function loadVehicles() {
        setLoading(true);
        setError('');

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
                'Error cargando vehículos:',
                err,
            );

            setError(
                err.message ??
                'No fue posible cargar el inventario.',
            );
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadVehicles();
    }, []);

    const filteredVehicles =
        useMemo(() => {
            const normalizedSearch =
                search
                    .trim()
                    .toLowerCase();

            return vehicles.filter(
                (vehicle) => {
                    const matchesStatus =
                        !statusFilter ||
                        vehicle.status ===
                        statusFilter;

                    if (!matchesStatus) {
                        return false;
                    }

                    if (!normalizedSearch) {
                        return true;
                    }

                    const searchableValues = [
                        vehicle.plate,
                        vehicle.vin,
                        vehicle.brand,
                        vehicle.model,
                        vehicle.version,
                        vehicle.branch,
                    ];

                    return searchableValues.some(
                        (value) =>
                            String(value ?? '')
                                .toLowerCase()
                                .includes(
                                    normalizedSearch,
                                ),
                    );
                },
            );
        }, [
            vehicles,
            search,
            statusFilter,
        ]);

    async function handleDelete(vehicle) {
        const description =
            [
                vehicle.brand,
                vehicle.model,
                vehicle.plate,
            ]
                .filter(Boolean)
                .join(' ');

        const confirmed =
            window.confirm(
                `¿Seguro que deseas eliminar ${description}? Esta acción no se puede deshacer.`,
            );

        if (!confirmed) {
            return;
        }

        setDeletingId(vehicle.id);

        try {
            await vehicleApi.remove(
                vehicle.id,
            );

            setVehicles(
                (current) =>
                    current.filter(
                        (item) =>
                            item.id !== vehicle.id,
                    ),
            );
        } catch (err) {
            console.error(
                'Error eliminando vehículo:',
                err,
            );

            window.alert(
                err.message ??
                'No fue posible eliminar el vehículo.',
            );
        } finally {
            setDeletingId(null);
        }
    }

    return (
        <div>
            <section className="page-heading">
                <div>
          <span className="eyebrow">
            INVENTARIO
          </span>

                    <h1>Vehículos</h1>

                    <p>
                        Consulta y administra el
                        inventario nacional de usados.
                    </p>
                </div>

                {canCreateOrEdit && (
                    <Link
                        to="/vehicles/new"
                        className="primary-button"
                    >
                        <Plus size={18} />
                        Nuevo vehículo
                    </Link>
                )}
            </section>

            <section className="inventory-summary">
                <div>
          <span>
            Vehículos registrados
          </span>

                    <strong>
                        {vehicles.length}
                    </strong>
                </div>

                <div>
          <span>
            Resultados visibles
          </span>

                    <strong>
                        {filteredVehicles.length}
                    </strong>
                </div>

                <div>
          <span>
            Disponibles
          </span>

                    <strong>
                        {
                            vehicles.filter(
                                (vehicle) =>
                                    vehicle.status ===
                                    'AVAILABLE',
                            ).length
                        }
                    </strong>
                </div>

                <div>
          <span>
            En taller
          </span>

                    <strong>
                        {
                            vehicles.filter(
                                (vehicle) =>
                                    vehicle.status ===
                                    'WORKSHOP',
                            ).length
                        }
                    </strong>
                </div>
            </section>

            <section className="content-card inventory-card">
                <div className="inventory-toolbar">
                    <div className="search-box">
                        <Search size={18} />

                        <input
                            type="text"
                            value={search}
                            onChange={(event) =>
                                setSearch(
                                    event.target.value,
                                )
                            }
                            placeholder="Buscar por patente, VIN, marca, modelo o sucursal..."
                        />
                    </div>

                    <div className="filter-box">
                        <SlidersHorizontal
                            size={17}
                        />

                        <select
                            value={statusFilter}
                            onChange={(event) =>
                                setStatusFilter(
                                    event.target.value,
                                )
                            }
                        >
                            {STATUS_OPTIONS.map(
                                (option) => (
                                    <option
                                        key={option.value}
                                        value={option.value}
                                    >
                                        {option.label}
                                    </option>
                                ),
                            )}
                        </select>
                    </div>

                    <button
                        type="button"
                        className="secondary-button"
                        onClick={loadVehicles}
                        disabled={loading}
                    >
                        <RefreshCw
                            size={17}
                            className={
                                loading
                                    ? 'spin'
                                    : ''
                            }
                        />

                        Actualizar
                    </button>
                </div>

                {error && (
                    <div className="alert alert-error">
                        <AlertCircle size={19} />

                        <div>
                            <strong>
                                No pudimos cargar el inventario
                            </strong>

                            <span>{error}</span>
                        </div>
                    </div>
                )}

                {loading ? (
                    <div className="loading-state">
                        <div className="spinner" />

                        <strong>
                            Cargando inventario...
                        </strong>

                        <span>
              Consultando información
              desde Salfa360
            </span>
                    </div>
                ) : filteredVehicles.length === 0 ? (
                    <div className="empty-state">
                        <CarFront size={38} />

                        <strong>
                            No encontramos vehículos
                        </strong>

                        <p>
                            Modifica los filtros o registra
                            un nuevo vehículo.
                        </p>
                    </div>
                ) : (
                    <div className="table-container">
                        <table className="data-table">
                            <thead>
                            <tr>
                                <th>Vehículo</th>
                                <th>Patente</th>
                                <th>Año</th>
                                <th>Kilometraje</th>
                                <th>Sucursal</th>
                                <th>Estado</th>

                                {canCreateOrEdit && (
                                    <th className="actions-column">
                                        Acciones
                                    </th>
                                )}
                            </tr>
                            </thead>

                            <tbody>
                            {filteredVehicles.map(
                                (vehicle) => (
                                    <tr key={vehicle.id}>
                                        <td>
                                            <div className="vehicle-cell">
                                                <div className="vehicle-icon">
                                                    <CarFront
                                                        size={19}
                                                    />
                                                </div>

                                                <div>
                                                    <strong>
                                                        {
                                                            vehicle.brand
                                                        }{' '}
                                                        {
                                                            vehicle.model
                                                        }
                                                    </strong>

                                                    <span>
                              {
                                  vehicle.version ||
                                  'Sin versión'
                              }
                            </span>
                                                </div>
                                            </div>
                                        </td>

                                        <td>
                        <span className="plate">
                          {vehicle.plate}
                        </span>
                                        </td>

                                        <td>
                                            {vehicle.year}
                                        </td>

                                        <td>
                                            {formatMileage(
                                                vehicle.mileage,
                                            )}
                                        </td>

                                        <td>
                                            {vehicle.branch}
                                        </td>

                                        <td>
                                            <VehicleStatusBadge
                                                status={
                                                    vehicle.status
                                                }
                                            />
                                        </td>

                                        {canCreateOrEdit && (
                                            <td>
                                                <div className="table-actions">
                                                    <Link
                                                        to={
                                                            `/vehicles/${vehicle.id}/edit`
                                                        }
                                                        className="table-action-button"
                                                        title="Editar vehículo"
                                                    >
                                                        <Pencil
                                                            size={16}
                                                        />
                                                    </Link>

                                                    {isAdmin && (
                                                        <button
                                                            type="button"
                                                            className="table-action-button danger"
                                                            title="Eliminar vehículo"
                                                            disabled={
                                                                deletingId ===
                                                                vehicle.id
                                                            }
                                                            onClick={() =>
                                                                handleDelete(
                                                                    vehicle,
                                                                )
                                                            }
                                                        >
                                                            <Trash2
                                                                size={16}
                                                            />
                                                        </button>
                                                    )}
                                                </div>
                                            </td>
                                        )}
                                    </tr>
                                ),
                            )}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>
        </div>
    );
}

function formatMileage(value) {
    if (
        value === null ||
        value === undefined
    ) {
        return '—';
    }

    return new Intl.NumberFormat(
        'es-CL',
    ).format(value) + ' km';
}