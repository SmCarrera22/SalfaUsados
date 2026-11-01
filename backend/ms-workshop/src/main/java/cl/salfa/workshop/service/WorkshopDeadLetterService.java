package cl.salfa.workshop.service;

import cl.salfa.workshop.entity.WorkshopDeadLetterEvent;
import cl.salfa.workshop.repository.WorkshopDeadLetterEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WorkshopDeadLetterService {

    private final WorkshopDeadLetterEventRepository repository;

    @Transactional
    public void register(
            String sourceQueue,
            String messageId,
            String payload
    ) {
        WorkshopDeadLetterEvent entity =
                new WorkshopDeadLetterEvent();

        entity.setSourceQueue(sourceQueue);
        entity.setMessageId(messageId);
        entity.setPayload(payload);
        entity.setReceivedAt(Instant.now());

        repository.saveAndFlush(entity);
    }
}