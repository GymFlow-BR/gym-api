package br.com.gymflow.api.controller;

import br.com.gymflow.api.dto.studentWorkoutProgress.StudentCurrentWorkoutProgressResponse;
import br.com.gymflow.api.dto.studentWorkoutProgress.StudentWorkoutExerciseProgressResponse;
import br.com.gymflow.api.service.StudentWorkoutProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/students/{studentId}/workouts/current")
@RestController
@RequiredArgsConstructor
public class StudentWorkoutProgressController {

    private final StudentWorkoutProgressService studentWorkoutProgressService;

    @PatchMapping("/exercises/{workoutExerciseId}/complete")
    public ResponseEntity<StudentWorkoutExerciseProgressResponse> completeExercise(
            @PathVariable Long studentId,
            @PathVariable Long workoutExerciseId
    ) {
        StudentWorkoutExerciseProgressResponse response =
                studentWorkoutProgressService.completeExercise(studentId, workoutExerciseId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/exercises/{workoutExerciseId}/uncomplete")
    public ResponseEntity<StudentWorkoutExerciseProgressResponse> uncompleteExercise(
            @PathVariable Long studentId,
            @PathVariable Long workoutExerciseId
    ) {
        StudentWorkoutExerciseProgressResponse response =
                studentWorkoutProgressService.uncompleteExercise(studentId, workoutExerciseId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress")
    public ResponseEntity<StudentCurrentWorkoutProgressResponse> getCurrentWorkoutProgress(
            @PathVariable Long studentId
    ) {
        StudentCurrentWorkoutProgressResponse response =
                studentWorkoutProgressService.getCurrentWorkoutProgress(studentId);

        return ResponseEntity.ok(response);
    }
}