package cl.salfa.bff.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String entraId,
        String name,
        String email,
        String branch,
        String role,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}