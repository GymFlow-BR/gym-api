package br.com.gymflow.api.dto.studentWorkoutProgress;

import java.time.LocalDateTime;

public record StudentCurrentWorkoutExerciseProgressResponse(
        Long workoutExerciseId,
        Long exerciseId,
        String exerciseName,
        Integer exerciseOrder,
        Boolean completed,
        LocalDateTime completedAt
) {
}