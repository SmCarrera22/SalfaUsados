package cl.salfa.user.service;

import cl.salfa.user.dto.UserRequest;
import cl.salfa.user.dto.UserResponse;
import cl.salfa.user.entity.User;
import cl.salfa.user.exception.ResourceNotFoundException;
import cl.salfa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse findById(Long id) {
        return toResponse(findUserById(id));
    }

    public UserResponse create(UserRequest request) {

        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con el correo: " + normalizedEmail
            );
        }

        if (request.entraId() != null
                && !request.entraId().isBlank()
                && userRepository.existsByEntraId(request.entraId())) {

            throw new IllegalArgumentException(
                    "Ya existe un usuario asociado al identificador de Entra ID"
            );
        }

        User user = User.builder()
                .entraId(normalizeNullable(request.entraId()))
                .name(request.name().trim())
                .email(normalizedEmail)
                .branch(request.branch().trim())
                .role(request.role())
                .active(request.active() == null || request.active())
                .build();

        return toResponse(userRepository.save(user));
    }

    public UserResponse update(Long id, UserRequest request) {

        User user = findUserById(id);

        String normalizedEmail = normalizeEmail(request.email());

        userRepository.findByEmail(normalizedEmail)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Ya existe un usuario con el correo: " + normalizedEmail
                    );
                });

        String entraId = normalizeNullable(request.entraId());

        if (entraId != null) {
            userRepository.findByEntraId(entraId)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException(
                                "El identificador de Entra ID ya está asociado a otro usuario"
                        );
                    });
        }

        user.setEntraId(entraId);
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setBranch(request.branch().trim());
        user.setRole(request.role());

        if (request.active() != null) {
            user.setActive(request.active());
        }

        return toResponse(userRepository.save(user));
    }

    public UserResponse changeStatus(Long id, boolean active) {
        User user = findUserById(id);

        user.setActive(active);

        return toResponse(userRepository.save(user));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado con ID: " + id
                        )
                );
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEntraId(),
                user.getName(),
                user.getEmail(),
                user.getBranch(),
                user.getRole(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}