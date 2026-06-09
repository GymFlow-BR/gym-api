package br.com.gymflow.api.mapper;

import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.dto.organization.CreateOrganizationRequest;
import br.com.gymflow.api.dto.organization.OrganizationResponse;
import br.com.gymflow.api.dto.organization.UpdateOrganizationRequest;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public Organization toEntity(CreateOrganizationRequest request) {
        Organization organization = new Organization();

        organization.setOrganizationName(request.organizationName());
        organization.setOrganizationType(request.organizationType());
        organization.setOrganizationEmail(request.organizationEmail());
        organization.setOrganizationPhone(request.organizationPhone());

        return organization;
    }

    public OrganizationResponse toResponse(Organization organization) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getOrganizationName(),
                organization.getOrganizationType(),
                organization.getOrganizationEmail(),
                organization.getOrganizationPhone(),
                organization.getActive(),
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }

    public void updateEntity(Organization organization, UpdateOrganizationRequest request) {
        organization.setOrganizationName(request.organizationName());
        organization.setOrganizationEmail(request.organizationEmail());
        organization.setOrganizationPhone(request.organizationPhone());

    }
}