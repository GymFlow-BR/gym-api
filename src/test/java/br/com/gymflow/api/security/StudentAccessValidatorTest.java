package br.com.gymflow.api.security;

import br.com.gymflow.api.config.security.StudentAccessValidator;
import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentAccessValidatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StudentAccessValidator studentAccessValidator;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldBlockAdminFromAccessingStudentFromAnotherOrganization() {
        // Arrange
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        User student = createUser(2L, UserRole.STUDENT, 2L);

        authenticate(admin);

        when(userRepository.findById(student.getId()))
                .thenReturn(Optional.of(student));

        // Act + Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> studentAccessValidator.validateStudentAccess(student.getId())
        );

        assertEquals("Access denied", exception.getMessage());

        verify(userRepository).findById(student.getId());
    }

    @Test
    void shouldAllowAdminToAccessStudentFromSameOrganization() {
        // Arrange
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        User student = createUser(2L, UserRole.STUDENT, 1L);

        authenticate(admin);

        when(userRepository.findById(student.getId()))
                .thenReturn(Optional.of(student));

        // Act + Assert
        assertDoesNotThrow(() ->
                studentAccessValidator.validateStudentAccess(student.getId())
        );

        verify(userRepository).findById(student.getId());
    }

    @Test
    void shouldAllowTeacherToAccessStudentFromSameOrganization() {
        User teacher = createUser(1L, UserRole.TEACHER, 1L);
        User student = createUser(2L, UserRole.STUDENT, 1L);

        authenticate(teacher);

        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        assertDoesNotThrow(() ->
                studentAccessValidator.validateStudentAccess(student.getId())
        );
    }

    @Test
    void shouldBlockTeacherFromAccessingStudentFromAnotherOrganization() {
        User teacher = createUser(1L, UserRole.TEACHER, 1L);
        User student = createUser(2L, UserRole.STUDENT, 2L);

        authenticate(teacher);

        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        assertThrows(AccessDeniedException.class, () ->
                studentAccessValidator.validateStudentAccess(student.getId())
        );
    }

    @Test
    void shouldAllowStudentToAccessOwnData() {
        User student = createUser(2L, UserRole.STUDENT, 1L);
        authenticate(student);

        assertDoesNotThrow(() ->
                studentAccessValidator.validateStudentAccess(2L)
        );
    }

    @Test
    void shouldBlockStudentFromAccessingOtherStudentData() {
        User student = createUser(2L, UserRole.STUDENT, 1L);
        authenticate(student);

        assertThrows(AccessDeniedException.class, () ->
                studentAccessValidator.validateStudentAccess(99L)
        );
    }

    @Test
    void shouldBlockAccessWhenAuthenticationIsMissing() {
        assertThrows(AccessDeniedException.class, () ->
                studentAccessValidator.validateStudentAccess(1L)
        );
    }

    private void authenticate(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private User createUser(Long id, UserRole role, Long organizationId) {
        Organization organization = new Organization();
        organization.setId(organizationId);

        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail("user" + id + "@gymflow.com");
        user.setPasswordHash("$2a$10$hash");
        user.setRole(role);
        user.setActive(true);
        user.setOrganization(organization);

        return user;
    }
}