package br.com.gymflow.api.auth;

import br.com.gymflow.api.auth.dto.LoginRequest;
import br.com.gymflow.api.auth.dto.LoginResponse;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.exception.BusinessRuleException;
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

    @InjectMocks
    private AuthService authService;

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

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
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