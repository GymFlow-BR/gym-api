package br.com.gymflow.api.service;

import br.com.gymflow.api.config.security.StudentAccessValidator;
import br.com.gymflow.api.domain.StudentWorkout;
import br.com.gymflow.api.domain.StudentWorkoutExerciseProgress;
import br.com.gymflow.api.domain.WorkoutExercise;
import br.com.gymflow.api.domain.enums.WeekDay;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
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

        return completeExerciseForStudentWorkout(studentWorkout, workoutExerciseId);
    }

    @Transactional
    public StudentWorkoutExerciseProgressResponse completeExercise(
            Long studentId,
            Long studentWorkoutId,
            Long workoutExerciseId
    ) {
        studentAccessValidator.validateStudentAccess(studentId);

        StudentWorkout studentWorkout = getActiveStudentWorkoutById(
                studentId,
                studentWorkoutId
        );

        return completeExerciseForStudentWorkout(studentWorkout, workoutExerciseId);
    }

    @Transactional
    public StudentWorkoutExerciseProgressResponse uncompleteExercise(
            Long studentId,
            Long workoutExerciseId
    ) {
        studentAccessValidator.validateStudentAccess(studentId);

        StudentWorkout studentWorkout = getCurrentActiveStudentWorkout(studentId);

        return uncompleteExerciseForStudentWorkout(studentWorkout, workoutExerciseId);
    }

    @Transactional
    public StudentWorkoutExerciseProgressResponse uncompleteExercise(
            Long studentId,
            Long studentWorkoutId,
            Long workoutExerciseId
    ) {
        studentAccessValidator.validateStudentAccess(studentId);

        StudentWorkout studentWorkout = getActiveStudentWorkoutById(
                studentId,
                studentWorkoutId
        );

        return uncompleteExerciseForStudentWorkout(studentWorkout, workoutExerciseId);
    }

    @Transactional(readOnly = true)
    public StudentCurrentWorkoutProgressResponse getCurrentWorkoutProgress(Long studentId) {
        studentAccessValidator.validateStudentAccess(studentId);

        StudentWorkout studentWorkout = getCurrentActiveStudentWorkout(studentId);

        return buildWorkoutProgressResponse(studentWorkout);
    }

    @Transactional(readOnly = true)
    public StudentCurrentWorkoutProgressResponse getWorkoutProgress(
            Long studentId,
            Long studentWorkoutId
    ) {
        studentAccessValidator.validateStudentAccess(studentId);

        StudentWorkout studentWorkout = getActiveStudentWorkoutById(
                studentId,
                studentWorkoutId
        );

        return buildWorkoutProgressResponse(studentWorkout);
    }

    private StudentWorkoutExerciseProgressResponse completeExerciseForStudentWorkout(
            StudentWorkout studentWorkout,
            Long workoutExerciseId
    ) {
        WorkoutExercise workoutExercise = getWorkoutExerciseFromStudentWorkout(
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

    private StudentWorkoutExerciseProgressResponse uncompleteExerciseForStudentWorkout(
            StudentWorkout studentWorkout,
            Long workoutExerciseId
    ) {
        WorkoutExercise workoutExercise = getWorkoutExerciseFromStudentWorkout(
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

    private StudentCurrentWorkoutProgressResponse buildWorkoutProgressResponse(
            StudentWorkout studentWorkout
    ) {
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
        WeekDay today = getTodayWeekDay();

        StudentWorkout studentWorkout = studentWorkoutRepository
                .findFirstByStudentIdAndStatusAndWeekDay(
                        studentId,
                        WorkoutStatus.ACTIVE,
                        today
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active workout not found student id: " + studentId
                ));

        validateStudentWorkoutIsActive(studentWorkout, studentId);

        return studentWorkout;
    }

    private StudentWorkout getActiveStudentWorkoutById(
            Long studentId,
            Long studentWorkoutId
    ) {
        StudentWorkout studentWorkout = studentWorkoutRepository
                .findById(studentWorkoutId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student workout not found with id: "
                                + studentWorkoutId
                                + " for student id: "
                                + studentId
                ));

        if (!studentWorkout.getStudent().getId().equals(studentId)) {
            throw new ResourceNotFoundException(
                    "Student workout not found with id: "
                            + studentWorkoutId
                            + " for student id: "
                            + studentId
            );
        }

        validateStudentWorkoutIsActive(studentWorkout, studentId);

        return studentWorkout;
    }

    private void validateStudentWorkoutIsActive(
            StudentWorkout studentWorkout,
            Long studentId
    ) {
        if (studentWorkout.getStatus() != WorkoutStatus.ACTIVE) {
            throw new ResourceNotFoundException(
                    "Active workout not found student id: " + studentId
            );
        }

        if (studentWorkout.getWorkout().getStatus() != WorkoutStatus.ACTIVE) {
            throw new ResourceNotFoundException(
                    "Active workout not found student id: " + studentId
            );
        }
    }

    private WorkoutExercise getWorkoutExerciseFromStudentWorkout(
            StudentWorkout studentWorkout,
            Long workoutExerciseId
    ) {
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(workoutExerciseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workout exercise not found with id: " + workoutExerciseId
                ));

        Long studentWorkoutWorkoutId = studentWorkout.getWorkout().getId();
        Long workoutExerciseWorkoutId = workoutExercise.getWorkout().getId();

        if (!studentWorkoutWorkoutId.equals(workoutExerciseWorkoutId)) {
            throw new ResourceNotFoundException(
                    "Workout exercise not found with id: "
                            + workoutExerciseId
                            + " for student workout"
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

    private WeekDay getTodayWeekDay() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();

        return switch (dayOfWeek) {
            case MONDAY -> WeekDay.MONDAY;
            case TUESDAY -> WeekDay.TUESDAY;
            case WEDNESDAY -> WeekDay.WEDNESDAY;
            case THURSDAY -> WeekDay.THURSDAY;
            case FRIDAY -> WeekDay.FRIDAY;
            case SATURDAY -> WeekDay.SATURDAY;
            case SUNDAY -> WeekDay.SUNDAY;
        };
    }
}