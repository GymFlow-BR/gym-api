package br.com.gymflow.api.controller;

import br.com.gymflow.api.dto.exercise.CreateExerciseRequest;
import br.com.gymflow.api.dto.exercise.ExerciseResponse;
import br.com.gymflow.api.dto.exercise.UpdateExerciseRequest;
import br.com.gymflow.api.service.ExerciseService;
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
        name = "Exercises",
        description = "Endpoints para cadastro, listagem e gerenciamento de exercícios"
)
@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @Operation(
            summary = "Criar exercício",
            description = "Cadastra um novo exercício vinculado a uma organização."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exercício criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @PostMapping
    public ResponseEntity<ExerciseResponse> create(
            @RequestBody @Valid CreateExerciseRequest request
    ) {
        ExerciseResponse response = exerciseService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @Operation(
            summary = "Listar exercícios",
            description = "Retorna todos os exercícios cadastrados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercícios encontrados")
    })
    @GetMapping
    public ResponseEntity<List<ExerciseResponse>> findAll() {
        List<ExerciseResponse> response = exerciseService.findAll();

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Buscar exercício por ID",
            description = "Retorna um exercício específico pelo seu identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercício encontrado"),
            @ApiResponse(responseCode = "404", description = "Exercício não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> findById(
            @PathVariable Long id
    ) {
        ExerciseResponse response = exerciseService.findById(id);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Atualizar exercício",
            description = "Atualiza os dados de um exercício cadastrado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercício atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "404", description = "Exercício não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExerciseResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid
            UpdateExerciseRequest request
    ) {
        ExerciseResponse response = exerciseService.update(id, request);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Remover exercício",
            description = "Remove um exercício cadastrado pelo seu identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Exercício removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Exercício não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id
    ) {
        exerciseService.delete(id);

        return ResponseEntity.noContent().build();
    }
}