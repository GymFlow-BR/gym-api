package br.com.gymflow.api.mapper;

import br.com.gymflow.api.domain.WorkoutExercise;
import br.com.gymflow.api.dto.workoutExercise.CreateWorkoutExerciseRequest;
import br.com.gymflow.api.dto.workoutExercise.WorkoutExerciseResponse;
import org.springframework.stereotype.Component;

@Component
public class WorkoutExerciseMapper {

    public WorkoutExercise toEntity(CreateWorkoutExerciseRequest request) {
        WorkoutExercise workoutExercise = new WorkoutExercise();

        workoutExercise.setExerciseOrder(request.exerciseOrder());
        workoutExercise.setNotes(request.notes());
        workoutExercise.setRecommendedLoad(request.recommendedLoad());
        workoutExercise.setReps(request.reps());
        workoutExercise.setSets(request.sets());
        workoutExercise.setRestTimeSeconds(request.restTimeSeconds());

        return workoutExercise;
    }


    public WorkoutExerciseResponse toResponse(WorkoutExercise workoutExercise) {
        return new WorkoutExerciseResponse(
                workoutExercise.getId(),
                workoutExercise.getWorkout().getId(),
                workoutExercise.getExercise().getId(),
                workoutExercise.getExercise().getExerciseName(),
                workoutExercise.getExercise().getMuscleGroup(),
                workoutExercise.getExercise().getEquipmentName(),
                workoutExercise.getExerciseOrder(),
                workoutExercise.getSets(),
                workoutExercise.getReps(),
                workoutExercise.getRecommendedLoad(),
                workoutExercise.getRestTimeSeconds(),
                workoutExercise.getNotes(),
                workoutExercise.getCreatedAt(),
                workoutExercise.getUpdatedAt()
        );
    }
}