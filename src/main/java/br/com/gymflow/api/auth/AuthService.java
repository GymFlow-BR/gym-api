package br.com.gymflow.api.auth;

import br.com.gymflow.api.auth.dto.AuthenticatedUserResponse;
import br.com.gymflow.api.auth.dto.LoginRequest;
import br.com.gymflow.api.auth.dto.LoginResult;
import br.com.gymflow.api.auth.dto.RegisterOrganizationRequest;
import br.com.gymflow.api.auth.dto.RegisterOrganizationResponse;
import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.exception.BusinessRuleException;
import br.com.gymflow.api.exception.DuplicateResourceException;
import br.com.gymflow.api.repository.OrganizationRepository;
import br.com.gymflow.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegisterOrganizationResponse registerOrganization(RegisterOrganizationRequest request) {
        String organizationEmail = normalizeEmail(request.organizationEmail());
        String adminEmail = normalizeEmail(request.adminEmail());

        validateOrganizationEmailIsAvailable(organizationEmail);
        validateAdminEmailIsAvailable(adminEmail);

        Organization organization = new Organization();
        organization.setOrganizationName(request.organizationName().trim());
        organization.setOrganizationType(request.organizationType());
        organization.setOrganizationEmail(organizationEmail);
        organization.setOrganizationPhone(normalizeOptionalValue(request.organizationPhone()));
        organization.setActive(true);

        Organization savedOrganization = organizationRepository.save(organization);

        User adminUser = new User();
        adminUser.setOrganization(savedOrganization);
        adminUser.setName(request.adminName().trim());
        adminUser.setEmail(adminEmail);
        adminUser.setPasswordHash(passwordEncoder.encode(request.password()));
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setActive(true);

        User savedAdminUser = userRepository.save(adminUser);

        return new RegisterOrganizationResponse(
                savedOrganization.getId(),
                savedOrganization.getOrganizationName(),
                savedOrganization.getOrganizationType(),
                savedAdminUser.getId(),
                savedAdminUser.getName(),
                savedAdminUser.getEmail()
        );
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessRuleException("Invalid email or password");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BusinessRuleException("User is inactive");
        }

        String token = jwtService.generateToken(user);

        AuthenticatedUserResponse response = new AuthenticatedUserResponse(
                user.getId(),
                user.getOrganization().getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        return new LoginResult(token, response);
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new BusinessRuleException("User is not authenticated");
        }

        return new AuthenticatedUserResponse(
                user.getId(),
                user.getOrganization().getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    private void validateOrganizationEmailIsAvailable(String organizationEmail) {
        if (organizationRepository.existsByOrganizationEmail(organizationEmail)) {
            throw new DuplicateResourceException("Organization email already in use");
        }
    }

    private void validateAdminEmailIsAvailable(String adminEmail) {
        if (userRepository.existsByEmail(adminEmail)) {
            throw new DuplicateResourceException("User email already in use");
        }
    }

    private String normalizeOptionalValue(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}