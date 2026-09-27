import {
    useEffect,
    useState,
} from 'react';

import {
    ArrowLeft,
    Save,
    UserRound,
    ShieldCheck,
    AlertCircle,
} from 'lucide-react';

import {
    Link,
    useNavigate,
    useParams,
} from 'react-router-dom';

import {
    userApi,
} from '../api/apiClient';

const INITIAL_FORM = {
    entraId: '',
    name: '',
    email: '',
    branch: '',
    role: 'ANALYST',
    active: true,
};

const ROLE_OPTIONS = [
    {
        value: 'ADMIN',
        label: 'Administrador',
    },
    {
        value: 'OPERATOR',
        label: 'Operador',
    },
    {
        value: 'ANALYST',
        label: 'Analista',
    },
];

export default function UserFormPage() {
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

        async function loadUser() {
            try {
                const user =
                    await userApi.getById(id);

                setForm({
                    entraId:
                        user.entraId ?? '',

                    name:
                        user.name ?? '',

                    email:
                        user.email ?? '',

                    branch:
                        user.branch ?? '',

                    role:
                        user.role ??
                        'ANALYST',

                    active:
                        user.active ?? true,
                });
            } catch (err) {
                console.error(
                    'Error cargando usuario:',
                    err,
                );

                setError(
                    err.message ??
                    'No fue posible cargar el perfil.',
                );
            } finally {
                setLoading(false);
            }
        }

        loadUser();
    }, [
        id,
        isEditing,
    ]);

    function handleChange(event) {
        const {
            name,
            value,
            type,
            checked,
        } = event.target;

        setForm(
            (current) => ({
                ...current,

                [name]:
                    type === 'checkbox'
                        ? checked
                        : value,
            }),
        );
    }

    async function handleSubmit(event) {
        event.preventDefault();

        setSaving(true);
        setError('');

        const payload = {
            entraId:
                form.entraId.trim() ||
                null,

            name:
                form.name.trim(),

            email:
                form.email.trim(),

            branch:
                form.branch.trim(),

            role:
            form.role,

            active:
            form.active,
        };

        try {
            if (isEditing) {
                await userApi.update(
                    id,
                    payload,
                );
            } else {
                await userApi.create(
                    payload,
                );
            }

            navigate(
                '/users',
                {
                    replace: true,
                },
            );
        } catch (err) {
            console.error(
                'Error guardando usuario:',
                err,
            );

            setError(
                getFriendlyErrorMessage(
                    err,
                ),
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
                    Cargando perfil...
                </strong>
            </div>
        );
    }

    return (
        <div>
            <section className="page-heading">
                <div>
                    <Link
                        to="/users"
                        className="back-link"
                    >
                        <ArrowLeft size={16} />
                        Volver a usuarios
                    </Link>

                    <span className="eyebrow">
            ADMINISTRACIÓN
          </span>

                    <h1>
                        {isEditing
                            ? 'Editar perfil'
                            : 'Nuevo perfil'}
                    </h1>

                    <p>
                        {isEditing
                            ? 'Actualiza la información de negocio asociada al usuario.'
                            : 'Registra el perfil de negocio asociado a una identidad de Microsoft Entra ID.'}
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
                            <UserRound size={21} />
                        </div>

                        <div>
                            <h3>
                                Información del perfil
                            </h3>

                            <p>
                                Datos utilizados dentro
                                de Salfa360.
                            </p>
                        </div>
                    </div>

                    <div className="form-grid">
                        <FormField
                            label="Nombre"
                            required
                        >
                            <input
                                name="name"
                                value={form.name}
                                onChange={handleChange}
                                placeholder="Ej: Juan Pérez"
                                required
                            />
                        </FormField>

                        <FormField
                            label="Correo"
                            required
                            wide
                        >
                            <input
                                type="email"
                                name="email"
                                value={form.email}
                                onChange={handleChange}
                                placeholder="usuario@empresa.cl"
                                required
                            />
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
                            label="Object ID de Entra"
                        >
                            <input
                                name="entraId"
                                value={form.entraId}
                                onChange={handleChange}
                                placeholder="OID del usuario en Entra"
                            />
                        </FormField>
                    </div>
                </section>

                <section className="form-card">
                    <div className="form-section-header">
                        <div className="form-section-icon">
                            <ShieldCheck size={21} />
                        </div>

                        <div>
                            <h3>
                                Perfil operacional
                            </h3>

                            <p>
                                Clasificación utilizada por
                                Salfa360 para representar
                                el perfil del usuario.
                            </p>
                        </div>
                    </div>

                    <div className="form-grid">
                        <FormField
                            label="Rol"
                            required
                        >
                            <select
                                name="role"
                                value={form.role}
                                onChange={handleChange}
                                required
                            >
                                {ROLE_OPTIONS.map(
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

                        <label className="switch-field">
                            <div>
                                <strong>
                                    Perfil activo
                                </strong>

                                <span>
                  Permite mantener el perfil
                  habilitado en Salfa360.
                </span>
                            </div>

                            <input
                                type="checkbox"
                                name="active"
                                checked={form.active}
                                onChange={handleChange}
                            />

                            <span className="switch-control" />
                        </label>
                    </div>

                    <div className="role-warning">
                        <ShieldCheck size={18} />

                        <p>
                            El rol seleccionado aquí representa
                            el perfil operacional almacenado por
                            Salfa360. La autorización efectiva
                            continúa siendo determinada por los
                            App Roles presentes en el token
                            emitido por Microsoft Entra ID.
                        </p>
                    </div>
                </section>

                <div className="form-actions">
                    <Link
                        to="/users"
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
                                : 'Crear perfil'}
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

function getFriendlyErrorMessage(
    error,
) {
    if (error?.status === 409) {
        return (
            'Ya existe un perfil con el correo u Object ID ingresado.'
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
            'Tu cuenta no tiene permisos de administrador.'
        );
    }

    return (
        error?.message ??
        'Ocurrió un error inesperado.'
    );
}