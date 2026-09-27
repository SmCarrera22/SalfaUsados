package cl.salfa.vehicles.service;

import cl.salfa.vehicles.dto.VehicleRequest;
import cl.salfa.vehicles.dto.VehicleResponse;
import cl.salfa.vehicles.entity.Vehicle;
import cl.salfa.vehicles.exception.ResourceNotFoundException;
import cl.salfa.vehicles.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public List<VehicleResponse> findAll() {
        return vehicleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public VehicleResponse findById(Long id) {
        Vehicle vehicle = findVehicleById(id);

        return toResponse(vehicle);
    }

    public VehicleResponse create(VehicleRequest request) {

        if (vehicleRepository.existsByVin(request.vin())) {
            throw new IllegalArgumentException(
                    "Ya existe un vehículo con el VIN: " + request.vin()
            );
        }

        if (vehicleRepository.existsByPlate(request.plate())) {
            throw new IllegalArgumentException(
                    "Ya existe un vehículo con la patente: " + request.plate()
            );
        }

        Vehicle vehicle = Vehicle.builder()
                .vin(normalize(request.vin()))
                .plate(normalize(request.plate()))
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

        return toResponse(savedVehicle);
    }

    public VehicleResponse update(Long id, VehicleRequest request) {

        Vehicle vehicle = findVehicleById(id);

        vehicleRepository.findByVin(request.vin())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Ya existe un vehículo con el VIN: " + request.vin()
                    );
                });

        vehicleRepository.findByPlate(request.plate())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Ya existe un vehículo con la patente: " + request.plate()
                    );
                });

        vehicle.setVin(normalize(request.vin()));
        vehicle.setPlate(normalize(request.plate()));
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

        return toResponse(updatedVehicle);
    }

    public void delete(Long id) {
        Vehicle vehicle = findVehicleById(id);

        vehicleRepository.delete(vehicle);
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
                : value.trim().toUpperCase();
    }
}