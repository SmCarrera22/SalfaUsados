
package cl.salfa.vehicles.service;

import cl.salfa.vehicles.dto.VehicleRequest;
import cl.salfa.vehicles.dto.VehicleResponse;
import cl.salfa.vehicles.entity.Vehicle;
import cl.salfa.vehicles.exception.ResourceNotFoundException;
import cl.salfa.vehicles.messaging.dto.VehicleEvent;
import cl.salfa.vehicles.messaging.outbox.VehicleOutboxService;
import cl.salfa.vehicles.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleOutboxService outboxService;

    @Transactional(readOnly = true)
    public List<VehicleResponse> findAll() {
        return vehicleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse findById(Long id) {
        Vehicle vehicle = findVehicleById(id);
        return toResponse(vehicle);
    }

    @Transactional
    public VehicleResponse create(VehicleRequest request) {

        String vin = normalize(request.vin());
        String plate = normalize(request.plate());

        if (vehicleRepository.existsByVin(vin)) {
            throw new IllegalArgumentException(
                    "Ya existe un vehículo con el VIN: " + vin
            );
        }

        if (vehicleRepository.existsByPlate(plate)) {
            throw new IllegalArgumentException(
                    "Ya existe un vehículo con la patente: " + plate
            );
        }

        Vehicle vehicle = Vehicle.builder()
                .vin(vin)
                .plate(plate)
                .brand(request.brand())
                .model(request.model())
                .version(request.version())
                .year(request.year())
                .mileage(request.mileage())
                .color(request.color())
                .fuelType(request.fuelType())
                .branch(request.branch())
                .status(request.status())
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        storeVehicleEvent(
                savedVehicle,
                "VEHICLE_CREATED"
        );

        return toResponse(savedVehicle);
    }

    @Transactional
    public VehicleResponse update(Long id, VehicleRequest request) {

        Vehicle vehicle = findVehicleById(id);

        String vin = normalize(request.vin());
        String plate = normalize(request.plate());

        vehicleRepository.findByVin(vin)
                .filter(existing ->
                        !existing.getId().equals(id)
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Ya existe un vehículo con el VIN: " + vin
                    );
                });

        vehicleRepository.findByPlate(plate)
                .filter(existing ->
                        !existing.getId().equals(id)
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Ya existe un vehículo con la patente: " + plate
                    );
                });

        vehicle.setVin(vin);
        vehicle.setPlate(plate);
        vehicle.setBrand(request.brand());
        vehicle.setModel(request.model());
        vehicle.setVersion(request.version());
        vehicle.setYear(request.year());
        vehicle.setMileage(request.mileage());
        vehicle.setColor(request.color());
        vehicle.setFuelType(request.fuelType());
        vehicle.setBranch(request.branch());
        vehicle.setStatus(request.status());

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);

        storeVehicleEvent(
                updatedVehicle,
                "VEHICLE_UPDATED"
        );

        return toResponse(updatedVehicle);
    }

    @Transactional
    public void delete(Long id) {

        Vehicle vehicle = findVehicleById(id);

        storeVehicleEvent(
                vehicle,
                "VEHICLE_DELETED"
        );

        vehicleRepository.delete(vehicle);
    }

    private void storeVehicleEvent(
            Vehicle vehicle,
            String eventType
    ) {

        VehicleEvent event = new VehicleEvent(
                UUID.randomUUID(),
                eventType,
                1,
                vehicle.getId(),
                vehicle.getVin(),
                vehicle.getBranch(),
                Instant.now()
        );

        outboxService.store(event);
    }

    private Vehicle findVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehículo no encontrado con ID: " + id
                        )
                );
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVin(),
                vehicle.getPlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getVersion(),
                vehicle.getYear(),
                vehicle.getMileage(),
                vehicle.getColor(),
                vehicle.getFuelType(),
                vehicle.getBranch(),
                vehicle.getStatus(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }

    private String normalize(String value) {
        return value == null
                ? null
                : value.trim().toUpperCase(Locale.ROOT);
    }
}
