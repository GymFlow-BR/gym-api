package br.com.gymflow.api.dto.organization;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(

        @Size(max = 150, message = "O nome da organização deve ter no máximo 150 caracteres")
        String organizationName,

        @Email(message = "O email deve ter formato válido")
        @Size(max = 150, message = "O email deve ter no máximo 150 caracteres")
        String organizationEmail,

        @Size(max = 30, message = "O telefone deve ter no máximo 30 caracteres")
        String organizationPhone
) {
}
