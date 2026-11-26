package cl.salfa.bff.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WorkshopBffService {

    private final RestClient restClient;

    public WorkshopBffService(
            RestClient.Builder builder,
            @Value("${services.workshop.url}") String baseUrl
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public ResponseEntity<String> get(String path) {

        ResponseEntity<String> response = restClient.get()
                .uri(path)
                .headers(headers ->
                        headers.setBearerAuth(currentAccessToken()))
                .retrieve()
                .toEntity(String.class);

        return buildResponse(response);
    }

    public ResponseEntity<String> send(
            HttpMethod method,
            String path,
            String json
    ) {

        ResponseEntity<String> response = restClient.method(method)
                .uri(path)
                .headers(headers ->
                        headers.setBearerAuth(currentAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toEntity(String.class);

        return buildResponse(response);
    }

    private ResponseEntity<String> buildResponse(
            ResponseEntity<String> response
    ) {

        ResponseEntity.BodyBuilder builder =
                ResponseEntity.status(response.getStatusCode());

        MediaType contentType =
                response.getHeaders().getContentType();

        if (contentType != null) {
            builder.contentType(contentType);
        }

        if (response.getHeaders().getLocation() != null) {
            builder.location(response.getHeaders().getLocation());
        }

        return builder.body(response.getBody());
    }

    private String currentAccessToken() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (!(authentication instanceof
                JwtAuthenticationToken jwtAuthentication)
                || !authentication.isAuthenticated()) {

            throw new org.springframework.security.authentication
                    .AuthenticationCredentialsNotFoundException(
                    "No existe un JWT autenticado para reenviar"
            );
        }

        return jwtAuthentication.getToken().getTokenValue();
    }
}
