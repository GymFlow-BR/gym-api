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
import br.com.gymflow.api.repository.UserRepository;
import br.com.gymflow.api.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static br.com.gymflow.api.domain.enums.WorkoutStatus.INACTIVE;

@RequiredArgsConstructor
@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final WorkoutMapper workoutMapper;
    private final UserRepository userRepository;


    @Transactional
    public WorkoutResponse create(CreateWorkoutRequest request) {
        User teacher = getTeacherById(request.teacherId());

        Workout workout = workoutMapper.toEntity(request);
        workout.setTeacher(teacher);
        workout.setStatus(WorkoutStatus.ACTIVE);

        Workout savedWorkout = workoutRepository.save(workout);

        return workoutMapper.toResponse(savedWorkout);
    }

    @Transactional(readOnly = true)
    public List<WorkoutResponse> findAll() {
        return workoutRepository.findAll()
                .stream()
                .map(workoutMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutResponse findById(Long id) {
        Workout workout = getWorkoutById(id);

        return workoutMapper.toResponse(workout);
    }


    @Transactional
    public WorkoutResponse patch(Long id, UpdateWorkoutRequest request) {
        Workout workout = getWorkoutById(id);

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

        workout.setStatus(INACTIVE);

        workoutRepository.save(workout);
    }


    private Workout getWorkoutById(Long id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found with id: " + id));
    }

    private User getTeacherById(Long teacherId) {

        User user =  userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        if (user.getRole() != UserRole.TEACHER && user.getRole() != UserRole.ADMIN) {
            throw new BusinessRuleException("User is not allowed to create workouts with id: " + teacherId);
        }

        return user;
    }
}