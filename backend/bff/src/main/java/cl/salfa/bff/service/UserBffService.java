package cl.salfa.bff.service;

import cl.salfa.bff.dto.UserRequest;
import cl.salfa.bff.dto.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class UserBffService {

    private final RestClient restClient;

    public UserBffService(
            RestClient.Builder builder,
            @Value("${services.user.base-url}") String baseUrl
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public List<UserResponse> findAll() {
        return restClient
                .get()
                .uri("/api/users")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public UserResponse findById(Long id) {
        return restClient
                .get()
                .uri("/api/users/{id}", id)
                .retrieve()
                .body(UserResponse.class);
    }

    public UserResponse create(UserRequest request) {
        return restClient
                .post()
                .uri("/api/users")
                .body(request)
                .retrieve()
                .body(UserResponse.class);
    }

    public UserResponse update(
            Long id,
            UserRequest request
    ) {
        return restClient
                .put()
                .uri("/api/users/{id}", id)
                .body(request)
                .retrieve()
                .body(UserResponse.class);
    }

    public UserResponse changeStatus(
            Long id,
            boolean active
    ) {
        return restClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/users/{id}/status")
                        .queryParam("active", active)
                        .build(id)
                )
                .retrieve()
                .body(UserResponse.class);
    }
}