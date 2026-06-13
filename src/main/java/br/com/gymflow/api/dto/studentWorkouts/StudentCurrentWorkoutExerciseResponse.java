package br.com.gymflow.api.dto.studentWorkouts;

import java.math.BigDecimal;

public record StudentCurrentWorkoutExerciseResponse(

        Long workoutExerciseId,
        Long exerciseId,
        String exerciseName,
        String equipmentName,
        String muscleGroup,
        String description,
        Integer exerciseOrder,
        Integer sets,
        Integer reps,
        BigDecimal recommendedLoad,
        Integer restTimeSeconds,
        String notes,
        String imageUrl,
        String videoUrl
) {
}