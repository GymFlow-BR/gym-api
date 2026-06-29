package br.com.gymflow.api.service;

import br.com.gymflow.api.config.security.StudentAccessValidator;
import br.com.gymflow.api.domain.StudentWorkout;
import br.com.gymflow.api.domain.StudentWorkoutExerciseProgress;
import br.com.gymflow.api.domain.WorkoutExercise;
import br.com.gymflow.api.domain.enums.WorkoutStatus;
import br.com.gymflow.api.dto.studentWorkoutProgress.StudentCurrentWorkoutExerciseProgressResponse;
import br.com.gymflow.api.dto.studentWorkoutProgress.StudentCurrentWorkoutProgressResponse;
import br.com.gymflow.api.dto.studentWorkoutProgress.StudentWorkoutExerciseProgressResponse;
import br.com.gymflow.api.event.StudentWorkoutExerciseCompletedEvent;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.repository.StudentWorkoutExerciseProgressRepository;
import br.com.gymflow.api.repository.StudentWorkoutRepository;
import br.com.gymflow.api.repository.WorkoutExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentWorkoutProgressService {

    private final StudentWorkoutRepository studentWorkoutRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final StudentWorkoutExerciseProgressRepository progressRepository;
    private final StudentAccessValidator studentAccessValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public StudentWorkoutExerciseProgressResponse completeExercise(
            Long studentId,
            Long workoutExerciseId
    ) {
        studentAccessValidator.validateStudentAccess(studentId);

        StudentWorkout studentWorkout = getCurrentActiveStudentWorkout(studentId);
        WorkoutExercise workoutExercise = getWorkoutExerciseFromCurrentWorkout(
                studentWorkout,
                workoutExerciseId
        );

        StudentWorkoutExerciseProgress progress = progressRepository
                .findByStudentWorkoutIdAndWorkoutExerciseId(
                        studentWorkout.getId(),
                        workoutExerciseId
                )
                .orElseGet(StudentWorkoutExerciseProgress::new);

        progress.setStudentWorkout(studentWorkout);
        progress.setWorkoutExercise(workoutExercise);
        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());

        StudentWorkoutExerciseProgress savedProgress = progressRepository.save(progress);

        eventPublisher.publishEvent(new StudentWorkoutExerciseCompletedEvent(
                studentWorkout.getStudent().getId(),
                studentWorkout.getId(),
                studentWorkout.getWorkout().getId(),
                workoutExercise.getId(),
                workoutExercise.getExercise().getId(),
                workoutExercise.getExercise().getExerciseName(),
                savedProgress.getCompletedAt()
        ));

        return toProgressResponse(savedProgress);
    }

    @Transactional
    public StudentWorkoutExerciseProgressResponse uncompleteExercise(
            Long studentId,
            Long workoutExerciseId
    ) {
        studentAccessValidator.validateStudentAccess(studentId);

        StudentWorkout studentWorkout = getCurrentActiveStudentWorkout(studentId);
        WorkoutExercise workoutExercise = getWorkoutExerciseFromCurrentWorkout(
                studentWorkout,
                workoutExerciseId
        );

        StudentWorkoutExerciseProgress progress = progressRepository
                .findByStudentWorkoutIdAndWorkoutExerciseId(
                        studentWorkout.getId(),
                        workoutExerciseId
                )
                .orElseGet(StudentWorkoutExerciseProgress::new);

        progress.setStudentWorkout(studentWorkout);
        progress.setWorkoutExercise(workoutExercise);
        progress.setCompleted(false);
        progress.setCompletedAt(null);

        StudentWorkoutExerciseProgress savedProgress = progressRepository.save(progress);

        return toProgressResponse(savedProgress);
    }

    @Transactional(readOnly = true)
    public StudentCurrentWorkoutProgressResponse getCurrentWorkoutProgress(Long studentId) {
        studentAccessValidator.validateStudentAccess(studentId);

        StudentWorkout studentWorkout = getCurrentActiveStudentWorkout(studentId);

        List<WorkoutExercise> workoutExercises = workoutExerciseRepository
                .findAllByWorkoutIdOrderByExerciseOrderAsc(studentWorkout.getWorkout().getId());

        List<StudentWorkoutExerciseProgress> progressList = progressRepository
                .findAllByStudentWorkoutId(studentWorkout.getId());

        Map<Long, StudentWorkoutExerciseProgress> progressByWorkoutExerciseId = progressList
                .stream()
                .collect(Collectors.toMap(
                        progress -> progress.getWorkoutExercise().getId(),
                        progress -> progress
                ));

        List<StudentCurrentWorkoutExerciseProgressResponse> exerciseResponses = workoutExercises
                .stream()
                .map(workoutExercise -> {
                    StudentWorkoutExerciseProgress progress = progressByWorkoutExerciseId
                            .get(workoutExercise.getId());

                    boolean completed = progress != null && Boolean.TRUE.equals(progress.getCompleted());

                    return new StudentCurrentWorkoutExerciseProgressResponse(
                            workoutExercise.getId(),
                            workoutExercise.getExercise().getId(),
                            workoutExercise.getExercise().getExerciseName(),
                            workoutExercise.getExerciseOrder(),
                            completed,
                            progress != null ? progress.getCompletedAt() : null
                    );
                })
                .toList();

        int totalExercises = workoutExercises.size();

        int completedExercises = (int) exerciseResponses
                .stream()
                .filter(StudentCurrentWorkoutExerciseProgressResponse::completed)
                .count();

        int progressPercentage = totalExercises == 0
                ? 0
                : (completedExercises * 100) / totalExercises;

        return new StudentCurrentWorkoutProgressResponse(
                studentWorkout.getStudent().getId(),
                studentWorkout.getId(),
                studentWorkout.getWorkout().getId(),
                studentWorkout.getWorkout().getWorkoutName(),
                totalExercises,
                completedExercises,
                progressPercentage,
                exerciseResponses
        );
    }

    private StudentWorkout getCurrentActiveStudentWorkout(Long studentId) {
        StudentWorkout studentWorkout = studentWorkoutRepository
                .findFirstByStudentIdAndStatusOrderByAssignedAtDesc(studentId, WorkoutStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active workout not found student id: " + studentId
                ));

        if (studentWorkout.getWorkout().getStatus() != WorkoutStatus.ACTIVE) {
            throw new ResourceNotFoundException(
                    "Active workout not found student id: " + studentId
            );
        }

        return studentWorkout;
    }

    private WorkoutExercise getWorkoutExerciseFromCurrentWorkout(
            StudentWorkout studentWorkout,
            Long workoutExerciseId
    ) {
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(workoutExerciseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workout exercise not found with id: " + workoutExerciseId
                ));

        Long currentWorkoutId = studentWorkout.getWorkout().getId();
        Long workoutExerciseWorkoutId = workoutExercise.getWorkout().getId();

        if (!currentWorkoutId.equals(workoutExerciseWorkoutId)) {
            throw new ResourceNotFoundException(
                    "Workout exercise not found with id: "
                            + workoutExerciseId
                            + " for current workout"
            );
        }

        return workoutExercise;
    }

    private StudentWorkoutExerciseProgressResponse toProgressResponse(
            StudentWorkoutExerciseProgress progress
    ) {
        return new StudentWorkoutExerciseProgressResponse(
                progress.getStudentWorkout().getId(),
                progress.getWorkoutExercise().getId(),
                progress.getCompleted(),
                progress.getCompletedAt()
        );
    }
}