package br.com.gymflow.api.controller;

import br.com.gymflow.api.dto.workoutExercise.CreateWorkoutExerciseRequest;
import br.com.gymflow.api.dto.workoutExercise.PatchWorkoutExerciseRequest;
import br.com.gymflow.api.dto.workoutExercise.WorkoutExerciseResponse;
import br.com.gymflow.api.service.WorkoutExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts/{workoutId}/exercises")
@RequiredArgsConstructor
public class WorkoutExerciseController {

    private final WorkoutExerciseService workoutExerciseService;

    @PostMapping
    public ResponseEntity<WorkoutExerciseResponse> create(
            @PathVariable Long workoutId,
            @RequestBody @Valid CreateWorkoutExerciseRequest request
    ) {
        WorkoutExerciseResponse response = workoutExerciseService.create(workoutId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{workoutExerciseId}")
    public ResponseEntity<WorkoutExerciseResponse> findById(
            @PathVariable Long workoutId,
            @PathVariable Long workoutExerciseId
    ) {
         WorkoutExerciseResponse response = workoutExerciseService.findById(workoutId, workoutExerciseId);

         return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<WorkoutExerciseResponse>> findAll(
            @PathVariable Long workoutId
    ) {
        List<WorkoutExerciseResponse> response = workoutExerciseService.findAllByWorkoutId(workoutId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{workoutExerciseId}")
    public ResponseEntity<WorkoutExerciseResponse> patch(
            @PathVariable Long workoutId,
            @PathVariable Long workoutExerciseId,
            @RequestBody @Valid PatchWorkoutExerciseRequest request
            ) {
        WorkoutExerciseResponse response = workoutExerciseService.patch(workoutId, workoutExerciseId, request);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{workoutExerciseId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long workoutId,
            @PathVariable Long workoutExerciseId
    ) {
        workoutExerciseService.delete(workoutId ,workoutExerciseId);

        return ResponseEntity.noContent().build();
    }
}