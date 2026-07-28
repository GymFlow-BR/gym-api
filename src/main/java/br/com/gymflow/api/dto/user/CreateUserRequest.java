package br.com.gymflow.api.dto.user;

import br.com.gymflow.api.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres")
        String name,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser valido")
        @Size(max = 150, message = "O email deve ter no maximo 150 caracteres")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, max = 100, message = "A senha deve ter entre 6 a 100 caracteres")
        String password,

        @NotNull(message = "O cargo é obrigatório")
        UserRole role
) {
}