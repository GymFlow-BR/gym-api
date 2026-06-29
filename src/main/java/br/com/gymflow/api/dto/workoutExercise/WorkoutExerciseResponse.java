package br.com.gymflow.api.dto.workoutExercise;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WorkoutExerciseResponse(
        Long id,
        Long workoutId,
        Long exerciseId,
        String exerciseName,
        String muscleGroup,
        String equipmentName,
        Integer exerciseOrder,
        Integer sets,
        String reps,
        BigDecimal recommendedLoad,
        Integer restTimeSeconds,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}