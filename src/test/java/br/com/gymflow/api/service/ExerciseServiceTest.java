package br.com.gymflow.api.service;

import br.com.gymflow.api.domain.Exercise;
import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.domain.enums.OrganizationType;
import br.com.gymflow.api.dto.exercise.CreateExerciseRequest;
import br.com.gymflow.api.dto.exercise.ExerciseResponse;
import br.com.gymflow.api.dto.exercise.UpdateExerciseRequest;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.mapper.ExerciseMapper;
import br.com.gymflow.api.repository.ExerciseRepository;
import br.com.gymflow.api.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ExerciseMapper exerciseMapper;

    @InjectMocks
    private ExerciseService exerciseService;


    @Test
    void shouldCreateExerciseSuccessfully() {
        // Arrange
        Long organizationId = 100L;
        Long exerciseId = 10L;

        CreateExerciseRequest request = createExerciseRequest(organizationId);

        Organization organization = createOrganization(organizationId);

        Exercise exerciseToSave = createExercise(
                null,
                organization,
                "Supino reto"
        );

        Exercise savedExercise = createExercise(
                exerciseId,
                organization,
                "Supino reto"
        );

        ExerciseResponse expectedResponse = createExerciseResponse(
                exerciseId,
                organizationId,
                "Supino reto"
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(exerciseMapper.toEntity(request))
                .thenReturn(exerciseToSave);

        when(exerciseRepository.save(exerciseToSave))
                .thenReturn(savedExercise);

        when(exerciseMapper.toResponse(savedExercise))
                .thenReturn(expectedResponse);

        // Act
        ExerciseResponse response = exerciseService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals(exerciseId, response.id());
        assertEquals(organizationId, response.organizationId());
        assertEquals("Supino reto", response.exerciseName());
        assertEquals("Peito", response.muscleGroup());
        assertEquals("Barra", response.equipmentName());

        assertEquals(organization, exerciseToSave.getOrganization());

        verify(organizationRepository).findById(organizationId);
        verify(exerciseMapper).toEntity(request);
        verify(exerciseRepository).save(exerciseToSave);
        verify(exerciseMapper).toResponse(savedExercise);
    }

    @Test
    void shouldFindAllExercisesByOrganizationIdSuccessfully() {
        // Arrange
        Long organizationId = 100L;

        Organization organization = createOrganization(organizationId);

        Exercise exerciseA = createExercise(
                10L,
                organization,
                "Supino reto"
        );

        Exercise exerciseB = createExercise(
                20L,
                organization,
                "Agachamento livre"
        );

        ExerciseResponse responseA = createExerciseResponse(
                10L,
                organizationId,
                "Supino reto"
        );

        ExerciseResponse responseB = createExerciseResponse(
                20L,
                organizationId,
                "Agachamento livre"
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(exerciseRepository.findByOrganizationId(organizationId))
                .thenReturn(List.of(exerciseA, exerciseB));

        when(exerciseMapper.toResponse(exerciseA))
                .thenReturn(responseA);

        when(exerciseMapper.toResponse(exerciseB))
                .thenReturn(responseB);

        // Act
        List<ExerciseResponse> response = exerciseService.findAllByOrganizationId(organizationId);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(10L, response.get(0).id());
        assertEquals(organizationId, response.get(0).organizationId());
        assertEquals("Supino reto", response.get(0).exerciseName());

        assertEquals(20L, response.get(1).id());
        assertEquals(organizationId, response.get(1).organizationId());
        assertEquals("Agachamento livre", response.get(1).exerciseName());

        verify(organizationRepository).findById(organizationId);
        verify(exerciseRepository).findByOrganizationId(organizationId);
        verify(exerciseMapper).toResponse(exerciseA);
        verify(exerciseMapper).toResponse(exerciseB);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenOrganizationDoesNotExistOnFindAllByOrganizationId() {
        // Arrange
        Long organizationId = 100L;

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> exerciseService.findAllByOrganizationId(organizationId)
        );

        assertEquals(
                "Organization not found with id: " + organizationId,
                exception.getMessage()
        );

        verify(organizationRepository).findById(organizationId);

        verify(exerciseRepository, never())
                .findByOrganizationId(anyLong());

        verifyNoInteractions(exerciseMapper);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenOrganizationDoesNotExistOnCreate() {
        // Arrange
        Long organizationId = 100L;

        CreateExerciseRequest request = createExerciseRequest(organizationId);

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> exerciseService.create(request)
        );

        assertEquals(
                "Organization not found with id: " + organizationId,
                exception.getMessage()
        );

        verify(organizationRepository).findById(organizationId);

        verifyNoInteractions(
                exerciseRepository,
                exerciseMapper
        );
    }

    @Test
    void shouldFindAllExercisesSuccessfully() {
        // Arrange
        Long organizationId = 100L;

        Organization organization = createOrganization(organizationId);

        Exercise exerciseA = createExercise(
                10L,
                organization,
                "Supino reto"
        );

        Exercise exerciseB = createExercise(
                20L,
                organization,
                "Agachamento livre"
        );

        ExerciseResponse responseA = createExerciseResponse(
                10L,
                organizationId,
                "Supino reto"
        );

        ExerciseResponse responseB = createExerciseResponse(
                20L,
                organizationId,
                "Agachamento livre"
        );

        when(exerciseRepository.findAll())
                .thenReturn(List.of(exerciseA, exerciseB));

        when(exerciseMapper.toResponse(exerciseA))
                .thenReturn(responseA);

        when(exerciseMapper.toResponse(exerciseB))
                .thenReturn(responseB);

        // Act
        List<ExerciseResponse> response = exerciseService.findAll();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(10L, response.get(0).id());
        assertEquals(organizationId, response.get(0).organizationId());
        assertEquals("Supino reto", response.get(0).exerciseName());

        assertEquals(20L, response.get(1).id());
        assertEquals(organizationId, response.get(1).organizationId());
        assertEquals("Agachamento livre", response.get(1).exerciseName());

        verify(exerciseRepository).findAll();
        verify(exerciseMapper).toResponse(exerciseA);
        verify(exerciseMapper).toResponse(exerciseB);

        verifyNoInteractions(organizationRepository);
    }

    @Test
    void shouldFindExerciseByIdSuccessfully() {
        // Arrange
        Long organizationId = 100L;
        Long exerciseId = 10L;

        Organization organization = createOrganization(organizationId);

        Exercise exercise = createExercise(
                exerciseId,
                organization,
                "Supino reto"
        );

        ExerciseResponse expectedResponse = createExerciseResponse(
                exerciseId,
                organizationId,
                "Supino reto"
        );

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(exercise));

        when(exerciseMapper.toResponse(exercise))
                .thenReturn(expectedResponse);

        // Act
        ExerciseResponse response = exerciseService.findById(exerciseId);

        // Assert
        assertNotNull(response);
        assertEquals(exerciseId, response.id());
        assertEquals(organizationId, response.organizationId());
        assertEquals("Supino reto", response.exerciseName());
        assertEquals("Peito", response.muscleGroup());
        assertEquals("Barra", response.equipmentName());

        verify(exerciseRepository).findById(exerciseId);
        verify(exerciseMapper).toResponse(exercise);

        verifyNoInteractions(organizationRepository);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenExerciseDoesNotExistOnFindById() {
        // Arrange
        Long exerciseId = 10L;

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> exerciseService.findById(exerciseId)
        );

        assertEquals(
                "Exercise not found with id: " + exerciseId,
                exception.getMessage()
        );

        verify(exerciseRepository).findById(exerciseId);

        verifyNoInteractions(
                organizationRepository,
                exerciseMapper
        );
    }

    @Test
    void shouldUpdateExerciseSuccessfully() {
        // Arrange
        Long organizationId = 100L;
        Long exerciseId = 10L;

        UpdateExerciseRequest request = createUpdateExerciseRequest();

        Organization organization = createOrganization(organizationId);

        Exercise exercise = createExercise(
                exerciseId,
                organization,
                "Supino reto"
        );

        Exercise updatedExercise = createExercise(
                exerciseId,
                organization,
                "Supino inclinado"
        );

        updatedExercise.setMuscleGroup("Peito");
        updatedExercise.setDescription("Variação inclinada para peitoral superior");
        updatedExercise.setEquipmentName("Halteres");
        updatedExercise.setImageUrl("https://example.com/supino-inclinado.png");
        updatedExercise.setVideoUrl("https://example.com/supino-inclinado.mp4");

        ExerciseResponse expectedResponse = new ExerciseResponse(
                exerciseId,
                organizationId,
                "Supino inclinado",
                "Peito",
                "Variação inclinada para peitoral superior",
                "Halteres",
                "https://example.com/supino-inclinado.png",
                "https://example.com/supino-inclinado.mp4",
                true,
                null,
                null
        );

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(exercise));

        when(exerciseRepository.save(exercise))
                .thenReturn(updatedExercise);

        when(exerciseMapper.toResponse(updatedExercise))
                .thenReturn(expectedResponse);

        // Act
        ExerciseResponse response = exerciseService.update(exerciseId, request);

        // Assert
        assertNotNull(response);
        assertEquals(exerciseId, response.id());
        assertEquals(organizationId, response.organizationId());
        assertEquals("Supino inclinado", response.exerciseName());
        assertEquals("Peito", response.muscleGroup());
        assertEquals("Variação inclinada para peitoral superior", response.description());
        assertEquals("Halteres", response.equipmentName());
        assertEquals("https://example.com/supino-inclinado.png", response.imageUrl());
        assertEquals("https://example.com/supino-inclinado.mp4", response.videoUrl());

        verify(exerciseRepository).findById(exerciseId);
        verify(exerciseMapper).updateEntity(exercise, request);
        verify(exerciseRepository).save(exercise);
        verify(exerciseMapper).toResponse(updatedExercise);

        verifyNoInteractions(organizationRepository);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenExerciseDoesNotExistOnUpdate() {
        // Arrange
        Long exerciseId = 10L;

        UpdateExerciseRequest request = createUpdateExerciseRequest();

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> exerciseService.update(exerciseId, request)
        );

        assertEquals(
                "Exercise not found with id: " + exerciseId,
                exception.getMessage()
        );

        verify(exerciseRepository).findById(exerciseId);

        verifyNoInteractions(
                organizationRepository,
                exerciseMapper
        );

        verify(exerciseRepository, never())
                .save(any(Exercise.class));
    }

    @Test
    void shouldDeleteExerciseSuccessfully() {
        // Arrange
        Long organizationId = 100L;
        Long exerciseId = 10L;

        Organization organization = createOrganization(organizationId);

        Exercise exercise = createExercise(
                exerciseId,
                organization,
                "Supino reto"
        );

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(exercise));

        when(exerciseRepository.save(exercise))
                .thenReturn(exercise);

        // Act
        exerciseService.delete(exerciseId);

        // Assert
        assertFalse(exercise.getActive());

        verify(exerciseRepository).findById(exerciseId);
        verify(exerciseRepository).save(exercise);

        verify(exerciseRepository, never())
                .delete(any(Exercise.class));

        verifyNoInteractions(
                organizationRepository,
                exerciseMapper
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenExerciseDoesNotExistOnDelete() {
        // Arrange
        Long exerciseId = 10L;

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> exerciseService.delete(exerciseId)
        );

        assertEquals(
                "Exercise not found with id: " + exerciseId,
                exception.getMessage()
        );

        verify(exerciseRepository).findById(exerciseId);

        verifyNoInteractions(
                organizationRepository,
                exerciseMapper
        );

        verify(exerciseRepository, never())
                .save(any(Exercise.class));

        verify(exerciseRepository, never())
                .delete(any(Exercise.class));
    }

    private CreateExerciseRequest createExerciseRequest(Long organizationId) {
        return new CreateExerciseRequest(
                organizationId,
                "Supino reto",
                "Peito",
                "Exercício para fortalecimento do peitoral",
                "Barra",
                "https://example.com/supino.png",
                "https://example.com/supino.mp4"
        );
    }

    private UpdateExerciseRequest createUpdateExerciseRequest() {
        return new UpdateExerciseRequest(
                "Supino inclinado",
                "Peito",
                "Variação inclinada para peitoral superior",
                "Halteres",
                "https://example.com/supino-inclinado.png",
                "https://example.com/supino-inclinado.mp4"
        );
    }

    private ExerciseResponse createExerciseResponse(
            Long exerciseId,
            Long organizationId,
            String exerciseName

    ) {
        return new ExerciseResponse(
                exerciseId,
                organizationId,
                exerciseName,
                "Peito",
                "Exercício para fortalecimento do peitoral",
                "Barra",
                "https://example.com/supino.png",
                "https://example.com/supino.mp4",
                true,
                null,
                null
        );
    }

    private Exercise createExercise(Long id, Organization organization, String exerciseName) {
        Exercise exercise = new Exercise();
        exercise.setId(id);
        exercise.setOrganization(organization);
        exercise.setExerciseName(exerciseName);
        exercise.setMuscleGroup("Peito");
        exercise.setDescription("Exercício para fortalecimento do peitoral");
        exercise.setEquipmentName("Barra");
        exercise.setActive(true);
        exercise.setImageUrl("https://example.com/supino.png");
        exercise.setVideoUrl("https://example.com/supino.mp4");
        return exercise;
    }

    private Organization createOrganization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        organization.setOrganizationName("GymFlow Academy");
        organization.setOrganizationType(OrganizationType.ACADEMY);
        organization.setActive(true);
        return organization;
    }
}
