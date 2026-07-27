package br.com.gymflow.api.auth;

import br.com.gymflow.api.auth.dto.AuthenticatedUserResponse;
import br.com.gymflow.api.auth.dto.LoginRequest;
import br.com.gymflow.api.auth.dto.LoginResult;
import br.com.gymflow.api.auth.dto.RegisterOrganizationRequest;
import br.com.gymflow.api.auth.dto.RegisterOrganizationResponse;
import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.enums.OrganizationType;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.exception.BusinessRuleException;
import br.com.gymflow.api.exception.DuplicateResourceException;
import br.com.gymflow.api.repository.OrganizationRepository;
import br.com.gymflow.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

        when(organizationRepository.existsByOrganizationEmail("contato@gymflowacademy.com"))
                .thenReturn(false);
        when(userRepository.existsByEmail("samuel@gymflowacademy.com"))
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

        verify(organizationRepository).existsByOrganizationEmail("contato@gymflowacademy.com");
        verify(userRepository).existsByEmail("samuel@gymflowacademy.com");
        verify(passwordEncoder).encode(request.password());
        verify(organizationRepository).save(any(Organization.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldSaveAdminUserWithCorrectInternalDataWhenRegisteringOrganization() {
        RegisterOrganizationRequest request = new RegisterOrganizationRequest(
                " GymFlow Academy ",
                OrganizationType.ACADEMY,
                " CONTATO@GYMFLOWACADEMY.COM ",
                "11999999999",
                " Samuel Gomes ",
                " SAMUEL@GYMFLOWACADEMY.COM ",
                "123456"
        );

        when(organizationRepository.existsByOrganizationEmail("contato@gymflowacademy.com"))
                .thenReturn(false);
        when(userRepository.existsByEmail("samuel@gymflowacademy.com"))
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

        authService.registerOrganization(request);

        verify(organizationRepository).save(argThat(organization ->
                organization.getOrganizationName().equals("GymFlow Academy") &&
                        organization.getOrganizationType() == OrganizationType.ACADEMY &&
                        organization.getOrganizationEmail().equals("contato@gymflowacademy.com") &&
                        organization.getOrganizationPhone().equals("11999999999") &&
                        Boolean.TRUE.equals(organization.getActive())
        ));

        verify(userRepository).save(argThat(user ->
                user.getOrganization().getId().equals(10L) &&
                        user.getName().equals("Samuel Gomes") &&
                        user.getEmail().equals("samuel@gymflowacademy.com") &&
                        user.getPasswordHash().equals("encoded-password") &&
                        user.getRole() == UserRole.ADMIN &&
                        Boolean.TRUE.equals(user.getActive())
        ));
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

        when(organizationRepository.existsByOrganizationEmail("contato@gymflowacademy.com"))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () ->
                authService.registerOrganization(request)
        );

        assertEquals("Organization email already in use", exception.getMessage());

        verify(organizationRepository).existsByOrganizationEmail("contato@gymflowacademy.com");
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

        when(organizationRepository.existsByOrganizationEmail("contato@gymflowacademy.com"))
                .thenReturn(false);
        when(userRepository.existsByEmail("samuel@gymflowacademy.com"))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () ->
                authService.registerOrganization(request)
        );

        assertEquals("User email already in use", exception.getMessage());

        verify(organizationRepository).existsByOrganizationEmail("contato@gymflowacademy.com");
        verify(userRepository).existsByEmail("samuel@gymflowacademy.com");
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

        when(userRepository.findByEmail("teacher.dev@gymflow.com")).thenReturn(Optional.of(user));
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

        verify(userRepository).findByEmail("teacher.dev@gymflow.com");
        verify(passwordEncoder).matches(request.password(), user.getPasswordHash());
        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldLoginSuccessfullyWhenEmailHasUppercaseOrSpaces() {
        LoginRequest request = new LoginRequest(" TEACHER.DEV@GYMFLOW.COM ", "123456");

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

        when(userRepository.findByEmail("teacher.dev@gymflow.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResult result = authService.login(request);

        assertNotNull(result);
        assertEquals("jwt-token", result.token());
        assertEquals("teacher.dev@gymflow.com", result.user().email());

        verify(userRepository).findByEmail("teacher.dev@gymflow.com");
        verify(passwordEncoder).matches(request.password(), user.getPasswordHash());
        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenEmailDoesNotExist() {
        LoginRequest request = new LoginRequest("notfound@gymflow.com", "123456");

        when(userRepository.findByEmail("notfound@gymflow.com")).thenReturn(Optional.empty());

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                authService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());

        verify(userRepository).findByEmail("notfound@gymflow.com");
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

        when(userRepository.findByEmail("teacher.dev@gymflow.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                authService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());

        verify(userRepository).findByEmail("teacher.dev@gymflow.com");
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

        when(userRepository.findByEmail("teacher.dev@gymflow.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                authService.login(request)
        );

        assertEquals("User is inactive", exception.getMessage());

        verify(userRepository).findByEmail("teacher.dev@gymflow.com");
        verify(passwordEncoder).matches(request.password(), user.getPasswordHash());
        verifyNoInteractions(jwtService);
    }
}