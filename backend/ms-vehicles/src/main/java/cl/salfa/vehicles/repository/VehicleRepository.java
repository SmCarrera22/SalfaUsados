package cl.salfa.vehicles.repository;

import cl.salfa.vehicles.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByVin(String vin);

    Optional<Vehicle> findByPlate(String plate);

    boolean existsByVin(String vin);

    boolean existsByPlate(String plate);
}