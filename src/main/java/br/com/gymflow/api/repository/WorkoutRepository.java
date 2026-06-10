package br.com.gymflow.api.repository;

import br.com.gymflow.api.domain.Workout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
}
