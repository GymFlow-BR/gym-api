package br.com.gymflow.api.controller;

import br.com.gymflow.api.dto.workoutExercise.CreateWorkoutExerciseRequest;
import br.com.gymflow.api.dto.workoutExercise.PatchWorkoutExerciseRequest;
import br.com.gymflow.api.dto.workoutExercise.WorkoutExerciseResponse;
import br.com.gymflow.api.service.WorkoutExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Workout Exercises",
        description = "Endpoints para associação, listagem e gerenciamento de exercícios em treinos modelo"
)
@RestController
@RequestMapping("/api/workouts/{workoutId}/exercises")
@RequiredArgsConstructor
public class WorkoutExerciseController {

    private final WorkoutExerciseService workoutExerciseService;


    @Operation(
            summary = "Adicionar exercício ao treino",
            description = "Associa um exercício existente a um treino modelo informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exercício adicionado ao treino com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "404", description = "Workout ou exercício não encontrado")
    })
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


    @Operation(
            summary = "Listar exercícios do treino",
            description = "Retorna todos os exercícios associados ao treino modelo informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercícios do treino encontrados"),
            @ApiResponse(responseCode = "404", description = "Workout não encontrado")
    })
    @GetMapping
    public ResponseEntity<List<WorkoutExerciseResponse>> findAll(
            @PathVariable Long workoutId
    ) {
        List<WorkoutExerciseResponse> response = workoutExerciseService.findAllByWorkoutId(workoutId);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Buscar exercício do treino por ID",
            description = "Retorna uma associação específica de exercício dentro do treino modelo informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercício do treino encontrado"),
            @ApiResponse(responseCode = "404", description = "Workout ou exercício do treino não encontrado")
    })
    @GetMapping("/{workoutExerciseId}")
    public ResponseEntity<WorkoutExerciseResponse> findById(
            @PathVariable Long workoutId,
            @PathVariable Long workoutExerciseId
    ) {
        WorkoutExerciseResponse response = workoutExerciseService.findById(workoutId, workoutExerciseId);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Atualizar exercício do treino",
            description = "Atualiza parcialmente os dados de um exercício associado ao treino modelo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercício do treino atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "404", description = "Workout ou exercício do treino não encontrado")
    })
    @PatchMapping("/{workoutExerciseId}")
    public ResponseEntity<WorkoutExerciseResponse> patch(
            @PathVariable Long workoutId,
            @PathVariable Long workoutExerciseId,
            @RequestBody @Valid PatchWorkoutExerciseRequest request
            ) {
        WorkoutExerciseResponse response = workoutExerciseService.patch(workoutId, workoutExerciseId, request);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Remover exercício do treino",
            description = "Remove a associação de um exercício ao treino modelo informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Exercício removido do treino com sucesso"),
            @ApiResponse(responseCode = "404", description = "Workout ou exercício do treino não encontrado")
    })
    @DeleteMapping("/{workoutExerciseId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long workoutId,
            @PathVariable Long workoutExerciseId
    ) {
        workoutExerciseService.delete(workoutId, workoutExerciseId);

        return ResponseEntity.noContent().build();
    }
}