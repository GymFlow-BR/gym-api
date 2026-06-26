package br.com.gymflow.api.dto.user;

import br.com.gymflow.api.domain.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        Long organizationId,
        String organizationName,
        String name,
        String email,
        UserRole role,
        Boolean active,
        LocalDateTime createdAt
) {
}