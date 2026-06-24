package br.com.gymflow.api.auth.dto;

import br.com.gymflow.api.domain.enums.UserRole;

public record LoginResponse(
        String Token,
        Long userId,
        String name,
        String email,
        UserRole role
) {
}