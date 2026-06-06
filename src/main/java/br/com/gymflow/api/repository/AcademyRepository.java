package br.com.gymflow.api.repository;

import br.com.gymflow.api.domain.Academy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademyRepository extends JpaRepository<Academy, Long> {
}
