package br.com.gymflow.api.controller;

import br.com.gymflow.api.dto.studentWorkouts.CreateStudentWorkoutRequest;
import br.com.gymflow.api.dto.studentWorkouts.PatchStudentWorkoutRequest;
import br.com.gymflow.api.dto.studentWorkouts.StudentCurrentWorkoutResponse;
import br.com.gymflow.api.dto.studentWorkouts.StudentWorkoutResponse;
import br.com.gymflow.api.service.StudentWorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students/{studentId}/workouts")
@RequiredArgsConstructor
public class StudentWorkoutController {

    private final StudentWorkoutService studentWorkoutService;

    @PostMapping
    public ResponseEntity<StudentWorkoutResponse> create(
            @PathVariable Long studentId,
            @RequestBody @Valid CreateStudentWorkoutRequest request
            ) {
        StudentWorkoutResponse response = studentWorkoutService.create(studentId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping("/{studentWorkoutId}")
    public ResponseEntity<StudentWorkoutResponse> findById(
            @PathVariable Long studentId,
            @PathVariable Long studentWorkoutId
    ) {
        StudentWorkoutResponse response = studentWorkoutService.findById(studentId, studentWorkoutId);

        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<List<StudentWorkoutResponse>> findAll(
            @PathVariable Long studentId
    ) {
        List<StudentWorkoutResponse> response = studentWorkoutService.findAllByStudentId(studentId);

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{studentWorkoutId}")
    public ResponseEntity<StudentWorkoutResponse> patch(
            @PathVariable Long studentId,
            @PathVariable Long studentWorkoutId,
            @RequestBody @Valid PatchStudentWorkoutRequest request
    ) {
        StudentWorkoutResponse response = studentWorkoutService.patch(studentId, studentWorkoutId, request);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/current")
    public ResponseEntity<StudentCurrentWorkoutResponse> findCurrentWorkout(
            @PathVariable Long studentId
    ) {
        StudentCurrentWorkoutResponse response = studentWorkoutService.findCurrentWorkout(studentId);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{studentWorkoutId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long studentId,
            @PathVariable Long studentWorkoutId
    ) {
        studentWorkoutService.delete(studentId, studentWorkoutId);

        return ResponseEntity.noContent().build();
    }
}