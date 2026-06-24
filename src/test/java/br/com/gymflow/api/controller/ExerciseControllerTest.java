package br.com.gymflow.api.controller;

import br.com.gymflow.api.config.security.JwtAuthenticationFilter;
import br.com.gymflow.api.dto.exercise.CreateExerciseRequest;
import br.com.gymflow.api.dto.exercise.ExerciseResponse;
import br.com.gymflow.api.dto.exercise.UpdateExerciseRequest;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.service.ExerciseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ExerciseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExerciseService exerciseService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateExerciseSuccessfully() throws Exception {
        // Arrange
        CreateExerciseRequest request = createExerciseRequest();

        ExerciseResponse response = createExerciseResponse(
                10L,
                100L,
                "Supino Reto",
                true
        );

        when(exerciseService.create(any(CreateExerciseRequest.class)))
                .thenReturn(response);

        //Act + Assert
        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.organizationId").value(100L))
                .andExpect(jsonPath("$.exerciseName").value("Supino Reto"))
                .andExpect(jsonPath("$.muscleGroup").value("Peito"))
                .andExpect(jsonPath("$.description").value("Exercicio para fortalecimento do peitoral"))
                .andExpect(jsonPath("$.equipmentName").value("Barra"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/supino.png"))
                .andExpect(jsonPath("$.videoUrl").value("https://example.com/supino.mp4"))
                .andExpect(jsonPath("$.active").value(true));

        verify(exerciseService).create(any(CreateExerciseRequest.class));

    }


    private CreateExerciseRequest createExerciseRequest() {
        return new CreateExerciseRequest(
                100L,
                "Supino Reto",
                "Peito",
                "Exercicio para fortalecimento de peitoral",
                "Barra",
                "https://example.com/supino.png",
                "https://example.com/supino.mp4"
        );
    }

    private UpdateExerciseRequest createUpdateExerciseRequest() {
        return new UpdateExerciseRequest(
                "Supino inclinado",
                "Peito",
                "Variacao inclinada para peitoral superior",
                "Halteres",
                "https://example.com/supino-inclinado.png",
                "https://example.com/supino-inclinado.mp4"
        );
    }

    private ExerciseResponse createExerciseResponse(
            Long id,
            Long organizationId,
            String exerciseName,
            Boolean active
    ) {
        return new ExerciseResponse(
                id,
                organizationId,
                exerciseName,
                "Peito",
                "Exercicio para fortalecimento do peitoral",
                "Barra",
                "https://example.com/supino.png",
                "https://example.com/supino.mp4",
                active,
                null,
                null
        );
    }

    @Test
    void shouldFindAllExercisesSuccessfully() throws Exception {
        ExerciseResponse responseA = createExerciseResponse(
                10L,
                100L,
                "Supino Reto",
                true
        );

        ExerciseResponse responseB = createExerciseResponse(
                20L,
                100L,
                "Agachamento Livre",
                true
        );

        when(exerciseService.findAll())
                .thenReturn(List.of(responseA, responseB));

        // Act + Assert
        mockMvc.perform(get("/api/exercises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].organizationId").value(100L))
                .andExpect(jsonPath("$[0].exerciseName").value("Supino Reto"))
                .andExpect(jsonPath("$[0].muscleGroup").value("Peito"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].id").value(20L))
                .andExpect(jsonPath("$[1].organizationId").value(100L))
                .andExpect(jsonPath("$[1].exerciseName").value("Agachamento Livre"))
                .andExpect(jsonPath("$[1].active").value(true));

        verify(exerciseService).findAll();
    }

    @Test
    void shouldFindExerciseByIdSuccessfully() throws Exception {
        // Arrange
        Long exerciseId = 10L;

        ExerciseResponse response = createExerciseResponse(
                exerciseId,
                100L,
                "Supino Reto",
                true
        );

        when(exerciseService.findById(exerciseId))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(get("/api/exercises/{id}", exerciseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.organizationId").value(100L))
                .andExpect(jsonPath("$.exerciseName").value("Supino Reto"))
                .andExpect(jsonPath("$.muscleGroup").value("Peito"))
                .andExpect(jsonPath("$.description").value("Exercicio para fortalecimento do peitoral"))
                .andExpect(jsonPath("$.equipmentName").value("Barra"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/supino.png"))
                .andExpect(jsonPath("$.videoUrl").value("https://example.com/supino.mp4"))
                .andExpect(jsonPath("$.active").value(true));

        verify(exerciseService).findById(exerciseId);
    }

    @Test
    void shouldFindAllExercisesByOrganizationIdSuccessfully() throws Exception {
        // Arrange
        Long organizationId = 100L;

        ExerciseResponse responseA = createExerciseResponse(
                10L,
                organizationId,
                "Supino Reto",
                true
        );

        ExerciseResponse responseB = createExerciseResponse(
                20L,
                organizationId,
                "Agachamento Livre",
                true
        );

        when(exerciseService.findAllByOrganizationId(organizationId))
                .thenReturn(List.of(responseA, responseB));

        // Act + Assert
        mockMvc.perform(get("/api/exercises/by-organization/{organizationId}", organizationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].organizationId").value(organizationId))
                .andExpect(jsonPath("$[0].exerciseName").value("Supino Reto"))
                .andExpect(jsonPath("$[0].muscleGroup").value("Peito"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].id").value(20L))
                .andExpect(jsonPath("$[1].organizationId").value(organizationId))
                .andExpect(jsonPath("$[1].exerciseName").value("Agachamento Livre"))
                .andExpect(jsonPath("$[1].active").value(true));

        verify(exerciseService).findAllByOrganizationId(organizationId);
    }

    @Test
    void shouldUpdateExerciseSuccessfully() throws Exception {
        // Arrange
        Long exerciseId = 10L;

        UpdateExerciseRequest request = createUpdateExerciseRequest();

        ExerciseResponse response = new ExerciseResponse(
                exerciseId,
                100L,
                "Supino inclinado",
                "Peito",
                "Variacao inclinada para peitoral superior",
                "Halteres",
                "https://example.com/supino-inclinado.png",
                "https://example.com/supino-inclinado.mp4",
                true,
                null,
                null
        );

        when(exerciseService.update(any(Long.class), any(UpdateExerciseRequest.class)))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(put("/api/exercises/{id}", exerciseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(exerciseId))
                .andExpect(jsonPath("$.organizationId").value(100L))
                .andExpect(jsonPath("$.exerciseName").value("Supino inclinado"))
                .andExpect(jsonPath("$.muscleGroup").value("Peito"))
                .andExpect(jsonPath("$.description").value("Variacao inclinada para peitoral superior"))
                .andExpect(jsonPath("$.equipmentName").value("Halteres"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/supino-inclinado.png"))
                .andExpect(jsonPath("$.videoUrl").value("https://example.com/supino-inclinado.mp4"))
                .andExpect(jsonPath("$.active").value(true));

        verify(exerciseService).update(any(Long.class), any(UpdateExerciseRequest.class));
    }

    @Test
    void shouldDeleteExerciseSuccessfully() throws Exception {
        // Arrange
        Long exerciseId = 10L;

        // Act + Assert
        mockMvc.perform(delete("/api/exercises/{id}", exerciseId))
                .andExpect(status().isNoContent());

        verify(exerciseService).delete(exerciseId);
    }

    @Test
    void shouldReturnNotFoundWhenExerciseDoesNotExist() throws Exception {
        // Arrange
        Long exerciseId = 10L;

        when(exerciseService.findById(exerciseId))
                .thenThrow(new ResourceNotFoundException("Exercise not found with id: " + exerciseId));

        // Act + Assert
        mockMvc.perform(get("/api/exercises/{id}", exerciseId))
                .andExpect(status().isNotFound());

        verify(exerciseService).findById(exerciseId);
    }

    @Test
    void shouldReturnBadRequestWhenCreateExerciseRequestIsInvalid() throws Exception {
        // Arrange
        CreateExerciseRequest request = new CreateExerciseRequest(
                null,
                "",
                "Peito",
                "Exercicio para fortalecimento do peitoral",
                "Barra",
                "https://example.com/supino.png",
                "https://example.com/supino.mp4"
        );

        // Act + Assert
        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(exerciseService, never())
                .create(any(CreateExerciseRequest.class));
    }
}