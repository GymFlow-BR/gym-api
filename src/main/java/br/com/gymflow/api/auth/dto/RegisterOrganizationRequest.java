package br.com.gymflow.api.auth.dto;

import br.com.gymflow.api.domain.enums.OrganizationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterOrganizationRequest(

        @NotBlank(message = "O nome da organização é obrigatório")
        @Size(max = 150, message = "O nome da organização deve ter no máximo 150 caracteres")
        String organizationName,

        @NotNull(message = "O tipo da organização é obrigatório")
        OrganizationType organizationType,

        @NotBlank(message = "O email da organização é obrigatório")
        @Email(message = "O email da organização deve ser válido")
        @Size(max = 150, message = "O email da organização deve ter no máximo 150 caracteres")
        String organizationEmail,

        @Size(max = 30, message = "O telefone da organização deve ter no máximo 30 caracteres")
        String organizationPhone,

        @NotBlank(message = "O nome do administrador é obrigatório")
        @Size(max = 150, message = "O nome do administrador deve ter no máximo 150 caracteres")
        String adminName,

        @NotBlank(message = "O email do administrador é obrigatório")
        @Email(message = "O email do administrador deve ser válido")
        @Size(max = 150, message = "O email do administrador deve ter no máximo 150 caracteres")
        String adminEmail,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, max = 100, message = "A senha deve ter entre 6 a 100 caracteres")
        String password
) {
}