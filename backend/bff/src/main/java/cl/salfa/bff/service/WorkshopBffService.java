package cl.salfa.bff.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
        return restClient.get()
                .uri(path)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> send(
            HttpMethod method,
            String path,
            String json
    ) {
        return restClient.method(method)
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toEntity(String.class);
    }
}