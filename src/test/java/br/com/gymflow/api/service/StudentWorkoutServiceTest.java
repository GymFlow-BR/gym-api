package br.com.gymflow.api.service;

import br.com.gymflow.api.config.security.StudentAccessValidator;
import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.domain.StudentWorkout;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.Workout;
import br.com.gymflow.api.domain.WorkoutExercise;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.domain.enums.WorkoutStatus;
import br.com.gymflow.api.dto.studentWorkouts.CreateStudentWorkoutRequest;
import br.com.gymflow.api.dto.studentWorkouts.PatchStudentWorkoutRequest;
import br.com.gymflow.api.dto.studentWorkouts.StudentCurrentWorkoutResponse;
import br.com.gymflow.api.dto.studentWorkouts.StudentWorkoutResponse;
import br.com.gymflow.api.exception.BusinessRuleException;
import br.com.gymflow.api.exception.DuplicateResourceException;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.mapper.StudentWorkoutMapper;
import br.com.gymflow.api.repository.StudentWorkoutRepository;
import br.com.gymflow.api.repository.UserRepository;
import br.com.gymflow.api.repository.WorkoutExerciseRepository;
import br.com.gymflow.api.repository.WorkoutRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentWorkoutServiceTest {

    @Mock
    private StudentWorkoutRepository studentWorkoutRepository;

    @Mock
    private StudentWorkoutMapper studentWorkoutMapper;

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Mock
    private StudentAccessValidator studentAccessValidator;

    @InjectMocks
    private StudentWorkoutService studentWorkoutService;

    @Test
    void shouldCreateStudentWorkoutSuccessfully() {
        Long studentId = 1L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(workoutId);

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        LocalDateTime assignedAt = LocalDateTime.now();

        StudentWorkout savedStudentWorkout = createStudentWorkout(
                50L,
                student,
                workout,
                WorkoutStatus.ACTIVE,
                assignedAt
        );

        StudentWorkoutResponse expectedResponse = createStudentWorkoutResponse(
                50L,
                studentId,
                workoutId,
                "Treino A",
                assignedAt,
                WorkoutStatus.ACTIVE
        );

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(studentWorkoutRepository.existsByStudentIdAndWorkoutId(studentId, workoutId)).thenReturn(false);
        when(studentWorkoutRepository.findAllByStudentIdAndStatus(studentId, WorkoutStatus.ACTIVE)).thenReturn(List.of());
        when(studentWorkoutMapper.toEntity(request)).thenReturn(new StudentWorkout());
        when(studentWorkoutRepository.save(any(StudentWorkout.class))).thenReturn(savedStudentWorkout);
        when(studentWorkoutMapper.toResponse(savedStudentWorkout)).thenReturn(expectedResponse);

        StudentWorkoutResponse response = studentWorkoutService.create(studentId, request);

        assertNotNull(response);
        assertEquals(50L, response.studentWorkoutId());
        assertEquals(studentId, response.studentId());
        assertEquals("Aluno Teste", response.studentName());
        assertEquals(workoutId, response.workoutId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.status());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(workoutRepository).findById(workoutId);
        verify(studentWorkoutRepository).existsByStudentIdAndWorkoutId(studentId, workoutId);
        verify(studentWorkoutRepository).findAllByStudentIdAndStatus(studentId, WorkoutStatus.ACTIVE);
        verify(studentWorkoutMapper).toEntity(request);
        verify(studentWorkoutRepository).save(any(StudentWorkout.class));
        verify(studentWorkoutMapper).toResponse(savedStudentWorkout);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenStudentDoesNotExist() {
        Long studentId = 1L;
        Long workoutId = 10L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(workoutId);

        when(userRepository.findById(studentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.create(studentId, request)
        );

        assertEquals("Student not found with id: " + studentId, exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);

        verifyNoInteractions(
                workoutRepository,
                studentWorkoutRepository,
                studentWorkoutMapper,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutDoesNotExist() {
        Long studentId = 1L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(workoutId);

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.create(studentId, request)
        );

        assertEquals("Workout not found with id: " + workoutId, exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(workoutRepository).findById(workoutId);

        verifyNoInteractions(
                studentWorkoutRepository,
                studentWorkoutMapper,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenUserIsNotStudent() {
        Long studentId = 1L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(workoutId);

        Organization organization = createOrganization(organizationId);
        User user = createUser(studentId, "Professor Teste", UserRole.TEACHER, organization);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(user));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> studentWorkoutService.create(studentId, request)
        );

        assertEquals("User is not a student with id: " + studentId, exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);

        verifyNoInteractions(
                workoutRepository,
                studentWorkoutRepository,
                studentWorkoutMapper,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenStudentAndWorkoutBelongToDifferentOrganizations() {
        Long studentId = 1L;
        Long workoutId = 10L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(workoutId);

        Organization studentOrganization = createOrganization(100L);
        Organization teacherOrganization = createOrganization(200L);

        User student = createStudent(studentId, studentOrganization);
        User teacher = createTeacher(2L, teacherOrganization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> studentWorkoutService.create(studentId, request)
        );

        assertEquals("Student does not belong to the same organization as the workout", exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(workoutRepository).findById(workoutId);

        verifyNoInteractions(
                studentWorkoutRepository,
                studentWorkoutMapper,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldThrowDuplicateResourceExceptionWhenStudentAlreadyHasWorkoutAssigned() {
        Long studentId = 1L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(workoutId);

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(studentWorkoutRepository.existsByStudentIdAndWorkoutId(studentId, workoutId)).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> studentWorkoutService.create(studentId, request)
        );

        assertEquals("Student already has this workout assigned", exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(workoutRepository).findById(workoutId);
        verify(studentWorkoutRepository).existsByStudentIdAndWorkoutId(studentId, workoutId);

        verifyNoInteractions(
                studentWorkoutMapper,
                workoutExerciseRepository
        );

        verify(studentWorkoutRepository, never()).findAllByStudentIdAndStatus(studentId, WorkoutStatus.ACTIVE);
        verify(studentWorkoutRepository, never()).save(any(StudentWorkout.class));
    }

    @Test
    void shouldDeactivateCurrentActiveWorkoutsWhenCreatingNewStudentWorkout() {
        Long studentId = 1L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(workoutId);

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);

        Workout workout = createWorkout(workoutId, teacher, "Treino Novo");
        Workout oldWorkout = createWorkout(99L, teacher, "Treino Antigo");

        StudentWorkout activeStudentWorkout = createStudentWorkout(
                20L,
                student,
                oldWorkout,
                WorkoutStatus.ACTIVE,
                LocalDateTime.now().minusDays(1)
        );

        StudentWorkout newStudentWorkout = new StudentWorkout();

        StudentWorkout savedStudentWorkout = createStudentWorkout(
                50L,
                student,
                workout,
                WorkoutStatus.ACTIVE,
                LocalDateTime.now()
        );

        StudentWorkoutResponse expectedResponse = createStudentWorkoutResponse(
                50L,
                studentId,
                workoutId,
                "Treino Novo",
                savedStudentWorkout.getAssignedAt(),
                WorkoutStatus.ACTIVE
        );

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(studentWorkoutRepository.existsByStudentIdAndWorkoutId(studentId, workoutId)).thenReturn(false);
        when(studentWorkoutRepository.findAllByStudentIdAndStatus(studentId, WorkoutStatus.ACTIVE))
                .thenReturn(List.of(activeStudentWorkout));
        when(studentWorkoutMapper.toEntity(request)).thenReturn(newStudentWorkout);
        when(studentWorkoutRepository.save(any(StudentWorkout.class))).thenReturn(savedStudentWorkout);
        when(studentWorkoutMapper.toResponse(savedStudentWorkout)).thenReturn(expectedResponse);

        StudentWorkoutResponse response = studentWorkoutService.create(studentId, request);

        assertNotNull(response);
        assertEquals("Aluno Teste", response.studentName());
        assertEquals(WorkoutStatus.ACTIVE, response.status());
        assertEquals(WorkoutStatus.INACTIVE, activeStudentWorkout.getStatus());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(studentWorkoutRepository).saveAll(List.of(activeStudentWorkout));
        verify(studentWorkoutRepository).save(any(StudentWorkout.class));
    }

    @Test
    void shouldFindAllStudentWorkoutsByStudentIdSuccessfully() {
        Long studentId = 1L;
        Long organizationId = 100L;

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);

        Workout workoutA = createWorkout(10L, teacher, "Treino A");
        Workout workoutB = createWorkout(20L, teacher, "Treino B");

        LocalDateTime assignedAtA = LocalDateTime.now().minusDays(2);
        LocalDateTime assignedAtB = LocalDateTime.now().minusDays(1);

        StudentWorkout studentWorkoutA = createStudentWorkout(1000L, student, workoutA, WorkoutStatus.INACTIVE, assignedAtA);
        StudentWorkout studentWorkoutB = createStudentWorkout(2000L, student, workoutB, WorkoutStatus.ACTIVE, assignedAtB);

        StudentWorkoutResponse responseA = createStudentWorkoutResponse(
                1000L,
                studentId,
                10L,
                "Treino A",
                assignedAtA,
                WorkoutStatus.INACTIVE
        );

        StudentWorkoutResponse responseB = createStudentWorkoutResponse(
                2000L,
                studentId,
                20L,
                "Treino B",
                assignedAtB,
                WorkoutStatus.ACTIVE
        );

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentWorkoutRepository.findAllByStudentId(studentId)).thenReturn(List.of(studentWorkoutA, studentWorkoutB));
        when(studentWorkoutMapper.toResponse(studentWorkoutA)).thenReturn(responseA);
        when(studentWorkoutMapper.toResponse(studentWorkoutB)).thenReturn(responseB);

        List<StudentWorkoutResponse> response = studentWorkoutService.findAllByStudentId(studentId);

        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(1000L, response.get(0).studentWorkoutId());
        assertEquals("Aluno Teste", response.get(0).studentName());
        assertEquals("Treino A", response.get(0).workoutName());
        assertEquals(WorkoutStatus.INACTIVE, response.get(0).status());

        assertEquals(2000L, response.get(1).studentWorkoutId());
        assertEquals("Aluno Teste", response.get(1).studentName());
        assertEquals("Treino B", response.get(1).workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.get(1).status());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(studentWorkoutRepository).findAllByStudentId(studentId);
        verify(studentWorkoutMapper).toResponse(studentWorkoutA);
        verify(studentWorkoutMapper).toResponse(studentWorkoutB);

        verifyNoInteractions(
                workoutRepository,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenStudentDoesNotExistOnFindAll() {
        Long studentId = 1L;

        when(userRepository.findById(studentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.findAllByStudentId(studentId)
        );

        assertEquals("Student not found with id: " + studentId, exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);

        verifyNoInteractions(
                studentWorkoutRepository,
                studentWorkoutMapper,
                workoutRepository,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldFindStudentWorkoutByIdSuccessfully() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        LocalDateTime assignedAt = LocalDateTime.now();

        StudentWorkout studentWorkout = createStudentWorkout(studentWorkoutId, student, workout, WorkoutStatus.ACTIVE, assignedAt);

        StudentWorkoutResponse expectedResponse = createStudentWorkoutResponse(
                studentWorkoutId,
                studentId,
                workoutId,
                "Treino A",
                assignedAt,
                WorkoutStatus.ACTIVE
        );

        when(studentWorkoutRepository.findById(studentWorkoutId)).thenReturn(Optional.of(studentWorkout));
        when(studentWorkoutMapper.toResponse(studentWorkout)).thenReturn(expectedResponse);

        StudentWorkoutResponse response = studentWorkoutService.findById(studentId, studentWorkoutId);

        assertNotNull(response);
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(studentId, response.studentId());
        assertEquals("Aluno Teste", response.studentName());
        assertEquals(workoutId, response.workoutId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.status());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(studentWorkoutRepository).findById(studentWorkoutId);
        verify(studentWorkoutMapper).toResponse(studentWorkout);

        verifyNoInteractions(
                userRepository,
                workoutRepository,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenStudentWorkoutDoesNotExistOnFindById() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;

        when(studentWorkoutRepository.findById(studentWorkoutId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.findById(studentId, studentWorkoutId)
        );

        assertEquals("Student workout not found with id: " + studentWorkoutId, exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(studentWorkoutRepository).findById(studentWorkoutId);

        verifyNoInteractions(
                userRepository,
                workoutRepository,
                studentWorkoutMapper,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenStudentWorkoutDoesNotBelongToStudent() {
        Long requestedStudentId = 1L;
        Long ownerStudentId = 2L;
        Long studentWorkoutId = 50L;
        Long organizationId = 100L;

        Organization organization = createOrganization(organizationId);
        User ownerStudent = createStudent(ownerStudentId, organization);

        StudentWorkout studentWorkout = new StudentWorkout();
        studentWorkout.setId(studentWorkoutId);
        studentWorkout.setStudent(ownerStudent);

        when(studentWorkoutRepository.findById(studentWorkoutId)).thenReturn(Optional.of(studentWorkout));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.findById(requestedStudentId, studentWorkoutId)
        );

        assertEquals(
                "Student workout not found with id: "
                        + studentWorkoutId
                        + " for student id: "
                        + requestedStudentId,
                exception.getMessage()
        );

        verify(studentAccessValidator).validateStudentAccess(requestedStudentId);
        verify(studentWorkoutRepository).findById(studentWorkoutId);

        verifyNoInteractions(
                userRepository,
                workoutRepository,
                studentWorkoutMapper,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldPatchStudentWorkoutStatusSuccessfully() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        PatchStudentWorkoutRequest request = new PatchStudentWorkoutRequest(WorkoutStatus.INACTIVE);

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        LocalDateTime assignedAt = LocalDateTime.now();

        StudentWorkout studentWorkout = createStudentWorkout(studentWorkoutId, student, workout, WorkoutStatus.ACTIVE, assignedAt);
        StudentWorkout updatedStudentWorkout = createStudentWorkout(studentWorkoutId, student, workout, WorkoutStatus.INACTIVE, assignedAt);

        StudentWorkoutResponse expectedResponse = createStudentWorkoutResponse(
                studentWorkoutId,
                studentId,
                workoutId,
                "Treino A",
                assignedAt,
                WorkoutStatus.INACTIVE
        );

        when(studentWorkoutRepository.findById(studentWorkoutId)).thenReturn(Optional.of(studentWorkout));
        when(studentWorkoutRepository.save(studentWorkout)).thenReturn(updatedStudentWorkout);
        when(studentWorkoutMapper.toResponse(updatedStudentWorkout)).thenReturn(expectedResponse);

        StudentWorkoutResponse response = studentWorkoutService.patch(studentId, studentWorkoutId, request);

        assertNotNull(response);
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(studentId, response.studentId());
        assertEquals("Aluno Teste", response.studentName());
        assertEquals(workoutId, response.workoutId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(WorkoutStatus.INACTIVE, response.status());

        assertEquals(WorkoutStatus.INACTIVE, studentWorkout.getStatus());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(studentWorkoutRepository).findById(studentWorkoutId);
        verify(studentWorkoutRepository).save(studentWorkout);
        verify(studentWorkoutMapper).toResponse(updatedStudentWorkout);

        verifyNoInteractions(
                userRepository,
                workoutRepository,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenStudentWorkoutDoesNotExistOnPatch() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;

        PatchStudentWorkoutRequest request = new PatchStudentWorkoutRequest(WorkoutStatus.INACTIVE);

        when(studentWorkoutRepository.findById(studentWorkoutId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.patch(studentId, studentWorkoutId, request)
        );

        assertEquals("Student workout not found with id: " + studentWorkoutId, exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(studentWorkoutRepository).findById(studentWorkoutId);

        verifyNoInteractions(
                userRepository,
                workoutRepository,
                studentWorkoutMapper,
                workoutExerciseRepository
        );

        verify(studentWorkoutRepository, never()).save(any(StudentWorkout.class));
    }

    @Test
    void shouldDeactivateOtherActiveWorkoutsWhenPatchingStudentWorkoutToActive() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long otherStudentWorkoutId = 60L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        PatchStudentWorkoutRequest request = new PatchStudentWorkoutRequest(WorkoutStatus.ACTIVE);

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        StudentWorkout studentWorkoutToActivate = createStudentWorkout(
                studentWorkoutId,
                student,
                workout,
                WorkoutStatus.INACTIVE,
                LocalDateTime.now().minusDays(2)
        );

        StudentWorkout otherActiveStudentWorkout = createStudentWorkout(
                otherStudentWorkoutId,
                student,
                workout,
                WorkoutStatus.ACTIVE,
                LocalDateTime.now().minusDays(1)
        );

        StudentWorkoutResponse expectedResponse = createStudentWorkoutResponse(
                studentWorkoutId,
                studentId,
                workoutId,
                "Treino A",
                studentWorkoutToActivate.getAssignedAt(),
                WorkoutStatus.ACTIVE
        );

        when(studentWorkoutRepository.findById(studentWorkoutId)).thenReturn(Optional.of(studentWorkoutToActivate));
        when(studentWorkoutRepository.findAllByStudentIdAndStatus(studentId, WorkoutStatus.ACTIVE))
                .thenReturn(List.of(otherActiveStudentWorkout));
        when(studentWorkoutRepository.save(studentWorkoutToActivate)).thenReturn(studentWorkoutToActivate);
        when(studentWorkoutMapper.toResponse(studentWorkoutToActivate)).thenReturn(expectedResponse);

        StudentWorkoutResponse response = studentWorkoutService.patch(studentId, studentWorkoutId, request);

        assertNotNull(response);
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(studentId, response.studentId());
        assertEquals("Aluno Teste", response.studentName());
        assertEquals(workoutId, response.workoutId());
        assertEquals(WorkoutStatus.ACTIVE, response.status());

        assertEquals(WorkoutStatus.ACTIVE, studentWorkoutToActivate.getStatus());
        assertEquals(WorkoutStatus.INACTIVE, otherActiveStudentWorkout.getStatus());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(studentWorkoutRepository).findById(studentWorkoutId);
        verify(studentWorkoutRepository).findAllByStudentIdAndStatus(studentId, WorkoutStatus.ACTIVE);
        verify(studentWorkoutRepository).saveAll(List.of(otherActiveStudentWorkout));
        verify(studentWorkoutRepository).save(studentWorkoutToActivate);
        verify(studentWorkoutMapper).toResponse(studentWorkoutToActivate);

        verifyNoInteractions(
                userRepository,
                workoutRepository,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldDeleteStudentWorkoutSuccessfully() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        StudentWorkout studentWorkout = createStudentWorkout(
                studentWorkoutId,
                student,
                workout,
                WorkoutStatus.ACTIVE,
                LocalDateTime.now()
        );

        when(studentWorkoutRepository.findById(studentWorkoutId)).thenReturn(Optional.of(studentWorkout));

        studentWorkoutService.delete(studentId, studentWorkoutId);

        assertEquals(WorkoutStatus.INACTIVE, studentWorkout.getStatus());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(studentWorkoutRepository).findById(studentWorkoutId);
        verify(studentWorkoutRepository).save(studentWorkout);

        verifyNoInteractions(
                userRepository,
                workoutRepository,
                studentWorkoutMapper,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenStudentWorkoutDoesNotExistOnDelete() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;

        when(studentWorkoutRepository.findById(studentWorkoutId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.delete(studentId, studentWorkoutId)
        );

        assertEquals("Student workout not found with id: " + studentWorkoutId, exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(studentWorkoutRepository).findById(studentWorkoutId);

        verifyNoInteractions(
                userRepository,
                workoutRepository,
                studentWorkoutMapper,
                workoutExerciseRepository
        );

        verify(studentWorkoutRepository, never()).save(any(StudentWorkout.class));
    }

    @Test
    void shouldFindCurrentWorkoutSuccessfully() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        LocalDateTime assignedAt = LocalDateTime.now();

        StudentWorkout studentWorkout = createStudentWorkout(studentWorkoutId, student, workout, WorkoutStatus.ACTIVE, assignedAt);

        WorkoutExercise workoutExerciseA = createWorkoutExercise(100L, workout);
        WorkoutExercise workoutExerciseB = createWorkoutExercise(200L, workout);

        List<WorkoutExercise> workoutExercises = List.of(workoutExerciseA, workoutExerciseB);

        StudentCurrentWorkoutResponse expectedResponse = new StudentCurrentWorkoutResponse(
                studentId,
                studentWorkoutId,
                workoutId,
                "Treino A",
                assignedAt,
                WorkoutStatus.ACTIVE,
                List.of()
        );

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(studentId, WorkoutStatus.ACTIVE))
                .thenReturn(Optional.of(studentWorkout));
        when(workoutExerciseRepository.findAllByWorkoutIdOrderByExerciseOrderAsc(workoutId)).thenReturn(workoutExercises);
        when(studentWorkoutMapper.toCurrentWorkoutResponse(studentWorkout, workoutExercises)).thenReturn(expectedResponse);

        StudentCurrentWorkoutResponse response = studentWorkoutService.findCurrentWorkout(studentId);

        assertNotNull(response);
        assertEquals(studentId, response.studentId());
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(workoutId, response.workoutId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.status());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(studentWorkoutRepository).findFirstByStudentIdAndStatusOrderByAssignedAtDesc(studentId, WorkoutStatus.ACTIVE);
        verify(workoutExerciseRepository).findAllByWorkoutIdOrderByExerciseOrderAsc(workoutId);
        verify(studentWorkoutMapper).toCurrentWorkoutResponse(studentWorkout, workoutExercises);

        verifyNoInteractions(workoutRepository);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenStudentDoesNotExistOnFindCurrentWorkout() {
        Long studentId = 1L;

        when(userRepository.findById(studentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.findCurrentWorkout(studentId)
        );

        assertEquals("Student not found with id: " + studentId, exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);

        verifyNoInteractions(
                studentWorkoutRepository,
                workoutRepository,
                studentWorkoutMapper,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenStudentDoesNotHaveActiveWorkout() {
        Long studentId = 1L;
        Long organizationId = 100L;

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(studentId, WorkoutStatus.ACTIVE))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.findCurrentWorkout(studentId)
        );

        assertEquals("Active workout not found student id: " + studentId, exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(studentWorkoutRepository).findFirstByStudentIdAndStatusOrderByAssignedAtDesc(studentId, WorkoutStatus.ACTIVE);

        verifyNoInteractions(
                workoutRepository,
                studentWorkoutMapper,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldFindCurrentWorkoutWithEmptyExerciseListWhenWorkoutHasNoExercises() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        LocalDateTime assignedAt = LocalDateTime.now();

        StudentWorkout studentWorkout = createStudentWorkout(studentWorkoutId, student, workout, WorkoutStatus.ACTIVE, assignedAt);
        List<WorkoutExercise> workoutExercises = List.of();

        StudentCurrentWorkoutResponse expectedResponse = new StudentCurrentWorkoutResponse(
                studentId,
                studentWorkoutId,
                workoutId,
                "Treino A",
                assignedAt,
                WorkoutStatus.ACTIVE,
                List.of()
        );

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(studentId, WorkoutStatus.ACTIVE))
                .thenReturn(Optional.of(studentWorkout));
        when(workoutExerciseRepository.findAllByWorkoutIdOrderByExerciseOrderAsc(workoutId)).thenReturn(workoutExercises);
        when(studentWorkoutMapper.toCurrentWorkoutResponse(studentWorkout, workoutExercises)).thenReturn(expectedResponse);

        StudentCurrentWorkoutResponse response = studentWorkoutService.findCurrentWorkout(studentId);

        assertNotNull(response);
        assertEquals(studentId, response.studentId());
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(workoutId, response.workoutId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.status());
        assertNotNull(response.exercises());
        assertEquals(0, response.exercises().size());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(studentWorkoutRepository).findFirstByStudentIdAndStatusOrderByAssignedAtDesc(studentId, WorkoutStatus.ACTIVE);
        verify(workoutExerciseRepository).findAllByWorkoutIdOrderByExerciseOrderAsc(workoutId);
        verify(studentWorkoutMapper).toCurrentWorkoutResponse(studentWorkout, workoutExercises);

        verifyNoInteractions(workoutRepository);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenCurrentWorkoutIsInactive() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);

        Workout workout = createWorkout(workoutId, teacher, "Treino A");
        workout.setStatus(WorkoutStatus.INACTIVE);

        StudentWorkout studentWorkout = createStudentWorkout(
                studentWorkoutId,
                student,
                workout,
                WorkoutStatus.ACTIVE,
                LocalDateTime.now()
        );

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(studentId, WorkoutStatus.ACTIVE))
                .thenReturn(Optional.of(studentWorkout));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.findCurrentWorkout(studentId)
        );

        assertEquals("Active workout not found student id: " + studentId, exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(studentWorkoutRepository).findFirstByStudentIdAndStatusOrderByAssignedAtDesc(studentId, WorkoutStatus.ACTIVE);

        verifyNoInteractions(
                workoutRepository,
                workoutExerciseRepository,
                studentWorkoutMapper
        );
    }

    private Organization createOrganization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }

    private User createStudent(Long id, Organization organization) {
        return createUser(id, "Aluno Teste", UserRole.STUDENT, organization);
    }

    private User createTeacher(Long id, Organization organization) {
        return createUser(id, "Professor Teste", UserRole.TEACHER, organization);
    }

    private User createUser(
            Long id,
            String name,
            UserRole role,
            Organization organization
    ) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setRole(role);
        user.setOrganization(organization);
        return user;
    }

    private Workout createWorkout(Long id, User teacher, String workoutName) {
        Workout workout = new Workout();
        workout.setId(id);
        workout.setTeacher(teacher);
        workout.setWorkoutName(workoutName);
        workout.setStatus(WorkoutStatus.ACTIVE);
        return workout;
    }

    private StudentWorkout createStudentWorkout(
            Long id,
            User student,
            Workout workout,
            WorkoutStatus status,
            LocalDateTime assignedAt
    ) {
        StudentWorkout studentWorkout = new StudentWorkout();
        studentWorkout.setId(id);
        studentWorkout.setStudent(student);
        studentWorkout.setWorkout(workout);
        studentWorkout.setStatus(status);
        studentWorkout.setAssignedAt(assignedAt);
        return studentWorkout;
    }

    private StudentWorkoutResponse createStudentWorkoutResponse(
            Long studentWorkoutId,
            Long studentId,
            Long workoutId,
            String workoutName,
            LocalDateTime assignedAt,
            WorkoutStatus status
    ) {
        return new StudentWorkoutResponse(
                studentWorkoutId,
                studentId,
                "Aluno Teste",
                workoutId,
                workoutName,
                assignedAt,
                status,
                null,
                null
        );
    }

    private WorkoutExercise createWorkoutExercise(Long id, Workout workout) {
        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setId(id);
        workoutExercise.setWorkout(workout);
        return workoutExercise;
    }
}