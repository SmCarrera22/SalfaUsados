package cl.salfa.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateQueueRequest(
        @NotBlank String name
) {}