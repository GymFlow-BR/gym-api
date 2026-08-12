package br.com.gymflow.api.controller;

import br.com.gymflow.api.config.security.JwtAuthenticationFilter;
import br.com.gymflow.api.dto.studentWorkoutProgress.StudentCurrentWorkoutExerciseProgressResponse;
import br.com.gymflow.api.dto.studentWorkoutProgress.StudentCurrentWorkoutProgressResponse;
import br.com.gymflow.api.dto.studentWorkoutProgress.StudentWorkoutExerciseProgressResponse;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.service.StudentWorkoutProgressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentWorkoutProgressController.class)
@AutoConfigureMockMvc(addFilters = false)
class StudentWorkoutProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentWorkoutProgressService studentWorkoutProgressService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldCompleteExerciseSuccessfully() throws Exception {
        // Arrange
        Long studentId = 1L;
        Long workoutExerciseId = 100L;

        LocalDateTime completedAt = LocalDateTime.of(2026, 6, 28, 20, 30);

        StudentWorkoutExerciseProgressResponse response =
                new StudentWorkoutExerciseProgressResponse(
                        50L,
                        workoutExerciseId,
                        true,
                        completedAt
                );

        when(studentWorkoutProgressService.completeExercise(studentId, workoutExerciseId))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(patch(
                        "/api/students/{studentId}/workouts/current/exercises/{workoutExerciseId}/complete",
                        studentId,
                        workoutExerciseId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentWorkoutId").value(50L))
                .andExpect(jsonPath("$.workoutExerciseId").value(workoutExerciseId))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.completedAt").exists());

        verify(studentWorkoutProgressService).completeExercise(studentId, workoutExerciseId);
    }

    @Test
    void shouldUncompleteExerciseSuccessfully() throws Exception {
        // Arrange
        Long studentId = 1L;
        Long workoutExerciseId = 100L;

        StudentWorkoutExerciseProgressResponse response =
                new StudentWorkoutExerciseProgressResponse(
                        50L,
                        workoutExerciseId,
                        false,
                        null
                );

        when(studentWorkoutProgressService.uncompleteExercise(studentId, workoutExerciseId))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(patch(
                        "/api/students/{studentId}/workouts/current/exercises/{workoutExerciseId}/uncomplete",
                        studentId,
                        workoutExerciseId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentWorkoutId").value(50L))
                .andExpect(jsonPath("$.workoutExerciseId").value(workoutExerciseId))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.completedAt").doesNotExist());

        verify(studentWorkoutProgressService).uncompleteExercise(studentId, workoutExerciseId);
    }

    @Test
    void shouldGetCurrentWorkoutProgressSuccessfully() throws Exception {
        // Arrange
        Long studentId = 1L;

        StudentCurrentWorkoutExerciseProgressResponse exerciseA =
                new StudentCurrentWorkoutExerciseProgressResponse(
                        100L,
                        20L,
                        "Supino reto",
                        1,
                        true,
                        LocalDateTime.of(2026, 6, 28, 20, 30)
                );

        StudentCurrentWorkoutExerciseProgressResponse exerciseB =
                new StudentCurrentWorkoutExerciseProgressResponse(
                        101L,
                        21L,
                        "Remada baixa",
                        2,
                        false,
                        null
                );

        StudentCurrentWorkoutProgressResponse response =
                new StudentCurrentWorkoutProgressResponse(
                        studentId,
                        50L,
                        10L,
                        "Treino A",
                        2,
                        1,
                        50,
                        List.of(exerciseA, exerciseB)
                );

        when(studentWorkoutProgressService.getCurrentWorkoutProgress(studentId))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(get("/api/students/{studentId}/workouts/current/progress", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(studentId))
                .andExpect(jsonPath("$.studentWorkoutId").value(50L))
                .andExpect(jsonPath("$.workoutId").value(10L))
                .andExpect(jsonPath("$.workoutName").value("Treino A"))
                .andExpect(jsonPath("$.totalExercises").value(2))
                .andExpect(jsonPath("$.completedExercises").value(1))
                .andExpect(jsonPath("$.progressPercentage").value(50))
                .andExpect(jsonPath("$.exercises[0].workoutExerciseId").value(100L))
                .andExpect(jsonPath("$.exercises[0].exerciseId").value(20L))
                .andExpect(jsonPath("$.exercises[0].exerciseName").value("Supino reto"))
                .andExpect(jsonPath("$.exercises[0].exerciseOrder").value(1))
                .andExpect(jsonPath("$.exercises[0].completed").value(true))
                .andExpect(jsonPath("$.exercises[0].completedAt").exists())
                .andExpect(jsonPath("$.exercises[1].workoutExerciseId").value(101L))
                .andExpect(jsonPath("$.exercises[1].exerciseId").value(21L))
                .andExpect(jsonPath("$.exercises[1].exerciseName").value("Remada baixa"))
                .andExpect(jsonPath("$.exercises[1].exerciseOrder").value(2))
                .andExpect(jsonPath("$.exercises[1].completed").value(false))
                .andExpect(jsonPath("$.exercises[1].completedAt").doesNotExist());

        verify(studentWorkoutProgressService).getCurrentWorkoutProgress(studentId);
    }

    @Test
    void shouldReturnNotFoundWhenCurrentWorkoutDoesNotExistOnComplete() throws Exception {
        // Arrange
        Long studentId = 1L;
        Long workoutExerciseId = 100L;

        when(studentWorkoutProgressService.completeExercise(studentId, workoutExerciseId))
                .thenThrow(new ResourceNotFoundException(
                        "Active workout not found student id: " + studentId
                ));

        // Act + Assert
        mockMvc.perform(patch(
                        "/api/students/{studentId}/workouts/current/exercises/{workoutExerciseId}/complete",
                        studentId,
                        workoutExerciseId
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        "Active workout not found student id: " + studentId
                ))
                .andExpect(jsonPath("$.path").value(
                        "/api/students/" + studentId + "/workouts/current/exercises/" + workoutExerciseId + "/complete"
                ));

        verify(studentWorkoutProgressService).completeExercise(studentId, workoutExerciseId);
    }

    @Test
    void shouldReturnNotFoundWhenWorkoutExerciseDoesNotBelongToCurrentWorkout() throws Exception {
        // Arrange
        Long studentId = 1L;
        Long workoutExerciseId = 999L;

        when(studentWorkoutProgressService.completeExercise(studentId, workoutExerciseId))
                .thenThrow(new ResourceNotFoundException(
                        "Workout exercise not found with id: " + workoutExerciseId + " for current workout"
                ));

        // Act + Assert
        mockMvc.perform(patch(
                        "/api/students/{studentId}/workouts/current/exercises/{workoutExerciseId}/complete",
                        studentId,
                        workoutExerciseId
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        "Workout exercise not found with id: " + workoutExerciseId + " for current workout"
                ))
                .andExpect(jsonPath("$.path").value(
                        "/api/students/" + studentId + "/workouts/current/exercises/" + workoutExerciseId + "/complete"
                ));

        verify(studentWorkoutProgressService).completeExercise(studentId, workoutExerciseId);
    }

    @Test
    void shouldReturnNotFoundWhenCurrentWorkoutDoesNotExistOnProgress() throws Exception {
        // Arrange
        Long studentId = 1L;

        when(studentWorkoutProgressService.getCurrentWorkoutProgress(studentId))
                .thenThrow(new ResourceNotFoundException(
                        "Active workout not found student id: " + studentId
                ));

        // Act + Assert
        mockMvc.perform(get("/api/students/{studentId}/workouts/current/progress", studentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        "Active workout not found student id: " + studentId
                ))
                .andExpect(jsonPath("$.path").value(
                        "/api/students/" + studentId + "/workouts/current/progress"
                ));

        verify(studentWorkoutProgressService).getCurrentWorkoutProgress(studentId);
    }

    @Test
    void shouldCompleteExerciseForSpecificStudentWorkoutSuccessfully() throws Exception {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutExerciseId = 100L;

        LocalDateTime completedAt = LocalDateTime.of(2026, 6, 28, 20, 30);

        StudentWorkoutExerciseProgressResponse response =
                new StudentWorkoutExerciseProgressResponse(
                        studentWorkoutId,
                        workoutExerciseId,
                        true,
                        completedAt
                );

        when(studentWorkoutProgressService.completeExercise(
                studentId,
                studentWorkoutId,
                workoutExerciseId
        )).thenReturn(response);

        mockMvc.perform(patch(
                        "/api/students/{studentId}/workouts/{studentWorkoutId}/exercises/{workoutExerciseId}/complete",
                        studentId,
                        studentWorkoutId,
                        workoutExerciseId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentWorkoutId").value(studentWorkoutId))
                .andExpect(jsonPath("$.workoutExerciseId").value(workoutExerciseId))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.completedAt").exists());

        verify(studentWorkoutProgressService).completeExercise(
                studentId,
                studentWorkoutId,
                workoutExerciseId
        );
    }

    @Test
    void shouldUncompleteExerciseForSpecificStudentWorkoutSuccessfully() throws Exception {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;
        Long workoutExerciseId = 100L;

        StudentWorkoutExerciseProgressResponse response =
                new StudentWorkoutExerciseProgressResponse(
                        studentWorkoutId,
                        workoutExerciseId,
                        false,
                        null
                );

        when(studentWorkoutProgressService.uncompleteExercise(
                studentId,
                studentWorkoutId,
                workoutExerciseId
        )).thenReturn(response);

        mockMvc.perform(patch(
                        "/api/students/{studentId}/workouts/{studentWorkoutId}/exercises/{workoutExerciseId}/uncomplete",
                        studentId,
                        studentWorkoutId,
                        workoutExerciseId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentWorkoutId").value(studentWorkoutId))
                .andExpect(jsonPath("$.workoutExerciseId").value(workoutExerciseId))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.completedAt").doesNotExist());

        verify(studentWorkoutProgressService).uncompleteExercise(
                studentId,
                studentWorkoutId,
                workoutExerciseId
        );
    }

    @Test
    void shouldGetSpecificStudentWorkoutProgressSuccessfully() throws Exception {
        Long studentId = 1L;
        Long studentWorkoutId = 50L;

        StudentCurrentWorkoutExerciseProgressResponse exerciseA =
                new StudentCurrentWorkoutExerciseProgressResponse(
                        100L,
                        20L,
                        "Supino reto",
                        1,
                        true,
                        LocalDateTime.of(2026, 6, 28, 20, 30)
                );

        StudentCurrentWorkoutExerciseProgressResponse exerciseB =
                new StudentCurrentWorkoutExerciseProgressResponse(
                        101L,
                        21L,
                        "Remada baixa",
                        2,
                        false,
                        null
                );

        StudentCurrentWorkoutProgressResponse response =
                new StudentCurrentWorkoutProgressResponse(
                        studentId,
                        studentWorkoutId,
                        10L,
                        "Treino A",
                        2,
                        1,
                        50,
                        List.of(exerciseA, exerciseB)
                );

        when(studentWorkoutProgressService.getWorkoutProgress(studentId, studentWorkoutId))
                .thenReturn(response);

        mockMvc.perform(get(
                        "/api/students/{studentId}/workouts/{studentWorkoutId}/progress",
                        studentId,
                        studentWorkoutId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(studentId))
                .andExpect(jsonPath("$.studentWorkoutId").value(studentWorkoutId))
                .andExpect(jsonPath("$.workoutId").value(10L))
                .andExpect(jsonPath("$.workoutName").value("Treino A"))
                .andExpect(jsonPath("$.totalExercises").value(2))
                .andExpect(jsonPath("$.completedExercises").value(1))
                .andExpect(jsonPath("$.progressPercentage").value(50))
                .andExpect(jsonPath("$.exercises[0].workoutExerciseId").value(100L))
                .andExpect(jsonPath("$.exercises[0].completed").value(true))
                .andExpect(jsonPath("$.exercises[1].workoutExerciseId").value(101L))
                .andExpect(jsonPath("$.exercises[1].completed").value(false));

        verify(studentWorkoutProgressService).getWorkoutProgress(studentId, studentWorkoutId);
    }
}