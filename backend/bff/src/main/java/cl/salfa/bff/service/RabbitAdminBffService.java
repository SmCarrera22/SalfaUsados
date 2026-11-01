package cl.salfa.bff.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RabbitAdminBffService {

    private final RestClient restClient;

    public RabbitAdminBffService(
            RestClient.Builder builder,
            @Value("${services.rabbit-admin.url}") String baseUrl
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public ResponseEntity<String> create(
            String path,
            String token,
            String json
    ) {
        return restClient.post()
                .uri(path)
                .headers(headers -> {
                    headers.setBearerAuth(token);
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .body(json)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<Void> deleteQueue(
            String name,
            String token
    ) {
        return restClient.delete()
                .uri("/api/rabbit-admin/queues/{name}", name)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .toBodilessEntity();
    }

    public ResponseEntity<Void> deleteExchange(
            String name,
            String token
    ) {
        return restClient.delete()
                .uri("/api/rabbit-admin/exchanges/{name}", name)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .toBodilessEntity();
    }

    public ResponseEntity<Void> deleteBinding(
            String token,
            String json
    ) {
        return restClient.method(HttpMethod.DELETE)
                .uri("/api/rabbit-admin/bindings")
                .headers(headers -> {
                    headers.setBearerAuth(token);
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .body(json)
                .retrieve()
                .toBodilessEntity();
    }
}
