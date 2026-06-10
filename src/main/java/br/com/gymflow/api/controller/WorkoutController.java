package br.com.gymflow.api.controller;

import br.com.gymflow.api.dto.workout.CreateWorkoutRequest;
import br.com.gymflow.api.dto.workout.UpdateWorkoutRequest;
import br.com.gymflow.api.dto.workout.WorkoutResponse;
import br.com.gymflow.api.service.WorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    @PostMapping
    public ResponseEntity<WorkoutResponse> create(
            @RequestBody @Valid CreateWorkoutRequest request
            ) {
        WorkoutResponse response = workoutService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<WorkoutResponse> findById(
            @PathVariable Long id
    ) {
        WorkoutResponse response = workoutService.findById(id);

        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<List<WorkoutResponse>> findAll() {
        List<WorkoutResponse> response = workoutService.findAll();

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{id}")
    public ResponseEntity<WorkoutResponse> patch(
            @PathVariable Long id,
            @RequestBody @Valid UpdateWorkoutRequest request
    ) {
        WorkoutResponse response = workoutService.patch(id, request);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        workoutService.delete(id);

        return ResponseEntity.noContent().build();
    }
}