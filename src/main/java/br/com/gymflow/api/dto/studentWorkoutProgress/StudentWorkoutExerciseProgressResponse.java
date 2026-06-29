package br.com.gymflow.api.dto.studentWorkoutProgress;

import java.time.LocalDateTime;

public record StudentWorkoutExerciseProgressResponse(
        Long studentWorkoutId,
        Long workoutExerciseId,
        Boolean completed,
        LocalDateTime completedAt
) {
}
