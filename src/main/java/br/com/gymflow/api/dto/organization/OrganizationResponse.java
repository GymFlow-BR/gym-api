package br.com.gymflow.api.dto.organization;

import br.com.gymflow.api.domain.enums.OrganizationType;

import java.time.LocalDateTime;

public record OrganizationResponse(
        Long id,
        String organizationName,
        OrganizationType organizationType,
        String organizationEmail,
        String organizationPhone,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
