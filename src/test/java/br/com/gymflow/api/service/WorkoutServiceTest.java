package br.com.gymflow.api.service;

import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.Workout;
import br.com.gymflow.api.domain.enums.OrganizationType;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.domain.enums.WorkoutStatus;
import br.com.gymflow.api.dto.workout.CreateWorkoutRequest;
import br.com.gymflow.api.dto.workout.UpdateWorkoutRequest;
import br.com.gymflow.api.dto.workout.WorkoutResponse;
import br.com.gymflow.api.exception.BusinessRuleException;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.mapper.WorkoutMapper;
import br.com.gymflow.api.repository.OrganizationRepository;
import br.com.gymflow.api.repository.UserRepository;
import br.com.gymflow.api.repository.WorkoutRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private WorkoutMapper workoutMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private WorkoutService workoutService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldCreateWorkoutSuccessfully() {
        // Arrange
        Long teacherId = 1L;
        Long workoutId = 10L;

        CreateWorkoutRequest request = createWorkoutRequest(
                teacherId,
                "Treino A"
        );

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(teacherId, organization);
        authenticate(teacher);

        Workout workoutToSave = createWorkout(
                null,
                teacher,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        Workout savedWorkout = createWorkout(
                workoutId,
                teacher,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        WorkoutResponse expectedResponse = createWorkoutResponse(
                workoutId,
                teacherId,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        when(userRepository.findById(teacherId))
                .thenReturn(Optional.of(teacher));

        when(workoutMapper.toEntity(request))
                .thenReturn(workoutToSave);

        when(workoutRepository.save(workoutToSave))
                .thenReturn(savedWorkout);

        when(workoutMapper.toResponse(savedWorkout))
                .thenReturn(expectedResponse);

        // Act
        WorkoutResponse response = workoutService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals(workoutId, response.workoutId());
        assertEquals(teacherId, response.teacherId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.status());

        assertEquals(teacher, workoutToSave.getTeacher());
        assertEquals(WorkoutStatus.ACTIVE, workoutToSave.getStatus());

        verify(userRepository).findById(teacherId);
        verify(workoutMapper).toEntity(request);
        verify(workoutRepository).save(workoutToSave);
        verify(workoutMapper).toResponse(savedWorkout);
    }

    @Test
    void shouldCreateWorkoutSuccessfullyWhenUserIsAdmin() {
        // Arrange
        Long adminId = 1L;
        Long workoutId = 10L;

        CreateWorkoutRequest request = createWorkoutRequest(
                adminId,
                "Treino A"
        );

        Organization organization = createOrganization(100L);
        User admin = createTeacher(adminId, organization);
        admin.setRole(UserRole.ADMIN);
        authenticate(admin);

        Workout workoutToSave = createWorkout(
                null,
                admin,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        Workout savedWorkout = createWorkout(
                workoutId,
                admin,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        WorkoutResponse expectedResponse = createWorkoutResponse(
                workoutId,
                adminId,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        when(userRepository.findById(adminId))
                .thenReturn(Optional.of(admin));

        when(workoutMapper.toEntity(request))
                .thenReturn(workoutToSave);

        when(workoutRepository.save(workoutToSave))
                .thenReturn(savedWorkout);

        when(workoutMapper.toResponse(savedWorkout))
                .thenReturn(expectedResponse);

        // Act
        WorkoutResponse response = workoutService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals(workoutId, response.workoutId());
        assertEquals(adminId, response.teacherId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.status());

        assertEquals(admin, workoutToSave.getTeacher());
        assertEquals(WorkoutStatus.ACTIVE, workoutToSave.getStatus());

        verify(userRepository).findById(adminId);
        verify(workoutMapper).toEntity(request);
        verify(workoutRepository).save(workoutToSave);
        verify(workoutMapper).toResponse(savedWorkout);
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenUserIsStudentOnCreate() {
        // Arrange
        Long studentId = 1L;

        CreateWorkoutRequest request = createWorkoutRequest(
                studentId,
                "Treino A"
        );

        Organization organization = createOrganization(100L);
        User student = createTeacher(studentId, organization);
        student.setRole(UserRole.STUDENT);

        when(userRepository.findById(studentId))
                .thenReturn(Optional.of(student));

        // Act + Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> workoutService.create(request)
        );

        assertEquals(
                "User is not allowed to create workouts with id: " + studentId,
                exception.getMessage()
        );

        verify(userRepository).findById(studentId);

        verifyNoInteractions(
                workoutRepository,
                workoutMapper
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenTeacherDoesNotExistOnCreate() {
        // Arrange
        Long teacherId = 1L;

        CreateWorkoutRequest request = createWorkoutRequest(
                teacherId,
                "Treino A"
        );

        when(userRepository.findById(teacherId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutService.create(request)
        );

        assertEquals(
                "Teacher not found with id: " + teacherId,
                exception.getMessage()
        );

        verify(userRepository).findById(teacherId);

        verifyNoInteractions(
                workoutRepository,
                workoutMapper
        );
    }

    @Test
    void shouldFindAllWorkoutsSuccessfully() {
        Long teacherId = 1L;

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(teacherId, organization);
        authenticate(teacher);

        Workout workoutA = createWorkout(
                10L,
                teacher,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        Workout workoutB = createWorkout(
                20L,
                teacher,
                "Treino B",
                WorkoutStatus.INACTIVE
        );

        WorkoutResponse responseA = createWorkoutResponse(
                10L,
                teacherId,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        WorkoutResponse responseB = createWorkoutResponse(
                20L,
                teacherId,
                "Treino B",
                WorkoutStatus.INACTIVE
        );

        when(workoutRepository.findByTeacherOrganizationId(100L))
                .thenReturn(List.of(workoutA, workoutB));

        when(workoutMapper.toResponse(workoutA))
                .thenReturn(responseA);

        when(workoutMapper.toResponse(workoutB))
                .thenReturn(responseB);

        List<WorkoutResponse> response = workoutService.findAll();

        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(10L, response.get(0).workoutId());
        assertEquals("Treino A", response.get(0).workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.get(0).status());

        assertEquals(20L, response.get(1).workoutId());
        assertEquals("Treino B", response.get(1).workoutName());
        assertEquals(WorkoutStatus.INACTIVE, response.get(1).status());

        verify(workoutRepository).findByTeacherOrganizationId(100L);
        verify(workoutRepository, never()).findAll();
        verify(workoutMapper).toResponse(workoutA);
        verify(workoutMapper).toResponse(workoutB);

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldFindWorkoutByIdSuccessfully() {
        // Arrange
        Long workoutId = 10L;
        Long teacherId = 1L;

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(teacherId, organization);
        authenticate(teacher);

        Workout workout = createWorkout(
                workoutId,
                teacher,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        WorkoutResponse expectedResponse = createWorkoutResponse(
                workoutId,
                teacherId,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        when(workoutMapper.toResponse(workout))
                .thenReturn(expectedResponse);

        // Act
        WorkoutResponse response = workoutService.findById(workoutId);

        // Assert
        assertNotNull(response);
        assertEquals(workoutId, response.workoutId());
        assertEquals(teacherId, response.teacherId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.status());

        verify(workoutRepository).findById(workoutId);
        verify(workoutMapper).toResponse(workout);

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutDoesNotExistOnFindById() {
        // Arrange
        Long workoutId = 10L;

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutService.findById(workoutId)
        );

        assertEquals(
                "Workout not found with id: " + workoutId,
                exception.getMessage()
        );

        verify(workoutRepository).findById(workoutId);

        verifyNoInteractions(
                userRepository,
                workoutMapper
        );
    }

    @Test
    void shouldPatchWorkoutSuccessfully() {
        // Arrange
        Long workoutId = 10L;
        Long teacherId = 1L;

        UpdateWorkoutRequest request = createUpdateWorkoutRequest(
                "Treino Atualizado",
                WorkoutStatus.INACTIVE
        );

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(teacherId, organization);
        authenticate(teacher);

        Workout workout = createWorkout(
                workoutId,
                teacher,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        Workout updatedWorkout = createWorkout(
                workoutId,
                teacher,
                "Treino Atualizado",
                WorkoutStatus.INACTIVE
        );

        WorkoutResponse expectedResponse = createWorkoutResponse(
                workoutId,
                teacherId,
                "Treino Atualizado",
                WorkoutStatus.INACTIVE
        );

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        when(workoutRepository.save(workout))
                .thenReturn(updatedWorkout);

        when(workoutMapper.toResponse(updatedWorkout))
                .thenReturn(expectedResponse);

        // Act
        WorkoutResponse response = workoutService.patch(workoutId, request);

        // Assert
        assertNotNull(response);
        assertEquals(workoutId, response.workoutId());
        assertEquals(teacherId, response.teacherId());
        assertEquals("Treino Atualizado", response.workoutName());
        assertEquals(WorkoutStatus.INACTIVE, response.status());

        assertEquals("Treino Atualizado", workout.getWorkoutName());
        assertEquals(WorkoutStatus.INACTIVE, workout.getStatus());

        verify(workoutRepository).findById(workoutId);
        verify(workoutRepository).save(workout);
        verify(workoutMapper).toResponse(updatedWorkout);

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutDoesNotExistOnPatch() {
        // Arrange
        Long workoutId = 10L;

        UpdateWorkoutRequest request = createUpdateWorkoutRequest(
                "Treino Atualizado",
                WorkoutStatus.INACTIVE
        );

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutService.patch(workoutId, request)
        );

        assertEquals(
                "Workout not found with id: " + workoutId,
                exception.getMessage()
        );

        verify(workoutRepository).findById(workoutId);

        verifyNoInteractions(
                userRepository,
                workoutMapper
        );

        verify(workoutRepository, never())
                .save(any(Workout.class));
    }

    @Test
    void shouldDeleteWorkoutSuccessfully() {
        // Arrange
        Long workoutId = 10L;
        Long teacherId = 1L;

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(teacherId, organization);
        authenticate(teacher);

        Workout workout = createWorkout(
                workoutId,
                teacher,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        // Act
        workoutService.delete(workoutId);

        // Assert
        assertEquals(WorkoutStatus.INACTIVE, workout.getStatus());

        verify(workoutRepository).findById(workoutId);
        verify(workoutRepository).save(workout);

        verifyNoInteractions(
                userRepository,
                workoutMapper
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutDoesNotExistOnDelete() {
        // Arrange
        Long workoutId = 10L;

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutService.delete(workoutId)
        );

        assertEquals(
                "Workout not found with id: " + workoutId,
                exception.getMessage()
        );

        verify(workoutRepository).findById(workoutId);

        verifyNoInteractions(
                userRepository,
                workoutMapper
        );

        verify(workoutRepository, never())
                .save(any(Workout.class));
    }

    @Test
    void shouldPatchOnlyWorkoutNameWhenStatusIsNull() {
        // Arrange
        Long workoutId = 10L;
        Long teacherId = 1L;

        UpdateWorkoutRequest request = createUpdateWorkoutRequest(
                "Treino Atualizado",
                null
        );

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(teacherId, organization);
        authenticate(teacher);

        Workout workout = createWorkout(
                workoutId,
                teacher,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        Workout updatedWorkout = createWorkout(
                workoutId,
                teacher,
                "Treino Atualizado",
                WorkoutStatus.ACTIVE
        );

        WorkoutResponse expectedResponse = createWorkoutResponse(
                workoutId,
                teacherId,
                "Treino Atualizado",
                WorkoutStatus.ACTIVE
        );

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        when(workoutRepository.save(workout))
                .thenReturn(updatedWorkout);

        when(workoutMapper.toResponse(updatedWorkout))
                .thenReturn(expectedResponse);

        // Act
        WorkoutResponse response = workoutService.patch(workoutId, request);

        // Assert
        assertNotNull(response);
        assertEquals(workoutId, response.workoutId());
        assertEquals(teacherId, response.teacherId());
        assertEquals("Treino Atualizado", response.workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.status());

        assertEquals("Treino Atualizado", workout.getWorkoutName());
        assertEquals(WorkoutStatus.ACTIVE, workout.getStatus());

        verify(workoutRepository).findById(workoutId);
        verify(workoutRepository).save(workout);
        verify(workoutMapper).toResponse(updatedWorkout);

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldPatchOnlyWorkoutStatusWhenWorkoutNameIsNull() {
        // Arrange
        Long workoutId = 10L;
        Long teacherId = 1L;

        UpdateWorkoutRequest request = createUpdateWorkoutRequest(
                null,
                WorkoutStatus.ARCHIVED
        );

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(teacherId, organization);
        authenticate(teacher);

        Workout workout = createWorkout(
                workoutId,
                teacher,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        Workout updatedWorkout = createWorkout(
                workoutId,
                teacher,
                "Treino A",
                WorkoutStatus.ARCHIVED
        );

        WorkoutResponse expectedResponse = createWorkoutResponse(
                workoutId,
                teacherId,
                "Treino A",
                WorkoutStatus.ARCHIVED
        );

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        when(workoutRepository.save(workout))
                .thenReturn(updatedWorkout);

        when(workoutMapper.toResponse(updatedWorkout))
                .thenReturn(expectedResponse);

        // Act
        WorkoutResponse response = workoutService.patch(workoutId, request);

        // Assert
        assertNotNull(response);
        assertEquals(workoutId, response.workoutId());
        assertEquals(teacherId, response.teacherId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(WorkoutStatus.ARCHIVED, response.status());

        assertEquals("Treino A", workout.getWorkoutName());
        assertEquals(WorkoutStatus.ARCHIVED, workout.getStatus());

        verify(workoutRepository).findById(workoutId);
        verify(workoutRepository).save(workout);
        verify(workoutMapper).toResponse(updatedWorkout);

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldFindAllWorkoutsByOrganizationIdSuccessfully() {
        // Arrange
        Long organizationId = 100L;

        Organization organization = createOrganization(organizationId);

        User teacher = createTeacher(1L, organization);
        authenticate(teacher);

        Workout workoutA = createWorkout(
                10L,
                teacher,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        Workout workoutB = createWorkout(
                20L,
                teacher,
                "Treino B",
                WorkoutStatus.ACTIVE
        );

        WorkoutResponse responseA = createWorkoutResponse(
                10L,
                teacher.getId(),
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        WorkoutResponse responseB = createWorkoutResponse(
                20L,
                teacher.getId(),
                "Treino B",
                WorkoutStatus.ACTIVE
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(workoutRepository.findByTeacherOrganizationId(organizationId))
                .thenReturn(List.of(workoutA, workoutB));

        when(workoutMapper.toResponse(workoutA))
                .thenReturn(responseA);

        when(workoutMapper.toResponse(workoutB))
                .thenReturn(responseB);

        // Act
        List<WorkoutResponse> response = workoutService.findAllByOrganizationId(organizationId);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(10L, response.get(0).workoutId());
        assertEquals("Treino A", response.get(0).workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.get(0).status());

        assertEquals(20L, response.get(1).workoutId());
        assertEquals("Treino B", response.get(1).workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.get(1).status());

        verify(organizationRepository).findById(organizationId);
        verify(workoutRepository).findByTeacherOrganizationId(organizationId);
        verify(workoutMapper).toResponse(workoutA);
        verify(workoutMapper).toResponse(workoutB);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenOrganizationDoesNotExistOnFindAllByOrganizationId() {
        // Arrange
        Long organizationId = 100L;

        Organization organization = createOrganization(organizationId);
        User admin = createTeacher(1L, organization);
        admin.setRole(UserRole.ADMIN);
        authenticate(admin);

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutService.findAllByOrganizationId(organizationId)
        );

        assertEquals(
                "Organization not found with id: " + organizationId,
                exception.getMessage()
        );

        verify(organizationRepository).findById(organizationId);

        verify(workoutRepository, never())
                .findByTeacherOrganizationId(anyLong());

        verifyNoInteractions(workoutMapper);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenCreateWorkoutWithTeacherFromAnotherOrganization() {
        Long teacherId = 1L;

        Organization authenticatedOrganization = createOrganization(100L);
        User authenticatedTeacher = createTeacher(99L, authenticatedOrganization);
        authenticate(authenticatedTeacher);

        Organization anotherOrganization = createOrganization(200L);
        User teacherFromAnotherOrganization = createTeacher(teacherId, anotherOrganization);

        CreateWorkoutRequest request = createWorkoutRequest(
                teacherId,
                "Treino Indevido"
        );

        when(userRepository.findById(teacherId))
                .thenReturn(Optional.of(teacherFromAnotherOrganization));

        assertThrows(AccessDeniedException.class, () ->
                workoutService.create(request)
        );

        verify(userRepository).findById(teacherId);
        verifyNoInteractions(workoutMapper);
        verify(workoutRepository, never()).save(any());
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenFindWorkoutByIdFromAnotherOrganization() {
        Long workoutId = 10L;

        Organization authenticatedOrganization = createOrganization(100L);
        User authenticatedTeacher = createTeacher(1L, authenticatedOrganization);
        authenticate(authenticatedTeacher);

        Organization anotherOrganization = createOrganization(200L);
        User teacherFromAnotherOrganization = createTeacher(2L, anotherOrganization);

        Workout workout = createWorkout(
                workoutId,
                teacherFromAnotherOrganization,
                "Treino Outra Organização",
                WorkoutStatus.ACTIVE
        );

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        assertThrows(AccessDeniedException.class, () ->
                workoutService.findById(workoutId)
        );

        verify(workoutRepository).findById(workoutId);
        verifyNoInteractions(workoutMapper);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenPatchWorkoutFromAnotherOrganization() {
        Long workoutId = 10L;

        Organization authenticatedOrganization = createOrganization(100L);
        User authenticatedTeacher = createTeacher(1L, authenticatedOrganization);
        authenticate(authenticatedTeacher);

        Organization anotherOrganization = createOrganization(200L);
        User teacherFromAnotherOrganization = createTeacher(2L, anotherOrganization);

        Workout workout = createWorkout(
                workoutId,
                teacherFromAnotherOrganization,
                "Treino Outra Organização",
                WorkoutStatus.ACTIVE
        );

        UpdateWorkoutRequest request = createUpdateWorkoutRequest(
                "Treino Atualizado",
                WorkoutStatus.INACTIVE
        );

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        assertThrows(AccessDeniedException.class, () ->
                workoutService.patch(workoutId, request)
        );

        verify(workoutRepository).findById(workoutId);
        verify(workoutRepository, never()).save(any());
        verifyNoInteractions(workoutMapper);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenDeleteWorkoutFromAnotherOrganization() {
        Long workoutId = 10L;

        Organization authenticatedOrganization = createOrganization(100L);
        User authenticatedTeacher = createTeacher(1L, authenticatedOrganization);
        authenticate(authenticatedTeacher);

        Organization anotherOrganization = createOrganization(200L);
        User teacherFromAnotherOrganization = createTeacher(2L, anotherOrganization);

        Workout workout = createWorkout(
                workoutId,
                teacherFromAnotherOrganization,
                "Treino Outra Organização",
                WorkoutStatus.ACTIVE
        );

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        assertThrows(AccessDeniedException.class, () ->
                workoutService.delete(workoutId)
        );

        verify(workoutRepository).findById(workoutId);
        verify(workoutRepository, never()).save(any());
        verifyNoInteractions(workoutMapper);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenFindAllWorkoutsByAnotherOrganization() {
        Organization authenticatedOrganization = createOrganization(100L);
        User authenticatedTeacher = createTeacher(1L, authenticatedOrganization);
        authenticate(authenticatedTeacher);

        assertThrows(AccessDeniedException.class, () ->
                workoutService.findAllByOrganizationId(200L)
        );

        verifyNoInteractions(organizationRepository);
        verifyNoInteractions(workoutRepository);
        verifyNoInteractions(workoutMapper);
    }


    private Organization createOrganization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        organization.setOrganizationName("GymFlow Academy");
        organization.setOrganizationType(OrganizationType.ACADEMY);
        organization.setActive(true);
        return organization;
    }

    private User createTeacher(Long id, Organization organization) {
        User teacher = new User();
        teacher.setId(id);
        teacher.setName("Professor Teste");
        teacher.setEmail("teacher" + id + "@gymflow.com");
        teacher.setPasswordHash("hashed-password");
        teacher.setRole(UserRole.TEACHER);
        teacher.setOrganization(organization);
        teacher.setActive(true);
        return teacher;
    }

    private Workout createWorkout(Long id, User teacher, String workoutName, WorkoutStatus status) {
        Workout workout = new Workout();
        workout.setId(id);
        workout.setTeacher(teacher);
        workout.setWorkoutName(workoutName);
        workout.setStatus(status);
        return workout;
    }

    private CreateWorkoutRequest createWorkoutRequest(Long teacherId, String workoutName) {
        return new CreateWorkoutRequest(
                teacherId,
                workoutName
        );
    }

    private UpdateWorkoutRequest createUpdateWorkoutRequest(String workoutName, WorkoutStatus status) {
        return new UpdateWorkoutRequest(
                workoutName,
                status
        );
    }

    private WorkoutResponse createWorkoutResponse(
            Long workoutId,
            Long teacherId,
            String workoutName,
            WorkoutStatus status
    ) {
        return new WorkoutResponse(
                workoutId,
                teacherId,
                workoutName,
                status,
                null,
                null
        );
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
}