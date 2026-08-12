package br.com.gymflow.api.dto.studentWorkouts;

import br.com.gymflow.api.domain.enums.WeekDay;
import br.com.gymflow.api.domain.enums.WorkoutStatus;

import java.time.LocalDateTime;

public record StudentWorkoutResponse(

        Long studentWorkoutId,
        Long studentId,
        String studentName,
        Long workoutId,
        String workoutName,
        String teacherName,
        LocalDateTime assignedAt,
        WeekDay weekDay,
        WorkoutStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}