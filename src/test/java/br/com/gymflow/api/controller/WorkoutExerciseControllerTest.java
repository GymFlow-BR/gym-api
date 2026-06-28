package br.com.gymflow.api.controller;

import br.com.gymflow.api.config.security.JwtAuthenticationFilter;
import br.com.gymflow.api.dto.workoutExercise.CreateWorkoutExerciseRequest;
import br.com.gymflow.api.dto.workoutExercise.PatchWorkoutExerciseRequest;
import br.com.gymflow.api.dto.workoutExercise.WorkoutExerciseResponse;
import br.com.gymflow.api.exception.DuplicateResourceException;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.service.WorkoutExerciseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(WorkoutExerciseController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkoutExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutExerciseService workoutExerciseService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateWorkoutExerciseSuccessfully() throws Exception {
        // Arrange
        Long workoutId = 10L;

        CreateWorkoutExerciseRequest request = createWorkoutExerciseRequest();

        WorkoutExerciseResponse response = createWorkoutExerciseResponse(
                100L,
                workoutId,
                20L
        );

        when(workoutExerciseService.create(eq(workoutId), any(CreateWorkoutExerciseRequest.class)))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(post("/api/workouts/{workoutId}/exercises", workoutId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.workoutId").value(workoutId))
                .andExpect(jsonPath("$.exerciseId").value(20L))
                .andExpect(jsonPath("$.exerciseOrder").value(1))
                .andExpect(jsonPath("$.sets").value(4))
                .andExpect(jsonPath("$.reps").value("8-12"))
                .andExpect(jsonPath("$.recommendedLoad").value(40.00))
                .andExpect(jsonPath("$.restTimeSeconds").value(60))
                .andExpect(jsonPath("$.notes").value("Manter controle do movimento."));

        verify(workoutExerciseService).create(eq(workoutId), any(CreateWorkoutExerciseRequest.class));
    }

    @Test
    void shouldFindAllWorkoutExercisesByWorkoutIdSuccessfully() throws Exception {
        // Arrange
        Long workoutId = 10L;

        WorkoutExerciseResponse responseA = createWorkoutExerciseResponse(
                100L,
                workoutId,
                20L
        );

        WorkoutExerciseResponse responseB = new WorkoutExerciseResponse(
                200L,
                workoutId,
                30L,
                2,
                4,
                "8-12",
                new BigDecimal("60.00"),
                90,
                "Atenção à postura durante a execução.",
                null,
                null
        );

        when(workoutExerciseService.findAllByWorkoutId(workoutId))
                .thenReturn(List.of(responseA, responseB));

        // Act + Assert
        mockMvc.perform(get("/api/workouts/{workoutId}/exercises", workoutId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].workoutId").value(workoutId))
                .andExpect(jsonPath("$[0].exerciseId").value(20L))
                .andExpect(jsonPath("$[0].exerciseOrder").value(1))
                .andExpect(jsonPath("$[0].sets").value(4))
                .andExpect(jsonPath("$[0].reps").value("8-12"))
                .andExpect(jsonPath("$[0].recommendedLoad").value(40.00))
                .andExpect(jsonPath("$[0].restTimeSeconds").value(60))
                .andExpect(jsonPath("$[0].notes").value("Manter controle do movimento."))
                .andExpect(jsonPath("$[1].id").value(200L))
                .andExpect(jsonPath("$[1].workoutId").value(workoutId))
                .andExpect(jsonPath("$[1].exerciseId").value(30L))
                .andExpect(jsonPath("$[1].exerciseOrder").value(2))
                .andExpect(jsonPath("$[1].sets").value(4))
                .andExpect(jsonPath("$[1].reps").value("8-12"))
                .andExpect(jsonPath("$[1].recommendedLoad").value(60.00))
                .andExpect(jsonPath("$[1].restTimeSeconds").value(90))
                .andExpect(jsonPath("$[1].notes").value("Atenção à postura durante a execução."));

        verify(workoutExerciseService).findAllByWorkoutId(workoutId);
    }

    @Test
    void shouldFindWorkoutExerciseByIdSuccessfully() throws Exception {
        // Arrange
        Long workoutId = 10L;
        Long workoutExerciseId = 100L;

        WorkoutExerciseResponse response = createWorkoutExerciseResponse(
                workoutExerciseId,
                workoutId,
                20L
        );

        when(workoutExerciseService.findById(workoutId, workoutExerciseId))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(get(
                        "/api/workouts/{workoutId}/exercises/{workoutExerciseId}",
                        workoutId,
                        workoutExerciseId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workoutExerciseId))
                .andExpect(jsonPath("$.workoutId").value(workoutId))
                .andExpect(jsonPath("$.exerciseId").value(20L))
                .andExpect(jsonPath("$.exerciseOrder").value(1))
                .andExpect(jsonPath("$.sets").value(4))
                .andExpect(jsonPath("$.reps").value("8-12"))
                .andExpect(jsonPath("$.recommendedLoad").value(40.00))
                .andExpect(jsonPath("$.restTimeSeconds").value(60))
                .andExpect(jsonPath("$.notes").value("Manter controle do movimento."));

        verify(workoutExerciseService).findById(workoutId, workoutExerciseId);
    }

    @Test
    void shouldPatchWorkoutExerciseSuccessfully() throws Exception {
        // Arrange
        Long workoutId = 10L;
        Long workoutExerciseId = 100L;

        PatchWorkoutExerciseRequest request = createPatchWorkoutExerciseRequest();

        WorkoutExerciseResponse response = new WorkoutExerciseResponse(
                workoutExerciseId,
                workoutId,
                20L,
                2,
                3,
                "8-12",
                new BigDecimal("35.00"),
                90,
                "Atualizar execução do exercício.",
                null,
                null
        );

        when(workoutExerciseService.patch(
                eq(workoutId),
                eq(workoutExerciseId),
                any(PatchWorkoutExerciseRequest.class)
        )).thenReturn(response);

        // Act + Assert
        mockMvc.perform(patch(
                        "/api/workouts/{workoutId}/exercises/{workoutExerciseId}",
                        workoutId,
                        workoutExerciseId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workoutExerciseId))
                .andExpect(jsonPath("$.workoutId").value(workoutId))
                .andExpect(jsonPath("$.exerciseId").value(20L))
                .andExpect(jsonPath("$.exerciseOrder").value(2))
                .andExpect(jsonPath("$.sets").value(3))
                .andExpect(jsonPath("$.reps").value("8-12"))
                .andExpect(jsonPath("$.recommendedLoad").value(35.00))
                .andExpect(jsonPath("$.restTimeSeconds").value(90))
                .andExpect(jsonPath("$.notes").value("Atualizar execução do exercício."));

        verify(workoutExerciseService).patch(
                eq(workoutId),
                eq(workoutExerciseId),
                any(PatchWorkoutExerciseRequest.class)
        );
    }

    @Test
    void shouldDeleteWorkoutExerciseSuccessfully() throws Exception {
        // Arrange
        Long workoutId = 10L;
        Long workoutExerciseId = 100L;

        // Act + Assert
        mockMvc.perform(delete(
                        "/api/workouts/{workoutId}/exercises/{workoutExerciseId}",
                        workoutId,
                        workoutExerciseId
                ))
                .andExpect(status().isNoContent());

        verify(workoutExerciseService).delete(workoutId, workoutExerciseId);
    }

    @Test
    void shouldReturnNotFoundWhenWorkoutExerciseDoesNotExist() throws Exception {
        // Arrange
        Long workoutId = 10L;
        Long workoutExerciseId = 100L;

        when(workoutExerciseService.findById(workoutId, workoutExerciseId))
                .thenThrow(new ResourceNotFoundException(
                        "Workout exercise not found with id: " + workoutExerciseId
                ));

        // Act + Assert
        mockMvc.perform(get(
                        "/api/workouts/{workoutId}/exercises/{workoutExerciseId}",
                        workoutId,
                        workoutExerciseId
                ))
                .andExpect(status().isNotFound());

        verify(workoutExerciseService).findById(workoutId, workoutExerciseId);
    }

    @Test
    void shouldReturnBadRequestWhenCreateWorkoutExerciseRequestIsInvalid() throws Exception {
        // Arrange
        Long workoutId = 10L;

        CreateWorkoutExerciseRequest request = new CreateWorkoutExerciseRequest(
                null,
                0,
                0,
                "0",
                new BigDecimal("-1.00"),
                -10,
                "Request inválida para teste"
        );

        // Act + Assert
        mockMvc.perform(post("/api/workouts/{workoutId}/exercises", workoutId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(workoutExerciseService, never())
                .create(eq(workoutId), any(CreateWorkoutExerciseRequest.class));
    }

    @Test
    void shouldReturnConflictWhenWorkoutAlreadyHasExerciseWithSameOrder() throws Exception {
        // Arrange
        Long workoutId = 10L;

        CreateWorkoutExerciseRequest request = new CreateWorkoutExerciseRequest(
                20L,
                1,
                4,
                "8-12",
                BigDecimal.valueOf(40.00),
                60,
                "Manter controle do movimento"
        );

        when(workoutExerciseService.create(eq(workoutId), any(CreateWorkoutExerciseRequest.class)))
                .thenThrow(new DuplicateResourceException(
                        "Workout already has an exercise with this order"
                ));

        // Act + Assert
        mockMvc.perform(post("/api/workouts/{workoutId}/exercises", workoutId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Workout already has an exercise with this order"))
                .andExpect(jsonPath("$.path").value("/api/workouts/" + workoutId + "/exercises"));

        verify(workoutExerciseService).create(eq(workoutId), any(CreateWorkoutExerciseRequest.class));
    }


    private CreateWorkoutExerciseRequest createWorkoutExerciseRequest() {
        return new CreateWorkoutExerciseRequest(
                20L,
                1,
                4,
                "8-12",
                new BigDecimal("40.00"),
                60,
                "Manter controle do movimento."
        );
    }

    private PatchWorkoutExerciseRequest createPatchWorkoutExerciseRequest() {
        return new PatchWorkoutExerciseRequest(
                2,
                3,
                "8-12",
                new BigDecimal("35.00"),
                90,
                "Atualizar execução do exercício."
        );
    }

    private WorkoutExerciseResponse createWorkoutExerciseResponse(
            Long id,
            Long workoutId,
            Long exerciseId
    ) {
        return new WorkoutExerciseResponse(
                id,
                workoutId,
                exerciseId,
                1,
                4,
                "8-12",
                new BigDecimal("40.00"),
                60,
                "Manter controle do movimento.",
                null,
                null
        );
    }
}