package br.com.gymflow.api.service;

import br.com.gymflow.api.config.security.StudentAccessValidator;
import br.com.gymflow.api.domain.*;
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
        //Arrange
        Long studentId = 1L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(workoutId);

        Organization organization = new Organization();
        organization.setId(organizationId);

        User student = new User();
        student.setId(studentId);
        student.setRole(UserRole.STUDENT);
        student.setOrganization(organization);

        User teacher = new User();
        teacher.setId(2L);
        teacher.setRole(UserRole.TEACHER);
        teacher.setOrganization(organization);

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setTeacher(teacher);
        workout.setWorkoutName("Treino A");

        LocalDateTime assignedAt = LocalDateTime.now();

        StudentWorkout savedStudentWorkout = new StudentWorkout();
        savedStudentWorkout.setId(50L);
        savedStudentWorkout.setStudent(student);
        savedStudentWorkout.setWorkout(workout);
        savedStudentWorkout.setStatus(WorkoutStatus.ACTIVE);
        savedStudentWorkout.setAssignedAt(assignedAt);

        StudentWorkoutResponse expectedResponse = new StudentWorkoutResponse(
                50L,
                studentId,
                workoutId,
                "Treino A",
                assignedAt,
                WorkoutStatus.ACTIVE,
                null,
                null
        );

        when(userRepository.findById(studentId))
                .thenReturn(Optional.of(student));

        when(workoutRepository.findById(workoutId))
                   .thenReturn(Optional.of(workout));

        when(studentWorkoutRepository.existsByStudentIdAndWorkoutId(studentId, workoutId))
                .thenReturn(false);

        when(studentWorkoutRepository.findAllByStudentIdAndStatus(studentId, WorkoutStatus.ACTIVE))
                .thenReturn(List.of());

        when(studentWorkoutMapper.toEntity(request))
                .thenReturn(new StudentWorkout());

        when(studentWorkoutRepository.save(any(StudentWorkout.class)))
                .thenReturn(savedStudentWorkout);

        when(studentWorkoutMapper.toResponse(savedStudentWorkout))
                .thenReturn(expectedResponse);

        // Act
        StudentWorkoutResponse response = studentWorkoutService.create(studentId, request);

        // Assert
        assertNotNull(response);
        assertEquals(50L, response.studentWorkoutId());
        assertEquals(studentId, response.studentId());
        assertEquals(workoutId, response.workoutId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.status());

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
        // Arrange
        Long studentId = 1L;
        Long workoutId = 10L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(workoutId);

        when(userRepository.findById(studentId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.create(studentId, request)
        );

        assertEquals("Student not found with id: "
                + studentId, exception.getMessage()
        );

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
        //Arrange
        Long studentId = 1L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(workoutId);

        Organization organization = new Organization();
        organization.setId(organizationId);

        User student = new User();
        student.setId(studentId);
        student.setRole(UserRole.STUDENT);
        student.setOrganization(organization);

        when(userRepository.findById(studentId))
                .thenReturn(Optional.of(student));

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.empty());

        //Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.create(studentId, request)
        );

        assertEquals("Workout not found with id: "
                + workoutId, exception.getMessage()
        );

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
        // Arrange
        Long studentId = 1L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(workoutId);

        Organization organization = new Organization();
        organization.setId(organizationId);

        User user = new User();
        user.setId(studentId);
        user.setRole(UserRole.TEACHER);
        user.setOrganization(organization);

        when(userRepository.findById(studentId))
                .thenReturn(Optional.of(user));

        // Act + Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> studentWorkoutService.create(studentId, request)
        );

        assertEquals("User is not a student with id: "
                + studentId, exception.getMessage()
        );

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
        // Arrange
        Long studentId = 1L;
        Long workoutId = 10L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(workoutId);

        Organization studentOrganization = new Organization();
        studentOrganization.setId(100L);

        Organization teacherOrganization = new Organization();
        teacherOrganization.setId(200L);

        User student = new User();
        student.setId(studentId);
        student.setRole(UserRole.STUDENT);
        student.setOrganization(studentOrganization);

        User teacher = new User();
        teacher.setId(2L);
        teacher.setRole(UserRole.TEACHER);
        teacher.setOrganization(teacherOrganization);

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setTeacher(teacher);

        when(userRepository.findById(studentId))
                .thenReturn(Optional.of(student));

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        // Act + Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> studentWorkoutService.create(studentId, request)
        );

        assertEquals("Student does not belong to the same organization as the workout",
                exception.getMessage()
        );

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
        // Arrange
        Long studentId = 1L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(workoutId);

        Organization organization = new Organization();
        organization.setId(organizationId);

        User student = new User();
        student.setId(studentId);
        student.setRole(UserRole.STUDENT);
        student.setOrganization(organization);

        User teacher = new User();
        teacher.setId(2L);
        teacher.setRole(UserRole.TEACHER);
        teacher.setOrganization(organization);

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setTeacher(teacher);

        when(userRepository.findById(studentId))
                .thenReturn(Optional.of(student));

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        when(studentWorkoutRepository.existsByStudentIdAndWorkoutId(studentId, workoutId))
                .thenReturn(true);

        // Act + Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> studentWorkoutService.create(studentId, request)
        );

        assertEquals("Student already has this workout assigned",
                exception.getMessage()
        );

        verify(userRepository).findById(studentId);
        verify(workoutRepository).findById(workoutId);
        verify(studentWorkoutRepository).existsByStudentIdAndWorkoutId(studentId, workoutId);

        verifyNoInteractions(
                studentWorkoutMapper,
                workoutExerciseRepository
        );

        verify(studentWorkoutRepository, never())
                .findAllByStudentIdAndStatus(studentId, WorkoutStatus.ACTIVE);

        verify(studentWorkoutRepository, never())
                .save(any(StudentWorkout.class));
    }

    @Test
    void shouldDeactivateCurrentActiveWorkoutsWhenCreatingNewStudentWorkout() {
        // Arrange
        Long studentId = 1L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(workoutId);

        Organization organization = new Organization();
        organization.setId(organizationId);

        User student = new User();
        student.setId(studentId);
        student.setRole(UserRole.STUDENT);
        student.setOrganization(organization);

        User teacher = new User();
        teacher.setId(2L);
        teacher.setRole(UserRole.TEACHER);
        teacher.setOrganization(organization);

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setTeacher(teacher);
        workout.setWorkoutName("Treino Novo");

        Workout oldWorkout = new Workout();
        oldWorkout.setId(99L);
        oldWorkout.setTeacher(teacher);
        oldWorkout.setWorkoutName("Treino Antigo");

        StudentWorkout activeStudentWorkout = new StudentWorkout();
        activeStudentWorkout.setId(20L);
        activeStudentWorkout.setStudent(student);
        activeStudentWorkout.setWorkout(oldWorkout);
        activeStudentWorkout.setStatus(WorkoutStatus.ACTIVE);
        activeStudentWorkout.setAssignedAt(LocalDateTime.now().minusDays(1));

        StudentWorkout newStudentWorkout = new StudentWorkout();

        StudentWorkout savedStudentWorkout = new StudentWorkout();
        savedStudentWorkout.setId(50L);
        savedStudentWorkout.setStudent(student);
        savedStudentWorkout.setWorkout(workout);
        savedStudentWorkout.setStatus(WorkoutStatus.ACTIVE);
        savedStudentWorkout.setAssignedAt(LocalDateTime.now());

        StudentWorkoutResponse expectedResponse = new StudentWorkoutResponse(
                50L,
                studentId,
                workoutId,
                "Treino Novo",
                savedStudentWorkout.getAssignedAt(),
                WorkoutStatus.ACTIVE,
                null,
                null
        );

        when(userRepository.findById(studentId))
                .thenReturn(Optional.of(student));

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        when(studentWorkoutRepository.existsByStudentIdAndWorkoutId(studentId, workoutId))
                .thenReturn(false);

        when(studentWorkoutRepository.findAllByStudentIdAndStatus(studentId, WorkoutStatus.ACTIVE))
                .thenReturn(List.of(activeStudentWorkout));

        when(studentWorkoutMapper.toEntity(request))
                .thenReturn(newStudentWorkout);

        when(studentWorkoutRepository.save(any(StudentWorkout.class)))
                .thenReturn(savedStudentWorkout);

        when(studentWorkoutMapper.toResponse(savedStudentWorkout))
                .thenReturn(expectedResponse);

        // Act
        StudentWorkoutResponse response = studentWorkoutService.create(studentId, request);

        // Assert
        assertNotNull(response);
        assertEquals(WorkoutStatus.ACTIVE, response.status());

        assertEquals(WorkoutStatus.INACTIVE, activeStudentWorkout.getStatus());

        verify(studentWorkoutRepository)
                .saveAll(List.of(activeStudentWorkout));

        verify(studentWorkoutRepository)
                .save(any(StudentWorkout.class));
    }

    @Test
    void shouldFindAllStudentWorkoutsByStudentIdSuccessfully() {
        // Arrange
        Long studentId = 1L;
        Long organizationId = 100L;

        Organization organization = new Organization();
        organization.setId(organizationId);

        User student = new User();
        student.setId(studentId);
        student.setRole(UserRole.STUDENT);
        student.setOrganization(organization);

        User teacher = new User();
        teacher.setId(2L);
        teacher.setRole(UserRole.TEACHER);
        teacher.setOrganization(organization);

        Workout workoutA = new Workout();
        workoutA.setId(10L);
        workoutA.setTeacher(teacher);
        workoutA.setWorkoutName("Treino A");

        Workout workoutB = new Workout();
        workoutB.setId(20L);
        workoutB.setTeacher(teacher);
        workoutB.setWorkoutName("Treino B");

        LocalDateTime assignedAtA = LocalDateTime.now().minusDays(2);
        LocalDateTime assignedAtB = LocalDateTime.now().minusDays(1);

        StudentWorkout studentWorkoutA = new StudentWorkout();
        studentWorkoutA.setId(1000L);
        studentWorkoutA.setStudent(student);
        studentWorkoutA.setWorkout(workoutA);
        studentWorkoutA.setAssignedAt(assignedAtA);
        studentWorkoutA.setStatus(WorkoutStatus.INACTIVE);

        StudentWorkout studentWorkoutB = new StudentWorkout();
        studentWorkoutB.setId(2000L);
        studentWorkoutB.setStudent(student);
        studentWorkoutB.setWorkout(workoutB);
        studentWorkoutB.setAssignedAt(assignedAtB);
        studentWorkoutB.setStatus(WorkoutStatus.ACTIVE);

        StudentWorkoutResponse responseA = new StudentWorkoutResponse(
                1000L,
                studentId,
                10L,
                "Treino A",
                assignedAtA,
                WorkoutStatus.INACTIVE,
                null,
                null
        );

        StudentWorkoutResponse responseB = new StudentWorkoutResponse(
                2000L,
                studentId,
                20L,
                "Treino B",
                assignedAtB,
                WorkoutStatus.ACTIVE,
                null,
                null
        );

        when(userRepository.findById(studentId))
                .thenReturn(Optional.of(student));

        when(studentWorkoutRepository.findAllByStudentId(studentId))
                .thenReturn(List.of(studentWorkoutA, studentWorkoutB));

        when(studentWorkoutMapper.toResponse(studentWorkoutA))
                .thenReturn(responseA);

        when(studentWorkoutMapper.toResponse(studentWorkoutB))
                .thenReturn(responseB);

        // Act
        List<StudentWorkoutResponse> response = studentWorkoutService.findAllByStudentId(studentId);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(1000L, response.get(0).studentWorkoutId());
        assertEquals("Treino A", response.get(0).workoutName());
        assertEquals(WorkoutStatus.INACTIVE, response.get(0).status());

        assertEquals(2000L, response.get(1).studentWorkoutId());
        assertEquals("Treino B", response.get(1).workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.get(1).status());

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
        // Arrange
        Long studentId = 1L;

        when(userRepository.findById(studentId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.findAllByStudentId(studentId)
        );

        assertEquals(
                "Student not found with id: " + studentId,
                exception.getMessage()
        );

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
        // Arrange
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        Organization organization = new Organization();
        organization.setId(organizationId);

        User student = new User();
        student.setId(studentId);
        student.setRole(UserRole.STUDENT);
        student.setOrganization(organization);

        User teacher = new User();
        teacher.setId(2L);
        teacher.setRole(UserRole.TEACHER);
        teacher.setOrganization(organization);

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setTeacher(teacher);
        workout.setWorkoutName("Treino A");

        LocalDateTime assignedAt = LocalDateTime.now();

        StudentWorkout studentWorkout = new StudentWorkout();
        studentWorkout.setId(studentWorkoutId);
        studentWorkout.setStudent(student);
        studentWorkout.setWorkout(workout);
        studentWorkout.setAssignedAt(assignedAt);
        studentWorkout.setStatus(WorkoutStatus.ACTIVE);

        StudentWorkoutResponse expectedResponse = new StudentWorkoutResponse(
                studentWorkoutId,
                studentId,
                workoutId,
                "Treino A",
                assignedAt,
                WorkoutStatus.ACTIVE,
                null,
                null
        );

        when(studentWorkoutRepository.findById(studentWorkoutId))
                .thenReturn(Optional.of(studentWorkout));

        when(studentWorkoutMapper.toResponse(studentWorkout))
                .thenReturn(expectedResponse);

        // Act
        StudentWorkoutResponse response = studentWorkoutService.findById(studentId, studentWorkoutId);

        // Assert
        assertNotNull(response);
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(studentId, response.studentId());
        assertEquals(workoutId, response.workoutId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.status());

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
        // Arrange
        Long studentId = 1L;
        Long studentWorkoutId = 50L;

        when(studentWorkoutRepository.findById(studentWorkoutId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.findById(studentId, studentWorkoutId)
        );

        assertEquals(
                "Student workout not found with id: " + studentWorkoutId,
                exception.getMessage()
        );

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
        // Arrange
        Long requestedStudentId = 1L;
        Long ownerStudentId = 2L;
        Long studentWorkoutId = 50L;
        Long organizationId = 100L;

        Organization organization = new Organization();
        organization.setId(organizationId);

        User ownerStudent = new User();
        ownerStudent.setId(ownerStudentId);
        ownerStudent.setRole(UserRole.STUDENT);
        ownerStudent.setOrganization(organization);

        StudentWorkout studentWorkout = new StudentWorkout();
        studentWorkout.setId(studentWorkoutId);
        studentWorkout.setStudent(ownerStudent);

        when(studentWorkoutRepository.findById(studentWorkoutId))
                .thenReturn(Optional.of(studentWorkout));

        // Act + Assert
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
        // Arrange
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        PatchStudentWorkoutRequest request = new PatchStudentWorkoutRequest(WorkoutStatus.INACTIVE);

        Organization organization = new Organization();
        organization.setId(organizationId);

        User student = new User();
        student.setId(studentId);
        student.setRole(UserRole.STUDENT);
        student.setOrganization(organization);

        User teacher = new User();
        teacher.setId(2L);
        teacher.setRole(UserRole.TEACHER);
        teacher.setOrganization(organization);

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setTeacher(teacher);
        workout.setWorkoutName("Treino A");

        LocalDateTime assignedAt = LocalDateTime.now();

        StudentWorkout studentWorkout = new StudentWorkout();
        studentWorkout.setId(studentWorkoutId);
        studentWorkout.setStudent(student);
        studentWorkout.setWorkout(workout);
        studentWorkout.setAssignedAt(assignedAt);
        studentWorkout.setStatus(WorkoutStatus.ACTIVE);

        StudentWorkout updatedStudentWorkout = new StudentWorkout();
        updatedStudentWorkout.setId(studentWorkoutId);
        updatedStudentWorkout.setStudent(student);
        updatedStudentWorkout.setWorkout(workout);
        updatedStudentWorkout.setAssignedAt(assignedAt);
        updatedStudentWorkout.setStatus(WorkoutStatus.INACTIVE);

        StudentWorkoutResponse expectedResponse = new StudentWorkoutResponse(
                studentWorkoutId,
                studentId,
                workoutId,
                "Treino A",
                assignedAt,
                WorkoutStatus.INACTIVE,
                null,
                null
        );

        when(studentWorkoutRepository.findById(studentWorkoutId))
                .thenReturn(Optional.of(studentWorkout));

        when(studentWorkoutRepository.save(studentWorkout))
                .thenReturn(updatedStudentWorkout);

        when(studentWorkoutMapper.toResponse(updatedStudentWorkout))
                .thenReturn(expectedResponse);

        // Act
        StudentWorkoutResponse response = studentWorkoutService.patch(
                studentId,
                studentWorkoutId,
                request
        );

        // Assert
        assertNotNull(response);
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(studentId, response.studentId());
        assertEquals(workoutId, response.workoutId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(WorkoutStatus.INACTIVE, response.status());

        assertEquals(WorkoutStatus.INACTIVE, studentWorkout.getStatus());

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
        // Arrange
        Long studentId = 1L;
        Long studentWorkoutId = 50L;

        PatchStudentWorkoutRequest request = new PatchStudentWorkoutRequest(WorkoutStatus.INACTIVE);

        when(studentWorkoutRepository.findById(studentWorkoutId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.patch(studentId, studentWorkoutId, request)
        );

        assertEquals(
                "Student workout not found with id: " + studentWorkoutId,
                exception.getMessage()
        );

        verify(studentWorkoutRepository).findById(studentWorkoutId);

        verifyNoInteractions(
                userRepository,
                workoutRepository,
                studentWorkoutMapper,
                workoutExerciseRepository
        );

        verify(studentWorkoutRepository, never())
                .save(any(StudentWorkout.class));
    }

    @Test
    void shouldDeactivateOtherActiveWorkoutsWhenPatchingStudentWorkoutToActive() {
        // Arrange
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long otherStudentWorkoutId = 60L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        PatchStudentWorkoutRequest request = new PatchStudentWorkoutRequest(WorkoutStatus.ACTIVE);

        Organization organization = new Organization();
        organization.setId(organizationId);

        User student = new User();
        student.setId(studentId);
        student.setRole(UserRole.STUDENT);
        student.setOrganization(organization);

        User teacher = new User();
        teacher.setId(2L);
        teacher.setRole(UserRole.TEACHER);
        teacher.setOrganization(organization);

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setTeacher(teacher);
        workout.setWorkoutName("Treino A");

        StudentWorkout studentWorkoutToActivate = new StudentWorkout();
        studentWorkoutToActivate.setId(studentWorkoutId);
        studentWorkoutToActivate.setStudent(student);
        studentWorkoutToActivate.setWorkout(workout);
        studentWorkoutToActivate.setAssignedAt(LocalDateTime.now().minusDays(2));
        studentWorkoutToActivate.setStatus(WorkoutStatus.INACTIVE);

        StudentWorkout otherActiveStudentWorkout = new StudentWorkout();
        otherActiveStudentWorkout.setId(otherStudentWorkoutId);
        otherActiveStudentWorkout.setStudent(student);
        otherActiveStudentWorkout.setWorkout(workout);
        otherActiveStudentWorkout.setAssignedAt(LocalDateTime.now().minusDays(1));
        otherActiveStudentWorkout.setStatus(WorkoutStatus.ACTIVE);

        StudentWorkoutResponse expectedResponse = new StudentWorkoutResponse(
                studentWorkoutId,
                studentId,
                workoutId,
                "Treino A",
                studentWorkoutToActivate.getAssignedAt(),
                WorkoutStatus.ACTIVE,
                null,
                null
        );

        when(studentWorkoutRepository.findById(studentWorkoutId))
                .thenReturn(Optional.of(studentWorkoutToActivate));

        when(studentWorkoutRepository.findAllByStudentIdAndStatus(studentId, WorkoutStatus.ACTIVE))
                .thenReturn(List.of(otherActiveStudentWorkout));

        when(studentWorkoutRepository.save(studentWorkoutToActivate))
                .thenReturn(studentWorkoutToActivate);

        when(studentWorkoutMapper.toResponse(studentWorkoutToActivate))
                .thenReturn(expectedResponse);

        // Act
        StudentWorkoutResponse response = studentWorkoutService.patch(
                studentId,
                studentWorkoutId,
                request
        );

        // Assert
        assertNotNull(response);
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(studentId, response.studentId());
        assertEquals(workoutId, response.workoutId());
        assertEquals(WorkoutStatus.ACTIVE, response.status());

        assertEquals(WorkoutStatus.ACTIVE, studentWorkoutToActivate.getStatus());
        assertEquals(WorkoutStatus.INACTIVE, otherActiveStudentWorkout.getStatus());

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
        // Arrange
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        Organization organization = new Organization();
        organization.setId(organizationId);

        User student = new User();
        student.setId(studentId);
        student.setRole(UserRole.STUDENT);
        student.setOrganization(organization);

        User teacher = new User();
        teacher.setId(2L);
        teacher.setRole(UserRole.TEACHER);
        teacher.setOrganization(organization);

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setTeacher(teacher);
        workout.setWorkoutName("Treino A");

        StudentWorkout studentWorkout = new StudentWorkout();
        studentWorkout.setId(studentWorkoutId);
        studentWorkout.setStudent(student);
        studentWorkout.setWorkout(workout);
        studentWorkout.setAssignedAt(LocalDateTime.now());
        studentWorkout.setStatus(WorkoutStatus.ACTIVE);

        when(studentWorkoutRepository.findById(studentWorkoutId))
                .thenReturn(Optional.of(studentWorkout));

        // Act
        studentWorkoutService.delete(studentId, studentWorkoutId);

        // Assert
        assertEquals(WorkoutStatus.INACTIVE, studentWorkout.getStatus());

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
        // Arrange
        Long studentId = 1L;
        Long studentWorkoutId = 50L;

        when(studentWorkoutRepository.findById(studentWorkoutId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.delete(studentId, studentWorkoutId)
        );

        assertEquals(
                "Student workout not found with id: " + studentWorkoutId,
                exception.getMessage()
        );

        verify(studentWorkoutRepository).findById(studentWorkoutId);

        verifyNoInteractions(
                userRepository,
                workoutRepository,
                studentWorkoutMapper,
                workoutExerciseRepository
        );

        verify(studentWorkoutRepository, never())
                .save(any(StudentWorkout.class));
    }

    @Test
    void shouldFindCurrentWorkoutSuccessfully() {
        // Arrange
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        Organization organization = new Organization();
        organization.setId(organizationId);

        User student = new User();
        student.setId(studentId);
        student.setRole(UserRole.STUDENT);
        student.setOrganization(organization);

        User teacher = new User();
        teacher.setId(2L);
        teacher.setRole(UserRole.TEACHER);
        teacher.setOrganization(organization);

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setTeacher(teacher);
        workout.setWorkoutName("Treino A");

        LocalDateTime assignedAt = LocalDateTime.now();

        StudentWorkout studentWorkout = new StudentWorkout();
        studentWorkout.setId(studentWorkoutId);
        studentWorkout.setStudent(student);
        studentWorkout.setWorkout(workout);
        studentWorkout.setAssignedAt(assignedAt);
        studentWorkout.setStatus(WorkoutStatus.ACTIVE);

        WorkoutExercise workoutExerciseA = new WorkoutExercise();
        workoutExerciseA.setId(100L);
        workoutExerciseA.setWorkout(workout);

        WorkoutExercise workoutExerciseB = new WorkoutExercise();
        workoutExerciseB.setId(200L);
        workoutExerciseB.setWorkout(workout);

        List<WorkoutExercise> workoutExercises = List.of(
                workoutExerciseA,
                workoutExerciseB
        );

        StudentCurrentWorkoutResponse expectedResponse = new StudentCurrentWorkoutResponse(
                studentId,
                studentWorkoutId,
                workoutId,
                "Treino A",
                assignedAt,
                WorkoutStatus.ACTIVE,
                List.of()
        );

        when(userRepository.findById(studentId))
                .thenReturn(Optional.of(student));

        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                studentId,
                WorkoutStatus.ACTIVE
        )).thenReturn(Optional.of(studentWorkout));

        when(workoutExerciseRepository.findAllByWorkoutIdOrderByExerciseOrderAsc(workoutId))
                .thenReturn(workoutExercises);

        when(studentWorkoutMapper.toCurrentWorkoutResponse(studentWorkout, workoutExercises))
                .thenReturn(expectedResponse);

        // Act
        StudentCurrentWorkoutResponse response = studentWorkoutService.findCurrentWorkout(studentId);

        // Assert
        assertNotNull(response);
        assertEquals(studentId, response.studentId());
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(workoutId, response.workoutId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.status());

        verify(userRepository).findById(studentId);

        verify(studentWorkoutRepository)
                .findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                        studentId,
                        WorkoutStatus.ACTIVE
                );

        verify(workoutExerciseRepository)
                .findAllByWorkoutIdOrderByExerciseOrderAsc(workoutId);

        verify(studentWorkoutMapper)
                .toCurrentWorkoutResponse(studentWorkout, workoutExercises);

        verifyNoInteractions(workoutRepository);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenStudentDoesNotExistOnFindCurrentWorkout() {
        // Arrange
        Long studentId = 1L;

        when(userRepository.findById(studentId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.findCurrentWorkout(studentId)
        );

        assertEquals(
                "Student not found with id: " + studentId,
                exception.getMessage()
        );

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
        // Arrange
        Long studentId = 1L;
        Long organizationId = 100L;

        Organization organization = new Organization();
        organization.setId(organizationId);

        User student = new User();
        student.setId(studentId);
        student.setRole(UserRole.STUDENT);
        student.setOrganization(organization);

        when(userRepository.findById(studentId))
                .thenReturn(Optional.of(student));

        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                studentId,
                WorkoutStatus.ACTIVE
        )).thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.findCurrentWorkout(studentId)
        );

        assertEquals(
                "Active workout not found student id: " + studentId,
                exception.getMessage()
        );

        verify(userRepository).findById(studentId);

        verify(studentWorkoutRepository)
                .findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                        studentId,
                        WorkoutStatus.ACTIVE
                );

        verifyNoInteractions(
                workoutRepository,
                studentWorkoutMapper,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldFindCurrentWorkoutWithEmptyExerciseListWhenWorkoutHasNoExercises() {
        // Arrange
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        Organization organization = new Organization();
        organization.setId(organizationId);

        User student = new User();
        student.setId(studentId);
        student.setRole(UserRole.STUDENT);
        student.setOrganization(organization);

        User teacher = new User();
        teacher.setId(2L);
        teacher.setRole(UserRole.TEACHER);
        teacher.setOrganization(organization);

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setTeacher(teacher);
        workout.setWorkoutName("Treino A");

        LocalDateTime assignedAt = LocalDateTime.now();

        StudentWorkout studentWorkout = new StudentWorkout();
        studentWorkout.setId(studentWorkoutId);
        studentWorkout.setStudent(student);
        studentWorkout.setWorkout(workout);
        studentWorkout.setAssignedAt(assignedAt);
        studentWorkout.setStatus(WorkoutStatus.ACTIVE);

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

        when(userRepository.findById(studentId))
                .thenReturn(Optional.of(student));

        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                studentId,
                WorkoutStatus.ACTIVE
        )).thenReturn(Optional.of(studentWorkout));

        when(workoutExerciseRepository.findAllByWorkoutIdOrderByExerciseOrderAsc(workoutId))
                .thenReturn(workoutExercises);

        when(studentWorkoutMapper.toCurrentWorkoutResponse(studentWorkout, workoutExercises))
                .thenReturn(expectedResponse);

        // Act
        StudentCurrentWorkoutResponse response = studentWorkoutService.findCurrentWorkout(studentId);

        // Assert
        assertNotNull(response);
        assertEquals(studentId, response.studentId());
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(workoutId, response.workoutId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(WorkoutStatus.ACTIVE, response.status());
        assertNotNull(response.exercises());
        assertEquals(0, response.exercises().size());

        verify(userRepository).findById(studentId);

        verify(studentWorkoutRepository)
                .findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                        studentId,
                        WorkoutStatus.ACTIVE
                );

        verify(workoutExerciseRepository)
                .findAllByWorkoutIdOrderByExerciseOrderAsc(workoutId);

        verify(studentWorkoutMapper)
                .toCurrentWorkoutResponse(studentWorkout, workoutExercises);

        verifyNoInteractions(workoutRepository);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenCurrentWorkoutIsInactive() {
        // Arrange
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        Organization organization = new Organization();
        organization.setId(organizationId);

        User student = new User();
        student.setId(studentId);
        student.setRole(UserRole.STUDENT);
        student.setOrganization(organization);

        User teacher = new User();
        teacher.setId(2L);
        teacher.setRole(UserRole.TEACHER);
        teacher.setOrganization(organization);

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setTeacher(teacher);
        workout.setWorkoutName("Treino A");
        workout.setStatus(WorkoutStatus.INACTIVE);

        StudentWorkout studentWorkout = new StudentWorkout();
        studentWorkout.setId(studentWorkoutId);
        studentWorkout.setStudent(student);
        studentWorkout.setWorkout(workout);
        studentWorkout.setAssignedAt(LocalDateTime.now());
        studentWorkout.setStatus(WorkoutStatus.ACTIVE);

        when(userRepository.findById(studentId))
                .thenReturn(Optional.of(student));

        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                studentId,
                WorkoutStatus.ACTIVE
        )).thenReturn(Optional.of(studentWorkout));

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.findCurrentWorkout(studentId)
        );

        assertEquals(
                "Active workout not found student id: " + studentId,
                exception.getMessage()
        );

        verify(userRepository).findById(studentId);

        verify(studentWorkoutRepository)
                .findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                        studentId,
                        WorkoutStatus.ACTIVE
                );

        verifyNoInteractions(
                workoutRepository,
                workoutExerciseRepository,
                studentWorkoutMapper
        );
    }
}