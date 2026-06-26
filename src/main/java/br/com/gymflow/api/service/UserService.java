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
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAllByOrganizationId(Long organizationId) {
        getOrganizationById(organizationId);

        return userRepository.findByOrganizationId(organizationId)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAllByOrganizationIdAndRole(Long organizationId, UserRole role) {
        getOrganizationById(organizationId);

        return userRepository.findByOrganizationIdAndRole(organizationId, role)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = getUserById(id);

        return userMapper.toResponse(user);
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private Organization getOrganizationById(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));
    }
}