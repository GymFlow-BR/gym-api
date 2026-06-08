package br.com.gymflow.api.dto.exercise;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CreateExerciseRequest(

        // Id so sera pedido no request nesta primeira versao
        @NotNull(message = "O id da academia é obrigatório")
        Long organizationId,

        @NotBlank(message = "O nome do exercício é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String exerciseName,

        @NotBlank(message = "O grupo muscular é obrigatório")
        @Size(max = 80, message = "O nome do grupo muscular deve ter no máximo 80 caracteres")
        String muscleGroup,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String description,


        @Size(max = 120, message = "O nome do equipamento deve ter no máximo 120 caracteres")
        String equipmentName,

        @URL(message = "A URL da imagem deve ser válida")
        @Size(max = 500, message = "A URL da imagem deve ter no máximo 500 caracteres")
        String imageUrl,

        @URL(message = "A URL do video deve ser válida")
        @Size(max = 500, message = "A URL do video deve ter no máximo 500 caracteres")
        String videoUrl
) {
}
