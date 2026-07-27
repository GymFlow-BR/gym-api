package br.com.gymflow.api.auth.dto;

import br.com.gymflow.api.domain.enums.OrganizationType;

public record RegisterOrganizationResponse(
        Long organizationId,
        String organizationName,
        OrganizationType organizationType,
        Long adminUserId,
        String adminName,
        String adminEmail
) {
}