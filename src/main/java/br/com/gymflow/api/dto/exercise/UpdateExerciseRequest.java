package br.com.gymflow.api.dto.exercise;

public record UpdateExerciseRequest(
        String exerciseName,
        String muscleGroup,
        String description,
        String equipmentName,
        String imageUrl,
        String videoUrl
) {
}
