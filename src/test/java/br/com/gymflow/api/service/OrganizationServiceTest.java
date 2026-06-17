package br.com.gymflow.api.service;

import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.domain.enums.OrganizationType;
import br.com.gymflow.api.dto.organization.CreateOrganizationRequest;
import br.com.gymflow.api.dto.organization.OrganizationResponse;
import br.com.gymflow.api.dto.organization.UpdateOrganizationRequest;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.mapper.OrganizationMapper;
import br.com.gymflow.api.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMapper organizationMapper;

    @InjectMocks
    private OrganizationService organizationService;


    @Test
    void shouldCreateOrganizationSuccessfully() {
        // Arrange
        Long organizationId = 10L;

        CreateOrganizationRequest request = createOrganizationRequest();

        Organization organizationToSave = createOrganization(null);
        Organization savedOrganization = createOrganization(organizationId);

        OrganizationResponse expectedResponse = createOrganizationResponse(
                organizationId,
                "GymFlow Academy",
                OrganizationType.ACADEMY,
                "contato@gymflow.com",
                "24999999999",
                true
        );

        when(organizationMapper.toEntity(request))
                .thenReturn(organizationToSave);

        when(organizationRepository.save(organizationToSave))
                .thenReturn(savedOrganization);

        when(organizationMapper.toResponse(savedOrganization))
                .thenReturn(expectedResponse);

        // Act
        OrganizationResponse response = organizationService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals(organizationId, response.id());
        assertEquals("GymFlow Academy", response.organizationName());
        assertEquals(OrganizationType.ACADEMY, response.organizationType());
        assertEquals("contato@gymflow.com", response.organizationEmail());
        assertEquals("24999999999", response.organizationPhone());
        assertTrue(response.active());

        verify(organizationMapper).toEntity(request);
        verify(organizationRepository).save(organizationToSave);
        verify(organizationMapper).toResponse(savedOrganization);
    }

    @Test
    void shouldFindAllOrganizationsSuccessfully() {
        // Arrange
        Organization organizationA = createOrganization(10L);

        Organization organizationB = createOrganization(20L);
        organizationB.setOrganizationName("Personal João");
        organizationB.setOrganizationType(OrganizationType.PERSONAL);
        organizationB.setOrganizationEmail("joao@gymflow.com");
        organizationB.setOrganizationPhone("24888888888");

        OrganizationResponse responseA = createOrganizationResponse(
                10L,
                "GymFlow Academy",
                OrganizationType.ACADEMY,
                "contato@gymflow.com",
                "24999999999",
                true
        );

        OrganizationResponse responseB = createOrganizationResponse(
                20L,
                "Personal João",
                OrganizationType.PERSONAL,
                "joao@gymflow.com",
                "24888888888",
                true
        );

        when(organizationRepository.findAll())
                .thenReturn(List.of(organizationA, organizationB));

        when(organizationMapper.toResponse(organizationA))
                .thenReturn(responseA);

        when(organizationMapper.toResponse(organizationB))
                .thenReturn(responseB);

        // Act
        List<OrganizationResponse> response = organizationService.findAll();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(10L, response.get(0).id());
        assertEquals("GymFlow Academy", response.get(0).organizationName());
        assertEquals(OrganizationType.ACADEMY, response.get(0).organizationType());
        assertTrue(response.get(0).active());

        assertEquals(20L, response.get(1).id());
        assertEquals("Personal João", response.get(1).organizationName());
        assertEquals(OrganizationType.PERSONAL, response.get(1).organizationType());
        assertTrue(response.get(1).active());

        verify(organizationRepository).findAll();
        verify(organizationMapper).toResponse(organizationA);
        verify(organizationMapper).toResponse(organizationB);
    }

    @Test
    void shouldFindOrganizationByIdSuccessfully() {
        // Arrange
        Long organizationId = 10L;

        Organization organization = createOrganization(organizationId);

        OrganizationResponse expectedResponse = createOrganizationResponse(
                organizationId,
                "GymFlow Academy",
                OrganizationType.ACADEMY,
                "contato@gymflow.com",
                "24999999999",
                true
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(organizationMapper.toResponse(organization))
                .thenReturn(expectedResponse);

        // Act
        OrganizationResponse response = organizationService.findById(organizationId);

        // Assert
        assertNotNull(response);
        assertEquals(organizationId, response.id());
        assertEquals("GymFlow Academy", response.organizationName());
        assertEquals(OrganizationType.ACADEMY, response.organizationType());
        assertEquals("contato@gymflow.com", response.organizationEmail());
        assertEquals("24999999999", response.organizationPhone());
        assertTrue(response.active());

        verify(organizationRepository).findById(organizationId);
        verify(organizationMapper).toResponse(organization);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenOrganizationDoesNotExistOnFindById() {
        // Arrange
        Long organizationId = 10L;

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.findById(organizationId)
        );

        assertEquals(
                "Organization not found with id: " + organizationId,
                exception.getMessage()
        );

        verify(organizationRepository).findById(organizationId);

        verifyNoInteractions(organizationMapper);
    }

    @Test
    void shouldUpdateOrganizationSuccessfully() {
        // Arrange
        Long organizationId = 10L;

        UpdateOrganizationRequest request = createUpdateOrganizationRequest();

        Organization organization = createOrganization(organizationId);

        Organization updatedOrganization = createOrganization(organizationId);
        updatedOrganization.setOrganizationName("GymFlow Personal");
        updatedOrganization.setOrganizationEmail("personal@gymflow.com");
        updatedOrganization.setOrganizationPhone("24888888888");

        OrganizationResponse expectedResponse = createOrganizationResponse(
                organizationId,
                "GymFlow Personal",
                OrganizationType.ACADEMY,
                "personal@gymflow.com",
                "24888888888",
                true
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(organizationRepository.save(organization))
                .thenReturn(updatedOrganization);

        when(organizationMapper.toResponse(updatedOrganization))
                .thenReturn(expectedResponse);

        // Act
        OrganizationResponse response = organizationService.update(
                organizationId,
                request
        );

        // Assert
        assertNotNull(response);
        assertEquals(organizationId, response.id());
        assertEquals("GymFlow Personal", response.organizationName());
        assertEquals("personal@gymflow.com", response.organizationEmail());
        assertEquals("24888888888", response.organizationPhone());
        assertTrue(response.active());

        verify(organizationRepository).findById(organizationId);
        verify(organizationMapper).updateEntity(organization, request);
        verify(organizationRepository).save(organization);
        verify(organizationMapper).toResponse(updatedOrganization);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenOrganizationDoesNotExistOnUpdate() {
        // Arrange
        Long organizationId = 10L;

        UpdateOrganizationRequest request = createUpdateOrganizationRequest();

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.update(organizationId, request)
        );

        assertEquals(
                "Organization not found with id: " + organizationId,
                exception.getMessage()
        );

        verify(organizationRepository).findById(organizationId);

        verifyNoInteractions(organizationMapper);

        verify(organizationRepository, never())
                .save(any(Organization.class));
    }

    @Test
    void shouldPatchOrganizationSuccessfully() {
        // Arrange
        Long organizationId = 10L;

        UpdateOrganizationRequest request = createUpdateOrganizationRequest();

        Organization organization = createOrganization(organizationId);

        Organization updatedOrganization = createOrganization(organizationId);
        updatedOrganization.setOrganizationName("GymFlow Personal");
        updatedOrganization.setOrganizationEmail("personal@gymflow.com");
        updatedOrganization.setOrganizationPhone("24888888888");

        OrganizationResponse expectedResponse = createOrganizationResponse(
                organizationId,
                "GymFlow Personal",
                OrganizationType.ACADEMY,
                "personal@gymflow.com",
                "24888888888",
                true
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(organizationRepository.save(organization))
                .thenReturn(updatedOrganization);

        when(organizationMapper.toResponse(updatedOrganization))
                .thenReturn(expectedResponse);

        // Act
        OrganizationResponse response = organizationService.patch(
                organizationId,
                request
        );

        // Assert
        assertNotNull(response);
        assertEquals(organizationId, response.id());
        assertEquals("GymFlow Personal", response.organizationName());
        assertEquals("personal@gymflow.com", response.organizationEmail());
        assertEquals("24888888888", response.organizationPhone());
        assertTrue(response.active());

        assertEquals("GymFlow Personal", organization.getOrganizationName());
        assertEquals("personal@gymflow.com", organization.getOrganizationEmail());
        assertEquals("24888888888", organization.getOrganizationPhone());

        verify(organizationRepository).findById(organizationId);
        verify(organizationRepository).save(organization);
        verify(organizationMapper).toResponse(updatedOrganization);

        verify(organizationMapper, never())
                .updateEntity(any(Organization.class), any(UpdateOrganizationRequest.class));
    }

    @Test
    void shouldPatchOnlyOrganizationNameWhenEmailAndPhoneAreNull() {
        // Arrange
        Long organizationId = 10L;

        UpdateOrganizationRequest request = new UpdateOrganizationRequest(
                "GymFlow Personal",
                null,
                null
        );

        Organization organization = createOrganization(organizationId);

        Organization updatedOrganization = createOrganization(organizationId);
        updatedOrganization.setOrganizationName("GymFlow Personal");

        OrganizationResponse expectedResponse = createOrganizationResponse(
                organizationId,
                "GymFlow Personal",
                OrganizationType.ACADEMY,
                "contato@gymflow.com",
                "24999999999",
                true
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(organizationRepository.save(organization))
                .thenReturn(updatedOrganization);

        when(organizationMapper.toResponse(updatedOrganization))
                .thenReturn(expectedResponse);

        // Act
        OrganizationResponse response = organizationService.patch(
                organizationId,
                request
        );

        // Assert
        assertNotNull(response);
        assertEquals(organizationId, response.id());
        assertEquals("GymFlow Personal", response.organizationName());
        assertEquals("contato@gymflow.com", response.organizationEmail());
        assertEquals("24999999999", response.organizationPhone());
        assertTrue(response.active());

        assertEquals("GymFlow Personal", organization.getOrganizationName());
        assertEquals("contato@gymflow.com", organization.getOrganizationEmail());
        assertEquals("24999999999", organization.getOrganizationPhone());

        verify(organizationRepository).findById(organizationId);
        verify(organizationRepository).save(organization);
        verify(organizationMapper).toResponse(updatedOrganization);

        verify(organizationMapper, never())
                .updateEntity(any(Organization.class), any(UpdateOrganizationRequest.class));
    }

    @Test
    void shouldPatchOnlyOrganizationEmailWhenNameAndPhoneAreNull() {
        // Arrange
        Long organizationId = 10L;

        UpdateOrganizationRequest request = new UpdateOrganizationRequest(
                null,
                "novo-email@gymflow.com",
                null
        );

        Organization organization = createOrganization(organizationId);

        Organization updatedOrganization = createOrganization(organizationId);
        updatedOrganization.setOrganizationEmail("novo-email@gymflow.com");

        OrganizationResponse expectedResponse = createOrganizationResponse(
                organizationId,
                "GymFlow Academy",
                OrganizationType.ACADEMY,
                "novo-email@gymflow.com",
                "24999999999",
                true
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(organizationRepository.save(organization))
                .thenReturn(updatedOrganization);

        when(organizationMapper.toResponse(updatedOrganization))
                .thenReturn(expectedResponse);

        // Act
        OrganizationResponse response = organizationService.patch(
                organizationId,
                request
        );

        // Assert
        assertNotNull(response);
        assertEquals(organizationId, response.id());
        assertEquals("GymFlow Academy", response.organizationName());
        assertEquals("novo-email@gymflow.com", response.organizationEmail());
        assertEquals("24999999999", response.organizationPhone());
        assertTrue(response.active());

        assertEquals("GymFlow Academy", organization.getOrganizationName());
        assertEquals("novo-email@gymflow.com", organization.getOrganizationEmail());
        assertEquals("24999999999", organization.getOrganizationPhone());

        verify(organizationRepository).findById(organizationId);
        verify(organizationRepository).save(organization);
        verify(organizationMapper).toResponse(updatedOrganization);

        verify(organizationMapper, never())
                .updateEntity(any(Organization.class), any(UpdateOrganizationRequest.class));
    }

    @Test
    void shouldPatchOnlyOrganizationPhoneWhenNameAndEmailAreNull() {
        // Arrange
        Long organizationId = 10L;

        UpdateOrganizationRequest request = new UpdateOrganizationRequest(
                null,
                null,
                "24777777777"
        );

        Organization organization = createOrganization(organizationId);

        Organization updatedOrganization = createOrganization(organizationId);
        updatedOrganization.setOrganizationPhone("24777777777");

        OrganizationResponse expectedResponse = createOrganizationResponse(
                organizationId,
                "GymFlow Academy",
                OrganizationType.ACADEMY,
                "contato@gymflow.com",
                "24777777777",
                true
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(organizationRepository.save(organization))
                .thenReturn(updatedOrganization);

        when(organizationMapper.toResponse(updatedOrganization))
                .thenReturn(expectedResponse);

        // Act
        OrganizationResponse response = organizationService.patch(
                organizationId,
                request
        );

        // Assert
        assertNotNull(response);
        assertEquals(organizationId, response.id());
        assertEquals("GymFlow Academy", response.organizationName());
        assertEquals("contato@gymflow.com", response.organizationEmail());
        assertEquals("24777777777", response.organizationPhone());
        assertTrue(response.active());

        assertEquals("GymFlow Academy", organization.getOrganizationName());
        assertEquals("contato@gymflow.com", organization.getOrganizationEmail());
        assertEquals("24777777777", organization.getOrganizationPhone());

        verify(organizationRepository).findById(organizationId);
        verify(organizationRepository).save(organization);
        verify(organizationMapper).toResponse(updatedOrganization);

        verify(organizationMapper, never())
                .updateEntity(any(Organization.class), any(UpdateOrganizationRequest.class));
    }

    @Test
    void shouldDeleteOrganizationSuccessfully() {
        // Arrange
        Long organizationId = 10L;

        Organization organization = createOrganization(organizationId);

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        // Act
        organizationService.delete(organizationId);

        // Assert
        assertFalse(organization.getActive());

        verify(organizationRepository).findById(organizationId);
        verify(organizationRepository).save(organization);

        verifyNoInteractions(organizationMapper);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenOrganizationDoesNotExistOnDelete() {
        // Arrange
        Long organizationId = 10L;

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.delete(organizationId)
        );

        assertEquals(
                "Organization not found with id: " + organizationId,
                exception.getMessage()
        );

        verify(organizationRepository).findById(organizationId);

        verifyNoInteractions(organizationMapper);

        verify(organizationRepository, never())
                .save(any(Organization.class));
    }


    private Organization createOrganization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        organization.setOrganizationName("GymFlow Academy");
        organization.setOrganizationType(OrganizationType.ACADEMY);
        organization.setOrganizationEmail("contato@gymflow.com");
        organization.setOrganizationPhone("24999999999");
        organization.setActive(true);
        return organization;
    }

    private CreateOrganizationRequest createOrganizationRequest() {
        return new CreateOrganizationRequest(
                "GymFlow Academy",
                OrganizationType.ACADEMY,
                "contato@gymflow.com",
                "24999999999"
        );
    }

    private UpdateOrganizationRequest createUpdateOrganizationRequest() {
        return new UpdateOrganizationRequest(
                "GymFlow Personal",
                "personal@gymflow.com",
                "24888888888"
        );
    }

    private OrganizationResponse createOrganizationResponse(
            Long id,
            String organizationName,
            OrganizationType organizationType,
            String organizationEmail,
            String organizationPhone,
            Boolean active
    ) {
        return new OrganizationResponse(
                id,
                organizationName,
                organizationType,
                organizationEmail,
                organizationPhone,
                active,
                null,
                null
        );
    }
}
