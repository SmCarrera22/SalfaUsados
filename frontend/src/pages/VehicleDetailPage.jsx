
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';

import {
    ArrowLeft,
    AlertCircle,
    CarFront,
    ClipboardList,
    History,
    RefreshCw,
    Wrench,
} from 'lucide-react';

import {
    vehicleApi,
    workshopApi,
} from '../api/apiClient';

import VehicleStatusBadge
    from '../components/vehicles/VehicleStatusBadge';

const EVENT_LABELS = {
    VEHICLE_CREATED: 'Vehículo registrado',
    VEHICLE_UPDATED: 'Vehículo actualizado',
    VEHICLE_DELETED: 'Vehículo eliminado',
};

function formatDate(value) {
    if (!value) return '—';

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return String(value);
    }

    return new Intl.DateTimeFormat('es-CL', {
        dateStyle: 'medium',
        timeStyle: 'short',
    }).format(date);
}

function formatMoney(value) {
    if (value === null || value === undefined) {
        return '—';
    }

    return new Intl.NumberFormat('es-CL', {
        style: 'currency',
        currency: 'CLP',
        maximumFractionDigits: 0,
    }).format(value);
}

export default function VehicleDetailPage() {
    const { id } = useParams();

    const [vehicle, setVehicle] = useState(null);
    const [history, setHistory] = useState([]);
    const [orders, setOrders] = useState([]);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [historyError, setHistoryError] = useState('');
    const [ordersError, setOrdersError] = useState('');

    const [refreshKey, setRefreshKey] = useState(0);

    useEffect(() => {
        let cancelled = false;

        async function loadProfile() {
            setLoading(true);
            setError('');
            setHistoryError('');
            setOrdersError('');
            setVehicle(null);
            setHistory([]);
            setOrders([]);

            try {
                const data = await vehicleApi.getById(id);

                if (cancelled) return;

                if (!data) {
                    throw new Error(
                        'No se recibió información del vehículo.'
                    );
                }

                setVehicle(data);

                const [historyResult, ordersResult] =
                    await Promise.allSettled([
                        vehicleApi.getHistoryByVin(data.vin),
                        workshopApi.getOrdersByVehicle(data.id),
                    ]);

                if (cancelled) return;

                if (historyResult.status === 'fulfilled') {
                    setHistory(
                        Array.isArray(historyResult.value)
                            ? historyResult.value
                            : []
                    );
                } else {
                    setHistoryError(
                        historyResult.reason?.message ??
                        'No fue posible consultar el historial.'
                    );
                }

                if (ordersResult.status === 'fulfilled') {
                    setOrders(
                        Array.isArray(ordersResult.value)
                            ? ordersResult.value
                            : []
                    );
                } else {
                    setOrdersError(
                        ordersResult.reason?.message ??
                        'No fue posible consultar las órdenes.'
                    );
                }
            } catch (err) {
                if (!cancelled) {
                    setError(
                        err.message ??
                        'No fue posible cargar el vehículo.'
                    );
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        loadProfile();

        return () => {
            cancelled = true;
        };
    }, [id, refreshKey]);

    return (
        <div>
            <section className="page-heading">
                <div>
                    <span className="eyebrow">
                        INVENTARIO / PERFIL OPERATIVO
                    </span>

                    <h1>Perfil del vehículo</h1>

                    <p>
                        Información del inventario, historial
                        Kafka y órdenes de taller.
                    </p>
                </div>

                <div className="form-actions">
                    <Link
                        to="/vehicles"
                        className="secondary-button"
                    >
                        <ArrowLeft size={17} />
                        Volver
                    </Link>

                    <button
                        type="button"
                        className="secondary-button"
                        onClick={() =>
                            setRefreshKey(value => value + 1)
                        }
                        disabled={loading}
                    >
                        <RefreshCw size={17} />
                        Actualizar
                    </button>
                </div>
            </section>

            {loading && (
                <div className="loading-state">
                    <div className="spinner" />
                    <strong>Cargando perfil operativo...</strong>
                </div>
            )}

            {error && (
                <div className="alert alert-error">
                    <AlertCircle size={19} />
                    <span>{error}</span>
                </div>
            )}

            {!loading && vehicle && (
                <>
                    <section className="content-card inventory-card">
                        <div className="form-section-header">
                            <div className="form-section-icon">
                                <CarFront size={21} />
                            </div>

                            <div>
                                <h3>
                                    {vehicle.brand} {vehicle.model}
                                </h3>

                                <p>
                                    {vehicle.plate} · VIN: {vehicle.vin}
                                </p>
                            </div>
                        </div>

                        <div className="inventory-summary">
                            <div>
                                <span>Sucursal</span>
                                <strong>
                                    {vehicle.branch || '—'}
                                </strong>
                            </div>

                            <div>
                                <span>Año</span>
                                <strong>
                                    {vehicle.year ?? '—'}
                                </strong>
                            </div>

                            <div>
                                <span>Kilometraje</span>
                                <strong>
                                    {vehicle.mileage != null
                                        ? `${new Intl.NumberFormat(
                                            'es-CL'
                                        ).format(vehicle.mileage)} km`
                                        : '—'}
                                </strong>
                            </div>

                            <div>
                                <span>Estado</span>
                                <VehicleStatusBadge
                                    status={vehicle.status}
                                />
                            </div>
                        </div>
                    </section>

                    <section className="content-card inventory-card">
                        <div className="form-section-header">
                            <div className="form-section-icon">
                                <History size={21} />
                            </div>

                            <div>
                                <h3>Historial de eventos</h3>
                                <p>
                                    Movimientos registrados mediante
                                    Kafka y persistidos por VIN.
                                </p>
                            </div>
                        </div>

                        {historyError && (
                            <div className="alert alert-error">
                                <AlertCircle size={18} />
                                <span>{historyError}</span>
                            </div>
                        )}

                        {!historyError && history.length === 0 && (
                            <div className="empty-state">
                                <History size={30} />
                                <strong>Sin eventos registrados</strong>
                            </div>
                        )}

                        {!historyError && history.length > 0 && (
                            <div className="table-container">
                                <table className="data-table">
                                    <thead>
                                    <tr>
                                        <th>Evento</th>
                                        <th>Sucursal</th>
                                        <th>Fecha</th>
                                    </tr>
                                    </thead>

                                    <tbody>
                                    {history.map(event => (
                                        <tr key={event.eventId}>
                                            <td>
                                                <strong>
                                                    {EVENT_LABELS[
                                                        event.eventType
                                                        ] ?? event.eventType}
                                                </strong>
                                            </td>

                                            <td>
                                                {event.branch || '—'}
                                            </td>

                                            <td>
                                                {formatDate(
                                                    event.occurredAt
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </section>

                    <section className="content-card inventory-card">
                        <div className="form-section-header">
                            <div className="form-section-icon">
                                <Wrench size={21} />
                            </div>

                            <div>
                                <h3>Órdenes de taller</h3>
                                <p>
                                    Órdenes asociadas al identificador
                                    del vehículo.
                                </p>
                            </div>
                        </div>

                        {ordersError && (
                            <div className="alert alert-error">
                                <AlertCircle size={18} />
                                <span>{ordersError}</span>
                            </div>
                        )}

                        {!ordersError && orders.length === 0 && (
                            <div className="empty-state">
                                <ClipboardList size={30} />
                                <strong>Sin órdenes registradas</strong>
                            </div>
                        )}

                        {!ordersError && orders.length > 0 && (
                            <div className="table-container">
                                <table className="data-table">
                                    <thead>
                                    <tr>
                                        <th>Orden</th>
                                        <th>Taller</th>
                                        <th>Ingreso</th>
                                        <th>Estado</th>
                                        <th>Presupuesto aprobado</th>
                                        <th>Costo final</th>
                                    </tr>
                                    </thead>

                                    <tbody>
                                    {orders.map(order => (
                                        <tr key={order.id}>
                                            <td>#{order.id}</td>

                                            <td>
                                                {order.workshopName || '—'}
                                            </td>

                                            <td>
                                                {order.entryDate || '—'}
                                            </td>

                                            <td>
                                                {order.status || '—'}
                                            </td>

                                            <td>
                                                {formatMoney(
                                                    order.approvedBudget
                                                )}
                                            </td>

                                            <td>
                                                {formatMoney(
                                                    order.finalCost
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </section>
                </>
            )}
        </div>
    );
}
