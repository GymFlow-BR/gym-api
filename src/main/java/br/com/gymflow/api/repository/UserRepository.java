package br.com.gymflow.api.repository;

import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByOrganizationId(Long organizationId);

    List<User> findByOrganizationIdAndRole(Long organizationId, UserRole role);
}