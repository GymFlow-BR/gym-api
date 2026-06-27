package br.com.gymflow.api.service;

import br.com.gymflow.api.domain.Exercise;
import br.com.gymflow.api.domain.Organization;
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

import java.util.List;

@RequiredArgsConstructor
@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final OrganizationRepository organizationRepository;
    private final ExerciseMapper exerciseMapper;

    @Transactional
    public ExerciseResponse create(CreateExerciseRequest request) {
        validateSameOrganization(request.organizationId());

        Organization organization = getOrganizationById(request.organizationId());

        Exercise exercise = exerciseMapper.toEntity(request);
        exercise.setOrganization(organization);

        Exercise savedExercise = exerciseRepository.save(exercise);

        return exerciseMapper.toResponse(savedExercise);
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> findAll() {
        Long organizationId = getAuthenticatedUserOrganizationId();

        return exerciseRepository.findByOrganizationId(organizationId)
                .stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> findAllByOrganizationId(Long organizationId) {
        validateSameOrganization(organizationId);
        getOrganizationById(organizationId);


        return exerciseRepository.findByOrganizationId(organizationId)
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
        Exercise exercise = getExerciseById(id);

        validateExerciseBelongsToAuthenticatedOrganization(exercise);

        exerciseMapper.updateEntity(exercise, request);

        Exercise updatedExercise = exerciseRepository.save(exercise);

        return exerciseMapper.toResponse(updatedExercise);
    }

    @Transactional
    public void delete(Long id) {
        Exercise exercise = getExerciseById(id);

        validateExerciseBelongsToAuthenticatedOrganization(exercise);

        exercise.setActive(false);

        exerciseRepository.save(exercise);
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
}