package cl.salfa.user.repository;

import cl.salfa.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEntraId(String entraId);

    boolean existsByEmail(String email);

    boolean existsByEntraId(String entraId);
}