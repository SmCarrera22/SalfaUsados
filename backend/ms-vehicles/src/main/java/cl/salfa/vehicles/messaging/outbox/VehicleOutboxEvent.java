package cl.salfa.vehicles.messaging.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "vehicle_outbox_events",
        indexes = {
                @Index(
                        name = "idx_vehicle_outbox_pending",
                        columnList = "status, next_attempt_at"
                ),
                @Index(
                        name = "idx_vehicle_outbox_aggregate",
                        columnList = "aggregate_id, created_at"
                )
        }
)
public class VehicleOutboxEvent {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(
            name = "aggregate_id",
            nullable = false,
            updatable = false
    )
    private Long aggregateId;

    @Column(
            name = "aggregate_type",
            nullable = false,
            length = 50,
            updatable = false
    )
    private String aggregateType;

    @Column(
            name = "event_type",
            nullable = false,
            length = 80,
            updatable = false
    )
    private String eventType;

    @Column(
            name = "event_version",
            nullable = false,
            updatable = false
    )
    private Integer eventVersion;

    @Column(
            name = "event_key",
            nullable = false,
            length = 17,
            updatable = false
    )
    private String eventKey;

    @Column(
            nullable = false,
            columnDefinition = "TEXT",
            updatable = false
    )
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(nullable = false)
    private Integer attempts;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "claim_token")
    private UUID claimToken;

    @Column(name = "claimed_until")
    private Instant claimedUntil;

    protected VehicleOutboxEvent() {
        // Constructor requerido por JPA
    }

    public VehicleOutboxEvent(
            UUID id,
            Long aggregateId,
            String eventType,
            Integer eventVersion,
            String eventKey,
            String payload
    ) {
        this.id = Objects.requireNonNull(id);
        this.aggregateId = Objects.requireNonNull(aggregateId);
        this.eventType = Objects.requireNonNull(eventType);
        this.eventVersion = Objects.requireNonNull(eventVersion);
        this.eventKey = Objects.requireNonNull(eventKey);
        this.payload = Objects.requireNonNull(payload);

        this.aggregateType = "VEHICLE";
        this.status = OutboxStatus.PENDING;
        this.createdAt = Instant.now();
        this.attempts = 0;
        this.nextAttemptAt = this.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Long getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getEventType() {
        return eventType;
    }

    public Integer getEventVersion() {
        return eventVersion;
    }

    public String getEventKey() {
        return eventKey;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public UUID getClaimToken() {
        return claimToken;
    }

    public Instant getClaimedUntil() {
        return claimedUntil;
    }

    public void claim(UUID token, Instant until) {
        if (token == null || until == null) {
            throw new IllegalArgumentException(
                    "Token y vencimiento son obligatorios"
            );
        }

        this.status = OutboxStatus.PROCESSING;
        this.claimToken = token;
        this.claimedUntil = until;
        this.attempts++;
    }

    public boolean isOwnedBy(UUID token) {
        return token != null
                && status == OutboxStatus.PROCESSING
                && token.equals(claimToken);
    }

    public void markPublished(Instant when) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = when;
        this.claimToken = null;
        this.claimedUntil = null;
        this.nextAttemptAt = null;
        this.lastError = null;
    }

    public void scheduleRetry(Instant when, String error) {
        this.status = OutboxStatus.PENDING;
        this.nextAttemptAt = when;
        this.lastError = error;
        this.claimToken = null;
        this.claimedUntil = null;
    }

    public void markFailed(String error) {
        this.status = OutboxStatus.FAILED;
        this.lastError = error;
        this.claimToken = null;
        this.claimedUntil = null;
    }

}
