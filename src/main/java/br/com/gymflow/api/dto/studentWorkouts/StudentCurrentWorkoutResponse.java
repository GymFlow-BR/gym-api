package br.com.gymflow.api.dto.studentWorkouts;

import br.com.gymflow.api.domain.enums.WeekDay;
import br.com.gymflow.api.domain.enums.WorkoutStatus;

import java.time.LocalDateTime;
import java.util.List;

public record StudentCurrentWorkoutResponse(

        Long studentId,
        Long studentWorkoutId,
        Long workoutId,
        String workoutName,
        String teacherName,
        LocalDateTime assignedAt,
        WeekDay weekDay,
        WorkoutStatus status,
        List<StudentCurrentWorkoutExerciseResponse> exercises
) {
}