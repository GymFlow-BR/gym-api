package br.com.gymflow.api.service;

import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.dto.user.CreateUserRequest;
import br.com.gymflow.api.dto.user.UserResponse;
import br.com.gymflow.api.exception.BusinessRuleException;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.mapper.UserMapper;
import br.com.gymflow.api.repository.OrganizationRepository;
import br.com.gymflow.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import br.com.gymflow.api.dto.user.UpdateUserRequest;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        User authenticatedUser = getAuthenticatedUser();

        validateSameOrganization(request.organizationId());
        validateCreatePermission(authenticatedUser, request);

        Organization organization = getOrganizationById(request.organizationId());

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("Email already in use");
        }

        User user = userMapper.toEntity(request);
        user.setOrganization(organization);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        Long organizationId = getAuthenticatedUserOrganizationId();

        return userRepository.findByOrganizationId(organizationId)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAllByOrganizationId(Long organizationId) {
        getOrganizationById(organizationId);
        validateSameOrganization(organizationId);

        return userRepository.findByOrganizationId(organizationId)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAllByOrganizationIdAndRole(Long organizationId, UserRole role) {
        User authenticatedUser = getAuthenticatedUser();

        getOrganizationById(organizationId);
        validateSameOrganization(organizationId);
        validateFindByRolePermission(authenticatedUser, role);

        return userRepository.findByOrganizationIdAndRole(organizationId, role)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = getUserById(id);

        validateUserBelongsToAuthenticatedOrganization(user);

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse patch(Long id, UpdateUserRequest request) {
        User authenticatedUser = getAuthenticatedUser();
        User user = getUserById(id);

        validateUserBelongsToAuthenticatedOrganization(user);
        validatePatchPermission(authenticatedUser, user, request);

        if (request.email() != null &&
                userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new BusinessRuleException("Email already in use");
        }

        userMapper.updateEntity(user, request);

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public void delete(Long id) {
        User user = getUserById(id);

        validateUserBelongsToAuthenticatedOrganization(user);

        user.setActive(false);

        userRepository.save(user);
    }


    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private Organization getOrganizationById(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Access denied");
        }

        return user;
    }

    private Long getAuthenticatedUserOrganizationId() {
        return getAuthenticatedUser().getOrganization().getId();
    }

    private void validateSameOrganization(Long organizationId) {
        Long authenticatedUserOrganizationId = getAuthenticatedUserOrganizationId();

        if (!authenticatedUserOrganizationId.equals(organizationId)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private void validateUserBelongsToAuthenticatedOrganization(User user) {
        validateSameOrganization(user.getOrganization().getId());
    }

    private void validateCreatePermission(User authenticatedUser, CreateUserRequest request) {
        if (authenticatedUser.getRole() == UserRole.ADMIN) {
            return;
        }

        if (authenticatedUser.getRole() == UserRole.TEACHER &&
                request.role() == UserRole.STUDENT) {
            return;
        }

        throw new AccessDeniedException("Access denied");
    }

    private void validatePatchPermission(User authenticatedUser, User targetUser, UpdateUserRequest request) {
        if (authenticatedUser.getRole() == UserRole.ADMIN) {
            return;
        }

        if (authenticatedUser.getRole() == UserRole.TEACHER &&
                targetUser.getRole() == UserRole.STUDENT &&
                request.active() == null) {
            return;
        }

        throw new AccessDeniedException("Access denied");
    }

    private void validateFindByRolePermission(User authenticatedUser, UserRole role) {
        if (authenticatedUser.getRole() == UserRole.ADMIN) {
            return;
        }

        if (authenticatedUser.getRole() == UserRole.TEACHER && role == UserRole.STUDENT) {
            return;
        }

        throw new AccessDeniedException("Access denied");
    }
}