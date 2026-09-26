const STATUS_CONFIG = {
    AVAILABLE: {
        label: 'Disponible',
        className: 'available',
    },

    RESERVED: {
        label: 'Reservado',
        className: 'reserved',
    },

    SOLD: {
        label: 'Vendido',
        className: 'sold',
    },

    WORKSHOP: {
        label: 'En taller',
        className: 'workshop',
    },
};

export default function VehicleStatusBadge({
                                               status,
                                           }) {
    const config =
        STATUS_CONFIG[status] ?? {
            label: status ?? 'Sin estado',
            className: 'default',
        };

    return (
        <span
            className={
                `status-badge ${config.className}`
            }
        >
      <span className="status-dot" />

            {config.label}
    </span>
    );
}