package br.com.gymflow.api.service;

import br.com.gymflow.api.domain.Exercise;
import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.Workout;
import br.com.gymflow.api.domain.WorkoutExercise;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.domain.enums.WorkoutStatus;
import br.com.gymflow.api.dto.workoutExercise.CreateWorkoutExerciseRequest;
import br.com.gymflow.api.dto.workoutExercise.PatchWorkoutExerciseRequest;
import br.com.gymflow.api.dto.workoutExercise.WorkoutExerciseResponse;
import br.com.gymflow.api.exception.BusinessRuleException;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.mapper.WorkoutExerciseMapper;
import br.com.gymflow.api.repository.ExerciseRepository;
import br.com.gymflow.api.repository.WorkoutExerciseRepository;
import br.com.gymflow.api.repository.WorkoutRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutExerciseServiceTest {

    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Mock
    private WorkoutExerciseMapper workoutExerciseMapper;

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private WorkoutExerciseService workoutExerciseService;


    @Test
    void shouldCreateWorkoutExerciseSuccessfully() {
        // Arrange
        Long workoutId = 10L;
        Long exerciseId = 20L;
        Long workoutExerciseId = 30L;

        CreateWorkoutExerciseRequest request = createWorkoutExerciseRequest(exerciseId);

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");
        Exercise exercise = createExercise(exerciseId, organization, "Supino reto");

        WorkoutExercise workoutExerciseToSave = createWorkoutExercise(null, workout, exercise);
        WorkoutExercise savedWorkoutExercise = createWorkoutExercise(workoutExerciseId, workout, exercise);

        WorkoutExerciseResponse expectedResponse = createWorkoutExerciseResponse(
                workoutExerciseId,
                workoutId,
                exerciseId
        );

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(exercise));

        when(workoutExerciseMapper.toEntity(request))
                .thenReturn(workoutExerciseToSave);

        when(workoutExerciseRepository.save(workoutExerciseToSave))
                .thenReturn(savedWorkoutExercise);

        when(workoutExerciseMapper.toResponse(savedWorkoutExercise))
                .thenReturn(expectedResponse);

        // Act
        WorkoutExerciseResponse response = workoutExerciseService.create(workoutId, request);

        // Assert
        assertNotNull(response);
        assertEquals(workoutExerciseId, response.id());
        assertEquals(workoutId, response.workoutId());
        assertEquals(exerciseId, response.exerciseId());
        assertEquals(1, response.exerciseOrder());
        assertEquals(3, response.sets());
        assertEquals(10, response.reps());

        assertEquals(workout, workoutExerciseToSave.getWorkout());
        assertEquals(exercise, workoutExerciseToSave.getExercise());

        verify(workoutRepository).findById(workoutId);
        verify(exerciseRepository).findById(exerciseId);
        verify(workoutExerciseMapper).toEntity(request);
        verify(workoutExerciseRepository).save(workoutExerciseToSave);
        verify(workoutExerciseMapper).toResponse(savedWorkoutExercise);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutDoesNotExistOnCreate() {
        // Arrange
        Long workoutId = 10L;
        Long exerciseId = 20L;

        CreateWorkoutExerciseRequest request = createWorkoutExerciseRequest(exerciseId);

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutExerciseService.create(workoutId, request)
        );

        assertEquals(
                "Workout not found with id: " + workoutId,
                exception.getMessage()
        );

        verify(workoutRepository).findById(workoutId);

        verifyNoInteractions(
                exerciseRepository,
                workoutExerciseRepository,
                workoutExerciseMapper
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenExerciseDoesNotExistOnCreate() {
        // Arrange
        Long workoutId = 10L;
        Long exerciseId = 20L;

        CreateWorkoutExerciseRequest request = createWorkoutExerciseRequest(exerciseId);

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutExerciseService.create(workoutId, request)
        );

        assertEquals(
                "Exercise not found with id: " + exerciseId,
                exception.getMessage()
        );

        verify(workoutRepository).findById(workoutId);
        verify(exerciseRepository).findById(exerciseId);

        verifyNoInteractions(
                workoutExerciseRepository,
                workoutExerciseMapper
        );
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenExerciseAndWorkoutBelongToDifferentOrganizations() {
        // Arrange
        Long workoutId = 10L;
        Long exerciseId = 20L;

        CreateWorkoutExerciseRequest request = createWorkoutExerciseRequest(exerciseId);

        Organization workoutOrganization = createOrganization(100L);
        Organization exerciseOrganization = createOrganization(200L);

        User teacher = createTeacher(2L, workoutOrganization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        Exercise exercise = createExercise(exerciseId, exerciseOrganization, "Supino reto");

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(exercise));

        // Act + Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> workoutExerciseService.create(workoutId, request)
        );

        assertEquals(
                "Exercise does not belong to the same organization as the workout",
                exception.getMessage()
        );

        verify(workoutRepository).findById(workoutId);
        verify(exerciseRepository).findById(exerciseId);

        verifyNoInteractions(
                workoutExerciseRepository,
                workoutExerciseMapper
        );
    }

    @Test
    void shouldFindAllWorkoutExercisesByWorkoutIdSuccessfully() {
        // Arrange
        Long workoutId = 10L;
        Long exerciseIdA = 20L;
        Long exerciseIdB = 21L;
        Long workoutExerciseIdA = 30L;
        Long workoutExerciseIdB = 31L;

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");

        Exercise exerciseA = createExercise(exerciseIdA, organization, "Supino reto");
        Exercise exerciseB = createExercise(exerciseIdB, organization, "Crucifixo");

        WorkoutExercise workoutExerciseA = createWorkoutExercise(
                workoutExerciseIdA,
                workout,
                exerciseA
        );

        WorkoutExercise workoutExerciseB = createWorkoutExercise(
                workoutExerciseIdB,
                workout,
                exerciseB
        );

        WorkoutExerciseResponse responseA = createWorkoutExerciseResponse(
                workoutExerciseIdA,
                workoutId,
                exerciseIdA
        );

        WorkoutExerciseResponse responseB = createWorkoutExerciseResponse(
                workoutExerciseIdB,
                workoutId,
                exerciseIdB
        );

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.of(workout));

        when(workoutExerciseRepository.findAllByWorkoutId(workoutId))
                .thenReturn(List.of(workoutExerciseA, workoutExerciseB));

        when(workoutExerciseMapper.toResponse(workoutExerciseA))
                .thenReturn(responseA);

        when(workoutExerciseMapper.toResponse(workoutExerciseB))
                .thenReturn(responseB);

        // Act
        List<WorkoutExerciseResponse> response = workoutExerciseService.findAllByWorkoutId(workoutId);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(workoutExerciseIdA, response.get(0).id());
        assertEquals(workoutId, response.get(0).workoutId());
        assertEquals(exerciseIdA, response.get(0).exerciseId());

        assertEquals(workoutExerciseIdB, response.get(1).id());
        assertEquals(workoutId, response.get(1).workoutId());
        assertEquals(exerciseIdB, response.get(1).exerciseId());

        verify(workoutRepository).findById(workoutId);
        verify(workoutExerciseRepository).findAllByWorkoutId(workoutId);
        verify(workoutExerciseMapper).toResponse(workoutExerciseA);
        verify(workoutExerciseMapper).toResponse(workoutExerciseB);

        verifyNoInteractions(exerciseRepository);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutDoesNotExistOnFindAll() {
        // Arrange
        Long workoutId = 10L;

        when(workoutRepository.findById(workoutId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutExerciseService.findAllByWorkoutId(workoutId)
        );

        assertEquals(
                "Workout not found with id: " + workoutId,
                exception.getMessage()
        );

        verify(workoutRepository).findById(workoutId);

        verifyNoInteractions(
                exerciseRepository,
                workoutExerciseRepository,
                workoutExerciseMapper
        );
    }

    @Test
    void shouldFindWorkoutExerciseByIdSuccessfully() {
        // Arrange
        Long workoutId = 10L;
        Long exerciseId = 20L;
        Long workoutExerciseId = 30L;

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");
        Exercise exercise = createExercise(exerciseId, organization, "Supino reto");

        WorkoutExercise workoutExercise = createWorkoutExercise(
                workoutExerciseId,
                workout,
                exercise
        );

        WorkoutExerciseResponse expectedResponse = createWorkoutExerciseResponse(
                workoutExerciseId,
                workoutId,
                exerciseId
        );

        when(workoutExerciseRepository.findById(workoutExerciseId))
                .thenReturn(Optional.of(workoutExercise));

        when(workoutExerciseMapper.toResponse(workoutExercise))
                .thenReturn(expectedResponse);

        // Act
        WorkoutExerciseResponse response = workoutExerciseService.findById(
                workoutId,
                workoutExerciseId
        );

        // Assert
        assertNotNull(response);
        assertEquals(workoutExerciseId, response.id());
        assertEquals(workoutId, response.workoutId());
        assertEquals(exerciseId, response.exerciseId());
        assertEquals(1, response.exerciseOrder());
        assertEquals(3, response.sets());
        assertEquals(10, response.reps());

        verify(workoutExerciseRepository).findById(workoutExerciseId);
        verify(workoutExerciseMapper).toResponse(workoutExercise);

        verifyNoInteractions(
                workoutRepository,
                exerciseRepository
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutExerciseDoesNotExistOnFindById() {
        // Arrange
        Long workoutId = 10L;
        Long workoutExerciseId = 30L;

        when(workoutExerciseRepository.findById(workoutExerciseId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutExerciseService.findById(workoutId, workoutExerciseId)
        );

        assertEquals(
                "Workout exercise not found with id: " + workoutExerciseId,
                exception.getMessage()
        );

        verify(workoutExerciseRepository).findById(workoutExerciseId);

        verifyNoInteractions(
                workoutRepository,
                exerciseRepository,
                workoutExerciseMapper
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutExerciseDoesNotBelongToWorkoutOnFindById() {
        // Arrange
        Long requestedWorkoutId = 10L;
        Long ownerWorkoutId = 99L;
        Long exerciseId = 20L;
        Long workoutExerciseId = 30L;

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(2L, organization);

        Workout ownerWorkout = createWorkout(ownerWorkoutId, teacher, "Treino B");
        Exercise exercise = createExercise(exerciseId, organization, "Supino reto");

        WorkoutExercise workoutExercise = createWorkoutExercise(
                workoutExerciseId,
                ownerWorkout,
                exercise
        );

        when(workoutExerciseRepository.findById(workoutExerciseId))
                .thenReturn(Optional.of(workoutExercise));

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutExerciseService.findById(requestedWorkoutId, workoutExerciseId)
        );

        assertEquals(
                "Workout exercise not found with id: "
                        + workoutExerciseId
                        + " for workout id: "
                        + requestedWorkoutId,
                exception.getMessage()
        );

        verify(workoutExerciseRepository).findById(workoutExerciseId);

        verifyNoInteractions(
                workoutRepository,
                exerciseRepository,
                workoutExerciseMapper
        );
    }

    @Test
    void shouldPatchWorkoutExerciseSuccessfully() {
        // Arrange
        Long workoutId = 10L;
        Long exerciseId = 20L;
        Long workoutExerciseId = 30L;

        PatchWorkoutExerciseRequest request = createPatchWorkoutExerciseRequest();

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");
        Exercise exercise = createExercise(exerciseId, organization, "Supino reto");

        WorkoutExercise workoutExercise = createWorkoutExercise(
                workoutExerciseId,
                workout,
                exercise
        );

        WorkoutExercise updatedWorkoutExercise = createWorkoutExercise(
                workoutExerciseId,
                workout,
                exercise
        );

        updatedWorkoutExercise.setExerciseOrder(2);
        updatedWorkoutExercise.setSets(4);
        updatedWorkoutExercise.setReps(12);
        updatedWorkoutExercise.setRecommendedLoad(BigDecimal.valueOf(45.00));
        updatedWorkoutExercise.setRestTimeSeconds(90);
        updatedWorkoutExercise.setNotes("Aumentar carga progressivamente");

        WorkoutExerciseResponse expectedResponse = new WorkoutExerciseResponse(
                workoutExerciseId,
                workoutId,
                exerciseId,
                2,
                4,
                12,
                BigDecimal.valueOf(45.00),
                90,
                "Aumentar carga progressivamente",
                null,
                null
        );

        when(workoutExerciseRepository.findById(workoutExerciseId))
                .thenReturn(Optional.of(workoutExercise));

        when(workoutExerciseRepository.save(workoutExercise))
                .thenReturn(updatedWorkoutExercise);

        when(workoutExerciseMapper.toResponse(updatedWorkoutExercise))
                .thenReturn(expectedResponse);

        // Act
        WorkoutExerciseResponse response = workoutExerciseService.patch(
                workoutId,
                workoutExerciseId,
                request
        );

        // Assert
        assertNotNull(response);
        assertEquals(workoutExerciseId, response.id());
        assertEquals(workoutId, response.workoutId());
        assertEquals(exerciseId, response.exerciseId());
        assertEquals(2, response.exerciseOrder());
        assertEquals(4, response.sets());
        assertEquals(12, response.reps());
        assertEquals(BigDecimal.valueOf(45.00), response.recommendedLoad());
        assertEquals(90, response.restTimeSeconds());
        assertEquals("Aumentar carga progressivamente", response.notes());

        assertEquals(2, workoutExercise.getExerciseOrder());
        assertEquals(4, workoutExercise.getSets());
        assertEquals(12, workoutExercise.getReps());
        assertEquals(BigDecimal.valueOf(45.00), workoutExercise.getRecommendedLoad());
        assertEquals(90, workoutExercise.getRestTimeSeconds());
        assertEquals("Aumentar carga progressivamente", workoutExercise.getNotes());

        verify(workoutExerciseRepository).findById(workoutExerciseId);
        verify(workoutExerciseRepository).save(workoutExercise);
        verify(workoutExerciseMapper).toResponse(updatedWorkoutExercise);

        verifyNoInteractions(
                workoutRepository,
                exerciseRepository
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutExerciseDoesNotExistOnPatch() {
        // Arrange
        Long workoutId = 10L;
        Long workoutExerciseId = 30L;

        PatchWorkoutExerciseRequest request = createPatchWorkoutExerciseRequest();

        when(workoutExerciseRepository.findById(workoutExerciseId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutExerciseService.patch(workoutId, workoutExerciseId, request)
        );

        assertEquals(
                "Workout exercise not found with id: " + workoutExerciseId,
                exception.getMessage()
        );

        verify(workoutExerciseRepository).findById(workoutExerciseId);

        verifyNoInteractions(
                workoutRepository,
                exerciseRepository,
                workoutExerciseMapper
        );

        verify(workoutExerciseRepository, never())
                .save(any(WorkoutExercise.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutExerciseDoesNotBelongToWorkoutOnPatch() {
        // Arrange
        Long requestedWorkoutId = 10L;
        Long ownerWorkoutId = 99L;
        Long exerciseId = 20L;
        Long workoutExerciseId = 30L;

        PatchWorkoutExerciseRequest request = createPatchWorkoutExerciseRequest();

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(2L, organization);

        Workout ownerWorkout = createWorkout(ownerWorkoutId, teacher, "Treino B");
        Exercise exercise = createExercise(exerciseId, organization, "Supino reto");

        WorkoutExercise workoutExercise = createWorkoutExercise(
                workoutExerciseId,
                ownerWorkout,
                exercise
        );

        when(workoutExerciseRepository.findById(workoutExerciseId))
                .thenReturn(Optional.of(workoutExercise));

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutExerciseService.patch(
                        requestedWorkoutId,
                        workoutExerciseId,
                        request
                )
        );

        assertEquals(
                "Workout exercise not found with id: "
                        + workoutExerciseId
                        + " for workout id: "
                        + requestedWorkoutId,
                exception.getMessage()
        );

        verify(workoutExerciseRepository).findById(workoutExerciseId);

        verifyNoInteractions(
                workoutRepository,
                exerciseRepository,
                workoutExerciseMapper
        );

        verify(workoutExerciseRepository, never())
                .save(any(WorkoutExercise.class));
    }

    @Test
    void shouldDeleteWorkoutExerciseSuccessfully() {
        // Arrange
        Long workoutId = 10L;
        Long exerciseId = 20L;
        Long workoutExerciseId = 30L;

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(2L, organization);
        Workout workout = createWorkout(workoutId, teacher, "Treino A");
        Exercise exercise = createExercise(exerciseId, organization, "Supino reto");

        WorkoutExercise workoutExercise = createWorkoutExercise(
                workoutExerciseId,
                workout,
                exercise
        );

        when(workoutExerciseRepository.findById(workoutExerciseId))
                .thenReturn(Optional.of(workoutExercise));

        // Act
        workoutExerciseService.delete(workoutId, workoutExerciseId);

        // Assert
        verify(workoutExerciseRepository).findById(workoutExerciseId);
        verify(workoutExerciseRepository).delete(workoutExercise);

        verifyNoInteractions(
                workoutRepository,
                exerciseRepository,
                workoutExerciseMapper
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutExerciseDoesNotExistOnDelete() {
        // Arrange
        Long workoutId = 10L;
        Long workoutExerciseId = 30L;

        when(workoutExerciseRepository.findById(workoutExerciseId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutExerciseService.delete(workoutId, workoutExerciseId)
        );

        assertEquals(
                "Workout exercise not found with id: " + workoutExerciseId,
                exception.getMessage()
        );

        verify(workoutExerciseRepository).findById(workoutExerciseId);

        verifyNoInteractions(
                workoutRepository,
                exerciseRepository,
                workoutExerciseMapper
        );

        verify(workoutExerciseRepository, never())
                .delete(any(WorkoutExercise.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutExerciseDoesNotBelongToWorkoutOnDelete() {
        // Arrange
        Long requestedWorkoutId = 10L;
        Long ownerWorkoutId = 99L;
        Long exerciseId = 20L;
        Long workoutExerciseId = 30L;

        Organization organization = createOrganization(100L);
        User teacher = createTeacher(2L, organization);

        Workout ownerWorkout = createWorkout(ownerWorkoutId, teacher, "Treino B");
        Exercise exercise = createExercise(exerciseId, organization, "Supino reto");

        WorkoutExercise workoutExercise = createWorkoutExercise(
                workoutExerciseId,
                ownerWorkout,
                exercise
        );

        when(workoutExerciseRepository.findById(workoutExerciseId))
                .thenReturn(Optional.of(workoutExercise));

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workoutExerciseService.delete(
                        requestedWorkoutId,
                        workoutExerciseId
                )
        );

        assertEquals(
                "Workout exercise not found with id: "
                        + workoutExerciseId
                        + " for workout id: "
                        + requestedWorkoutId,
                exception.getMessage()
        );

        verify(workoutExerciseRepository).findById(workoutExerciseId);

        verifyNoInteractions(
                workoutRepository,
                exerciseRepository,
                workoutExerciseMapper
        );

        verify(workoutExerciseRepository, never())
                .delete(any(WorkoutExercise.class));
    }


    private Organization createOrganization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }

    private User createTeacher(Long id, Organization organization) {
        User teacher = new User();
        teacher.setId(id);
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

    private WorkoutExercise createWorkoutExercise(Long id, Workout workout, Exercise exercise) {
        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setId(id);
        workoutExercise.setWorkout(workout);
        workoutExercise.setExercise(exercise);
        workoutExercise.setExerciseOrder(1);
        workoutExercise.setSets(3);
        workoutExercise.setReps(10);
        workoutExercise.setRecommendedLoad(BigDecimal.valueOf(40.00));
        workoutExercise.setRestTimeSeconds(60);
        workoutExercise.setNotes("Manter controle do movimento");
        return workoutExercise;
    }

    private CreateWorkoutExerciseRequest createWorkoutExerciseRequest(Long exerciseId) {
        return new CreateWorkoutExerciseRequest(
                exerciseId,
                1,
                3,
                10,
                BigDecimal.valueOf(40.00),
                60,
                "Manter controle do movimento"
        );
    }

    private PatchWorkoutExerciseRequest createPatchWorkoutExerciseRequest() {
        return new PatchWorkoutExerciseRequest(
                2,
                4,
                12,
                BigDecimal.valueOf(45.00),
                90,
                "Aumentar carga progressivamente"
        );
    }

    private WorkoutExerciseResponse createWorkoutExerciseResponse(
            Long id,
            Long workoutId,
            Long exerciseId
    ) {
        return new WorkoutExerciseResponse(
                id,
                workoutId,
                exerciseId,
                1,
                3,
                10,
                BigDecimal.valueOf(40.00),
                60,
                "Manter controle do movimento",
                null,
                null
        );
    }
}