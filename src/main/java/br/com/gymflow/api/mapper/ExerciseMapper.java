package br.com.gymflow.api.mapper;

import br.com.gymflow.api.domain.Exercise;
import br.com.gymflow.api.dto.exercise.CreateExerciseRequest;
import br.com.gymflow.api.dto.exercise.ExerciseResponse;
import br.com.gymflow.api.dto.exercise.UpdateExerciseRequest;
import org.springframework.stereotype.Component;

@Component
public class ExerciseMapper {

    public Exercise toEntity(CreateExerciseRequest request) {
        Exercise exercise = new Exercise();

        exercise.setExerciseName(request.exerciseName());
        exercise.setMuscleGroup(request.muscleGroup());
        exercise.setDescription(request.description());
        exercise.setEquipmentName(request.equipmentName());
        exercise.setVideoUrl(request.videoUrl());
        exercise.setImageUrl(request.imageUrl());

        return exercise;
    }

    public ExerciseResponse toResponse(Exercise exercise) {
        return new ExerciseResponse(
                exercise.getId(),
                exercise.getOrganization().getId(),
                exercise.getExerciseName(),
                exercise.getMuscleGroup(),
                exercise.getDescription(),
                exercise.getEquipmentName(),
                exercise.getImageUrl(),
                exercise.getVideoUrl(),
                exercise.getCreatedAt(),
                exercise.getUpdatedAt()
        );
    }

    public void updateEntity(Exercise exercise, UpdateExerciseRequest request) {
        exercise.setExerciseName(request.exerciseName());
        exercise.setMuscleGroup(request.muscleGroup());
        exercise.setDescription(request.description());
        exercise.setEquipmentName(request.equipmentName());
        exercise.setImageUrl(request.imageUrl());
        exercise.setVideoUrl(request.videoUrl());
    }
}
