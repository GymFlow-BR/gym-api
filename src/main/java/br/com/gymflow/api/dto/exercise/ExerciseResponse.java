package br.com.gymflow.api.dto.exercise;

import java.time.LocalDateTime;

public record ExerciseResponse(
        Long id,
        Long academyId,
        String exerciseName,
        String muscleGroup,
        String description,
        String equipmentName,
        String imageUrl,
        String videoUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
