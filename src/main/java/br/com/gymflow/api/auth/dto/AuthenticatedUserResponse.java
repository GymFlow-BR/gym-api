package br.com.gymflow.api.auth.dto;

import br.com.gymflow.api.domain.enums.UserRole;

public record AuthenticatedUserResponse(
        Long userId,
        Long organizationId,
        String name,
        String email,
        UserRole role
) {
}
