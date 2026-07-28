package br.com.gymflow.api.service;

import br.com.gymflow.api.domain.Exercise;
import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.domain.enums.ExerciseMediaType;
import br.com.gymflow.api.domain.enums.UserRole;
import br.com.gymflow.api.dto.exercise.CreateExerciseRequest;
import br.com.gymflow.api.dto.exercise.ExerciseResponse;
import br.com.gymflow.api.dto.exercise.UpdateExerciseRequest;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.mapper.ExerciseMapper;
import br.com.gymflow.api.repository.ExerciseRepository;
import br.com.gymflow.api.repository.OrganizationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import br.com.gymflow.api.domain.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import br.com.gymflow.api.exception.DuplicateResourceException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final OrganizationRepository organizationRepository;
    private final ExerciseMapper exerciseMapper;
    private final CloudinaryStorageService cloudinaryStorageService;

    @Transactional
    public ExerciseResponse create(CreateExerciseRequest request) {
        User authenticatedUser = getAuthenticatedUser();

        validateUserCanManageExercises(authenticatedUser);

        Organization organization = authenticatedUser.getOrganization();

        validateExerciseNameDoesNotExistOnCreate(
                organization.getId(),
                request.exerciseName()
        );

        Exercise exercise = exerciseMapper.toEntity(request);
        exercise.setOrganization(organization);

        Exercise savedExercise = exerciseRepository.save(exercise);

        return exerciseMapper.toResponse(savedExercise);
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> findAll() {
        Long organizationId = getAuthenticatedUserOrganizationId();

        return exerciseRepository.findByOrganizationIdAndActiveTrue(organizationId)
                .stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> findAllByOrganizationId(Long organizationId) {
        validateSameOrganization(organizationId);
        getOrganizationById(organizationId);


        return exerciseRepository.findByOrganizationIdAndActiveTrue(organizationId)
                .stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseResponse findById(Long id) {
        Exercise exercise = getExerciseById(id);

        validateExerciseBelongsToAuthenticatedOrganization(exercise);

        return exerciseMapper.toResponse(exercise);
    }

    @Transactional
    public ExerciseResponse update(Long id, UpdateExerciseRequest request) {
        User authenticatedUser = getAuthenticatedUser();

        validateUserCanManageExercises(authenticatedUser);

        Exercise exercise = getExerciseById(id);

        validateExerciseBelongsToAuthenticatedOrganization(exercise);

        validateExerciseNameDoesNotExistOnUpdate(
                exercise.getOrganization().getId(),
                request.exerciseName(),
                exercise.getId()
        );

        exerciseMapper.updateEntity(exercise, request);

        Exercise updatedExercise = exerciseRepository.save(exercise);

        return exerciseMapper.toResponse(updatedExercise);
    }

    @Transactional
    public void delete(Long id) {
        User authenticatedUser = getAuthenticatedUser();

        validateUserCanManageExercises(authenticatedUser);

        Exercise exercise = getExerciseById(id);

        validateExerciseBelongsToAuthenticatedOrganization(exercise);

        exercise.setActive(false);

        exerciseRepository.save(exercise);
    }

    @Transactional
    public ExerciseResponse uploadExerciseImage(Long exerciseId, MultipartFile file) {
        User authenticatedUser = getAuthenticatedUser();

        validateUserCanManageExercises(authenticatedUser);

        Exercise exercise = getExerciseById(exerciseId);

        validateExerciseBelongsToAuthenticatedOrganization(exercise);

        String imageUrl = cloudinaryStorageService.uploadExerciseMedia(
                file,
                ExerciseMediaType.IMAGE
        );

        exercise.setImageUrl(imageUrl);

        Exercise savedExercise = exerciseRepository.save(exercise);

        return exerciseMapper.toResponse(savedExercise);
    }

    @Transactional
    public ExerciseResponse uploadExerciseVideo(Long exerciseId, MultipartFile file) {
        User authenticatedUser = getAuthenticatedUser();

        validateUserCanManageExercises(authenticatedUser);

        Exercise exercise = getExerciseById(exerciseId);

        validateExerciseBelongsToAuthenticatedOrganization(exercise);

        String videoUrl = cloudinaryStorageService.uploadExerciseMedia(
                file,
                ExerciseMediaType.VIDEO
        );

        exercise.setVideoUrl(videoUrl);

        Exercise savedExercise = exerciseRepository.save(exercise);

        return exerciseMapper.toResponse(savedExercise);
    }


    private Exercise getExerciseById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with id: " + id));
    }

    private Organization getOrganizationById(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Access denied");
        }

        return user;
    }

    private Long getAuthenticatedUserOrganizationId() {
        return getAuthenticatedUser().getOrganization().getId();
    }

    private void validateSameOrganization(Long organizationId) {
        Long authenticatedUserOrganizationId = getAuthenticatedUserOrganizationId();

        if (!authenticatedUserOrganizationId.equals(organizationId)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private void validateExerciseBelongsToAuthenticatedOrganization(Exercise exercise) {
        validateSameOrganization(exercise.getOrganization().getId());
    }

    private void validateUserCanManageExercises(User user) {
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.TEACHER) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private void validateExerciseNameDoesNotExistOnCreate(Long organizationId, String exerciseName) {
        boolean exerciseNameAlreadyExists =
                exerciseRepository.existsByOrganizationIdAndExerciseNameIgnoreCase(
                        organizationId,
                        exerciseName
                );

        if (exerciseNameAlreadyExists) {
            throw new DuplicateResourceException("Já existe um exercício com este nome nesta organização.");
        }
    }

    private void validateExerciseNameDoesNotExistOnUpdate(Long organizationId, String exerciseName, Long exerciseId) {
        if (exerciseName == null) {
            return;
        }

        boolean exerciseNameAlreadyExists =
                exerciseRepository.existsByOrganizationIdAndExerciseNameIgnoreCaseAndIdNot(
                        organizationId,
                        exerciseName,
                        exerciseId
                );

        if (exerciseNameAlreadyExists) {
            throw new DuplicateResourceException("Já existe um exercício com este nome nesta organização.");
        }
    }


}