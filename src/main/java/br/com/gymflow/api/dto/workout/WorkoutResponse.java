package br.com.gymflow.api.dto.workout;

import br.com.gymflow.api.domain.enums.WorkoutStatus;

import java.time.LocalDateTime;

public record WorkoutResponse(
        Long workoutId,
        Long teacherId,
        String workoutName,
        WorkoutStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}