package cl.salfa.bff.dto;

public record UserRequest(
        String entraId,
        String name,
        String email,
        String branch,
        String role,
        Boolean active
) {
}