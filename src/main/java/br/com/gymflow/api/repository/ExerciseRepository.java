package br.com.gymflow.api.repository;

import br.com.gymflow.api.domain.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findByOrganizationId(Long organizationId);

    List<Exercise> findByOrganizationIdAndActiveTrue(Long organizationId);

    boolean existsByOrganizationIdAndExerciseNameIgnoreCase(
            Long organizationId,
            String exerciseName
    );

    boolean existsByOrganizationIdAndExerciseNameIgnoreCaseAndIdNot(
            Long organizationId,
            String exerciseName,
            Long id
    );
}