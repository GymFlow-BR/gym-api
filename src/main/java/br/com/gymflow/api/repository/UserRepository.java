package br.com.gymflow.api.repository;

import br.com.gymflow.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
