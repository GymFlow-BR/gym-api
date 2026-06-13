package br.com.gymflow.api.mapper;

import br.com.gymflow.api.domain.StudentWorkout;
import br.com.gymflow.api.dto.studentWorkouts.CreateStudentWorkoutRequest;
import br.com.gymflow.api.dto.studentWorkouts.StudentWorkoutResponse;
import org.springframework.stereotype.Component;

@Component
public class StudentWorkoutMapper {

    public StudentWorkout toEntity(CreateStudentWorkoutRequest request) {
        return new StudentWorkout();
    }

    public StudentWorkoutResponse toResponse(StudentWorkout studentWorkout) {
        return new StudentWorkoutResponse(
                studentWorkout.getId(),
                studentWorkout.getStudent().getId(),
                studentWorkout.getWorkout().getId(),
                studentWorkout.getAssignedAt(),
                studentWorkout.getStatus(),
                studentWorkout.getCreatedAt(),
                studentWorkout.getUpdatedAt()
        );
    }
}