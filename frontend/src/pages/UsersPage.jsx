import {
    useEffect,
    useMemo,
    useState,
} from 'react';

import {
    Users,
    Search,
    RefreshCw,
    ShieldCheck,
    UserCheck,
    UserX,
    Pencil,
    Plus,
    AlertCircle,
    SlidersHorizontal,
} from 'lucide-react';

import {
    Link,
} from 'react-router-dom';

import {
    userApi,
} from '../api/apiClient';

import UserRoleBadge
    from '../components/users/UserRoleBadge';

const ROLE_OPTIONS = [
    {
        value: '',
        label: 'Todos los roles',
    },
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

export default function UsersPage() {
    const [users, setUsers] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState('');

    const [search, setSearch] =
        useState('');

    const [roleFilter, setRoleFilter] =
        useState('');

    const [updatingId, setUpdatingId] =
        useState(null);

    async function loadUsers() {
        setLoading(true);
        setError('');

        try {
            const data =
                await userApi.getAll();

            setUsers(
                Array.isArray(data)
                    ? data
                    : [],
            );
        } catch (err) {
            console.error(
                'Error cargando usuarios:',
                err,
            );

            setError(
                err.message ??
                'No fue posible cargar los usuarios.',
            );
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadUsers();
    }, []);

    const filteredUsers =
        useMemo(() => {
            const normalizedSearch =
                search
                    .trim()
                    .toLowerCase();

            return users.filter(
                (user) => {
                    const matchesRole =
                        !roleFilter ||
                        user.role === roleFilter;

                    if (!matchesRole) {
                        return false;
                    }

                    if (!normalizedSearch) {
                        return true;
                    }

                    return [
                        user.name,
                        user.email,
                        user.branch,
                        user.entraId,
                    ].some(
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
            users,
            search,
            roleFilter,
        ]);

    const metrics =
        useMemo(
            () => ({
                total:
                users.length,

                active:
                users.filter(
                    (user) =>
                        user.active,
                ).length,

                admins:
                users.filter(
                    (user) =>
                        user.role === 'ADMIN',
                ).length,

                others:
                users.filter(
                    (user) =>
                        user.role === 'OPERATOR' ||
                        user.role === 'ANALYST',
                ).length,
            }),
            [users],
        );

    async function handleStatusChange(
        user,
    ) {
        const newStatus =
            !user.active;

        const action =
            newStatus
                ? 'activar'
                : 'desactivar';

        const confirmed =
            window.confirm(
                `¿Deseas ${action} el perfil de ${user.name}?`,
            );

        if (!confirmed) {
            return;
        }

        setUpdatingId(user.id);

        try {
            const updated =
                await userApi.setActive(
                    user.id,
                    newStatus,
                );

            setUsers(
                (current) =>
                    current.map(
                        (item) =>
                            item.id === user.id
                                ? (
                                    updated ?? {
                                        ...item,
                                        active:
                                        newStatus,
                                    }
                                )
                                : item,
                    ),
            );
        } catch (err) {
            console.error(
                'Error actualizando usuario:',
                err,
            );

            window.alert(
                err.message ??
                'No fue posible actualizar el usuario.',
            );
        } finally {
            setUpdatingId(null);
        }
    }

    return (
        <div>
            <section className="page-heading">
                <div>
          <span className="eyebrow">
            ADMINISTRACIÓN
          </span>

                    <h1>Usuarios</h1>

                    <p>
                        Administra los perfiles de
                        negocio asociados a Salfa360.
                    </p>
                </div>

                <Link
                    to="/users/new"
                    className="primary-button"
                >
                    <Plus size={18} />

                    Nuevo perfil
                </Link>
            </section>

            <section className="users-summary">
                <SummaryCard
                    icon={<Users size={19} />}
                    label="Total perfiles"
                    value={metrics.total}
                />

                <SummaryCard
                    icon={<UserCheck size={19} />}
                    label="Activos"
                    value={metrics.active}
                />

                <SummaryCard
                    icon={<ShieldCheck size={19} />}
                    label="Administradores"
                    value={metrics.admins}
                />

                <SummaryCard
                    icon={<Users size={19} />}
                    label="Operadores / Analistas"
                    value={metrics.others}
                />
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
                            placeholder="Buscar por nombre, correo o sucursal..."
                        />
                    </div>

                    <div className="filter-box">
                        <SlidersHorizontal
                            size={17}
                        />

                        <select
                            value={roleFilter}
                            onChange={(event) =>
                                setRoleFilter(
                                    event.target.value,
                                )
                            }
                        >
                            {ROLE_OPTIONS.map(
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
                        onClick={loadUsers}
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
                                No pudimos cargar los usuarios
                            </strong>

                            <span>{error}</span>
                        </div>
                    </div>
                )}

                {loading ? (
                    <div className="loading-state">
                        <div className="spinner" />

                        <strong>
                            Cargando usuarios...
                        </strong>

                        <span>
              Consultando perfiles
              desde Salfa360
            </span>
                    </div>
                ) : filteredUsers.length === 0 ? (
                    <div className="empty-state">
                        <Users size={38} />

                        <strong>
                            No encontramos usuarios
                        </strong>

                        <p>
                            Modifica los filtros o
                            registra un nuevo perfil.
                        </p>
                    </div>
                ) : (
                    <div className="table-container">
                        <table className="data-table">
                            <thead>
                            <tr>
                                <th>Usuario</th>
                                <th>Sucursal</th>
                                <th>Rol</th>
                                <th>Estado</th>
                                <th className="actions-column">
                                    Acciones
                                </th>
                            </tr>
                            </thead>

                            <tbody>
                            {filteredUsers.map(
                                (user) => (
                                    <tr key={user.id}>
                                        <td>
                                            <div className="user-table-cell">
                                                <div className="user-table-avatar">
                                                    {getInitials(
                                                        user.name,
                                                    )}
                                                </div>

                                                <div>
                                                    <strong>
                                                        {user.name}
                                                    </strong>

                                                    <span>
                              {user.email}
                            </span>
                                                </div>
                                            </div>
                                        </td>

                                        <td>
                                            {user.branch ||
                                                '—'}
                                        </td>

                                        <td>
                                            <UserRoleBadge
                                                role={user.role}
                                            />
                                        </td>

                                        <td>
                        <span
                            className={
                                `user-status ${
                                    user.active
                                        ? 'active'
                                        : 'inactive'
                                }`
                            }
                        >
                          <span />

                            {user.active
                                ? 'Activo'
                                : 'Inactivo'}
                        </span>
                                        </td>

                                        <td>
                                            <div className="table-actions">
                                                <Link
                                                    to={
                                                        `/users/${user.id}/edit`
                                                    }
                                                    className="table-action-button"
                                                    title="Editar perfil"
                                                >
                                                    <Pencil
                                                        size={16}
                                                    />
                                                </Link>

                                                <button
                                                    type="button"
                                                    className={
                                                        `table-action-button ${
                                                            user.active
                                                                ? 'danger'
                                                                : ''
                                                        }`
                                                    }
                                                    title={
                                                        user.active
                                                            ? 'Desactivar'
                                                            : 'Activar'
                                                    }
                                                    disabled={
                                                        updatingId ===
                                                        user.id
                                                    }
                                                    onClick={() =>
                                                        handleStatusChange(
                                                            user,
                                                        )
                                                    }
                                                >
                                                    {user.active ? (
                                                        <UserX
                                                            size={16}
                                                        />
                                                    ) : (
                                                        <UserCheck
                                                            size={16}
                                                        />
                                                    )}
                                                </button>
                                            </div>
                                        </td>
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

function SummaryCard({
                         icon,
                         label,
                         value,
                     }) {
    return (
        <div className="user-summary-card">
            <div className="user-summary-icon">
                {icon}
            </div>

            <div>
                <span>{label}</span>
                <strong>{value}</strong>
            </div>
        </div>
    );
}

function getInitials(name) {
    if (!name) {
        return '?';
    }

    return name
        .trim()
        .split(/\s+/)
        .slice(0, 2)
        .map(
            (part) =>
                part[0]?.toUpperCase(),
        )
        .join('');
}