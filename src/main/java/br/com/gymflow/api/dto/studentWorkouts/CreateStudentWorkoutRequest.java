package br.com.gymflow.api.dto.studentWorkouts;

import br.com.gymflow.api.domain.enums.WeekDay;
import jakarta.validation.constraints.NotNull;

public record CreateStudentWorkoutRequest(

        @NotNull(message = "O treino é obrigatório")
        Long workoutId,

        @NotNull(message = "O dia da semana é obrigatório")
        WeekDay weekDay
) {
}