package br.com.gymflow.api.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres")
        String name,

        @Email(message = "O email deve ser valido")
        @Size(max = 150, message = "O email deve ter no maximo 150 caracteres")
        String email,

        Boolean active
){
}