package cl.salfa.bff.service;

import cl.salfa.bff.dto.VehicleRequest;
import cl.salfa.bff.dto.VehicleResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class VehicleBffService {

    private final RestClient restClient;

    public VehicleBffService(
            RestClient.Builder builder,
            @Value("${services.vehicles.base-url}") String baseUrl
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public List<VehicleResponse> findAll() {
        return restClient
                .get()
                .uri("/api/vehicles")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public VehicleResponse findById(Long id) {
        return restClient
                .get()
                .uri("/api/vehicles/{id}", id)
                .retrieve()
                .body(VehicleResponse.class);
    }

    public VehicleResponse create(VehicleRequest request) {
        return restClient
                .post()
                .uri("/api/vehicles")
                .body(request)
                .retrieve()
                .body(VehicleResponse.class);
    }

    public VehicleResponse update(
            Long id,
            VehicleRequest request
    ) {
        return restClient
                .put()
                .uri("/api/vehicles/{id}", id)
                .body(request)
                .retrieve()
                .body(VehicleResponse.class);
    }

    public void delete(Long id) {
        restClient
                .delete()
                .uri("/api/vehicles/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}