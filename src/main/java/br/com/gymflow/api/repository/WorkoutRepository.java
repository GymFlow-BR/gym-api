package br.com.gymflow.api.repository;

import br.com.gymflow.api.domain.Workout;
import br.com.gymflow.api.domain.enums.WorkoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    List<Workout> findByTeacherOrganizationId(Long organizationId);

    List<Workout> findByTeacherOrganizationIdAndStatus(Long organizationId, WorkoutStatus status);
}