package br.com.gymflow.api.auth;

import br.com.gymflow.api.auth.dto.*;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.enums.OrganizationType;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.exception.BusinessRuleException;
import br.com.gymflow.api.exception.DuplicateResourceException;
import br.com.gymflow.api.repository.OrganizationRepository;
import br.com.gymflow.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.com.gymflow.api.domain.Organization;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterOrganizationAndAdminSuccessfully() {
        RegisterOrganizationRequest request = new RegisterOrganizationRequest(
                "GymFlow Academy",
                OrganizationType.ACADEMY,
                "contato@gymflowacademy.com",
                "11999999999",
                "Samuel Gomes",
                "samuel@gymflowacademy.com",
                "123456"
        );

        when(organizationRepository.existsByOrganizationEmail(request.organizationEmail()))
                .thenReturn(false);
        when(userRepository.existsByEmail(request.adminEmail()))
                .thenReturn(false);
        when(passwordEncoder.encode(request.password()))
                .thenReturn("encoded-password");

        when(organizationRepository.save(any(Organization.class)))
                .thenAnswer(invocation -> {
                    Organization organization = invocation.getArgument(0);
                    organization.setId(10L);
                    return organization;
                });

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(20L);
                    return user;
                });

        RegisterOrganizationResponse response = authService.registerOrganization(request);

        assertNotNull(response);
        assertEquals(10L, response.organizationId());
        assertEquals("GymFlow Academy", response.organizationName());
        assertEquals(OrganizationType.ACADEMY, response.organizationType());
        assertEquals(20L, response.adminUserId());
        assertEquals("Samuel Gomes", response.adminName());
        assertEquals("samuel@gymflowacademy.com", response.adminEmail());

        verify(organizationRepository).existsByOrganizationEmail(request.organizationEmail());
        verify(userRepository).existsByEmail(request.adminEmail());
        verify(passwordEncoder).encode(request.password());
        verify(organizationRepository).save(any(Organization.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowDuplicateResourceExceptionWhenOrganizationEmailAlreadyExists() {
        RegisterOrganizationRequest request = new RegisterOrganizationRequest(
                "GymFlow Academy",
                OrganizationType.ACADEMY,
                "contato@gymflowacademy.com",
                "11999999999",
                "Samuel Gomes",
                "samuel@gymflowacademy.com",
                "123456"
        );

        when(organizationRepository.existsByOrganizationEmail(request.organizationEmail()))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () ->
                authService.registerOrganization(request)
        );

        assertEquals("Organization email already in use", exception.getMessage());

        verify(organizationRepository).existsByOrganizationEmail(request.organizationEmail());
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(organizationRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowDuplicateResourceExceptionWhenAdminEmailAlreadyExists() {
        RegisterOrganizationRequest request = new RegisterOrganizationRequest(
                "GymFlow Academy",
                OrganizationType.ACADEMY,
                "contato@gymflowacademy.com",
                "11999999999",
                "Samuel Gomes",
                "samuel@gymflowacademy.com",
                "123456"
        );

        when(organizationRepository.existsByOrganizationEmail(request.organizationEmail()))
                .thenReturn(false);
        when(userRepository.existsByEmail(request.adminEmail()))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () ->
                authService.registerOrganization(request)
        );

        assertEquals("User email already in use", exception.getMessage());

        verify(organizationRepository).existsByOrganizationEmail(request.organizationEmail());
        verify(userRepository).existsByEmail(request.adminEmail());
        verifyNoInteractions(passwordEncoder);
        verify(organizationRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfullyWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("teacher.dev@gymflow.com", "123456");

        Organization organization = new Organization();
        organization.setId(100L);
        organization.setOrganizationName("GymFlow Academy Dev");

        User user = new User();
        user.setId(1L);
        user.setName("Professor Dev");
        user.setEmail("teacher.dev@gymflow.com");
        user.setPasswordHash("$2a$10$hash");
        user.setRole(UserRole.TEACHER);
        user.setActive(true);
        user.setOrganization(organization);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResult result = authService.login(request);

        assertNotNull(result);
        assertEquals("jwt-token", result.token());

        AuthenticatedUserResponse response = result.user();

        assertNotNull(response);
        assertEquals(1L, response.userId());
        assertEquals(100L, response.organizationId());
        assertEquals("Professor Dev", response.name());
        assertEquals("teacher.dev@gymflow.com", response.email());
        assertEquals(UserRole.TEACHER, response.role());

        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder).matches(request.password(), user.getPasswordHash());
        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenEmailDoesNotExist() {
        LoginRequest request = new LoginRequest("notfound@gymflow.com", "123456");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                authService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());

        verify(userRepository).findByEmail(request.email());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest("teacher.dev@gymflow.com", "wrong-password");

        User user = new User();
        user.setId(1L);
        user.setName("Professor Dev");
        user.setEmail("teacher.dev@gymflow.com");
        user.setPasswordHash("$2a$10$hash");
        user.setRole(UserRole.TEACHER);
        user.setActive(true);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                authService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());

        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder).matches(request.password(), user.getPasswordHash());
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenUserIsInactive() {
        LoginRequest request = new LoginRequest("teacher.dev@gymflow.com", "123456");

        User user = new User();
        user.setId(1L);
        user.setName("Professor Dev");
        user.setEmail("teacher.dev@gymflow.com");
        user.setPasswordHash("$2a$10$hash");
        user.setRole(UserRole.TEACHER);
        user.setActive(false);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                authService.login(request)
        );

        assertEquals("User is inactive", exception.getMessage());

        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder).matches(request.password(), user.getPasswordHash());
        verifyNoInteractions(jwtService);
    }
}