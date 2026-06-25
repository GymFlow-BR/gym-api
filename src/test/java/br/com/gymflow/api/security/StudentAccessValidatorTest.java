package br.com.gymflow.api.security;

import br.com.gymflow.api.config.security.StudentAccessValidator;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudentAccessValidatorTest {

    private final StudentAccessValidator studentAccessValidator = new StudentAccessValidator();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAllowAdminToAccessAnyStudentData() {
        User admin = createUser(1L, UserRole.ADMIN);
        authenticate(admin);

        assertDoesNotThrow(() ->
                studentAccessValidator.validateStudentAccess(99L)
        );
    }

    @Test
    void shouldAllowTeacherToAccessAnyStudentData() {
        User teacher = createUser(1L, UserRole.TEACHER);
        authenticate(teacher);

        assertDoesNotThrow(() ->
                studentAccessValidator.validateStudentAccess(99L)
        );
    }

    @Test
    void shouldAllowStudentToAccessOwnData() {
        User student = createUser(2L, UserRole.STUDENT);
        authenticate(student);

        assertDoesNotThrow(() ->
                studentAccessValidator.validateStudentAccess(2L)
        );
    }

    @Test
    void shouldBlockStudentFromAccessingOtherStudentData() {
        User student = createUser(2L, UserRole.STUDENT);
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

    private User createUser(Long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail("user" + id + "@gymflow.com");
        user.setPasswordHash("$2a$10$hash");
        user.setRole(role);
        user.setActive(true);
        return user;
    }
}