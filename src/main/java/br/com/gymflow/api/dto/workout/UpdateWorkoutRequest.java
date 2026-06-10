package br.com.gymflow.api.dto.workout;

import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.enums.WorkoutStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateWorkoutRequest(

        @Size(max = 120, message = "O nome do treino deve ter no máximo 120 caracteres")
        String workoutName,

        WorkoutStatus status
) {
}