package br.com.gymflow.api.controller;

import br.com.gymflow.api.config.security.JwtAuthenticationFilter;
import br.com.gymflow.api.domain.enums.WorkoutStatus;
import br.com.gymflow.api.dto.workout.CreateWorkoutRequest;
import br.com.gymflow.api.dto.workout.UpdateWorkoutRequest;
import br.com.gymflow.api.dto.workout.WorkoutResponse;
import br.com.gymflow.api.exception.BusinessRuleException;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.service.WorkoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;


@WebMvcTest(WorkoutController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutService workoutService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateWorkoutSuccessfully() throws Exception {
        // Arrange
        CreateWorkoutRequest request = createWorkoutRequest();

        WorkoutResponse response = createWorkoutResponse(
                10L,
                1L,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        when(workoutService.create(any(CreateWorkoutRequest.class)))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workoutId").value(10L))
                .andExpect(jsonPath("$.teacherId").value(1L))
                .andExpect(jsonPath("$.workoutName").value("Treino A"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(workoutService).create(any(CreateWorkoutRequest.class));
    }

    @Test
    void shouldFindAllWorkoutsSuccessfully() throws Exception {
        // Arrange
        WorkoutResponse responseA = createWorkoutResponse(
                10L,
                1L,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        WorkoutResponse responseB = createWorkoutResponse(
                20L,
                1L,
                "Treino B",
                WorkoutStatus.ACTIVE
        );

        when(workoutService.findAll())
                .thenReturn(List.of(responseA, responseB));

        // Act + Assert
        mockMvc.perform(get("/api/workouts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workoutId").value(10L))
                .andExpect(jsonPath("$[0].teacherId").value(1L))
                .andExpect(jsonPath("$[0].workoutName").value("Treino A"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].workoutId").value(20L))
                .andExpect(jsonPath("$[1].teacherId").value(1L))
                .andExpect(jsonPath("$[1].workoutName").value("Treino B"))
                .andExpect(jsonPath("$[1].status").value("ACTIVE"));

        verify(workoutService).findAll();
    }

    @Test
    void shouldFindWorkoutByIdSuccessfully() throws Exception {
        // Arrange
        Long workoutId = 10L;

        WorkoutResponse response = createWorkoutResponse(
                workoutId,
                1L,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        when(workoutService.findById(workoutId))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(get("/api/workouts/{id}", workoutId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutId").value(10L))
                .andExpect(jsonPath("$.teacherId").value(1L))
                .andExpect(jsonPath("$.workoutName").value("Treino A"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(workoutService).findById(workoutId);
    }

    @Test
    void shouldFindAllWorkoutsByOrganizationIdSuccessfully() throws Exception {
        // Arrange
        Long organizationId = 100L;

        WorkoutResponse responseA = createWorkoutResponse(
                10L,
                1L,
                "Treino A",
                WorkoutStatus.ACTIVE
        );

        WorkoutResponse responseB = createWorkoutResponse(
                20L,
                1L,
                "Treino B",
                WorkoutStatus.ACTIVE
        );

        when(workoutService.findAllByOrganizationId(organizationId))
                .thenReturn(List.of(responseA, responseB));

        // Act + Assert
        mockMvc.perform(get("/api/workouts/by-organization/{organizationId}", organizationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workoutId").value(10L))
                .andExpect(jsonPath("$[0].teacherId").value(1L))
                .andExpect(jsonPath("$[0].workoutName").value("Treino A"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].workoutId").value(20L))
                .andExpect(jsonPath("$[1].teacherId").value(1L))
                .andExpect(jsonPath("$[1].workoutName").value("Treino B"))
                .andExpect(jsonPath("$[1].status").value("ACTIVE"));

        verify(workoutService).findAllByOrganizationId(organizationId);
    }

    @Test
    void shouldPatchWorkoutSuccessfully() throws Exception {
        // Arrange
        Long workoutId = 10L;

        UpdateWorkoutRequest request = createUpdateWorkoutRequest();

        WorkoutResponse response = createWorkoutResponse(
                workoutId,
                1L,
                "Treino B",
                WorkoutStatus.INACTIVE
        );

        when(workoutService.patch(any(Long.class), any(UpdateWorkoutRequest.class)))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(patch("/api/workouts/{id}", workoutId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutId").value(workoutId))
                .andExpect(jsonPath("$.teacherId").value(1L))
                .andExpect(jsonPath("$.workoutName").value("Treino B"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(workoutService).patch(any(Long.class), any(UpdateWorkoutRequest.class));
    }

    @Test
    void shouldReturnNotFoundWhenWorkoutDoesNotExist() throws Exception {
        // Arrange
        Long workoutId = 10L;

        when(workoutService.findById(workoutId))
                .thenThrow(new ResourceNotFoundException("Workout not found with id: " + workoutId));

        // Act + Assert
        mockMvc.perform(get("/api/workouts/{id}", workoutId))
                .andExpect(status().isNotFound());

        verify(workoutService).findById(workoutId);
    }

    @Test
    void shouldReturnBadRequestWhenBusinessRuleIsViolatedOnCreate() throws Exception {
        // Arrange
        CreateWorkoutRequest request = createWorkoutRequest();

        when(workoutService.create(any(CreateWorkoutRequest.class)))
                .thenThrow(new BusinessRuleException("User is not allowed to create workouts with id: 1"));

        // Act + Assert
        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(workoutService).create(any(CreateWorkoutRequest.class));
    }

    @Test
    void shouldDeleteWorkoutSuccessfully() throws Exception {
        // Arrange
        Long workoutId = 10L;

        // Act + Assert
        mockMvc.perform(delete("/api/workouts/{id}", workoutId))
                .andExpect(status().isNoContent());

        verify(workoutService).delete(workoutId);
    }

    private CreateWorkoutRequest createWorkoutRequest() {
        return new CreateWorkoutRequest(
                1L,
                "Treino A"
        );
    }

    private UpdateWorkoutRequest createUpdateWorkoutRequest() {
        return new UpdateWorkoutRequest(
                "Treino B",
                WorkoutStatus.INACTIVE
        );
    }

    private WorkoutResponse createWorkoutResponse(
            Long workoutId,
            Long teacherId,
            String workoutName,
            WorkoutStatus status
    ) {
        return new WorkoutResponse(
                workoutId,
                teacherId,
                workoutName,
                status,
                null,
                null
        );
    }
}