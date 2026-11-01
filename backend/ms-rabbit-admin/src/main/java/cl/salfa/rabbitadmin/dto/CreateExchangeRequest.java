package cl.salfa.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateExchangeRequest(
        @NotBlank String name,

        @NotBlank String type
) {}