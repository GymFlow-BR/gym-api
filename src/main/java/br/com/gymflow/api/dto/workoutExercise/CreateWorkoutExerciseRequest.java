package br.com.gymflow.api.dto.workoutExercise;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateWorkoutExerciseRequest(


        @NotNull(message = "O exercicio é obrigatório")
        Long exerciseId,

        @NotNull(message = "A ordem dos exercicios é obrigatória")
        @Min(value = 1, message = "A ordem do exercício deve ser maior ou igual a 1")
        Integer exerciseOrder,

        @NotNull(message = "O numero de séries é obrigatório")
        @Min(value = 1, message = "A quantidade de séries deve ser maior ou igual a 1")
        Integer sets,

        @NotBlank(message = "As repetições são obrigatórias")
        @Size(max = 50, message = "As repetições devem ter no máximo 50 caracteres")
        String reps,

        @DecimalMin(value = "0.00", message = "A carga recomendada não pode ser negativa")
        @Digits(integer = 4, fraction = 2, message = "A carga recomendada deve ter no máximo 4 dígitos inteiros e 2 casas decimais")
        BigDecimal recommendedLoad,

        @Min(value = 0, message = "O tempo de descanso não pode ser negativo")
        Integer restTimeSeconds,

        String notes
) {
}