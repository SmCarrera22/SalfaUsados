package cl.salfa.workshop.config;

public final class RabbitNames {

    private RabbitNames() {}

    public static final String COMMAND_EXCHANGE = "salfa360.commands";
    public static final String EVENT_EXCHANGE = "salfa360.events";
    public static final String DLX = "salfa360.dlx";

    public static final String WORKSHOP_QUEUE = "workshop.commands.q";
    public static final String NOTIFICATION_QUEUE = "notification.events.q";
    public static final String AUDIT_QUEUE = "audit.events.q";

    public static final String WORKSHOP_DLQ = "workshop.commands.dlq";
    public static final String NOTIFICATION_DLQ = "notification.events.dlq";
    public static final String AUDIT_DLQ = "audit.events.dlq";

    public static final String WORKSHOP_KEY = "workshop.order.create";
    public static final String NOTIFICATION_KEY = "workshop.order.*";
    public static final String AUDIT_KEY = "workshop.#";
}