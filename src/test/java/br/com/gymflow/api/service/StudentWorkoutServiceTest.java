package br.com.gymflow.api.service;

import br.com.gymflow.api.config.security.StudentAccessValidator;
import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.domain.StudentWorkout;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.Workout;
import br.com.gymflow.api.domain.WorkoutExercise;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.domain.enums.WeekDay;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(
                workoutId,
                WeekDay.MONDAY
        );

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
                assignedAt,
                WeekDay.MONDAY
        );

        StudentWorkoutResponse expectedResponse = createStudentWorkoutResponse(
                50L,
                studentId,
                workoutId,
                "Treino A",
                assignedAt,
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE
        );

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(studentWorkoutRepository.findByStudentIdAndWorkoutIdAndWeekDay(
                studentId,
                workoutId,
                WeekDay.MONDAY
        )).thenReturn(Optional.empty());
        when(studentWorkoutRepository.existsByStudentIdAndWeekDayAndStatus(
                studentId,
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE
        )).thenReturn(false);
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
        assertEquals("Professor Teste", response.teacherName());
        assertEquals(WeekDay.MONDAY, response.weekDay());
        assertEquals(WorkoutStatus.ACTIVE, response.status());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(workoutRepository).findById(workoutId);
        verify(studentWorkoutRepository).findByStudentIdAndWorkoutIdAndWeekDay(
                studentId,
                workoutId,
                WeekDay.MONDAY
        );
        verify(studentWorkoutRepository).existsByStudentIdAndWeekDayAndStatus(
                studentId,
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE
        );
        verify(studentWorkoutMapper).toEntity(request);
        verify(studentWorkoutRepository).save(any(StudentWorkout.class));
        verify(studentWorkoutMapper).toResponse(savedStudentWorkout);
        verify(studentWorkoutRepository, never()).saveAll(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenStudentDoesNotExist() {
        Long studentId = 1L;
        Long workoutId = 10L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(
                workoutId,
                WeekDay.MONDAY
        );

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

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(
                workoutId,
                WeekDay.MONDAY
        );

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

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(
                workoutId,
                WeekDay.MONDAY
        );

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

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(
                workoutId,
                WeekDay.MONDAY
        );

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
    void shouldThrowDuplicateResourceExceptionWhenStudentAlreadyHasWorkoutAssignedForSameWeekDay() {
        Long studentId = 1L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(
                workoutId,
                WeekDay.MONDAY
        );

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        StudentWorkout activeStudentWorkout = createStudentWorkout(
                50L,
                student,
                workout,
                WorkoutStatus.ACTIVE,
                LocalDateTime.now(),
                WeekDay.MONDAY
        );

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(studentWorkoutRepository.findByStudentIdAndWorkoutIdAndWeekDay(
                studentId,
                workoutId,
                WeekDay.MONDAY
        )).thenReturn(Optional.of(activeStudentWorkout));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> studentWorkoutService.create(studentId, request)
        );

        assertEquals("Student already has this workout assigned for this week day", exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(workoutRepository).findById(workoutId);
        verify(studentWorkoutRepository).findByStudentIdAndWorkoutIdAndWeekDay(
                studentId,
                workoutId,
                WeekDay.MONDAY
        );

        verifyNoInteractions(
                studentWorkoutMapper,
                workoutExerciseRepository
        );

        verify(studentWorkoutRepository, never()).existsByStudentIdAndWeekDayAndStatus(any(), any(), any());
        verify(studentWorkoutRepository, never()).save(any(StudentWorkout.class));
        verify(studentWorkoutRepository, never()).saveAll(any());
    }

    @Test
    void shouldThrowDuplicateResourceExceptionWhenStudentAlreadyHasActiveWorkoutForSameWeekDay() {
        Long studentId = 1L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(
                workoutId,
                WeekDay.MONDAY
        );

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino Novo");

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(studentWorkoutRepository.findByStudentIdAndWorkoutIdAndWeekDay(
                studentId,
                workoutId,
                WeekDay.MONDAY
        )).thenReturn(Optional.empty());
        when(studentWorkoutRepository.existsByStudentIdAndWeekDayAndStatus(
                studentId,
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE
        )).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> studentWorkoutService.create(studentId, request)
        );

        assertEquals("Student already has an active workout for this week day", exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(workoutRepository).findById(workoutId);
        verify(studentWorkoutRepository).findByStudentIdAndWorkoutIdAndWeekDay(
                studentId,
                workoutId,
                WeekDay.MONDAY
        );
        verify(studentWorkoutRepository).existsByStudentIdAndWeekDayAndStatus(
                studentId,
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE
        );

        verify(studentWorkoutMapper, never()).toEntity(request);
        verify(studentWorkoutRepository, never()).save(any(StudentWorkout.class));
        verify(studentWorkoutRepository, never()).saveAll(any());
        verifyNoInteractions(workoutExerciseRepository);
    }

    @Test
    void shouldCreateMultipleActiveStudentWorkoutsOnDifferentWeekDays() {
        Long studentId = 1L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(
                workoutId,
                WeekDay.WEDNESDAY
        );

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino B");

        StudentWorkout savedStudentWorkout = createStudentWorkout(
                50L,
                student,
                workout,
                WorkoutStatus.ACTIVE,
                LocalDateTime.now(),
                WeekDay.WEDNESDAY
        );

        StudentWorkoutResponse expectedResponse = createStudentWorkoutResponse(
                50L,
                studentId,
                workoutId,
                "Treino B",
                savedStudentWorkout.getAssignedAt(),
                WeekDay.WEDNESDAY,
                WorkoutStatus.ACTIVE
        );

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(studentWorkoutRepository.findByStudentIdAndWorkoutIdAndWeekDay(
                studentId,
                workoutId,
                WeekDay.WEDNESDAY
        )).thenReturn(Optional.empty());
        when(studentWorkoutRepository.existsByStudentIdAndWeekDayAndStatus(
                studentId,
                WeekDay.WEDNESDAY,
                WorkoutStatus.ACTIVE
        )).thenReturn(false);
        when(studentWorkoutMapper.toEntity(request)).thenReturn(new StudentWorkout());
        when(studentWorkoutRepository.save(any(StudentWorkout.class))).thenReturn(savedStudentWorkout);
        when(studentWorkoutMapper.toResponse(savedStudentWorkout)).thenReturn(expectedResponse);

        StudentWorkoutResponse response = studentWorkoutService.create(studentId, request);

        assertNotNull(response);
        assertEquals(WorkoutStatus.ACTIVE, response.status());
        assertEquals(WeekDay.WEDNESDAY, response.weekDay());

        verify(studentWorkoutRepository).existsByStudentIdAndWeekDayAndStatus(
                studentId,
                WeekDay.WEDNESDAY,
                WorkoutStatus.ACTIVE
        );
        verify(studentWorkoutRepository).save(any(StudentWorkout.class));
        verify(studentWorkoutRepository, never()).saveAll(any());
    }

    @Test
    void shouldReactivateInactiveStudentWorkoutWithoutDeactivatingDifferentWeekDayWorkout() {
        Long studentId = 1L;
        Long workoutId = 10L;
        Long organizationId = 100L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(
                workoutId,
                WeekDay.MONDAY
        );

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workoutToReactivate = createWorkout(workoutId, teacher, "Treino A");

        StudentWorkout inactiveStudentWorkout = createStudentWorkout(
                50L,
                student,
                workoutToReactivate,
                WorkoutStatus.INACTIVE,
                LocalDateTime.now().minusDays(3),
                WeekDay.MONDAY
        );

        StudentWorkoutResponse expectedResponse = createStudentWorkoutResponse(
                50L,
                studentId,
                workoutId,
                "Treino A",
                LocalDateTime.now(),
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE
        );

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workoutToReactivate));
        when(studentWorkoutRepository.findByStudentIdAndWorkoutIdAndWeekDay(
                studentId,
                workoutId,
                WeekDay.MONDAY
        )).thenReturn(Optional.of(inactiveStudentWorkout));
        when(studentWorkoutRepository.existsByStudentIdAndWeekDayAndStatusAndIdNot(
                studentId,
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE,
                50L
        )).thenReturn(false);
        when(studentWorkoutRepository.save(inactiveStudentWorkout)).thenReturn(inactiveStudentWorkout);
        when(studentWorkoutMapper.toResponse(inactiveStudentWorkout)).thenReturn(expectedResponse);

        StudentWorkoutResponse response = studentWorkoutService.create(studentId, request);

        assertNotNull(response);
        assertEquals(50L, response.studentWorkoutId());
        assertEquals(WorkoutStatus.ACTIVE, inactiveStudentWorkout.getStatus());
        assertEquals(WeekDay.MONDAY, inactiveStudentWorkout.getWeekDay());

        verify(studentWorkoutRepository).existsByStudentIdAndWeekDayAndStatusAndIdNot(
                studentId,
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE,
                50L
        );
        verify(studentWorkoutRepository).save(inactiveStudentWorkout);
        verify(studentWorkoutRepository, never()).saveAll(any());
        verify(studentWorkoutMapper, never()).toEntity(request);
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

        StudentWorkout studentWorkoutA = createStudentWorkout(
                1000L,
                student,
                workoutA,
                WorkoutStatus.INACTIVE,
                assignedAtA,
                WeekDay.MONDAY
        );

        StudentWorkout studentWorkoutB = createStudentWorkout(
                2000L,
                student,
                workoutB,
                WorkoutStatus.ACTIVE,
                assignedAtB,
                WeekDay.WEDNESDAY
        );

        StudentWorkoutResponse responseA = createStudentWorkoutResponse(
                1000L,
                studentId,
                10L,
                "Treino A",
                assignedAtA,
                WeekDay.MONDAY,
                WorkoutStatus.INACTIVE
        );

        StudentWorkoutResponse responseB = createStudentWorkoutResponse(
                2000L,
                studentId,
                20L,
                "Treino B",
                assignedAtB,
                WeekDay.WEDNESDAY,
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
        assertEquals(WeekDay.MONDAY, response.get(0).weekDay());
        assertEquals(WorkoutStatus.INACTIVE, response.get(0).status());

        assertEquals(2000L, response.get(1).studentWorkoutId());
        assertEquals("Aluno Teste", response.get(1).studentName());
        assertEquals("Treino B", response.get(1).workoutName());
        assertEquals(WeekDay.WEDNESDAY, response.get(1).weekDay());
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

        StudentWorkout studentWorkout = createStudentWorkout(
                studentWorkoutId,
                student,
                workout,
                WorkoutStatus.ACTIVE,
                assignedAt,
                WeekDay.MONDAY
        );

        StudentWorkoutResponse expectedResponse = createStudentWorkoutResponse(
                studentWorkoutId,
                studentId,
                workoutId,
                "Treino A",
                assignedAt,
                WeekDay.MONDAY,
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
        assertEquals(WeekDay.MONDAY, response.weekDay());
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

        StudentWorkout studentWorkout = createStudentWorkout(
                studentWorkoutId,
                student,
                workout,
                WorkoutStatus.ACTIVE,
                assignedAt,
                WeekDay.MONDAY
        );

        StudentWorkout updatedStudentWorkout = createStudentWorkout(
                studentWorkoutId,
                student,
                workout,
                WorkoutStatus.INACTIVE,
                assignedAt,
                WeekDay.MONDAY
        );

        StudentWorkoutResponse expectedResponse = createStudentWorkoutResponse(
                studentWorkoutId,
                studentId,
                workoutId,
                "Treino A",
                assignedAt,
                WeekDay.MONDAY,
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
        assertEquals(WeekDay.MONDAY, response.weekDay());
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
    void shouldPatchStudentWorkoutToActiveWhenThereIsNoActiveWorkoutForSameWeekDay() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
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
                LocalDateTime.now().minusDays(2),
                WeekDay.MONDAY
        );

        StudentWorkoutResponse expectedResponse = createStudentWorkoutResponse(
                studentWorkoutId,
                studentId,
                workoutId,
                "Treino A",
                studentWorkoutToActivate.getAssignedAt(),
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE
        );

        when(studentWorkoutRepository.findById(studentWorkoutId)).thenReturn(Optional.of(studentWorkoutToActivate));
        when(studentWorkoutRepository.existsByStudentIdAndWeekDayAndStatusAndIdNot(
                studentId,
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE,
                studentWorkoutId
        )).thenReturn(false);
        when(studentWorkoutRepository.save(studentWorkoutToActivate)).thenReturn(studentWorkoutToActivate);
        when(studentWorkoutMapper.toResponse(studentWorkoutToActivate)).thenReturn(expectedResponse);

        StudentWorkoutResponse response = studentWorkoutService.patch(studentId, studentWorkoutId, request);

        assertNotNull(response);
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(studentId, response.studentId());
        assertEquals("Aluno Teste", response.studentName());
        assertEquals(workoutId, response.workoutId());
        assertEquals(WeekDay.MONDAY, response.weekDay());
        assertEquals(WorkoutStatus.ACTIVE, response.status());
        assertEquals(WorkoutStatus.ACTIVE, studentWorkoutToActivate.getStatus());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(studentWorkoutRepository).findById(studentWorkoutId);
        verify(studentWorkoutRepository).existsByStudentIdAndWeekDayAndStatusAndIdNot(
                studentId,
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE,
                studentWorkoutId
        );
        verify(studentWorkoutRepository).save(studentWorkoutToActivate);
        verify(studentWorkoutMapper).toResponse(studentWorkoutToActivate);
        verify(studentWorkoutRepository, never()).saveAll(any());

        verifyNoInteractions(
                userRepository,
                workoutRepository,
                workoutExerciseRepository
        );
    }

    @Test
    void shouldThrowDuplicateResourceExceptionWhenPatchingStudentWorkoutToActiveAndSameWeekDayAlreadyHasActiveWorkout() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
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
                LocalDateTime.now().minusDays(2),
                WeekDay.MONDAY
        );

        when(studentWorkoutRepository.findById(studentWorkoutId)).thenReturn(Optional.of(studentWorkoutToActivate));
        when(studentWorkoutRepository.existsByStudentIdAndWeekDayAndStatusAndIdNot(
                studentId,
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE,
                studentWorkoutId
        )).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> studentWorkoutService.patch(studentId, studentWorkoutId, request)
        );

        assertEquals("Student already has an active workout for this week day", exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(studentWorkoutRepository).findById(studentWorkoutId);
        verify(studentWorkoutRepository).existsByStudentIdAndWeekDayAndStatusAndIdNot(
                studentId,
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE,
                studentWorkoutId
        );
        verify(studentWorkoutRepository, never()).save(any(StudentWorkout.class));
        verify(studentWorkoutRepository, never()).saveAll(any());

        verifyNoInteractions(
                userRepository,
                workoutRepository,
                studentWorkoutMapper,
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
                LocalDateTime.now(),
                WeekDay.MONDAY
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
        WeekDay today = getTodayWeekDay();

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        LocalDateTime assignedAt = LocalDateTime.now();

        StudentWorkout studentWorkout = createStudentWorkout(
                studentWorkoutId,
                student,
                workout,
                WorkoutStatus.ACTIVE,
                assignedAt,
                today
        );

        WorkoutExercise workoutExerciseA = createWorkoutExercise(100L, workout);
        WorkoutExercise workoutExerciseB = createWorkoutExercise(200L, workout);

        List<WorkoutExercise> workoutExercises = List.of(workoutExerciseA, workoutExerciseB);

        StudentCurrentWorkoutResponse expectedResponse = new StudentCurrentWorkoutResponse(
                studentId,
                studentWorkoutId,
                workoutId,
                "Treino A",
                "Professor Teste",
                assignedAt,
                today,
                WorkoutStatus.ACTIVE,
                List.of()
        );

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentWorkoutRepository.findFirstByStudentIdAndStatusAndWeekDay(
                studentId,
                WorkoutStatus.ACTIVE,
                today
        )).thenReturn(Optional.of(studentWorkout));
        when(workoutExerciseRepository.findAllByWorkoutIdOrderByExerciseOrderAsc(workoutId))
                .thenReturn(workoutExercises);
        when(studentWorkoutMapper.toCurrentWorkoutResponse(studentWorkout, workoutExercises))
                .thenReturn(expectedResponse);

        StudentCurrentWorkoutResponse response = studentWorkoutService.findCurrentWorkout(studentId);

        assertNotNull(response);
        assertEquals(studentId, response.studentId());
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(workoutId, response.workoutId());
        assertEquals("Treino A", response.workoutName());
        assertEquals("Professor Teste", response.teacherName());
        assertEquals(today, response.weekDay());
        assertEquals(WorkoutStatus.ACTIVE, response.status());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(studentWorkoutRepository).findFirstByStudentIdAndStatusAndWeekDay(
                studentId,
                WorkoutStatus.ACTIVE,
                today
        );
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
    void shouldThrowResourceNotFoundExceptionWhenStudentDoesNotHaveActiveWorkoutForToday() {
        Long studentId = 1L;
        Long organizationId = 100L;
        WeekDay today = getTodayWeekDay();

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentWorkoutRepository.findFirstByStudentIdAndStatusAndWeekDay(
                studentId,
                WorkoutStatus.ACTIVE,
                today
        )).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.findCurrentWorkout(studentId)
        );

        assertEquals("Active workout not found student id: " + studentId, exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(studentWorkoutRepository).findFirstByStudentIdAndStatusAndWeekDay(
                studentId,
                WorkoutStatus.ACTIVE,
                today
        );

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
        WeekDay today = getTodayWeekDay();

        Organization organization = createOrganization(organizationId);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        LocalDateTime assignedAt = LocalDateTime.now();

        StudentWorkout studentWorkout = createStudentWorkout(
                studentWorkoutId,
                student,
                workout,
                WorkoutStatus.ACTIVE,
                assignedAt,
                today
        );

        List<WorkoutExercise> workoutExercises = List.of();

        StudentCurrentWorkoutResponse expectedResponse = new StudentCurrentWorkoutResponse(
                studentId,
                studentWorkoutId,
                workoutId,
                "Treino A",
                "Professor Teste",
                assignedAt,
                today,
                WorkoutStatus.ACTIVE,
                List.of()
        );

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentWorkoutRepository.findFirstByStudentIdAndStatusAndWeekDay(
                studentId,
                WorkoutStatus.ACTIVE,
                today
        )).thenReturn(Optional.of(studentWorkout));
        when(workoutExerciseRepository.findAllByWorkoutIdOrderByExerciseOrderAsc(workoutId))
                .thenReturn(workoutExercises);
        when(studentWorkoutMapper.toCurrentWorkoutResponse(studentWorkout, workoutExercises))
                .thenReturn(expectedResponse);

        StudentCurrentWorkoutResponse response = studentWorkoutService.findCurrentWorkout(studentId);

        assertNotNull(response);
        assertEquals(studentId, response.studentId());
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(workoutId, response.workoutId());
        assertEquals("Treino A", response.workoutName());
        assertEquals("Professor Teste", response.teacherName());
        assertEquals(today, response.weekDay());
        assertEquals(WorkoutStatus.ACTIVE, response.status());
        assertNotNull(response.exercises());
        assertEquals(0, response.exercises().size());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(studentWorkoutRepository).findFirstByStudentIdAndStatusAndWeekDay(
                studentId,
                WorkoutStatus.ACTIVE,
                today
        );
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
        WeekDay today = getTodayWeekDay();

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
                LocalDateTime.now(),
                today
        );

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentWorkoutRepository.findFirstByStudentIdAndStatusAndWeekDay(
                studentId,
                WorkoutStatus.ACTIVE,
                today
        )).thenReturn(Optional.of(studentWorkout));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutService.findCurrentWorkout(studentId)
        );

        assertEquals("Active workout not found student id: " + studentId, exception.getMessage());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(userRepository).findById(studentId);
        verify(studentWorkoutRepository).findFirstByStudentIdAndStatusAndWeekDay(
                studentId,
                WorkoutStatus.ACTIVE,
                today
        );

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
            LocalDateTime assignedAt,
            WeekDay weekDay
    ) {
        StudentWorkout studentWorkout = new StudentWorkout();
        studentWorkout.setId(id);
        studentWorkout.setStudent(student);
        studentWorkout.setWorkout(workout);
        studentWorkout.setStatus(status);
        studentWorkout.setAssignedAt(assignedAt);
        studentWorkout.setWeekDay(weekDay);
        return studentWorkout;
    }

    private StudentWorkoutResponse createStudentWorkoutResponse(
            Long studentWorkoutId,
            Long studentId,
            Long workoutId,
            String workoutName,
            LocalDateTime assignedAt,
            WeekDay weekDay,
            WorkoutStatus status
    ) {
        return new StudentWorkoutResponse(
                studentWorkoutId,
                studentId,
                "Aluno Teste",
                workoutId,
                workoutName,
                "Professor Teste",
                assignedAt,
                weekDay,
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

    private WeekDay getTodayWeekDay() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();

        return switch (dayOfWeek) {
            case MONDAY -> WeekDay.MONDAY;
            case TUESDAY -> WeekDay.TUESDAY;
            case WEDNESDAY -> WeekDay.WEDNESDAY;
            case THURSDAY -> WeekDay.THURSDAY;
            case FRIDAY -> WeekDay.FRIDAY;
            case SATURDAY -> WeekDay.SATURDAY;
            case SUNDAY -> WeekDay.SUNDAY;
        };
    }
}