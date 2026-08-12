package br.com.gymflow.api.service;

import br.com.gymflow.api.config.security.StudentAccessValidator;
import br.com.gymflow.api.domain.StudentWorkout;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.Workout;
import br.com.gymflow.api.domain.WorkoutExercise;
import br.com.gymflow.api.domain.enums.WeekDay;
import br.com.gymflow.api.domain.enums.WorkoutStatus;
import br.com.gymflow.api.dto.studentWorkouts.CreateStudentWorkoutRequest;
import br.com.gymflow.api.dto.studentWorkouts.PatchStudentWorkoutRequest;
import br.com.gymflow.api.dto.studentWorkouts.StudentCurrentWorkoutResponse;
import br.com.gymflow.api.dto.studentWorkouts.StudentWorkoutResponse;
import br.com.gymflow.api.exception.BusinessRuleException;
import br.com.gymflow.api.exception.DuplicateResourceException;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.mapper.StudentWorkoutMapper;
import br.com.gymflow.api.repository.StudentWorkoutRepository;
import br.com.gymflow.api.repository.UserRepository;
import br.com.gymflow.api.repository.WorkoutExerciseRepository;
import br.com.gymflow.api.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentWorkoutService {

    private final StudentWorkoutRepository studentWorkoutRepository;
    private final StudentWorkoutMapper studentWorkoutMapper;
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final StudentAccessValidator studentAccessValidator;

    @Transactional
    public StudentWorkoutResponse create(Long studentId, CreateStudentWorkoutRequest request) {
        studentAccessValidator.validateStudentAccess(studentId);

        User student = getStudentById(studentId);
        Workout workout = getWorkoutById(request.workoutId());

        validateStudentBelongsToWorkoutOrganization(student, workout);

        return studentWorkoutRepository
                .findByStudentIdAndWorkoutIdAndWeekDay(
                        studentId,
                        request.workoutId(),
                        request.weekDay()
                )
                .map(existingStudentWorkout ->
                        reactivateExistingStudentWorkout(studentId, request, existingStudentWorkout)
                )
                .orElseGet(() ->
                        createNewStudentWorkout(studentId, request, student, workout)
                );
    }

    @Transactional(readOnly = true)
    public List<StudentWorkoutResponse> findAllByStudentId(Long studentId) {
        studentAccessValidator.validateStudentAccess(studentId);
        getStudentById(studentId);

        return studentWorkoutRepository.findAllByStudentId(studentId)
                .stream()
                .map(studentWorkoutMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentWorkoutResponse findById(Long studentId, Long studentWorkoutId) {
        studentAccessValidator.validateStudentAccess(studentId);

        StudentWorkout studentWorkout = getStudentWorkoutById(studentWorkoutId);

        validateStudentWorkoutBelongsToStudent(studentWorkout, studentId);

        return studentWorkoutMapper.toResponse(studentWorkout);
    }

    @Transactional
    public StudentWorkoutResponse patch(
            Long studentId,
            Long studentWorkoutId,
            PatchStudentWorkoutRequest request
    ) {
        studentAccessValidator.validateStudentAccess(studentId);

        StudentWorkout studentWorkout = getStudentWorkoutById(studentWorkoutId);

        validateStudentWorkoutBelongsToStudent(studentWorkout, studentId);

        if (request.status() == WorkoutStatus.ACTIVE) {
            validateActiveWorkoutConflictForWeekDay(
                    studentId,
                    studentWorkout.getWeekDay(),
                    studentWorkoutId
            );
        }

        if (request.status() != null) {
            studentWorkout.setStatus(request.status());
        }

        StudentWorkout updatedStudentWorkout = studentWorkoutRepository.save(studentWorkout);

        return studentWorkoutMapper.toResponse(updatedStudentWorkout);
    }

    @Transactional
    public void delete(Long studentId, Long studentWorkoutId) {
        studentAccessValidator.validateStudentAccess(studentId);

        StudentWorkout studentWorkout = getStudentWorkoutById(studentWorkoutId);

        validateStudentWorkoutBelongsToStudent(studentWorkout, studentId);

        studentWorkout.setStatus(WorkoutStatus.INACTIVE);

        studentWorkoutRepository.save(studentWorkout);
    }

    @Transactional(readOnly = true)
    public StudentCurrentWorkoutResponse findCurrentWorkout(Long studentId) {
        studentAccessValidator.validateStudentAccess(studentId);

        getStudentById(studentId);

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

        if (studentWorkout.getWorkout().getStatus() != WorkoutStatus.ACTIVE) {
            throw new ResourceNotFoundException(
                    "Active workout not found student id: " + studentId
            );
        }

        List<WorkoutExercise> workoutExercises = workoutExerciseRepository
                .findAllByWorkoutIdOrderByExerciseOrderAsc(studentWorkout.getWorkout().getId());

        return studentWorkoutMapper.toCurrentWorkoutResponse(studentWorkout, workoutExercises);
    }

    private StudentWorkout getStudentWorkoutById(Long studentWorkoutId) {
        return studentWorkoutRepository.findById(studentWorkoutId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student workout not found with id: " + studentWorkoutId
                ));
    }

    private User getStudentById(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + studentId
                ));

        if (!student.getRole().name().equals("STUDENT")) {
            throw new BusinessRuleException("User is not a student with id: " + studentId);
        }

        return student;
    }

    private Workout getWorkoutById(Long workoutId) {
        return workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workout not found with id: " + workoutId
                ));
    }

    private void validateStudentWorkoutBelongsToStudent(
            StudentWorkout studentWorkout,
            Long studentId
    ) {
        if (!studentWorkout.getStudent().getId().equals(studentId)) {
            throw new ResourceNotFoundException(
                    "Student workout not found with id: "
                            + studentWorkout.getId()
                            + " for student id: "
                            + studentId
            );
        }
    }

    private void validateStudentBelongsToWorkoutOrganization(User student, Workout workout) {
        Long studentOrganizationId = student.getOrganization().getId();
        Long workoutOrganizationId = workout.getTeacher().getOrganization().getId();

        if (!studentOrganizationId.equals(workoutOrganizationId)) {
            throw new BusinessRuleException(
                    "Student does not belong to the same organization as the workout"
            );
        }
    }

    private StudentWorkoutResponse reactivateExistingStudentWorkout(
            Long studentId,
            CreateStudentWorkoutRequest request,
            StudentWorkout existingStudentWorkout
    ) {
        if (existingStudentWorkout.getStatus() == WorkoutStatus.ACTIVE) {
            throw new DuplicateResourceException(
                    "Student already has this workout assigned for this week day"
            );
        }

        validateActiveWorkoutConflictForWeekDay(
                studentId,
                request.weekDay(),
                existingStudentWorkout.getId()
        );

        existingStudentWorkout.setStatus(WorkoutStatus.ACTIVE);
        existingStudentWorkout.setAssignedAt(LocalDateTime.now());
        existingStudentWorkout.setWeekDay(request.weekDay());

        StudentWorkout savedStudentWorkout = studentWorkoutRepository.save(existingStudentWorkout);

        return studentWorkoutMapper.toResponse(savedStudentWorkout);
    }

    private StudentWorkoutResponse createNewStudentWorkout(
            Long studentId,
            CreateStudentWorkoutRequest request,
            User student,
            Workout workout
    ) {
        validateActiveWorkoutConflictForWeekDay(studentId, request.weekDay(), null);

        StudentWorkout studentWorkout = studentWorkoutMapper.toEntity(request);
        studentWorkout.setStudent(student);
        studentWorkout.setWorkout(workout);
        studentWorkout.setStatus(WorkoutStatus.ACTIVE);
        studentWorkout.setAssignedAt(LocalDateTime.now());
        studentWorkout.setWeekDay(request.weekDay());

        StudentWorkout savedStudentWorkout = studentWorkoutRepository.save(studentWorkout);

        return studentWorkoutMapper.toResponse(savedStudentWorkout);
    }

    private void validateActiveWorkoutConflictForWeekDay(
            Long studentId,
            WeekDay weekDay,
            Long studentWorkoutIdToIgnore
    ) {
        boolean hasActiveWorkoutForWeekDay;

        if (studentWorkoutIdToIgnore == null) {
            hasActiveWorkoutForWeekDay = studentWorkoutRepository
                    .existsByStudentIdAndWeekDayAndStatus(
                            studentId,
                            weekDay,
                            WorkoutStatus.ACTIVE
                    );
        } else {
            hasActiveWorkoutForWeekDay = studentWorkoutRepository
                    .existsByStudentIdAndWeekDayAndStatusAndIdNot(
                            studentId,
                            weekDay,
                            WorkoutStatus.ACTIVE,
                            studentWorkoutIdToIgnore
                    );
        }

        if (hasActiveWorkoutForWeekDay) {
            throw new DuplicateResourceException(
                    "Student already has an active workout for this week day"
            );
        }
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