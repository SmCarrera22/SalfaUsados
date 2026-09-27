package cl.salfa.user.dto;

import cl.salfa.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(

        String entraId,

        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String email,

        @NotBlank(message = "La sucursal es obligatoria")
        String branch,

        @NotNull(message = "El rol es obligatorio")
        UserRole role,

        Boolean active

) {
}