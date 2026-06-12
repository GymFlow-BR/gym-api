package br.com.gymflow.api.dto.studentWorkouts;

import br.com.gymflow.api.domain.enums.WorkoutStatus;
import jakarta.validation.constraints.NotNull;


public record PatchStudentWorkoutRequest(

        @NotNull(message = "O status do treino é obrigatório")
        WorkoutStatus status
) {
}