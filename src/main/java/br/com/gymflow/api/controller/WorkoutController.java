package br.com.gymflow.api.controller;

import br.com.gymflow.api.dto.workout.CreateWorkoutRequest;
import br.com.gymflow.api.dto.workout.UpdateWorkoutRequest;
import br.com.gymflow.api.dto.workout.WorkoutResponse;
import br.com.gymflow.api.service.WorkoutService;
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
        name = "Workouts",
        description = "Endpoints para criação, listagem e gerenciamento de treinos modelo"
)
@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;


    @Operation(
            summary = "Criar treino modelo",
            description = "Cria um novo treino modelo vinculado a um professor."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Treino modelo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "404", description = "Professor não encontrado")
    })
    @PostMapping
    public ResponseEntity<WorkoutResponse> create(
            @RequestBody @Valid CreateWorkoutRequest request
    ) {
        WorkoutResponse response = workoutService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @Operation(
            summary = "Listar treinos modelo",
            description = "Retorna todos os treinos modelo cadastrados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treinos modelo encontrados")
    })
    @GetMapping
    public ResponseEntity<List<WorkoutResponse>> findAll() {
        List<WorkoutResponse> response = workoutService.findAll();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Listar treinos modelo por organização",
            description = "Retorna todos os treinos modelo criados por professores de uma organização específica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treinos modelo encontrados"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @GetMapping("/by-organization/{organizationId}")
    public ResponseEntity<List<WorkoutResponse>> findAllByOrganizationId(
            @PathVariable  Long organizationId
    ) {
        List<WorkoutResponse> response = workoutService.findAllByOrganizationId(organizationId);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Buscar treino modelo por ID",
            description = "Retorna um treino modelo específico pelo seu identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treino modelo encontrado"),
            @ApiResponse(responseCode = "404", description = "Treino modelo não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutResponse> findById(
            @PathVariable Long id
    ) {
        WorkoutResponse response = workoutService.findById(id);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Atualizar treino modelo",
            description = "Atualiza parcialmente os dados de um treino modelo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treino modelo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "404", description = "Treino modelo não encontrado")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<WorkoutResponse> patch(
            @PathVariable Long id,
            @RequestBody @Valid UpdateWorkoutRequest request
    ) {
        WorkoutResponse response = workoutService.patch(id, request);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Inativar treino modelo",
            description = "Inativa um treino modelo, alterando seu status para INACTIVE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Treino modelo inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Treino modelo não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        workoutService.delete(id);

        return ResponseEntity.noContent().build();
    }
}