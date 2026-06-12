package br.com.gymflow.api.dto.studentWorkouts;

import jakarta.validation.constraints.NotNull;


public record CreateStudentWorkoutRequest(

        @NotNull(message = "O treino é obrigatório")
        Long workoutId
) {
}