package br.com.gymflow.api.event;

import java.time.LocalDateTime;

public record StudentWorkoutExerciseCompletedEvent(
        Long studentId,
        Long studentWorkoutId,
        Long workoutId,
        Long workoutExerciseId,
        Long exerciseId,
        String exerciseName,
        LocalDateTime completedAt
) {
}