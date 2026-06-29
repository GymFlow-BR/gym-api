package br.com.gymflow.api.mapper;

import br.com.gymflow.api.domain.StudentWorkout;
import br.com.gymflow.api.domain.WorkoutExercise;
import br.com.gymflow.api.dto.studentWorkouts.CreateStudentWorkoutRequest;
import br.com.gymflow.api.dto.studentWorkouts.StudentCurrentWorkoutExerciseResponse;
import br.com.gymflow.api.dto.studentWorkouts.StudentCurrentWorkoutResponse;
import br.com.gymflow.api.dto.studentWorkouts.StudentWorkoutResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentWorkoutMapper {

    public StudentWorkout toEntity(CreateStudentWorkoutRequest request) {
        return new StudentWorkout();
    }

    public StudentWorkoutResponse toResponse(StudentWorkout studentWorkout) {
        return new StudentWorkoutResponse(
                studentWorkout.getId(),
                studentWorkout.getStudent().getId(),
                studentWorkout.getStudent().getName(),
                studentWorkout.getWorkout().getId(),
                studentWorkout.getWorkout().getWorkoutName(),
                studentWorkout.getAssignedAt(),
                studentWorkout.getStatus(),
                studentWorkout.getCreatedAt(),
                studentWorkout.getUpdatedAt()
        );
    }

    public StudentCurrentWorkoutResponse toCurrentWorkoutResponse(
            StudentWorkout studentWorkout,
            List<WorkoutExercise> workoutExercises
    ) {
        List<StudentCurrentWorkoutExerciseResponse> exercises = workoutExercises
                .stream()
                .map(this::toCurrentWorkoutExerciseResponse)
                .toList();

        return new StudentCurrentWorkoutResponse(
                studentWorkout.getStudent().getId(),
                studentWorkout.getId(),
                studentWorkout.getWorkout().getId(),
                studentWorkout.getWorkout().getWorkoutName(),
                studentWorkout.getAssignedAt(),
                studentWorkout.getStatus(),
                exercises
        );
    }

    private StudentCurrentWorkoutExerciseResponse toCurrentWorkoutExerciseResponse(
            WorkoutExercise workoutExercise
    ) {
        return new StudentCurrentWorkoutExerciseResponse(
                workoutExercise.getId(),
                workoutExercise.getExercise().getId(),
                workoutExercise.getExercise().getExerciseName(),
                workoutExercise.getExercise().getEquipmentName(),
                workoutExercise.getExercise().getMuscleGroup(),
                workoutExercise.getExercise().getDescription(),
                workoutExercise.getExerciseOrder(),
                workoutExercise.getSets(),
                workoutExercise.getReps(),
                workoutExercise.getRecommendedLoad(),
                workoutExercise.getRestTimeSeconds(),
                workoutExercise.getNotes(),
                workoutExercise.getExercise().getImageUrl(),
                workoutExercise.getExercise().getVideoUrl()
        );
    }
}