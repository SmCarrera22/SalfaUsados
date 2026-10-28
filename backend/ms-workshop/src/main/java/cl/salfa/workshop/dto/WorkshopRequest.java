package cl.salfa.workshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkshopRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 100)
        String city,

        @Size(max = 150)
        String address

) {}