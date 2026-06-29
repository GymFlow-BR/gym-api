package br.com.gymflow.api.service;

import br.com.gymflow.api.config.security.StudentAccessValidator;
import br.com.gymflow.api.domain.*;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.domain.enums.WorkoutStatus;
import br.com.gymflow.api.dto.studentWorkoutProgress.StudentCurrentWorkoutProgressResponse;
import br.com.gymflow.api.dto.studentWorkoutProgress.StudentWorkoutExerciseProgressResponse;
import br.com.gymflow.api.event.StudentWorkoutExerciseCompletedEvent;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.repository.StudentWorkoutExerciseProgressRepository;
import br.com.gymflow.api.repository.StudentWorkoutRepository;
import br.com.gymflow.api.repository.WorkoutExerciseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentWorkoutProgressServiceTest {

    @Mock
    private StudentWorkoutRepository studentWorkoutRepository;

    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Mock
    private StudentWorkoutExerciseProgressRepository progressRepository;

    @Mock
    private StudentAccessValidator studentAccessValidator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private StudentWorkoutProgressService studentWorkoutProgressService;

    @Test
    void shouldCompleteExerciseSuccessfullyCreatingProgressWhenItDoesNotExist() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long workoutExerciseId = 100L;

        Organization organization = createOrganization(1L);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");
        StudentWorkout studentWorkout = createStudentWorkout(studentWorkoutId, student, workout);
        Exercise exercise = createExercise(20L, organization, "Supino reto");
        WorkoutExercise workoutExercise = createWorkoutExercise(workoutExerciseId, workout, exercise, 1);

        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                studentId,
                WorkoutStatus.ACTIVE
        )).thenReturn(Optional.of(studentWorkout));

        when(workoutExerciseRepository.findById(workoutExerciseId))
                .thenReturn(Optional.of(workoutExercise));

        when(progressRepository.findByStudentWorkoutIdAndWorkoutExerciseId(
                studentWorkoutId,
                workoutExerciseId
        )).thenReturn(Optional.empty());

        when(progressRepository.save(any(StudentWorkoutExerciseProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentWorkoutExerciseProgressResponse response =
                studentWorkoutProgressService.completeExercise(studentId, workoutExerciseId);

        assertNotNull(response);
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(workoutExerciseId, response.workoutExerciseId());
        assertTrue(response.completed());
        assertNotNull(response.completedAt());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(studentWorkoutRepository).findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                studentId,
                WorkoutStatus.ACTIVE
        );
        verify(workoutExerciseRepository).findById(workoutExerciseId);
        verify(progressRepository).findByStudentWorkoutIdAndWorkoutExerciseId(
                studentWorkoutId,
                workoutExerciseId
        );
        verify(progressRepository).save(any(StudentWorkoutExerciseProgress.class));

        ArgumentCaptor<StudentWorkoutExerciseCompletedEvent> eventCaptor =
                ArgumentCaptor.forClass(StudentWorkoutExerciseCompletedEvent.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        StudentWorkoutExerciseCompletedEvent event = eventCaptor.getValue();

        assertEquals(studentId, event.studentId());
        assertEquals(studentWorkoutId, event.studentWorkoutId());
        assertEquals(workoutId, event.workoutId());
        assertEquals(workoutExerciseId, event.workoutExerciseId());
        assertEquals(20L, event.exerciseId());
        assertEquals("Supino reto", event.exerciseName());
        assertNotNull(event.completedAt());
    }

    @Test
    void shouldCompleteExerciseSuccessfullyUpdatingExistingProgress() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long workoutExerciseId = 100L;

        Organization organization = createOrganization(1L);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");
        StudentWorkout studentWorkout = createStudentWorkout(studentWorkoutId, student, workout);
        Exercise exercise = createExercise(20L, organization, "Supino reto");
        WorkoutExercise workoutExercise = createWorkoutExercise(workoutExerciseId, workout, exercise, 1);

        StudentWorkoutExerciseProgress existingProgress = createProgress(
                500L,
                studentWorkout,
                workoutExercise,
                false,
                null
        );

        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                studentId,
                WorkoutStatus.ACTIVE
        )).thenReturn(Optional.of(studentWorkout));

        when(workoutExerciseRepository.findById(workoutExerciseId))
                .thenReturn(Optional.of(workoutExercise));

        when(progressRepository.findByStudentWorkoutIdAndWorkoutExerciseId(
                studentWorkoutId,
                workoutExerciseId
        )).thenReturn(Optional.of(existingProgress));

        when(progressRepository.save(existingProgress))
                .thenReturn(existingProgress);

        StudentWorkoutExerciseProgressResponse response =
                studentWorkoutProgressService.completeExercise(studentId, workoutExerciseId);

        assertTrue(response.completed());
        assertNotNull(response.completedAt());
        assertTrue(existingProgress.getCompleted());
        assertNotNull(existingProgress.getCompletedAt());

        verify(progressRepository).save(existingProgress);
    }

    @Test
    void shouldUncompleteExerciseSuccessfully() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;
        Long workoutExerciseId = 100L;

        Organization organization = createOrganization(1L);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");
        StudentWorkout studentWorkout = createStudentWorkout(studentWorkoutId, student, workout);
        Exercise exercise = createExercise(20L, organization, "Supino reto");
        WorkoutExercise workoutExercise = createWorkoutExercise(workoutExerciseId, workout, exercise, 1);

        StudentWorkoutExerciseProgress existingProgress = createProgress(
                500L,
                studentWorkout,
                workoutExercise,
                true,
                LocalDateTime.now()
        );

        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                studentId,
                WorkoutStatus.ACTIVE
        )).thenReturn(Optional.of(studentWorkout));

        when(workoutExerciseRepository.findById(workoutExerciseId))
                .thenReturn(Optional.of(workoutExercise));

        when(progressRepository.findByStudentWorkoutIdAndWorkoutExerciseId(
                studentWorkoutId,
                workoutExerciseId
        )).thenReturn(Optional.of(existingProgress));

        when(progressRepository.save(existingProgress))
                .thenReturn(existingProgress);

        StudentWorkoutExerciseProgressResponse response =
                studentWorkoutProgressService.uncompleteExercise(studentId, workoutExerciseId);

        assertFalse(response.completed());
        assertNull(response.completedAt());
        assertFalse(existingProgress.getCompleted());
        assertNull(existingProgress.getCompletedAt());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(progressRepository).save(existingProgress);
        verify(eventPublisher, never()).publishEvent(any(StudentWorkoutExerciseCompletedEvent.class));
    }

    @Test
    void shouldGetCurrentWorkoutProgressSuccessfully() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;

        Organization organization = createOrganization(1L);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");
        StudentWorkout studentWorkout = createStudentWorkout(studentWorkoutId, student, workout);

        Exercise exerciseA = createExercise(20L, organization, "Supino reto");
        Exercise exerciseB = createExercise(21L, organization, "Remada baixa");

        WorkoutExercise workoutExerciseA = createWorkoutExercise(100L, workout, exerciseA, 1);
        WorkoutExercise workoutExerciseB = createWorkoutExercise(101L, workout, exerciseB, 2);

        StudentWorkoutExerciseProgress progressA = createProgress(
                500L,
                studentWorkout,
                workoutExerciseA,
                true,
                LocalDateTime.now()
        );

        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                studentId,
                WorkoutStatus.ACTIVE
        )).thenReturn(Optional.of(studentWorkout));

        when(workoutExerciseRepository.findAllByWorkoutIdOrderByExerciseOrderAsc(workoutId))
                .thenReturn(List.of(workoutExerciseA, workoutExerciseB));

        when(progressRepository.findAllByStudentWorkoutId(studentWorkoutId))
                .thenReturn(List.of(progressA));

        StudentCurrentWorkoutProgressResponse response =
                studentWorkoutProgressService.getCurrentWorkoutProgress(studentId);

        assertNotNull(response);
        assertEquals(studentId, response.studentId());
        assertEquals(studentWorkoutId, response.studentWorkoutId());
        assertEquals(workoutId, response.workoutId());
        assertEquals("Treino A", response.workoutName());
        assertEquals(2, response.totalExercises());
        assertEquals(1, response.completedExercises());
        assertEquals(50, response.progressPercentage());
        assertEquals(2, response.exercises().size());

        assertEquals(100L, response.exercises().get(0).workoutExerciseId());
        assertTrue(response.exercises().get(0).completed());
        assertNotNull(response.exercises().get(0).completedAt());

        assertEquals(101L, response.exercises().get(1).workoutExerciseId());
        assertFalse(response.exercises().get(1).completed());
        assertNull(response.exercises().get(1).completedAt());

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(progressRepository).findAllByStudentWorkoutId(studentWorkoutId);
    }

    @Test
    void shouldReturnZeroProgressWhenWorkoutHasNoExercises() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;

        Organization organization = createOrganization(1L);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");
        StudentWorkout studentWorkout = createStudentWorkout(studentWorkoutId, student, workout);

        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                studentId,
                WorkoutStatus.ACTIVE
        )).thenReturn(Optional.of(studentWorkout));

        when(workoutExerciseRepository.findAllByWorkoutIdOrderByExerciseOrderAsc(workoutId))
                .thenReturn(List.of());

        when(progressRepository.findAllByStudentWorkoutId(studentWorkoutId))
                .thenReturn(List.of());

        StudentCurrentWorkoutProgressResponse response =
                studentWorkoutProgressService.getCurrentWorkoutProgress(studentId);

        assertEquals(0, response.totalExercises());
        assertEquals(0, response.completedExercises());
        assertEquals(0, response.progressPercentage());
        assertTrue(response.exercises().isEmpty());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenStudentDoesNotHaveActiveWorkout() {
        Long studentId = 1L;

        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                studentId,
                WorkoutStatus.ACTIVE
        )).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutProgressService.getCurrentWorkoutProgress(studentId)
        );

        assertEquals(
                "Active workout not found student id: " + studentId,
                exception.getMessage()
        );

        verify(studentAccessValidator).validateStudentAccess(studentId);
        verify(eventPublisher, never()).publishEvent(any(StudentWorkoutExerciseCompletedEvent.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenCurrentWorkoutIsInactive() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutId = 10L;

        Organization organization = createOrganization(1L);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);

        Workout workout = createWorkout(workoutId, teacher, "Treino A");
        workout.setStatus(WorkoutStatus.INACTIVE);

        StudentWorkout studentWorkout = createStudentWorkout(studentWorkoutId, student, workout);

        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                studentId,
                WorkoutStatus.ACTIVE
        )).thenReturn(Optional.of(studentWorkout));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutProgressService.getCurrentWorkoutProgress(studentId)
        );

        assertEquals(
                "Active workout not found student id: " + studentId,
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutExerciseDoesNotBelongToCurrentWorkout() {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long currentWorkoutId = 10L;
        Long anotherWorkoutId = 99L;
        Long workoutExerciseId = 100L;

        Organization organization = createOrganization(1L);
        User student = createStudent(studentId, organization);
        User teacher = createTeacher(2L, organization);

        Workout currentWorkout = createWorkout(currentWorkoutId, teacher, "Treino A");
        Workout anotherWorkout = createWorkout(anotherWorkoutId, teacher, "Treino B");

        StudentWorkout studentWorkout = createStudentWorkout(studentWorkoutId, student, currentWorkout);

        Exercise exercise = createExercise(20L, organization, "Supino reto");
        WorkoutExercise workoutExerciseFromAnotherWorkout =
                createWorkoutExercise(workoutExerciseId, anotherWorkout, exercise, 1);

        when(studentWorkoutRepository.findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
                studentId,
                WorkoutStatus.ACTIVE
        )).thenReturn(Optional.of(studentWorkout));

        when(workoutExerciseRepository.findById(workoutExerciseId))
                .thenReturn(Optional.of(workoutExerciseFromAnotherWorkout));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentWorkoutProgressService.completeExercise(studentId, workoutExerciseId)
        );

        assertEquals(
                "Workout exercise not found with id: "
                        + workoutExerciseId
                        + " for current workout",
                exception.getMessage()
        );

        verify(progressRepository, never()).save(any(StudentWorkoutExerciseProgress.class));
    }

    private Organization createOrganization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }

    private User createStudent(Long id, Organization organization) {
        User student = new User();
        student.setId(id);
        student.setName("Aluno Teste");
        student.setRole(UserRole.STUDENT);
        student.setOrganization(organization);
        return student;
    }

    private User createTeacher(Long id, Organization organization) {
        User teacher = new User();
        teacher.setId(id);
        teacher.setName("Professor Teste");
        teacher.setRole(UserRole.TEACHER);
        teacher.setOrganization(organization);
        return teacher;
    }

    private Workout createWorkout(Long id, User teacher, String workoutName) {
        Workout workout = new Workout();
        workout.setId(id);
        workout.setTeacher(teacher);
        workout.setWorkoutName(workoutName);
        workout.setStatus(WorkoutStatus.ACTIVE);
        return workout;
    }

    private Exercise createExercise(Long id, Organization organization, String exerciseName) {
        Exercise exercise = new Exercise();
        exercise.setId(id);
        exercise.setOrganization(organization);
        exercise.setExerciseName(exerciseName);
        exercise.setMuscleGroup("Peito");
        exercise.setEquipmentName("Barra");
        return exercise;
    }

    private WorkoutExercise createWorkoutExercise(
            Long id,
            Workout workout,
            Exercise exercise,
            Integer exerciseOrder
    ) {
        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setId(id);
        workoutExercise.setWorkout(workout);
        workoutExercise.setExercise(exercise);
        workoutExercise.setExerciseOrder(exerciseOrder);
        workoutExercise.setSets(4);
        workoutExercise.setReps("8-12");
        workoutExercise.setRestTimeSeconds(60);
        return workoutExercise;
    }

    private StudentWorkout createStudentWorkout(
            Long id,
            User student,
            Workout workout
    ) {
        StudentWorkout studentWorkout = new StudentWorkout();
        studentWorkout.setId(id);
        studentWorkout.setStudent(student);
        studentWorkout.setWorkout(workout);
        studentWorkout.setStatus(WorkoutStatus.ACTIVE);
        studentWorkout.setAssignedAt(LocalDateTime.now());
        return studentWorkout;
    }

    private StudentWorkoutExerciseProgress createProgress(
            Long id,
            StudentWorkout studentWorkout,
            WorkoutExercise workoutExercise,
            Boolean completed,
            LocalDateTime completedAt
    ) {
        StudentWorkoutExerciseProgress progress = new StudentWorkoutExerciseProgress();
        progress.setId(id);
        progress.setStudentWorkout(studentWorkout);
        progress.setWorkoutExercise(workoutExercise);
        progress.setCompleted(completed);
        progress.setCompletedAt(completedAt);
        return progress;
    }
}