package br.com.gymflow.api.repository;

import br.com.gymflow.api.domain.StudentWorkoutExerciseProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentWorkoutExerciseProgressRepository
        extends JpaRepository<StudentWorkoutExerciseProgress, Long> {

    Optional<StudentWorkoutExerciseProgress> findByStudentWorkoutIdAndWorkoutExerciseId(
            Long studentWorkoutId,
            Long workoutExerciseId
    );

    List<StudentWorkoutExerciseProgress> findAllByStudentWorkoutId(Long studentWorkoutId);
}