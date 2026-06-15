package br.com.gymflow.api.controller;

import br.com.gymflow.api.dto.studentWorkouts.CreateStudentWorkoutRequest;
import br.com.gymflow.api.dto.studentWorkouts.PatchStudentWorkoutRequest;
import br.com.gymflow.api.dto.studentWorkouts.StudentCurrentWorkoutResponse;
import br.com.gymflow.api.dto.studentWorkouts.StudentWorkoutResponse;
import br.com.gymflow.api.service.StudentWorkoutService;
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
        name = "Student Workouts",
        description = "Endpoints para atribuição, listagem e visualização de treinos dos alunos"
)
@RestController
@RequestMapping("/api/students/{studentId}/workouts")
@RequiredArgsConstructor
public class StudentWorkoutController {

    private final StudentWorkoutService studentWorkoutService;

    @Operation(
            summary = "Atribuir treino ao aluno",
            description = "Cria uma nova atribuição de workout para o aluno informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Treino atribuído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada."),
            @ApiResponse(responseCode = "404", description = "Aluno ou workout não encontrado"),
            @ApiResponse(responseCode = "409", description = "Workout já atribuído ao aluno")

    })
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


    @Operation(
            summary = "Listar treinos do aluno",
            description = "Retorna todas as atribuições de treino vinculadas ao aluno informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treinos encontrados"),
            @ApiResponse(responseCode = "400", description = "Regra de negócio inválida"),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado")
    })
    @GetMapping
    public ResponseEntity<List<StudentWorkoutResponse>> findAll(
            @PathVariable Long studentId
    ) {
        List<StudentWorkoutResponse> response = studentWorkoutService.findAllByStudentId(studentId);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Buscar atribuição de treino por ID",
            description = "Retorna uma atribuição específica de treino do aluno informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atribuição encontrada"),
            @ApiResponse(responseCode = "404", description = "Atribuição ou aluno não encontrado")
    })
    @GetMapping("/{studentWorkoutId}")
    public ResponseEntity<StudentWorkoutResponse> findById(
            @PathVariable Long studentId,
            @PathVariable Long studentWorkoutId
    ) {
        StudentWorkoutResponse response = studentWorkoutService.findById(studentId, studentWorkoutId);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Atualizar status da atribuição",
            description = "Atualiza parcialmente o status de uma atribuição de treino do aluno."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atribuição atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada"),
            @ApiResponse(responseCode = "404", description = "Atribuição ou aluno não encontrado")
    })
    @PatchMapping("/{studentWorkoutId}")
    public ResponseEntity<StudentWorkoutResponse> patch(
            @PathVariable Long studentId,
            @PathVariable Long studentWorkoutId,
            @RequestBody @Valid PatchStudentWorkoutRequest request
    ) {
        StudentWorkoutResponse response = studentWorkoutService.patch(studentId, studentWorkoutId, request);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Inativar atribuição de treino",
            description = "Inativa uma atribuição de treino do aluno, alterando seu status para INACTIVE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Atribuição inativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Atribuição ou aluno não encontrado")
    })
    @DeleteMapping("/{studentWorkoutId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long studentId,
            @PathVariable Long studentWorkoutId
    ) {
        studentWorkoutService.delete(studentId, studentWorkoutId);

        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Buscar treino atual do aluno",
            description = "Retorna o treino ativo atual do aluno com os exercícios detalhados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treino atual encontrado"),
            @ApiResponse(responseCode = "400", description = "Regra de negócio inválida"),
            @ApiResponse(responseCode = "404", description = "Aluno ou treino ativo não encontrado")
    })
    @GetMapping("/current")
    public ResponseEntity<StudentCurrentWorkoutResponse> findCurrentWorkout(
            @PathVariable Long studentId
    ) {
        StudentCurrentWorkoutResponse response = studentWorkoutService.findCurrentWorkout(studentId);

        return ResponseEntity.ok(response);
    }
}