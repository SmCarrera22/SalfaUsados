const ROLE_CONFIG = {
    ADMIN: {
        label: 'Administrador',
        className: 'admin',
    },

    OPERATOR: {
        label: 'Operador',
        className: 'operator',
    },

    ANALYST: {
        label: 'Analista',
        className: 'analyst',
    },
};

export default function UserRoleBadge({
                                          role,
                                      }) {
    const config =
        ROLE_CONFIG[role] ?? {
            label: role ?? 'Sin rol',
            className: 'default',
        };

    return (
        <span
            className={
                `role-badge ${config.className}`
            }
        >
      {config.label}
    </span>
    );
}