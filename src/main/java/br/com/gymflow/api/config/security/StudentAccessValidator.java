package br.com.gymflow.api.config.security;

import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentAccessValidator {

    private final UserRepository userRepository;

    public void validateStudentAccess(Long studentId) {
        User authenticatedUser = getAuthenticatedUser();

        if (authenticatedUser.getRole() == UserRole.ADMIN ) {
            return;
        }

        if (authenticatedUser.getRole() == UserRole.STUDENT &&
                authenticatedUser.getId().equals(studentId)) {
            return;
        }

        if (authenticatedUser.getRole() == UserRole.TEACHER) {
            validateTeacherAccess(authenticatedUser, studentId);
            return;
        }

        throw new AccessDeniedException("Access denied");
    }

    private void validateTeacherAccess(User teacher, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new AccessDeniedException("Access denied"));

        if (student.getRole() != UserRole.STUDENT) {
            throw new AccessDeniedException("Access denied");
        }

        Long teacherOrganizationId = teacher.getOrganization().getId();
        Long studentOrganizationId = student.getOrganization().getId();

        if (!teacherOrganizationId.equals(studentOrganizationId)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof  User user)) {
            throw new AccessDeniedException("Access denied");
        }

        return user;
    }
}