package br.com.gymflow.api.mapper;

import br.com.gymflow.api.domain.Workout;
import br.com.gymflow.api.dto.workout.CreateWorkoutRequest;
import br.com.gymflow.api.dto.workout.UpdateWorkoutRequest;
import br.com.gymflow.api.dto.workout.WorkoutResponse;
import org.springframework.stereotype.Component;

@Component
public class WorkoutMapper {

    public Workout toEntity(CreateWorkoutRequest request) {
        Workout workout = new Workout();

        workout.setWorkoutName(request.workoutName());

        return workout;
    }

    public WorkoutResponse toResponse(Workout workout) {
        return new WorkoutResponse(
                workout.getId(),
                workout.getTeacher().getId(),
                workout.getWorkoutName(),
                workout.getStatus(),
                workout.getCreatedAt(),
                workout.getUpdatedAt()
        );
    }
}