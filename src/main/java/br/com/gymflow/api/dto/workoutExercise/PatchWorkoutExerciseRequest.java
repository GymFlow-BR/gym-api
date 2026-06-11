package br.com.gymflow.api.dto.workoutExercise;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record PatchWorkoutExerciseRequest(

        @Min(value = 1, message = "A ordem do exercício deve ser maior ou igual a 1")
        Integer exerciseOrder,

        @Min(value = 1, message = "A quantidade de séries deve ser maior ou igual a 1")
        Integer sets,

        @Min(value = 1, message = "A quantidade de repetições deve ser maior ou igual a 1")
        Integer reps,

        @DecimalMin(value = "0.00", message = "A carga recomendada não pode ser negativa")
        @Digits(integer = 4, fraction = 2, message = "A carga recomendada deve ter no máximo 4 dígitos inteiros e 2 casas decimais")
        BigDecimal recommendedLoad,

        @Min(value = 0, message = "O tempo de descanso não pode ser negativo")
        Integer restTimeSeconds,

        String notes
) {
}