package br.com.gymflow.api.controller;

import br.com.gymflow.api.config.security.JwtAuthenticationFilter;
import br.com.gymflow.api.domain.enums.WeekDay;
import br.com.gymflow.api.domain.enums.WorkoutStatus;
import br.com.gymflow.api.dto.studentWorkouts.CreateStudentWorkoutRequest;
import br.com.gymflow.api.dto.studentWorkouts.PatchStudentWorkoutRequest;
import br.com.gymflow.api.dto.studentWorkouts.StudentCurrentWorkoutExerciseResponse;
import br.com.gymflow.api.dto.studentWorkouts.StudentCurrentWorkoutResponse;
import br.com.gymflow.api.dto.studentWorkouts.StudentWorkoutResponse;
import br.com.gymflow.api.exception.BusinessRuleException;
import br.com.gymflow.api.exception.DuplicateResourceException;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.service.StudentWorkoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentWorkoutController.class)
@AutoConfigureMockMvc(addFilters = false)
class StudentWorkoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentWorkoutService studentWorkoutService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateStudentWorkoutSuccessfully() throws Exception {
        Long studentId = 1L;

        CreateStudentWorkoutRequest request = createStudentWorkoutRequest();

        StudentWorkoutResponse response = createStudentWorkoutResponse(
                100L,
                studentId,
                10L,
                WorkoutStatus.ACTIVE,
                WeekDay.MONDAY
        );

        when(studentWorkoutService.create(eq(studentId), any(CreateStudentWorkoutRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/students/{studentId}/workouts", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentWorkoutId").value(100L))
                .andExpect(jsonPath("$.studentId").value(studentId))
                .andExpect(jsonPath("$.studentName").value("Aluno Teste"))
                .andExpect(jsonPath("$.workoutId").value(10L))
                .andExpect(jsonPath("$.workoutName").value("Treino A"))
                .andExpect(jsonPath("$.teacherName").value("Professor Teste"))
                .andExpect(jsonPath("$.weekDay").value("MONDAY"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(studentWorkoutService).create(eq(studentId), any(CreateStudentWorkoutRequest.class));
    }

    @Test
    void shouldFindAllStudentWorkoutsSuccessfully() throws Exception {
        Long studentId = 1L;

        StudentWorkoutResponse responseA = createStudentWorkoutResponse(
                100L,
                studentId,
                10L,
                WorkoutStatus.ACTIVE,
                WeekDay.MONDAY
        );

        StudentWorkoutResponse responseB = new StudentWorkoutResponse(
                200L,
                studentId,
                "Aluno Teste",
                20L,
                "Treino B",
                "Professor Teste",
                LocalDateTime.of(2026, 6, 23, 11, 0),
                WeekDay.WEDNESDAY,
                WorkoutStatus.INACTIVE,
                null,
                null
        );

        when(studentWorkoutService.findAllByStudentId(studentId))
                .thenReturn(List.of(responseA, responseB));

        mockMvc.perform(get("/api/students/{studentId}/workouts", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentWorkoutId").value(100L))
                .andExpect(jsonPath("$[0].studentId").value(studentId))
                .andExpect(jsonPath("$[0].studentName").value("Aluno Teste"))
                .andExpect(jsonPath("$[0].workoutId").value(10L))
                .andExpect(jsonPath("$[0].workoutName").value("Treino A"))
                .andExpect(jsonPath("$[0].teacherName").value("Professor Teste"))
                .andExpect(jsonPath("$[0].weekDay").value("MONDAY"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].studentWorkoutId").value(200L))
                .andExpect(jsonPath("$[1].studentId").value(studentId))
                .andExpect(jsonPath("$[1].studentName").value("Aluno Teste"))
                .andExpect(jsonPath("$[1].workoutId").value(20L))
                .andExpect(jsonPath("$[1].workoutName").value("Treino B"))
                .andExpect(jsonPath("$[1].teacherName").value("Professor Teste"))
                .andExpect(jsonPath("$[1].weekDay").value("WEDNESDAY"))
                .andExpect(jsonPath("$[1].status").value("INACTIVE"));

        verify(studentWorkoutService).findAllByStudentId(studentId);
    }

    @Test
    void shouldFindStudentWorkoutByIdSuccessfully() throws Exception {
        Long studentId = 1L;
        Long studentWorkoutId = 100L;

        StudentWorkoutResponse response = createStudentWorkoutResponse(
                studentWorkoutId,
                studentId,
                10L,
                WorkoutStatus.ACTIVE,
                WeekDay.MONDAY
        );

        when(studentWorkoutService.findById(studentId, studentWorkoutId))
                .thenReturn(response);

        mockMvc.perform(get(
                        "/api/students/{studentId}/workouts/{studentWorkoutId}",
                        studentId,
                        studentWorkoutId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentWorkoutId").value(studentWorkoutId))
                .andExpect(jsonPath("$.studentId").value(studentId))
                .andExpect(jsonPath("$.studentName").value("Aluno Teste"))
                .andExpect(jsonPath("$.workoutId").value(10L))
                .andExpect(jsonPath("$.workoutName").value("Treino A"))
                .andExpect(jsonPath("$.teacherName").value("Professor Teste"))
                .andExpect(jsonPath("$.weekDay").value("MONDAY"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(studentWorkoutService).findById(studentId, studentWorkoutId);
    }

    @Test
    void shouldPatchStudentWorkoutSuccessfully() throws Exception {
        Long studentId = 1L;
        Long studentWorkoutId = 100L;

        PatchStudentWorkoutRequest request = createPatchStudentWorkoutRequest();

        StudentWorkoutResponse response = createStudentWorkoutResponse(
                studentWorkoutId,
                studentId,
                10L,
                WorkoutStatus.INACTIVE,
                WeekDay.MONDAY
        );

        when(studentWorkoutService.patch(
                eq(studentId),
                eq(studentWorkoutId),
                any(PatchStudentWorkoutRequest.class)
        )).thenReturn(response);

        mockMvc.perform(patch(
                        "/api/students/{studentId}/workouts/{studentWorkoutId}",
                        studentId,
                        studentWorkoutId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentWorkoutId").value(studentWorkoutId))
                .andExpect(jsonPath("$.studentId").value(studentId))
                .andExpect(jsonPath("$.studentName").value("Aluno Teste"))
                .andExpect(jsonPath("$.workoutId").value(10L))
                .andExpect(jsonPath("$.workoutName").value("Treino A"))
                .andExpect(jsonPath("$.teacherName").value("Professor Teste"))
                .andExpect(jsonPath("$.weekDay").value("MONDAY"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(studentWorkoutService).patch(
                eq(studentId),
                eq(studentWorkoutId),
                any(PatchStudentWorkoutRequest.class)
        );
    }

    @Test
    void shouldDeleteStudentWorkoutSuccessfully() throws Exception {
        Long studentId = 1L;
        Long studentWorkoutId = 100L;

        mockMvc.perform(delete(
                        "/api/students/{studentId}/workouts/{studentWorkoutId}",
                        studentId,
                        studentWorkoutId
                ))
                .andExpect(status().isNoContent());

        verify(studentWorkoutService).delete(studentId, studentWorkoutId);
    }

    @Test
    void shouldFindCurrentWorkoutSuccessfully() throws Exception {
        Long studentId = 1L;

        StudentCurrentWorkoutResponse response = createStudentCurrentWorkoutResponse(studentId);

        when(studentWorkoutService.findCurrentWorkout(studentId))
                .thenReturn(response);

        mockMvc.perform(get("/api/students/{studentId}/workouts/current", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(studentId))
                .andExpect(jsonPath("$.studentWorkoutId").value(100L))
                .andExpect(jsonPath("$.workoutId").value(10L))
                .andExpect(jsonPath("$.workoutName").value("Treino A"))
                .andExpect(jsonPath("$.teacherName").value("Professor Teste"))
                .andExpect(jsonPath("$.weekDay").value("MONDAY"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.exercises[0].workoutExerciseId").value(1000L))
                .andExpect(jsonPath("$.exercises[0].exerciseId").value(20L))
                .andExpect(jsonPath("$.exercises[0].exerciseName").value("Supino reto"))
                .andExpect(jsonPath("$.exercises[0].equipmentName").value("Barra"))
                .andExpect(jsonPath("$.exercises[0].muscleGroup").value("Peitoral"))
                .andExpect(jsonPath("$.exercises[0].description").value("Exercício para peitoral"))
                .andExpect(jsonPath("$.exercises[0].exerciseOrder").value(1))
                .andExpect(jsonPath("$.exercises[0].sets").value(4))
                .andExpect(jsonPath("$.exercises[0].reps").value("8-12"))
                .andExpect(jsonPath("$.exercises[0].recommendedLoad").value(40.00))
                .andExpect(jsonPath("$.exercises[0].restTimeSeconds").value(60))
                .andExpect(jsonPath("$.exercises[0].notes").value("Manter controle do movimento."))
                .andExpect(jsonPath("$.exercises[0].imageUrl").value("https://example.com/image.jpg"))
                .andExpect(jsonPath("$.exercises[0].videoUrl").value("https://example.com/video.mp4"));

        verify(studentWorkoutService).findCurrentWorkout(studentId);
    }

    @Test
    void shouldFindWorkoutDetailsSuccessfully() throws Exception {
        Long studentId = 1L;
        Long studentWorkoutId = 100L;

        StudentCurrentWorkoutResponse response = createStudentCurrentWorkoutResponse(studentId);

        when(studentWorkoutService.findWorkoutDetails(studentId, studentWorkoutId))
                .thenReturn(response);

        mockMvc.perform(get(
                        "/api/students/{studentId}/workouts/{studentWorkoutId}/details",
                        studentId,
                        studentWorkoutId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(studentId))
                .andExpect(jsonPath("$.studentWorkoutId").value(100L))
                .andExpect(jsonPath("$.workoutId").value(10L))
                .andExpect(jsonPath("$.workoutName").value("Treino A"))
                .andExpect(jsonPath("$.teacherName").value("Professor Teste"))
                .andExpect(jsonPath("$.weekDay").value("MONDAY"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.exercises[0].workoutExerciseId").value(1000L))
                .andExpect(jsonPath("$.exercises[0].exerciseId").value(20L))
                .andExpect(jsonPath("$.exercises[0].exerciseName").value("Supino reto"))
                .andExpect(jsonPath("$.exercises[0].equipmentName").value("Barra"))
                .andExpect(jsonPath("$.exercises[0].muscleGroup").value("Peitoral"))
                .andExpect(jsonPath("$.exercises[0].description").value("Exercício para peitoral"))
                .andExpect(jsonPath("$.exercises[0].exerciseOrder").value(1))
                .andExpect(jsonPath("$.exercises[0].sets").value(4))
                .andExpect(jsonPath("$.exercises[0].reps").value("8-12"))
                .andExpect(jsonPath("$.exercises[0].recommendedLoad").value(40.00))
                .andExpect(jsonPath("$.exercises[0].restTimeSeconds").value(60))
                .andExpect(jsonPath("$.exercises[0].notes").value("Manter controle do movimento."))
                .andExpect(jsonPath("$.exercises[0].imageUrl").value("https://example.com/image.jpg"))
                .andExpect(jsonPath("$.exercises[0].videoUrl").value("https://example.com/video.mp4"));

        verify(studentWorkoutService).findWorkoutDetails(studentId, studentWorkoutId);
    }

    @Test
    void shouldReturnNotFoundWhenWorkoutDetailsDoesNotExist() throws Exception {
        Long studentId = 1L;
        Long studentWorkoutId = 100L;

        when(studentWorkoutService.findWorkoutDetails(studentId, studentWorkoutId))
                .thenThrow(new ResourceNotFoundException(
                        "Student workout not found with id: " + studentWorkoutId
                ));

        mockMvc.perform(get(
                        "/api/students/{studentId}/workouts/{studentWorkoutId}/details",
                        studentId,
                        studentWorkoutId
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Student workout not found with id: " + studentWorkoutId))
                .andExpect(jsonPath("$.path").value(
                        "/api/students/" + studentId + "/workouts/" + studentWorkoutId + "/details"
                ));

        verify(studentWorkoutService).findWorkoutDetails(studentId, studentWorkoutId);
    }

    @Test
    void shouldReturnNotFoundWhenStudentWorkoutDoesNotExist() throws Exception {
        Long studentId = 1L;
        Long studentWorkoutId = 100L;

        when(studentWorkoutService.findById(studentId, studentWorkoutId))
                .thenThrow(new ResourceNotFoundException(
                        "Student workout not found with id: " + studentWorkoutId
                ));

        mockMvc.perform(get(
                        "/api/students/{studentId}/workouts/{studentWorkoutId}",
                        studentId,
                        studentWorkoutId
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Student workout not found with id: " + studentWorkoutId))
                .andExpect(jsonPath("$.path").value("/api/students/" + studentId + "/workouts/" + studentWorkoutId));

        verify(studentWorkoutService).findById(studentId, studentWorkoutId);
    }

    @Test
    void shouldReturnConflictWhenStudentAlreadyHasActiveWorkoutForWeekDay() throws Exception {
        Long studentId = 1L;

        CreateStudentWorkoutRequest request = createStudentWorkoutRequest();

        when(studentWorkoutService.create(eq(studentId), any(CreateStudentWorkoutRequest.class)))
                .thenThrow(new DuplicateResourceException(
                        "Student already has an active workout for this week day"
                ));

        mockMvc.perform(post("/api/students/{studentId}/workouts", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Student already has an active workout for this week day"))
                .andExpect(jsonPath("$.path").value("/api/students/" + studentId + "/workouts"));

        verify(studentWorkoutService).create(eq(studentId), any(CreateStudentWorkoutRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreateStudentWorkoutRequestIsInvalid() throws Exception {
        Long studentId = 1L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(
                null,
                WeekDay.MONDAY
        );

        mockMvc.perform(post("/api/students/{studentId}/workouts", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("workoutId: O treino é obrigatório"))
                .andExpect(jsonPath("$.path").value("/api/students/" + studentId + "/workouts"));

        verify(studentWorkoutService, never())
                .create(eq(studentId), any(CreateStudentWorkoutRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenWeekDayIsNull() throws Exception {
        Long studentId = 1L;

        CreateStudentWorkoutRequest request = new CreateStudentWorkoutRequest(
                10L,
                null
        );

        mockMvc.perform(post("/api/students/{studentId}/workouts", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("weekDay: O dia da semana é obrigatório"))
                .andExpect(jsonPath("$.path").value("/api/students/" + studentId + "/workouts"));

        verify(studentWorkoutService, never())
                .create(eq(studentId), any(CreateStudentWorkoutRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenBusinessRuleIsViolated() throws Exception {
        Long studentId = 1L;

        CreateStudentWorkoutRequest request = createStudentWorkoutRequest();

        when(studentWorkoutService.create(eq(studentId), any(CreateStudentWorkoutRequest.class)))
                .thenThrow(new BusinessRuleException(
                        "User is not a student with id: " + studentId
                ));

        mockMvc.perform(post("/api/students/{studentId}/workouts", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("User is not a student with id: " + studentId))
                .andExpect(jsonPath("$.path").value("/api/students/" + studentId + "/workouts"));

        verify(studentWorkoutService).create(eq(studentId), any(CreateStudentWorkoutRequest.class));
    }

    @Test
    void shouldReturnNotFoundWhenCurrentWorkoutDoesNotExist() throws Exception {
        Long studentId = 1L;

        when(studentWorkoutService.findCurrentWorkout(studentId))
                .thenThrow(new ResourceNotFoundException(
                        "Active workout not found student id: " + studentId
                ));

        mockMvc.perform(get("/api/students/{studentId}/workouts/current", studentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Active workout not found student id: " + studentId))
                .andExpect(jsonPath("$.path").value("/api/students/" + studentId + "/workouts/current"));

        verify(studentWorkoutService).findCurrentWorkout(studentId);
    }

    @Test
    void shouldFindCurrentWorkoutWithEmptyExercisesSuccessfully() throws Exception {
        Long studentId = 1L;

        StudentCurrentWorkoutResponse response = new StudentCurrentWorkoutResponse(
                studentId,
                100L,
                10L,
                "Treino A",
                "Professor Teste",
                LocalDateTime.of(2026, 6, 23, 10, 0),
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE,
                List.of()
        );

        when(studentWorkoutService.findCurrentWorkout(studentId))
                .thenReturn(response);

        mockMvc.perform(get("/api/students/{studentId}/workouts/current", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(studentId))
                .andExpect(jsonPath("$.studentWorkoutId").value(100L))
                .andExpect(jsonPath("$.workoutId").value(10L))
                .andExpect(jsonPath("$.workoutName").value("Treino A"))
                .andExpect(jsonPath("$.teacherName").value("Professor Teste"))
                .andExpect(jsonPath("$.weekDay").value("MONDAY"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.exercises").isArray())
                .andExpect(jsonPath("$.exercises").isEmpty());

        verify(studentWorkoutService).findCurrentWorkout(studentId);
    }

    private CreateStudentWorkoutRequest createStudentWorkoutRequest() {
        return new CreateStudentWorkoutRequest(
                10L,
                WeekDay.MONDAY
        );
    }

    private PatchStudentWorkoutRequest createPatchStudentWorkoutRequest() {
        return new PatchStudentWorkoutRequest(
                WorkoutStatus.INACTIVE
        );
    }

    private StudentWorkoutResponse createStudentWorkoutResponse(
            Long studentWorkoutId,
            Long studentId,
            Long workoutId,
            WorkoutStatus status,
            WeekDay weekDay
    ) {
        return new StudentWorkoutResponse(
                studentWorkoutId,
                studentId,
                "Aluno Teste",
                workoutId,
                "Treino A",
                "Professor Teste",
                LocalDateTime.of(2026, 6, 23, 10, 0),
                weekDay,
                status,
                null,
                null
        );
    }

    private StudentCurrentWorkoutResponse createStudentCurrentWorkoutResponse(Long studentId) {
        StudentCurrentWorkoutExerciseResponse exerciseResponse = new StudentCurrentWorkoutExerciseResponse(
                1000L,
                20L,
                "Supino reto",
                "Barra",
                "Peitoral",
                "Exercício para peitoral",
                1,
                4,
                "8-12",
                new BigDecimal("40.00"),
                60,
                "Manter controle do movimento.",
                "https://example.com/image.jpg",
                "https://example.com/video.mp4"
        );

        return new StudentCurrentWorkoutResponse(
                studentId,
                100L,
                10L,
                "Treino A",
                "Professor Teste",
                LocalDateTime.of(2026, 6, 23, 10, 0),
                WeekDay.MONDAY,
                WorkoutStatus.ACTIVE,
                List.of(exerciseResponse)
        );
    }
}