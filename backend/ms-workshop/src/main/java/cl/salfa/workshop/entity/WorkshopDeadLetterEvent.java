package cl.salfa.workshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "workshop_dead_letter_events")
@Getter
@Setter
@NoArgsConstructor
public class WorkshopDeadLetterEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_queue", nullable = false, length = 150)
    private String sourceQueue;

    @Column(name = "message_id", length = 255)
    private String messageId;

    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;
}