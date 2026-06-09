package br.com.gymflow.api.dto.organization;

import br.com.gymflow.api.domain.enums.OrganizationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record CreateOrganizationRequest(

        @NotBlank(message = "O nome da organização é obrigatório")
        @Size(max = 150, message = "O nome da organização deve ter no máximo 150 caracteres")
        String organizationName,

        @NotNull(message = "O tipo da organização é obrigatório")
        OrganizationType organizationType,

        @Email(message = "O email deve ter formato válido")
        @Size(max = 150, message = "O email deve ter no máximo 150 caracteres")
        String organizationEmail,

        @Size(max = 30, message = "O telefone deve ter no máximo 30 caracteres")
        String organizationPhone
) {
}