package br.com.gymflow.api.dto.studentWorkouts;

import br.com.gymflow.api.domain.enums.WorkoutStatus;

import java.time.LocalDateTime;

public record StudentWorkoutResponse(

        Long studentWorkoutId,
        Long studentId,
        Long workoutId,
        String workoutName,
        LocalDateTime assignedAt,
        WorkoutStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
