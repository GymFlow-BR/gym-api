package br.com.gymflow.api.service;

import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.dto.organization.CreateOrganizationRequest;
import br.com.gymflow.api.dto.organization.OrganizationResponse;
import br.com.gymflow.api.dto.organization.UpdateOrganizationRequest;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.mapper.OrganizationMapper;
import br.com.gymflow.api.repository.OrganizationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Transactional
    public OrganizationResponse create(CreateOrganizationRequest request) {
        Organization organization = organizationMapper.toEntity(request);
        Organization organizationSaved = organizationRepository.save(organization);

        return organizationMapper.toResponse(organizationSaved);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> findAll() {
        return organizationRepository.findAll()
                .stream()
                .map(organizationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganizationResponse findById(Long id) {
        Organization organization = getOrganizationById(id);

        return organizationMapper.toResponse(organization);
    }

    @Transactional
    public OrganizationResponse update(Long id, UpdateOrganizationRequest request) {
        Organization organization = getOrganizationById(id);

        organizationMapper.updateEntity(organization, request);
        Organization updatedOrganization = organizationRepository.save(organization);

        return organizationMapper.toResponse(updatedOrganization);
    }

    @Transactional
    public OrganizationResponse patch(Long id, UpdateOrganizationRequest request) {
        Organization organization = getOrganizationById(id);

        if (request.organizationName() != null) {
            organization.setOrganizationName((request.organizationName()));
        }

        if (request.organizationEmail() != null) {
            organization.setOrganizationEmail(request.organizationEmail());
        }

        if (request.organizationPhone() != null) {
            organization.setOrganizationPhone(request.organizationPhone());
        }

        Organization updateOrganization = organizationRepository.save(organization);

        return organizationMapper.toResponse(updateOrganization);
    }

    @Transactional
    public void delete(Long id){
        Organization organization = getOrganizationById(id);

        organization.setActive(false);

        organizationRepository.save(organization);
    }

    private Organization getOrganizationById(Long id) {
        return  organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
    }
}