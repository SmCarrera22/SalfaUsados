import {
    useEffect,
    useState,
} from 'react';

import {
    ArrowLeft,
    Save,
    CarFront,
    AlertCircle,
} from 'lucide-react';

import {
    Link,
    useNavigate,
    useParams,
} from 'react-router-dom';

import {
    vehicleApi,
} from '../api/apiClient';

const INITIAL_FORM = {
    vin: '',
    plate: '',
    brand: '',
    model: '',
    version: '',
    year: '',
    mileage: '',
    color: '',
    fuelType: '',
    branch: '',
    status: 'AVAILABLE',
};

const STATUS_OPTIONS = [
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

const FUEL_OPTIONS = [
    'Gasolina',
    'Diésel',
    'Híbrido',
    'Eléctrico',
];

export default function VehicleFormPage() {
    const {
        id,
    } = useParams();

    const navigate =
        useNavigate();

    const isEditing =
        Boolean(id);

    const [form, setForm] =
        useState(INITIAL_FORM);

    const [loading, setLoading] =
        useState(isEditing);

    const [saving, setSaving] =
        useState(false);

    const [error, setError] =
        useState('');

    useEffect(() => {
        if (!isEditing) {
            return;
        }

        async function loadVehicle() {
            try {
                const vehicle =
                    await vehicleApi.getById(id);

                setForm({
                    vin:
                        vehicle.vin ?? '',
                    plate:
                        vehicle.plate ?? '',
                    brand:
                        vehicle.brand ?? '',
                    model:
                        vehicle.model ?? '',
                    version:
                        vehicle.version ?? '',
                    year:
                        vehicle.year ?? '',
                    mileage:
                        vehicle.mileage ?? '',
                    color:
                        vehicle.color ?? '',
                    fuelType:
                        vehicle.fuelType ?? '',
                    branch:
                        vehicle.branch ?? '',
                    status:
                        vehicle.status ??
                        'AVAILABLE',
                });
            } catch (err) {
                console.error(
                    'Error cargando vehículo:',
                    err,
                );

                setError(
                    err.message ??
                    'No fue posible cargar el vehículo.',
                );
            } finally {
                setLoading(false);
            }
        }

        loadVehicle();
    }, [
        id,
        isEditing,
    ]);

    function handleChange(event) {
        const {
            name,
            value,
        } = event.target;

        setForm(
            (current) => ({
                ...current,
                [name]: value,
            }),
        );
    }

    async function handleSubmit(event) {
        event.preventDefault();

        setSaving(true);
        setError('');

        const payload = {
            vin:
                form.vin.trim(),
            plate:
                form.plate.trim(),
            brand:
                form.brand.trim(),
            model:
                form.model.trim(),
            version:
                form.version.trim(),
            year:
                Number(form.year),
            mileage:
                Number(form.mileage),
            color:
                form.color.trim(),
            fuelType:
            form.fuelType,
            branch:
                form.branch.trim(),
            status:
            form.status,
        };

        try {
            if (isEditing) {
                await vehicleApi.update(
                    id,
                    payload,
                );
            } else {
                await vehicleApi.create(
                    payload,
                );
            }

            navigate(
                '/vehicles',
                {
                    replace: true,
                },
            );
        } catch (err) {
            console.error(
                'Error guardando vehículo:',
                err,
            );

            setError(
                getFriendlyErrorMessage(err),
            );
        } finally {
            setSaving(false);
        }
    }

    if (loading) {
        return (
            <div className="loading-state page-loading">
                <div className="spinner" />

                <strong>
                    Cargando vehículo...
                </strong>
            </div>
        );
    }

    return (
        <div>
            <section className="page-heading">
                <div>
                    <Link
                        to="/vehicles"
                        className="back-link"
                    >
                        <ArrowLeft size={16} />
                        Volver al inventario
                    </Link>

                    <span className="eyebrow">
            {isEditing
                ? 'EDITAR VEHÍCULO'
                : 'NUEVO VEHÍCULO'}
          </span>

                    <h1>
                        {isEditing
                            ? 'Editar vehículo'
                            : 'Registrar vehículo'}
                    </h1>

                    <p>
                        {isEditing
                            ? 'Actualiza la información operacional del vehículo.'
                            : 'Ingresa los antecedentes para incorporarlo al inventario.'}
                    </p>
                </div>
            </section>

            <form
                className="vehicle-form"
                onSubmit={handleSubmit}
            >
                {error && (
                    <div className="alert alert-error form-alert">
                        <AlertCircle size={19} />

                        <div>
                            <strong>
                                No fue posible guardar
                            </strong>

                            <span>{error}</span>
                        </div>
                    </div>
                )}

                <section className="form-card">
                    <div className="form-section-header">
                        <div className="form-section-icon">
                            <CarFront size={21} />
                        </div>

                        <div>
                            <h3>
                                Identificación
                            </h3>

                            <p>
                                Información principal del
                                vehículo.
                            </p>
                        </div>
                    </div>

                    <div className="form-grid">
                        <FormField
                            label="Patente"
                            required
                        >
                            <input
                                name="plate"
                                value={form.plate}
                                onChange={handleChange}
                                placeholder="Ej: ABCD12"
                                required
                            />
                        </FormField>

                        <FormField
                            label="VIN"
                            required
                            wide
                        >
                            <input
                                name="vin"
                                value={form.vin}
                                onChange={handleChange}
                                placeholder="Número de chasis"
                                required
                            />
                        </FormField>

                        <FormField
                            label="Marca"
                            required
                        >
                            <input
                                name="brand"
                                value={form.brand}
                                onChange={handleChange}
                                placeholder="Ej: Chevrolet"
                                required
                            />
                        </FormField>

                        <FormField
                            label="Modelo"
                            required
                        >
                            <input
                                name="model"
                                value={form.model}
                                onChange={handleChange}
                                placeholder="Ej: Tracker"
                                required
                            />
                        </FormField>

                        <FormField
                            label="Versión"
                        >
                            <input
                                name="version"
                                value={form.version}
                                onChange={handleChange}
                                placeholder="Ej: 1.2 Turbo LT"
                            />
                        </FormField>

                        <FormField
                            label="Año"
                            required
                        >
                            <input
                                type="number"
                                name="year"
                                value={form.year}
                                onChange={handleChange}
                                min="1900"
                                max="2100"
                                required
                            />
                        </FormField>
                    </div>
                </section>

                <section className="form-card">
                    <div className="form-section-header">
                        <div className="form-section-icon">
                            <CarFront size={21} />
                        </div>

                        <div>
                            <h3>
                                Información operacional
                            </h3>

                            <p>
                                Datos utilizados para la
                                gestión del inventario.
                            </p>
                        </div>
                    </div>

                    <div className="form-grid">
                        <FormField
                            label="Kilometraje"
                            required
                        >
                            <input
                                type="number"
                                name="mileage"
                                value={form.mileage}
                                onChange={handleChange}
                                min="0"
                                placeholder="Ej: 45000"
                                required
                            />
                        </FormField>

                        <FormField
                            label="Color"
                            required
                        >
                            <input
                                name="color"
                                value={form.color}
                                onChange={handleChange}
                                placeholder="Ej: Blanco"
                                required
                            />
                        </FormField>

                        <FormField
                            label="Combustible"
                            required
                        >
                            <select
                                name="fuelType"
                                value={form.fuelType}
                                onChange={handleChange}
                                required
                            >
                                <option value="">
                                    Seleccionar
                                </option>

                                {FUEL_OPTIONS.map(
                                    (fuel) => (
                                        <option
                                            key={fuel}
                                            value={fuel}
                                        >
                                            {fuel}
                                        </option>
                                    ),
                                )}
                            </select>
                        </FormField>

                        <FormField
                            label="Sucursal"
                            required
                        >
                            <input
                                name="branch"
                                value={form.branch}
                                onChange={handleChange}
                                placeholder="Ej: Movicenter"
                                required
                            />
                        </FormField>

                        <FormField
                            label="Estado"
                            required
                        >
                            <select
                                name="status"
                                value={form.status}
                                onChange={handleChange}
                                required
                            >
                                {STATUS_OPTIONS.map(
                                    (option) => (
                                        <option
                                            key={
                                                option.value
                                            }
                                            value={
                                                option.value
                                            }
                                        >
                                            {option.label}
                                        </option>
                                    ),
                                )}
                            </select>
                        </FormField>
                    </div>
                </section>

                <div className="form-actions">
                    <Link
                        to="/vehicles"
                        className="secondary-button"
                    >
                        Cancelar
                    </Link>

                    <button
                        type="submit"
                        className="primary-button"
                        disabled={saving}
                    >
                        <Save size={18} />

                        {saving
                            ? 'Guardando...'
                            : isEditing
                                ? 'Guardar cambios'
                                : 'Registrar vehículo'}
                    </button>
                </div>
            </form>
        </div>
    );
}

function FormField({
                       label,
                       required = false,
                       wide = false,
                       children,
                   }) {
    return (
        <label
            className={
                `form-field ${
                    wide
                        ? 'form-field-wide'
                        : ''
                }`
            }
        >
      <span>
        {label}

          {required && (
              <em>*</em>
          )}
      </span>

            {children}
        </label>
    );
}

function getFriendlyErrorMessage(error) {
    if (error?.status === 409) {
        return (
            'Ya existe un vehículo con la patente o VIN ingresado.'
        );
    }

    if (error?.status === 400) {
        return (
            error.message ??
            'Revisa los datos ingresados.'
        );
    }

    if (error?.status === 403) {
        return (
            'Tu rol no tiene permisos para realizar esta operación.'
        );
    }

    return (
        error?.message ??
        'Ocurrió un error inesperado.'
    );
}