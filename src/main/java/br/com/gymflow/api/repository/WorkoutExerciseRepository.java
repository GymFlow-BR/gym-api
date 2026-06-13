package br.com.gymflow.api.repository;

import br.com.gymflow.api.domain.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {
    List<WorkoutExercise> findAllByWorkoutId(Long workoutId);

    List<WorkoutExercise> findAllByWorkoutIdOrderByExerciseOrderAsc(Long workoutId);
}
