package br.com.gymflow.api.service;

import br.com.gymflow.api.domain.Exercise;
import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.enums.ExerciseMediaType;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.dto.exercise.CreateExerciseRequest;
import br.com.gymflow.api.dto.exercise.ExerciseResponse;
import br.com.gymflow.api.dto.exercise.UpdateExerciseRequest;
import br.com.gymflow.api.exception.DuplicateResourceException;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.mapper.ExerciseMapper;
import br.com.gymflow.api.repository.ExerciseRepository;
import br.com.gymflow.api.repository.OrganizationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Mock
    private CloudinaryStorageService cloudinaryStorageService;

    @InjectMocks
    private ExerciseService exerciseService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateExerciseSuccessfully() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        CreateExerciseRequest request = new CreateExerciseRequest(
                "Supino reto",
                "Peito",
                "Exercício para peitoral",
                "Barra",
                null,
                null
        );

        Exercise exerciseToSave = createExercise(null, 1L);
        Exercise savedExercise = createExercise(1L, 1L);
        ExerciseResponse expectedResponse = mock(ExerciseResponse.class);

        when(exerciseRepository.existsByOrganizationIdAndExerciseNameIgnoreCase(
                1L,
                request.exerciseName()
        )).thenReturn(false);

        when(exerciseMapper.toEntity(request)).thenReturn(exerciseToSave);
        when(exerciseRepository.save(exerciseToSave)).thenReturn(savedExercise);
        when(exerciseMapper.toResponse(savedExercise)).thenReturn(expectedResponse);

        ExerciseResponse response = exerciseService.create(request);

        assertNotNull(response);
        assertSame(expectedResponse, response);
        assertEquals(admin.getOrganization(), exerciseToSave.getOrganization());

        verify(exerciseRepository).existsByOrganizationIdAndExerciseNameIgnoreCase(
                1L,
                request.exerciseName()
        );
        verify(exerciseMapper).toEntity(request);
        verify(exerciseRepository).save(exerciseToSave);
        verify(exerciseMapper).toResponse(savedExercise);
        verifyNoInteractions(organizationRepository);
    }

    @Test
    void shouldThrowDuplicateResourceExceptionWhenCreateExerciseWithDuplicatedNameInSameOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        CreateExerciseRequest request = new CreateExerciseRequest(
                "Supino reto",
                "Peito",
                "Exercício duplicado",
                "Barra",
                null,
                null
        );

        when(exerciseRepository.existsByOrganizationIdAndExerciseNameIgnoreCase(
                1L,
                request.exerciseName()
        )).thenReturn(true);

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () ->
                exerciseService.create(request)
        );

        assertEquals("Já existe um exercício com este nome nesta organização.", exception.getMessage());

        verify(exerciseRepository).existsByOrganizationIdAndExerciseNameIgnoreCase(
                1L,
                request.exerciseName()
        );
        verify(exerciseMapper, never()).toEntity(any());
        verify(exerciseRepository, never()).save(any());
        verifyNoInteractions(organizationRepository);
    }

    @Test
    void shouldFindAllExercisesSuccessfully() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Exercise exercise = createExercise(1L, 1L);
        ExerciseResponse expectedResponse = mock(ExerciseResponse.class);

        when(exerciseRepository.findByOrganizationIdAndActiveTrue(1L)).thenReturn(List.of(exercise));
        when(exerciseMapper.toResponse(exercise)).thenReturn(expectedResponse);

        List<ExerciseResponse> response = exerciseService.findAll();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertSame(expectedResponse, response.get(0));

        verify(exerciseRepository).findByOrganizationIdAndActiveTrue(1L);
        verify(exerciseRepository, never()).findByOrganizationId(1L);
    }

    @Test
    void shouldFindAllExercisesByOrganizationIdSuccessfully() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Organization organization = createOrganization(1L);
        Exercise exercise = createExercise(1L, 1L);
        ExerciseResponse expectedResponse = mock(ExerciseResponse.class);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(exerciseRepository.findByOrganizationIdAndActiveTrue(1L)).thenReturn(List.of(exercise));
        when(exerciseMapper.toResponse(exercise)).thenReturn(expectedResponse);

        List<ExerciseResponse> response = exerciseService.findAllByOrganizationId(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertSame(expectedResponse, response.get(0));

        verify(organizationRepository).findById(1L);
        verify(exerciseRepository).findByOrganizationIdAndActiveTrue(1L);
        verify(exerciseRepository, never()).findByOrganizationId(1L);
        verify(exerciseMapper).toResponse(exercise);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenFindAllExercisesByAnotherOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        assertThrows(AccessDeniedException.class, () ->
                exerciseService.findAllByOrganizationId(2L)
        );

        verifyNoInteractions(organizationRepository);
        verifyNoInteractions(exerciseRepository);
        verifyNoInteractions(exerciseMapper);
    }

    @Test
    void shouldFindExerciseByIdSuccessfully() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Exercise exercise = createExercise(1L, 1L);
        ExerciseResponse expectedResponse = mock(ExerciseResponse.class);

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(exerciseMapper.toResponse(exercise)).thenReturn(expectedResponse);

        ExerciseResponse response = exerciseService.findById(1L);

        assertNotNull(response);
        assertSame(expectedResponse, response);

        verify(exerciseRepository).findById(1L);
        verify(exerciseMapper).toResponse(exercise);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenFindExerciseByIdFromAnotherOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Exercise exercise = createExercise(1L, 2L);

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        assertThrows(AccessDeniedException.class, () ->
                exerciseService.findById(1L)
        );

        verify(exerciseRepository).findById(1L);
        verifyNoInteractions(exerciseMapper);
    }

    @Test
    void shouldUpdateExerciseSuccessfully() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Exercise exercise = createExercise(1L, 1L);

        UpdateExerciseRequest request = new UpdateExerciseRequest(
                "Supino inclinado",
                "Peito",
                "Descrição atualizada",
                "Halteres",
                null,
                null
        );

        ExerciseResponse expectedResponse = mock(ExerciseResponse.class);

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        when(exerciseRepository.existsByOrganizationIdAndExerciseNameIgnoreCaseAndIdNot(
                1L,
                request.exerciseName(),
                1L
        )).thenReturn(false);

        when(exerciseRepository.save(exercise)).thenReturn(exercise);
        when(exerciseMapper.toResponse(exercise)).thenReturn(expectedResponse);

        ExerciseResponse response = exerciseService.update(1L, request);

        assertNotNull(response);
        assertSame(expectedResponse, response);

        verify(exerciseRepository).findById(1L);
        verify(exerciseRepository).existsByOrganizationIdAndExerciseNameIgnoreCaseAndIdNot(
                1L,
                request.exerciseName(),
                1L
        );
        verify(exerciseMapper).updateEntity(exercise, request);
        verify(exerciseRepository).save(exercise);
        verify(exerciseMapper).toResponse(exercise);
    }

    @Test
    void shouldThrowDuplicateResourceExceptionWhenUpdateExerciseWithDuplicatedNameInSameOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Exercise exercise = createExercise(1L, 1L);

        UpdateExerciseRequest request = new UpdateExerciseRequest(
                "Agachamento livre",
                "Pernas",
                "Descrição atualizada",
                "Barra",
                null,
                null
        );

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        when(exerciseRepository.existsByOrganizationIdAndExerciseNameIgnoreCaseAndIdNot(
                1L,
                request.exerciseName(),
                1L
        )).thenReturn(true);

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () ->
                exerciseService.update(1L, request)
        );

        assertEquals("Já existe um exercício com este nome nesta organização.", exception.getMessage());

        verify(exerciseRepository).findById(1L);
        verify(exerciseRepository).existsByOrganizationIdAndExerciseNameIgnoreCaseAndIdNot(
                1L,
                request.exerciseName(),
                1L
        );
        verify(exerciseMapper, never()).updateEntity(any(), any());
        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUpdateExerciseFromAnotherOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Exercise exercise = createExercise(1L, 2L);

        UpdateExerciseRequest request = new UpdateExerciseRequest(
                "Supino inclinado",
                null,
                null,
                null,
                null,
                null
        );

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        assertThrows(AccessDeniedException.class, () ->
                exerciseService.update(1L, request)
        );

        verify(exerciseRepository).findById(1L);
        verify(exerciseMapper, never()).updateEntity(any(), any());
        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void shouldDeleteExerciseSuccessfully() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Exercise exercise = createExercise(1L, 1L);

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(exerciseRepository.save(exercise)).thenReturn(exercise);

        exerciseService.delete(1L);

        assertFalse(exercise.getActive());

        verify(exerciseRepository).findById(1L);
        verify(exerciseRepository).save(exercise);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenDeleteExerciseFromAnotherOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Exercise exercise = createExercise(1L, 2L);

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        assertThrows(AccessDeniedException.class, () ->
                exerciseService.delete(1L)
        );

        verify(exerciseRepository).findById(1L);
        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void shouldUploadExerciseImageSuccessfully() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Exercise exercise = createExercise(1L, 1L);
        ExerciseResponse expectedResponse = mock(ExerciseResponse.class);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.png",
                "image/png",
                "image-content".getBytes()
        );

        String imageUrl = "https://res.cloudinary.com/gymflow/image/upload/supino.png";

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(cloudinaryStorageService.uploadExerciseMedia(file, ExerciseMediaType.IMAGE))
                .thenReturn(imageUrl);
        when(exerciseRepository.save(exercise)).thenReturn(exercise);
        when(exerciseMapper.toResponse(exercise)).thenReturn(expectedResponse);

        ExerciseResponse response = exerciseService.uploadExerciseImage(1L, file);

        assertNotNull(response);
        assertSame(expectedResponse, response);
        assertEquals(imageUrl, exercise.getImageUrl());
        assertNull(exercise.getVideoUrl());

        verify(exerciseRepository).findById(1L);
        verify(cloudinaryStorageService).uploadExerciseMedia(file, ExerciseMediaType.IMAGE);
        verify(exerciseRepository).save(exercise);
        verify(exerciseMapper).toResponse(exercise);
    }

    @Test
    void shouldUploadExerciseVideoSuccessfully() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Exercise exercise = createExercise(1L, 1L);
        ExerciseResponse expectedResponse = mock(ExerciseResponse.class);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.mp4",
                "video/mp4",
                "video-content".getBytes()
        );

        String videoUrl = "https://res.cloudinary.com/gymflow/video/upload/supino.mp4";

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(cloudinaryStorageService.uploadExerciseMedia(file, ExerciseMediaType.VIDEO))
                .thenReturn(videoUrl);
        when(exerciseRepository.save(exercise)).thenReturn(exercise);
        when(exerciseMapper.toResponse(exercise)).thenReturn(expectedResponse);

        ExerciseResponse response = exerciseService.uploadExerciseVideo(1L, file);

        assertNotNull(response);
        assertSame(expectedResponse, response);
        assertNull(exercise.getImageUrl());
        assertEquals(videoUrl, exercise.getVideoUrl());

        verify(exerciseRepository).findById(1L);
        verify(cloudinaryStorageService).uploadExerciseMedia(file, ExerciseMediaType.VIDEO);
        verify(exerciseRepository).save(exercise);
        verify(exerciseMapper).toResponse(exercise);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUploadImageToNonExistingExercise() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.png",
                "image/png",
                "image-content".getBytes()
        );

        when(exerciseRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                exerciseService.uploadExerciseImage(1L, file)
        );

        assertEquals("Exercise not found with id: 1", exception.getMessage());

        verify(exerciseRepository).findById(1L);
        verifyNoInteractions(cloudinaryStorageService);
        verify(exerciseRepository, never()).save(any());
        verifyNoInteractions(exerciseMapper);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUploadVideoToNonExistingExercise() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.mp4",
                "video/mp4",
                "video-content".getBytes()
        );

        when(exerciseRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                exerciseService.uploadExerciseVideo(1L, file)
        );

        assertEquals("Exercise not found with id: 1", exception.getMessage());

        verify(exerciseRepository).findById(1L);
        verifyNoInteractions(cloudinaryStorageService);
        verify(exerciseRepository, never()).save(any());
        verifyNoInteractions(exerciseMapper);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUploadImageToExerciseFromAnotherOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Exercise exercise = createExercise(1L, 2L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.png",
                "image/png",
                "image-content".getBytes()
        );

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        assertThrows(AccessDeniedException.class, () ->
                exerciseService.uploadExerciseImage(1L, file)
        );

        verify(exerciseRepository).findById(1L);
        verifyNoInteractions(cloudinaryStorageService);
        verify(exerciseRepository, never()).save(any());
        verifyNoInteractions(exerciseMapper);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUploadVideoToExerciseFromAnotherOrganization() {
        User admin = createUser(1L, UserRole.ADMIN, 1L);
        authenticate(admin);

        Exercise exercise = createExercise(1L, 2L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.mp4",
                "video/mp4",
                "video-content".getBytes()
        );

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        assertThrows(AccessDeniedException.class, () ->
                exerciseService.uploadExerciseVideo(1L, file)
        );

        verify(exerciseRepository).findById(1L);
        verifyNoInteractions(cloudinaryStorageService);
        verify(exerciseRepository, never()).save(any());
        verifyNoInteractions(exerciseMapper);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenStudentCreatesExercise() {
        User student = createUser(1L, UserRole.STUDENT, 1L);
        authenticate(student);

        CreateExerciseRequest request = new CreateExerciseRequest(
                "Exercício Indevido",
                "Peito",
                "Aluno não deve criar exercício",
                "Barra",
                null,
                null
        );

        assertThrows(AccessDeniedException.class, () ->
                exerciseService.create(request)
        );

        verifyNoInteractions(organizationRepository);
        verifyNoInteractions(exerciseRepository);
        verifyNoInteractions(exerciseMapper);
        verifyNoInteractions(cloudinaryStorageService);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenStudentUpdatesExercise() {
        User student = createUser(1L, UserRole.STUDENT, 1L);
        authenticate(student);

        UpdateExerciseRequest request = new UpdateExerciseRequest(
                "Supino inclinado",
                "Peito",
                "Descrição atualizada",
                "Halteres",
                null,
                null
        );

        assertThrows(AccessDeniedException.class, () ->
                exerciseService.update(1L, request)
        );

        verifyNoInteractions(organizationRepository);
        verifyNoInteractions(exerciseRepository);
        verifyNoInteractions(exerciseMapper);
        verifyNoInteractions(cloudinaryStorageService);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenStudentDeletesExercise() {
        User student = createUser(1L, UserRole.STUDENT, 1L);
        authenticate(student);

        assertThrows(AccessDeniedException.class, () ->
                exerciseService.delete(1L)
        );

        verifyNoInteractions(organizationRepository);
        verifyNoInteractions(exerciseRepository);
        verifyNoInteractions(exerciseMapper);
        verifyNoInteractions(cloudinaryStorageService);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenStudentUploadsExerciseImage() {
        User student = createUser(1L, UserRole.STUDENT, 1L);
        authenticate(student);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.png",
                "image/png",
                "image-content".getBytes()
        );

        assertThrows(AccessDeniedException.class, () ->
                exerciseService.uploadExerciseImage(1L, file)
        );

        verifyNoInteractions(organizationRepository);
        verifyNoInteractions(exerciseRepository);
        verifyNoInteractions(exerciseMapper);
        verifyNoInteractions(cloudinaryStorageService);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenStudentUploadsExerciseVideo() {
        User student = createUser(1L, UserRole.STUDENT, 1L);
        authenticate(student);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.mp4",
                "video/mp4",
                "video-content".getBytes()
        );

        assertThrows(AccessDeniedException.class, () ->
                exerciseService.uploadExerciseVideo(1L, file)
        );

        verifyNoInteractions(organizationRepository);
        verifyNoInteractions(exerciseRepository);
        verifyNoInteractions(exerciseMapper);
        verifyNoInteractions(cloudinaryStorageService);
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

    private Exercise createExercise(Long id, Long organizationId) {
        Organization organization = createOrganization(organizationId);

        Exercise exercise = new Exercise();
        exercise.setId(id);
        exercise.setExerciseName("Supino reto");
        exercise.setMuscleGroup("Peito");
        exercise.setDescription("Exercício para peitoral");
        exercise.setEquipmentName("Barra");
        exercise.setImageUrl(null);
        exercise.setVideoUrl(null);
        exercise.setActive(true);
        exercise.setOrganization(organization);

        return exercise;
    }
}