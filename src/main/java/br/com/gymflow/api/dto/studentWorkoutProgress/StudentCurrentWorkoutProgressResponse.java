package br.com.gymflow.api.dto.studentWorkoutProgress;

import java.util.List;

public record StudentCurrentWorkoutProgressResponse(
        Long studentId,
        Long studentWorkoutId,
        Long workoutId,
        String workoutName,
        Integer totalExercises,
        Integer completedExercises,
        Integer progressPercentage,
        List<StudentCurrentWorkoutExerciseProgressResponse> exercises
) {
}