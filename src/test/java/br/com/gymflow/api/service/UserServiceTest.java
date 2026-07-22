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
import org.springframework.security.crypto.password.PasswordEncoder;
import br.com.gymflow.api.dto.user.UpdateUserRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateUserWhenAdminCreatesUserInOwnOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Organization organization = createOrganization(1L);

        CreateUserRequest request = new CreateUserRequest(
                1L,
                "Aluno Teste",
                "student.test@gymflow.com",
                "123456",
                UserRole.STUDENT
        );

        User userToSave = createUser(null, UserRole.STUDENT, 1L);
        User savedUser = createUser(2L, UserRole.STUDENT, 1L);

        UserResponse expectedResponse = new UserResponse(
                2L,
                1L,
                "GymFlow Academy Dev",
                "Aluno Teste",
                "student.test@gymflow.com",
                UserRole.STUDENT,
                true,
                null
        );

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(userToSave);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(userToSave)).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        UserResponse response = userService.create(request);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals(1L, response.organizationId());
        assertEquals("student.test@gymflow.com", response.email());
        assertEquals(UserRole.STUDENT, response.role());

        verify(organizationRepository).findById(1L);
        verify(userRepository).existsByEmail(request.email());
        verify(userMapper).toEntity(request);
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(userToSave);
        verify(userMapper).toResponse(savedUser);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenAdminCreatesUserInAnotherOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        CreateUserRequest request = new CreateUserRequest(
                2L,
                "Aluno Outra Org",
                "student.other@gymflow.com",
                "123456",
                UserRole.STUDENT
        );

        assertThrows(AccessDeniedException.class, () ->
                userService.create(request)
        );

        verifyNoInteractions(organizationRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userMapper);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldCreateStudentWhenTeacherCreatesStudentInOwnOrganization() {
        User teacher = createUser(1L, UserRole.TEACHER, 1L);
        authenticate(teacher);

        Organization organization = createOrganization(1L);

        CreateUserRequest request = new CreateUserRequest(
                1L,
                "Aluno Criado pelo Professor",
                "student.teacher.created@gymflow.com",
                "123456",
                UserRole.STUDENT
        );

        User userToSave = createUser(null, UserRole.STUDENT, 1L);
        User savedUser = createUser(2L, UserRole.STUDENT, 1L);

        UserResponse expectedResponse = new UserResponse(
                2L,
                1L,
                "GymFlow Academy Dev",
                "Aluno Criado pelo Professor",
                "student.teacher.created@gymflow.com",
                UserRole.STUDENT,
                true,
                null
        );


        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(userToSave);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(userToSave)).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        UserResponse response = userService.create(request);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals(1L, response.organizationId());
        assertEquals("student.teacher.created@gymflow.com", response.email());
        assertEquals(UserRole.STUDENT, response.role());

        verify(organizationRepository).findById(1L);
        verify(userRepository).existsByEmail(request.email());
        verify(userMapper).toEntity(request);
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(userToSave);
        verify(userMapper).toResponse(savedUser);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenTeacherCreatesAdmin() {
        User teacher = createUser(1L, UserRole.TEACHER, 1L);
        authenticate(teacher);

        CreateUserRequest request = new CreateUserRequest(
                1L,
                "Admin Indevido",
                "admin.invalid@gymflow.com",
                "123456",
                UserRole.ADMIN
        );

        assertThrows(AccessDeniedException.class, () ->
                userService.create(request)
        );

        verifyNoInteractions(organizationRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userMapper);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenTeacherCreatesTeacher() {
        User teacher = createUser(1L, UserRole.TEACHER, 1L);
        authenticate(teacher);

        CreateUserRequest request = new CreateUserRequest(
                1L,
                "Professor Indevido",
                "teacher.invalid@gymflow.com",
                "123456",
                UserRole.TEACHER
        );

        assertThrows(AccessDeniedException.class, () ->
                userService.create(request)
        );

        verifyNoInteractions(organizationRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userMapper);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenTeacherCreatesStudentInAnotherOrganization() {
        User teacher = createUser(1L, UserRole.TEACHER, 1L);
        authenticate(teacher);

        CreateUserRequest request = new CreateUserRequest(
                2L,
                "Aluno Outra Org",
                "student.other.org@gymflow.com",
                "123456",
                UserRole.STUDENT
        );

        assertThrows(AccessDeniedException.class, () ->
                userService.create(request)
        );

        verifyNoInteractions(organizationRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userMapper);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenEmailAlreadyExistsOnCreate() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Organization organization = createOrganization(1L);

        CreateUserRequest request = new CreateUserRequest(
                1L,
                "Aluno Teste",
                "student.test@gymflow.com",
                "123456",
                UserRole.STUDENT
        );

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                userService.create(request)
        );

        assertEquals("Email already in use", exception.getMessage());

        verify(organizationRepository).findById(1L);
        verify(userRepository).existsByEmail(request.email());
        verifyNoInteractions(userMapper);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenOrganizationDoesNotExistOnCreate() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        CreateUserRequest request = new CreateUserRequest(
                1L,
                "Aluno Teste",
                "student.test@gymflow.com",
                "123456",
                UserRole.STUDENT
        );

        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                userService.create(request)
        );

        assertEquals("Organization not found with id: 1", exception.getMessage());

        verify(organizationRepository).findById(1L);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userMapper);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldFindAllUsersFromAuthenticatedUserOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        User teacher = createUser(2L, UserRole.TEACHER, 1L);
        User student = createUser(3L, UserRole.STUDENT, 1L);

        UserResponse teacherResponse = new UserResponse(
                2L,
                1L,
                "GymFlow Academy Dev",
                "Test User",
                "user2@gymflow.com",
                UserRole.TEACHER,
                true,
                null
        );

        UserResponse studentResponse = new UserResponse(
                3L,
                1L,
                "GymFlow Academy Dev",
                "Test User",
                "user3@gymflow.com",
                UserRole.STUDENT,
                true,
                null
        );

        when(userRepository.findByOrganizationId(1L)).thenReturn(List.of(teacher, student));
        when(userMapper.toResponse(teacher)).thenReturn(teacherResponse);
        when(userMapper.toResponse(student)).thenReturn(studentResponse);

        List<UserResponse> response = userService.findAll();

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(1L, response.get(0).organizationId());
        assertEquals(1L, response.get(1).organizationId());

        verify(userRepository).findByOrganizationId(1L);
        verify(userRepository, never()).findAll();
        verify(userMapper).toResponse(teacher);
        verify(userMapper).toResponse(student);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenFindByIdUserFromAnotherOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        User userFromAnotherOrganization = createUser(2L, UserRole.STUDENT, 2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(userFromAnotherOrganization));

        assertThrows(AccessDeniedException.class, () ->
                userService.findById(2L)
        );

        verify(userRepository).findById(2L);
        verifyNoInteractions(userMapper);
    }

    @Test
    void shouldFindByIdWhenUserBelongsToAuthenticatedUserOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        User targetUser = createUser(2L, UserRole.STUDENT, 1L);

        UserResponse expectedResponse = new UserResponse(
                2L,
                1L,
                "GymFlow Academy Dev",
                "Test User",
                "user2@gymflow.com",
                UserRole.STUDENT,
                true,
                null
        );

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userMapper.toResponse(targetUser)).thenReturn(expectedResponse);

        UserResponse response = userService.findById(2L);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals(1L, response.organizationId());
        assertEquals(UserRole.STUDENT, response.role());

        verify(userRepository).findById(2L);
        verify(userMapper).toResponse(targetUser);
    }

    @Test
    void shouldPatchUserWhenUserBelongsToAuthenticatedUserOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        User targetUser = createUser(2L, UserRole.STUDENT, 1L);

        UpdateUserRequest request = new UpdateUserRequest(
                "Aluno Atualizado",
                "student.updated@gymflow.com",
                false
        );

        UserResponse expectedResponse = new UserResponse(
                2L,
                1L,
                "GymFlow Academy Dev",
                "Aluno Atualizado",
                "student.updated@gymflow.com",
                UserRole.STUDENT,
                false,
                null
        );

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.existsByEmailAndIdNot(request.email(), 2L)).thenReturn(false);
        when(userRepository.save(targetUser)).thenReturn(targetUser);
        when(userMapper.toResponse(targetUser)).thenReturn(expectedResponse);

        UserResponse response = userService.patch(2L, request);

        assertNotNull(response);
        assertEquals("Aluno Atualizado", response.name());
        assertEquals("student.updated@gymflow.com", response.email());
        assertEquals(UserRole.STUDENT, response.role());
        assertFalse(response.active());

        verify(userRepository).findById(2L);
        verify(userRepository).existsByEmailAndIdNot(request.email(), 2L);
        verify(userMapper).updateEntity(targetUser, request);
        verify(userRepository).save(targetUser);
        verify(userMapper).toResponse(targetUser);
    }

    @Test
    void shouldPatchStudentWhenAuthenticatedUserIsTeacherAndTargetUserIsStudent() {
        User teacher = createUser(1L, UserRole.TEACHER, 1L);
        authenticate(teacher);

        User targetStudent = createUser(2L, UserRole.STUDENT, 1L);

        UpdateUserRequest request = new UpdateUserRequest(
                "Aluno Atualizado pelo Professor",
                "student.updated.by.teacher@gymflow.com",
                null
        );

        UserResponse expectedResponse = new UserResponse(
                2L,
                1L,
                "GymFlow Academy Dev",
                "Aluno Atualizado pelo Professor",
                "student.updated.by.teacher@gymflow.com",
                UserRole.STUDENT,
                true,
                null
        );

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetStudent));
        when(userRepository.existsByEmailAndIdNot(request.email(), 2L)).thenReturn(false);
        when(userRepository.save(targetStudent)).thenReturn(targetStudent);
        when(userMapper.toResponse(targetStudent)).thenReturn(expectedResponse);

        UserResponse response = userService.patch(2L, request);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("Aluno Atualizado pelo Professor", response.name());
        assertEquals("student.updated.by.teacher@gymflow.com", response.email());
        assertEquals(UserRole.STUDENT, response.role());
        assertTrue(response.active());

        verify(userRepository).findById(2L);
        verify(userRepository).existsByEmailAndIdNot(request.email(), 2L);
        verify(userMapper).updateEntity(targetStudent, request);
        verify(userRepository).save(targetStudent);
        verify(userMapper).toResponse(targetStudent);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenTeacherPatchesStudentActiveStatus() {
        User teacher = createUser(1L, UserRole.TEACHER, 1L);
        authenticate(teacher);

        User targetStudent = createUser(2L, UserRole.STUDENT, 1L);

        UpdateUserRequest request = new UpdateUserRequest(
                null,
                null,
                false
        );

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetStudent));

        assertThrows(AccessDeniedException.class, () ->
                userService.patch(2L, request)
        );

        verify(userRepository).findById(2L);
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
        verify(userMapper, never()).updateEntity(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenTeacherPatchesAdmin() {
        User teacher = createUser(1L, UserRole.TEACHER, 1L);
        authenticate(teacher);

        User targetAdmin = createUser(2L, UserRole.ADMIN, 1L);

        UpdateUserRequest request = new UpdateUserRequest(
                "Admin Editado Indevidamente",
                null,
                null
        );

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetAdmin));

        assertThrows(AccessDeniedException.class, () ->
                userService.patch(2L, request)
        );

        verify(userRepository).findById(2L);
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
        verify(userMapper, never()).updateEntity(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenTeacherPatchesTeacher() {
        User teacher = createUser(1L, UserRole.TEACHER, 1L);
        authenticate(teacher);

        User targetTeacher = createUser(2L, UserRole.TEACHER, 1L);

        UpdateUserRequest request = new UpdateUserRequest(
                "Professor Editado Indevidamente",
                null,
                null
        );

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetTeacher));

        assertThrows(AccessDeniedException.class, () ->
                userService.patch(2L, request)
        );

        verify(userRepository).findById(2L);
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
        verify(userMapper, never()).updateEntity(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenEmailAlreadyExistsOnPatch() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        User targetUser = createUser(2L, UserRole.STUDENT, 1L);

        UpdateUserRequest request = new UpdateUserRequest(
                null,
                "teacher.dev@gymflow.com",
                null
        );

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.existsByEmailAndIdNot(request.email(), 2L)).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                userService.patch(2L, request)
        );

        assertEquals("Email already in use", exception.getMessage());

        verify(userRepository).findById(2L);
        verify(userRepository).existsByEmailAndIdNot(request.email(), 2L);
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).updateEntity(any(), any());
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenPatchUserFromAnotherOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        User targetUser = createUser(2L, UserRole.STUDENT, 2L);

        UpdateUserRequest request = new UpdateUserRequest(
                "Aluno Indevido",
                null,
                null
        );

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        assertThrows(AccessDeniedException.class, () ->
                userService.patch(2L, request)
        );

        verify(userRepository).findById(2L);
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).updateEntity(any(), any());
    }

    @Test
    void shouldDeactivateUserWhenUserBelongsToAuthenticatedUserOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        User targetUser = createUser(2L, UserRole.STUDENT, 1L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(targetUser)).thenReturn(targetUser);

        userService.delete(2L);

        assertFalse(targetUser.getActive());

        verify(userRepository).findById(2L);
        verify(userRepository).save(targetUser);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenDeleteUserFromAnotherOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        User targetUser = createUser(2L, UserRole.STUDENT, 2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        assertThrows(AccessDeniedException.class, () ->
                userService.delete(2L)
        );

        verify(userRepository).findById(2L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldFindStudentsByOrganizationAndRoleWhenAuthenticatedUserIsAdmin() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Organization organization = createOrganization(1L);

        User student = createUser(3L, UserRole.STUDENT, 1L);

        UserResponse studentResponse = new UserResponse(
                3L,
                1L,
                "GymFlow Academy Dev",
                "Test User",
                "user3@gymflow.com",
                UserRole.STUDENT,
                true,
                null
        );

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(userRepository.findByOrganizationIdAndRole(1L, UserRole.STUDENT))
                .thenReturn(List.of(student));
        when(userMapper.toResponse(student)).thenReturn(studentResponse);

        List<UserResponse> response = userService.findAllByOrganizationIdAndRole(1L, UserRole.STUDENT);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(3L, response.get(0).id());
        assertEquals(1L, response.get(0).organizationId());
        assertEquals(UserRole.STUDENT, response.get(0).role());

        verify(organizationRepository).findById(1L);
        verify(userRepository).findByOrganizationIdAndRole(1L, UserRole.STUDENT);
        verify(userMapper).toResponse(student);
    }

    @Test
    void shouldFindStudentsByOrganizationAndRoleWhenAuthenticatedUserIsTeacher() {
        User teacher = createUser(2L, UserRole.TEACHER, 1L);
        authenticate(teacher);

        Organization organization = createOrganization(1L);

        User student = createUser(3L, UserRole.STUDENT, 1L);

        UserResponse studentResponse = new UserResponse(
                3L,
                1L,
                "GymFlow Academy Dev",
                "Test User",
                "user3@gymflow.com",
                UserRole.STUDENT,
                true,
                null
        );

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(userRepository.findByOrganizationIdAndRole(1L, UserRole.STUDENT))
                .thenReturn(List.of(student));
        when(userMapper.toResponse(student)).thenReturn(studentResponse);

        List<UserResponse> response = userService.findAllByOrganizationIdAndRole(1L, UserRole.STUDENT);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(3L, response.get(0).id());
        assertEquals(1L, response.get(0).organizationId());
        assertEquals(UserRole.STUDENT, response.get(0).role());

        verify(organizationRepository).findById(1L);
        verify(userRepository).findByOrganizationIdAndRole(1L, UserRole.STUDENT);
        verify(userMapper).toResponse(student);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenTeacherFindsAdminsByOrganizationAndRole() {
        User teacher = createUser(2L, UserRole.TEACHER, 1L);
        authenticate(teacher);

        Organization organization = createOrganization(1L);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));

        assertThrows(AccessDeniedException.class, () ->
                userService.findAllByOrganizationIdAndRole(1L, UserRole.ADMIN)
        );

        verify(organizationRepository).findById(1L);
        verify(userRepository, never()).findByOrganizationIdAndRole(anyLong(), any());
        verifyNoInteractions(userMapper);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenTeacherFindsTeachersByOrganizationAndRole() {
        User teacher = createUser(2L, UserRole.TEACHER, 1L);
        authenticate(teacher);

        Organization organization = createOrganization(1L);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));

        assertThrows(AccessDeniedException.class, () ->
                userService.findAllByOrganizationIdAndRole(1L, UserRole.TEACHER)
        );

        verify(organizationRepository).findById(1L);
        verify(userRepository, never()).findByOrganizationIdAndRole(anyLong(), any());
        verifyNoInteractions(userMapper);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenTeacherFindsStudentsFromAnotherOrganization() {
        User teacher = createUser(2L, UserRole.TEACHER, 1L);
        authenticate(teacher);

        Organization anotherOrganization = createOrganization(2L);

        when(organizationRepository.findById(2L)).thenReturn(Optional.of(anotherOrganization));

        assertThrows(AccessDeniedException.class, () ->
                userService.findAllByOrganizationIdAndRole(2L, UserRole.STUDENT)
        );

        verify(organizationRepository).findById(2L);
        verify(userRepository, never()).findByOrganizationIdAndRole(anyLong(), any());
        verifyNoInteractions(userMapper);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenStudentFindsStudentsByOrganizationAndRole() {
        User student = createUser(3L, UserRole.STUDENT, 1L);
        authenticate(student);

        Organization organization = createOrganization(1L);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));

        assertThrows(AccessDeniedException.class, () ->
                userService.findAllByOrganizationIdAndRole(1L, UserRole.STUDENT)
        );

        verify(organizationRepository).findById(1L);
        verify(userRepository, never()).findByOrganizationIdAndRole(anyLong(), any());
        verifyNoInteractions(userMapper);
    }

    private void authenticate(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private User createUser(Long id, UserRole role, Long organizationId) {
        Organization organization = createOrganization(organizationId);

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

    private Organization createOrganization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        organization.setOrganizationName("GymFlow Academy Dev");
        return organization;
    }
}