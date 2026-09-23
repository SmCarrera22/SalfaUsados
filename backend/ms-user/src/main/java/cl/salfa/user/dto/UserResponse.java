package cl.salfa.user.dto;

import cl.salfa.user.entity.UserRole;

import java.time.LocalDateTime;

public record UserResponse(

        Long id,
        String entraId,
        String name,
        String email,
        String branch,
        UserRole role,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}