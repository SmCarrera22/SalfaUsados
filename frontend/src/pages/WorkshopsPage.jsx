import {
    useEffect,
    useMemo,
    useState,
} from 'react';

import {
    AlertCircle,
    CheckCircle2,
    ClipboardList,
    Plus,
    RefreshCw,
    Search,
    Wrench,
} from 'lucide-react';

import {
    useAuth,
} from '../auth/AuthContext';

import {
    workshopApi,
    vehicleApi,
} from '../api/apiClient';

function todayLocal() {
    const date = new Date();

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
}

const INITIAL_FORM = {
    vehicleId: '',
    workshopId: '',
    entryDate: todayLocal(),
    estimatedExitDate: '',
    description: '',
};

export default function WorkshopsPage() {
    const {
        isAdmin,
        isOperator,
    } = useAuth();

    const canCreate = isAdmin || isOperator;

    // ==========================================
    // TALLERES
    // ==========================================

    const [workshops, setWorkshops] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // ==========================================
    // VEHÍCULOS
    // ==========================================

    const [vehicles, setVehicles] = useState([]);
    const [vehiclesLoading, setVehiclesLoading] = useState(true);
    const [vehiclesError, setVehiclesError] = useState('');

    const sortedVehicles = useMemo(() => {
        return vehicles
            .filter(vehicle =>
                vehicle.id != null &&
                typeof vehicle.plate === 'string' &&
                vehicle.plate.trim()
            )
            .sort((a, b) =>
                a.plate.localeCompare(b.plate, 'es-CL')
            );
    }, [vehicles]);

    const vehiclesById = useMemo(() => {
        return new Map(
            vehicles.map(vehicle => [
                String(vehicle.id),
                vehicle,
            ])
        );
    }, [vehicles]);

    const activeWorkshops = useMemo(() => {
        return workshops.filter(workshop => workshop.active);
    }, [workshops]);

    // ==========================================
    // FORMULARIO Y COMANDOS
    // ==========================================

    const [form, setForm] = useState(INITIAL_FORM);
    const [saving, setSaving] = useState(false);
    const [command, setCommand] = useState(null);

    // ==========================================
    // CONSULTA DE ÓRDENES
    // ==========================================

    const [searchVehicleId, setSearchVehicleId] = useState('');
    const [orders, setOrders] = useState([]);
    const [searching, setSearching] = useState(false);
    const [searchError, setSearchError] = useState('');
    const [hasSearched, setHasSearched] = useState(false);

    // ==========================================
    // CARGA INICIAL
    // ==========================================

    useEffect(() => {
        let cancelled = false;

        async function loadWorkshops() {
            try {
                const data = await workshopApi.getAll();

                if (!cancelled) {
                    setWorkshops(
                        Array.isArray(data) ? data : []
                    );
                }
            } catch (err) {
                if (!cancelled) {
                    setError(
                        err.message ??
                        'No fue posible cargar los talleres.'
                    );
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        async function loadVehicles() {
            try {
                const data = await vehicleApi.getAll();

                if (!cancelled) {
                    setVehicles(
                        Array.isArray(data) ? data : []
                    );
                }
            } catch (err) {
                if (!cancelled) {
                    setVehiclesError(
                        err.message ??
                        'No fue posible cargar el inventario.'
                    );
                }
            } finally {
                if (!cancelled) {
                    setVehiclesLoading(false);
                }
            }
        }

        loadWorkshops();
        loadVehicles();

        return () => {
            cancelled = true;
        };
    }, []);

    // ==========================================
    // CAMBIOS DEL FORMULARIO
    // ==========================================

    function handleChange(event) {
        const { name, value } = event.target;

        setForm(current => ({
            ...current,
            [name]: value,
        }));

        // El resultado anterior no corresponde
        // necesariamente al formulario editado.
        setCommand(null);
    }

    // ==========================================
    // CONSULTAR ÓRDENES
    // ==========================================

    async function searchOrders(vehicleId = searchVehicleId) {
        const id = String(vehicleId).trim();

        if (!/^[1-9]\d*$/.test(id)) {
            setSearchError(
                'Selecciona una patente válida.'
            );

            setOrders([]);
            setHasSearched(false);
            return;
        }

        setSearching(true);
        setSearchError('');
        setHasSearched(true);
        setSearchVehicleId(id);
        setOrders([]);

        try {
            const data =
                await workshopApi.getOrdersByVehicle(id);

            setOrders(
                Array.isArray(data) ? data : []
            );
        } catch (err) {
            setOrders([]);

            setSearchError(
                err.message ??
                'No fue posible consultar las órdenes.'
            );
        } finally {
            setSearching(false);
        }
    }

    function handleSearchChange(event) {
        setSearchVehicleId(event.target.value);
        setOrders([]);
        setHasSearched(false);
        setSearchError('');
    }

    // ==========================================
    // CREAR ORDEN ASÍNCRONA
    // ==========================================

    async function handleSubmit(event) {
        event.preventDefault();

        if (!canCreate || saving) {
            return;
        }

        const vehicleId = Number(form.vehicleId);
        const workshopId = Number(form.workshopId);

        if (
            !Number.isSafeInteger(vehicleId) ||
            vehicleId <= 0 ||
            !Number.isSafeInteger(workshopId) ||
            workshopId <= 0
        ) {
            setError(
                'Selecciona un vehículo y un taller válidos.'
            );
            return;
        }

        const selectedVehicle =
            vehiclesById.get(String(vehicleId));

        if (!selectedVehicle) {
            setError(
                'El vehículo seleccionado no está disponible en el inventario.'
            );
            return;
        }

        const selectedWorkshop =
            activeWorkshops.find(workshop =>
                Number(workshop.id) === workshopId
            );

        if (!selectedWorkshop) {
            setError(
                'Selecciona un taller activo.'
            );
            return;
        }

        if (
            !form.entryDate ||
            !form.estimatedExitDate ||
            form.estimatedExitDate < form.entryDate
        ) {
            setError(
                'La fecha de salida estimada no puede ser anterior al ingreso.'
            );
            return;
        }

        const description = form.description.trim();

        if (!description) {
            setError(
                'Ingresa una descripción del trabajo.'
            );
            return;
        }

        setSaving(true);
        setError('');
        setCommand(null);

        const payload = {
            vehicleId,
            workshopId,
            entryDate: form.entryDate,
            estimatedExitDate: form.estimatedExitDate,
            description,
        };

        try {
            const result =
                await workshopApi.createOrderAsync(payload);

            if (!result?.commandId) {
                throw new Error(
                    'El servidor no devolvió un identificador de comando.'
                );
            }

            setCommand({
                commandId: result.commandId,
                vehicleId,
                plate: selectedVehicle.plate,
            });

            setSearchVehicleId(String(vehicleId));
            setOrders([]);
            setHasSearched(false);
            setSearchError('');
        } catch (err) {
            setError(
                err.message ??
                'No fue posible solicitar el ingreso a taller.'
            );
        } finally {
            setSaving(false);
        }
    }

    // ==========================================
    // RENDER
    // ==========================================

    return (
        <div className="workshops-page">

            {/* ENCABEZADO */}

            <section className="page-heading">
                <div>
                    <span className="eyebrow">
                        OPERACIONES / RABBITMQ
                    </span>

                    <h1>Gestión de talleres</h1>

                    <p>
                        Consulta órdenes y solicita ingresos
                        a taller mediante procesamiento asíncrono.
                    </p>
                </div>
            </section>

            {/* ERRORES GENERALES */}

            {error && (
                <div className="alert alert-error">
                    <AlertCircle size={19} />

                    <div>
                        <strong>No fue posible completar la operación</strong>
                        <span>{error}</span>
                    </div>
                </div>
            )}

            {vehiclesError && (
                <div className="alert alert-error">
                    <AlertCircle size={19} />

                    <div>
                        <strong>Error consultando inventario</strong>
                        <span>{vehiclesError}</span>
                    </div>
                </div>
            )}

            {/* INDICADORES */}

            <section className="inventory-summary">
                <div>
                    <span>Talleres registrados</span>
                    <strong>
                        {workshops.length}
                    </strong>
                </div>

                <div>
                    <span>Talleres activos</span>
                    <strong>
                        {activeWorkshops.length}
                    </strong>
                </div>

                <div>
                    <span>Mensajería</span>
                    <strong>RabbitMQ</strong>
                </div>
            </section>

            {/* LISTADO DE TALLERES */}

            <section className="content-card inventory-card workshops-list-card">
                <div className="form-section-header">
                    <div className="form-section-icon">
                        <Wrench size={21} />
                    </div>

                    <div>
                        <h3>Talleres disponibles</h3>
                        <p>
                            Información consultada desde AWS.
                        </p>
                    </div>
                </div>

                {loading ? (
                    <div className="loading-state">
                        <div className="spinner" />
                        <strong>Cargando talleres...</strong>
                    </div>
                ) : workshops.length === 0 ? (
                    <div className="empty-state">
                        <Wrench size={32} />
                        <strong>No hay talleres disponibles</strong>
                    </div>
                ) : (
                    <div className="table-container">
                        <table className="data-table">
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Taller</th>
                                <th>Ciudad</th>
                                <th>Dirección</th>
                                <th>Estado</th>
                            </tr>
                            </thead>

                            <tbody>
                            {workshops.map(workshop => (
                                <tr key={workshop.id}>
                                    <td>{workshop.id}</td>

                                    <td>
                                        <strong>
                                            {workshop.name}
                                        </strong>
                                    </td>

                                    <td>
                                        {workshop.city}
                                    </td>

                                    <td>
                                        {workshop.address}
                                    </td>

                                    <td>
                                        {workshop.active
                                            ? 'Activo'
                                            : 'Inactivo'}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>

            {/* CREACIÓN ASÍNCRONA */}

            {canCreate && (
                <section className="form-card">
                    <div className="form-section-header">
                        <div className="form-section-icon">
                            <Plus size={21} />
                        </div>

                        <div>
                            <h3>Nueva orden de taller</h3>
                            <p>
                                Selecciona un vehículo por patente
                                y publica su solicitud mediante RabbitMQ.
                            </p>
                        </div>
                    </div>

                    <form onSubmit={handleSubmit}>
                        <div className="form-grid workshop-form-grid">

                            <label className="form-field">
                                <span>Patente del vehículo *</span>

                                <select
                                    name="vehicleId"
                                    required
                                    value={form.vehicleId}
                                    onChange={handleChange}
                                    disabled={
                                        vehiclesLoading ||
                                        Boolean(vehiclesError)
                                    }
                                >
                                    <option value="">
                                        {vehiclesLoading
                                            ? 'Cargando vehículos...'
                                            : 'Seleccionar vehículo'}
                                    </option>

                                    {sortedVehicles.map(vehicle => (
                                        <option
                                            key={vehicle.id}
                                            value={vehicle.id}
                                        >
                                            {vehicle.plate} — {vehicle.brand} {vehicle.model}
                                        </option>
                                    ))}
                                </select>
                            </label>

                            <label className="form-field">
                                <span>Taller *</span>

                                <select
                                    name="workshopId"
                                    required
                                    value={form.workshopId}
                                    onChange={handleChange}
                                    disabled={loading}
                                >
                                    <option value="">
                                        Seleccionar taller
                                    </option>

                                    {activeWorkshops.map(workshop => (
                                        <option
                                            key={workshop.id}
                                            value={workshop.id}
                                        >
                                            {workshop.name} — #{workshop.id}
                                        </option>
                                    ))}
                                </select>
                            </label>

                            <label className="form-field">
                                <span>Fecha de ingreso *</span>

                                <input
                                    name="entryDate"
                                    type="date"
                                    required
                                    value={form.entryDate}
                                    onChange={handleChange}
                                />
                            </label>

                            <label className="form-field">
                                <span>Salida estimada *</span>

                                <input
                                    name="estimatedExitDate"
                                    type="date"
                                    min={form.entryDate}
                                    required
                                    value={form.estimatedExitDate}
                                    onChange={handleChange}
                                />
                            </label>

                            <label className="form-field form-field-wide">
                                <span>Descripción del trabajo *</span>

                                <textarea
                                    name="description"
                                    required
                                    rows={3}
                                    value={form.description}
                                    onChange={handleChange}
                                    placeholder="Ej.: Ingreso por ralentí inestable en motor"
                                />
                            </label>
                        </div>

                        <div className="form-actions">
                            <button
                                type="submit"
                                className="primary-button"
                                disabled={
                                    saving ||
                                    loading ||
                                    vehiclesLoading ||
                                    Boolean(vehiclesError) ||
                                    activeWorkshops.length === 0 ||
                                    sortedVehicles.length === 0
                                }
                            >
                                <Plus size={18} />

                                {saving
                                    ? 'Enviando comando...'
                                    : 'Solicitar ingreso a taller'}
                            </button>
                        </div>
                    </form>

                    {/* RESPUESTA DE RABBITMQ */}

                    {command && (
                        <div className="alert">
                            <CheckCircle2 size={21} />

                            <div>
                                <strong>
                                    Comando recibido para procesamiento
                                </strong>

                                <p>
                                    Se recibió la solicitud para la patente{' '}
                                    <strong>{command.plate}</strong>.
                                    La orden todavía no está confirmada.
                                </p>

                                <code>
                                    {command.commandId}
                                </code>

                                <div style={{ marginTop: '12px' }}>
                                    <button
                                        type="button"
                                        className="secondary-button"
                                        disabled={searching}
                                        onClick={() =>
                                            searchOrders(command.vehicleId)
                                        }
                                    >
                                        <RefreshCw size={16} />
                                        Consultar resultado
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}
                </section>
            )}

            {/* BÚSQUEDA DE ÓRDENES */}

            <section className="content-card inventory-card">
                <div className="form-section-header">
                    <div className="form-section-icon">
                        <ClipboardList size={21} />
                    </div>

                    <div>
                        <h3>Consultar órdenes por patente</h3>
                        <p>
                            Revisa el historial de órdenes
                            de un vehículo del inventario.
                        </p>
                    </div>
                </div>

                <div className="inventory-toolbar">
                    <div className="search-box">
                        <Search size={18} />

                        <select
                            value={searchVehicleId}
                            onChange={handleSearchChange}
                            disabled={
                                vehiclesLoading ||
                                Boolean(vehiclesError)
                            }
                            aria-label="Seleccionar patente"
                        >
                            <option value="">
                                {vehiclesLoading
                                    ? 'Cargando vehículos...'
                                    : 'Seleccionar patente'}
                            </option>

                            {sortedVehicles.map(vehicle => (
                                <option
                                    key={vehicle.id}
                                    value={vehicle.id}
                                >
                                    {vehicle.plate} — {vehicle.brand} {vehicle.model}
                                </option>
                            ))}
                        </select>
                    </div>

                    <button
                        type="button"
                        className="secondary-button"
                        disabled={
                            searching ||
                            !searchVehicleId ||
                            vehiclesLoading
                        }
                        onClick={() => searchOrders()}
                    >
                        <RefreshCw
                            size={17}
                            className={searching ? 'spin' : ''}
                        />

                        {searching
                            ? 'Consultando...'
                            : 'Buscar órdenes'}
                    </button>
                </div>

                {searchError && (
                    <div className="alert alert-error">
                        <AlertCircle size={19} />
                        <span>{searchError}</span>
                    </div>
                )}

                {hasSearched && !searchError && (
                    orders.length === 0 ? (
                        <div className="empty-state">
                            <ClipboardList size={32} />

                            <strong>
                                No se encontraron órdenes
                            </strong>

                            <p>
                                Si acabas de solicitar un ingreso,
                                el comando podría seguir pendiente
                                o haber fallado durante el procesamiento.
                            </p>
                        </div>
                    ) : (
                        <div className="table-container">
                            <table className="data-table">
                                <thead>
                                <tr>
                                    <th>Orden</th>
                                    <th>Patente</th>
                                    <th>Taller</th>
                                    <th>Ingreso</th>
                                    <th>Salida estimada</th>
                                    <th>Estado</th>
                                </tr>
                                </thead>

                                <tbody>
                                {orders.map(order => {
                                    const vehicle =
                                        vehiclesById.get(
                                            String(order.vehicleId)
                                        );

                                    return (
                                        <tr key={order.id}>
                                            <td>#{order.id}</td>

                                            <td>
                                                {vehicle?.plate ??
                                                    `ID ${order.vehicleId}`}
                                            </td>

                                            <td>
                                                {order.workshopName}
                                            </td>

                                            <td>
                                                {order.entryDate}
                                            </td>

                                            <td>
                                                {order.estimatedExitDate}
                                            </td>

                                            <td>
                                                {order.status}
                                            </td>
                                        </tr>
                                    );
                                })}
                                </tbody>
                            </table>
                        </div>
                    )
                )}
            </section>
        </div>
    );
}
