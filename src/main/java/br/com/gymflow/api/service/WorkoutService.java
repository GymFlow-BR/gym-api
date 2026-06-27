package br.com.gymflow.api.service;

import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.Workout;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static br.com.gymflow.api.domain.enums.WorkoutStatus.INACTIVE;

@RequiredArgsConstructor
@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final WorkoutMapper workoutMapper;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;


    @Transactional
    public WorkoutResponse create(CreateWorkoutRequest request) {
        User teacher = getUserById(request.teacherId());

        validateSameOrganization(teacher.getOrganization().getId());
        validateUserCanCreateWorkout(teacher);

        Workout workout = workoutMapper.toEntity(request);
        workout.setTeacher(teacher);
        workout.setStatus(WorkoutStatus.ACTIVE);

        Workout savedWorkout = workoutRepository.save(workout);

        return workoutMapper.toResponse(savedWorkout);
    }

    @Transactional(readOnly = true)
    public List<WorkoutResponse> findAll() {
        Long organizationId = getAuthenticatedUserOrganizationId();

        return workoutRepository.findByTeacherOrganizationIdAndStatus(organizationId, WorkoutStatus.ACTIVE)
                .stream()
                .map(workoutMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutResponse> findAllByOrganizationId(Long organizationId) {
        validateSameOrganization(organizationId);
        getOrganizationById(organizationId);

        return workoutRepository.findByTeacherOrganizationIdAndStatus(organizationId, WorkoutStatus.ACTIVE)
                .stream()
                .map(workoutMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutResponse findById(Long id) {
        Workout workout = getWorkoutById(id);

        validateWorkoutBelongsToAuthenticatedOrganization(workout);

        return workoutMapper.toResponse(workout);
    }


    @Transactional
    public WorkoutResponse patch(Long id, UpdateWorkoutRequest request) {
        Workout workout = getWorkoutById(id);

        validateWorkoutBelongsToAuthenticatedOrganization(workout);

        if (request.workoutName() != null) {
            workout.setWorkoutName(request.workoutName());
        }

        if (request.status() != null) {
            workout.setStatus(request.status());
        }

        Workout savedWorkout = workoutRepository.save(workout);

        return workoutMapper.toResponse(savedWorkout);
    }


    @Transactional
    public void delete(Long id) {
        Workout workout = getWorkoutById(id);

        validateWorkoutBelongsToAuthenticatedOrganization(workout);

        workout.setStatus(INACTIVE);

        workoutRepository.save(workout);
    }


    private Workout getWorkoutById(Long id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found with id: " + id));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + userId));
    }

    private void validateUserCanCreateWorkout(User user) {
        if (user.getRole() != UserRole.TEACHER && user.getRole() != UserRole.ADMIN) {
            throw new BusinessRuleException("User is not allowed to create workouts with id: " + user.getId());
        }
    }

    private void getOrganizationById(Long organizationId) {
        organizationRepository.findById(organizationId)
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

    private void validateWorkoutBelongsToAuthenticatedOrganization(Workout workout) {
        validateSameOrganization(workout.getTeacher().getOrganization().getId());
    }
}