package br.com.gymflow.api.service;

import br.com.gymflow.api.domain.Exercise;
import br.com.gymflow.api.domain.Workout;
import br.com.gymflow.api.domain.WorkoutExercise;
import br.com.gymflow.api.dto.workoutExercise.CreateWorkoutExerciseRequest;
import br.com.gymflow.api.dto.workoutExercise.PatchWorkoutExerciseRequest;
import br.com.gymflow.api.dto.workoutExercise.WorkoutExerciseResponse;
import br.com.gymflow.api.exception.BusinessRuleException;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.mapper.WorkoutExerciseMapper;
import br.com.gymflow.api.repository.ExerciseRepository;
import br.com.gymflow.api.repository.WorkoutExerciseRepository;
import br.com.gymflow.api.repository.WorkoutRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import br.com.gymflow.api.exception.DuplicateResourceException;

import java.util.List;


@Service
@RequiredArgsConstructor
public class WorkoutExerciseService {

    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutExerciseMapper workoutExerciseMapper;
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;

    @Transactional
    public WorkoutExerciseResponse create(Long workoutId, CreateWorkoutExerciseRequest request) {
        Workout workout = getWorkoutById(workoutId);
        Exercise exercise = getExerciseById(request.exerciseId());

        validateExerciseBelongsToWorkoutOrganization(workout, exercise);
        validateExerciseOrderIsAvailable(workoutId, request.exerciseOrder());

        WorkoutExercise workoutExercise = workoutExerciseMapper.toEntity(request);
        workoutExercise.setWorkout(workout);
        workoutExercise.setExercise(exercise);

        WorkoutExercise savedWorkoutExercise = workoutExerciseRepository.save(workoutExercise);

        return workoutExerciseMapper.toResponse(savedWorkoutExercise);
    }


    @Transactional(readOnly = true)
    public List<WorkoutExerciseResponse> findAllByWorkoutId(Long workoutId) {
        getWorkoutById(workoutId);
        return workoutExerciseRepository.findAllByWorkoutId(workoutId)
                .stream()
                .map(workoutExerciseMapper::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public WorkoutExerciseResponse findById(Long workoutId, Long workoutExerciseId) {
        WorkoutExercise workoutExercise = getWorkoutExerciseById(workoutExerciseId);

        validateWorkoutExerciseBelongsToWorkout(workoutExercise, workoutId);

        return workoutExerciseMapper.toResponse(workoutExercise);
    }


    @Transactional
    public WorkoutExerciseResponse patch(Long workoutId, Long workoutExerciseId, PatchWorkoutExerciseRequest request) {
        WorkoutExercise workoutExercise = getWorkoutExerciseById(workoutExerciseId);

        validateWorkoutExerciseBelongsToWorkout(workoutExercise, workoutId);

        if (request.exerciseOrder() != null) {
            workoutExercise.setExerciseOrder(request.exerciseOrder());
        }

        if (request.reps() != null) {
            workoutExercise.setReps(request.reps());
        }

        if (request.sets() != null) {
            workoutExercise.setSets(request.sets());
        }

        if (request.recommendedLoad() != null) {
            workoutExercise.setRecommendedLoad(request.recommendedLoad());
        }

        if (request.restTimeSeconds() != null) {
            workoutExercise.setRestTimeSeconds(request.restTimeSeconds());
        }

        if (request.notes() != null) {
            workoutExercise.setNotes(request.notes());
        }

        WorkoutExercise updatedWorkoutExercise = workoutExerciseRepository.save(workoutExercise);

        return workoutExerciseMapper.toResponse(updatedWorkoutExercise);
    }


    @Transactional
    public void delete(Long workoutId, Long workoutExerciseId) {
        WorkoutExercise workoutExercise = getWorkoutExerciseById(workoutExerciseId);

        validateWorkoutExerciseBelongsToWorkout(workoutExercise, workoutId);

        workoutExerciseRepository.delete(workoutExercise);
    }


    private WorkoutExercise getWorkoutExerciseById(Long id) {
        return workoutExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise not found with id: " + id));
    }

    private void validateWorkoutExerciseBelongsToWorkout(WorkoutExercise workoutExercise, Long workoutId) {
        if (!workoutExercise.getWorkout().getId().equals(workoutId)) {
            throw new ResourceNotFoundException(
                    "Workout exercise not found with id: " + workoutExercise.getId() + " for workout id: " + workoutId
            );
        }
    }

    private void validateExerciseBelongsToWorkoutOrganization(Workout workout, Exercise exercise) {
        Long workoutOrganizationId = workout.getTeacher().getOrganization().getId();
        Long exerciseOrganizationId = exercise.getOrganization().getId();

        if (!workoutOrganizationId.equals(exerciseOrganizationId)) {
            throw new BusinessRuleException(
                    "Exercise does not belong to the same organization as the workout"
            );
        }
    }

    private Workout getWorkoutById(Long workoutId) {
        return workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found with id: " + workoutId));
    }

    private Exercise getExerciseById(Long exerciseId) {
        return exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with id: " + exerciseId));
    }

    private void validateExerciseOrderIsAvailable(Long workoutId, Integer exerciseOrder) {
        boolean alreadyExists = workoutExerciseRepository.existsByWorkoutIdAndExerciseOrder(
                workoutId,
                exerciseOrder
        );

        if (alreadyExists) {
            throw new DuplicateResourceException(
                    "Workout already has an exercise with this order"
            );
        }
    }
}