package br.com.gymflow.api.config.security;

import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.enums.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class StudentAccessValidator {

    public void validateStudentAccess(Long studentId) {
        User authenticatedUser = getAuthenticatedUser();

        if (authenticatedUser.getRole() == UserRole.ADMIN ||
                authenticatedUser.getRole() == UserRole.TEACHER) {
            return;
        }

        if (authenticatedUser.getRole() == UserRole.STUDENT &&
                authenticatedUser.getId().equals(studentId)) {
            return;
        }

        throw new AccessDeniedException("Access denied");
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