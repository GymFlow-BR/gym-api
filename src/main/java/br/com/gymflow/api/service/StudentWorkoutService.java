package br.com.gymflow.api.service;

import br.com.gymflow.api.domain.StudentWorkout;
import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.domain.Workout;
import br.com.gymflow.api.domain.WorkoutExercise;
import br.com.gymflow.api.domain.enums.WorkoutStatus;
import br.com.gymflow.api.dto.studentWorkouts.CreateStudentWorkoutRequest;
import br.com.gymflow.api.dto.studentWorkouts.PatchStudentWorkoutRequest;
import br.com.gymflow.api.dto.studentWorkouts.StudentCurrentWorkoutResponse;
import br.com.gymflow.api.dto.studentWorkouts.StudentWorkoutResponse;
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

    @Transactional
    public StudentWorkoutResponse create(Long studentId, CreateStudentWorkoutRequest request) {
        User student = getStudentById(studentId);
        Workout workout = getWorkoutById(request.workoutId());

        validateStudentBelongsToWorkoutOrganization(student, workout);
        validateStudentWorkoutDoesNotAlreadyExist(studentId, request.workoutId());

        StudentWorkout studentWorkout = studentWorkoutMapper.toEntity(request);
        studentWorkout.setStudent(student);
        studentWorkout.setWorkout(workout);
        studentWorkout.setStatus(WorkoutStatus.ACTIVE);
        studentWorkout.setAssignedAt(LocalDateTime.now());

        StudentWorkout savedStudentWorkout = studentWorkoutRepository.save(studentWorkout);

        return studentWorkoutMapper.toResponse(savedStudentWorkout);
    }


    @Transactional(readOnly = true)
    public List<StudentWorkoutResponse> findAllByStudentId(Long studentId) {
        getStudentById(studentId);

        return studentWorkoutRepository.findAllByStudentId(studentId)
                .stream()
                .map(studentWorkoutMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentWorkoutResponse findById(Long studentId,Long studentWorkoutId) {
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
        StudentWorkout studentWorkout = getStudentWorkoutById(studentWorkoutId);

        validateStudentWorkoutBelongsToStudent(studentWorkout, studentId);

        if (request.status() != null) {
            studentWorkout.setStatus(request.status());
        }

        StudentWorkout updatedStudentWorkout = studentWorkoutRepository.save(studentWorkout);

        return studentWorkoutMapper.toResponse(updatedStudentWorkout);
    }


    @Transactional
    public void delete(Long studentId, Long studentWorkoutId) {
        StudentWorkout studentWorkout = getStudentWorkoutById(studentWorkoutId);

        validateStudentWorkoutBelongsToStudent(studentWorkout, studentId);

        studentWorkout.setStatus(WorkoutStatus.INACTIVE);

        studentWorkoutRepository.save(studentWorkout);
    }


    @Transactional(readOnly = true)
    public StudentCurrentWorkoutResponse findCurrentWorkout(Long studentId) {
        getStudentById(studentId);

        StudentWorkout studentWorkout = studentWorkoutRepository
                .findFirstByStudentIdAndStatusOrderByAssignedAtDesc(studentId, WorkoutStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active workout not found student id: " + studentId
                ));

        List<WorkoutExercise> workoutExercises = workoutExerciseRepository
                .findAllByWorkoutIdOrderByExerciseOrderAsc(studentWorkout.getWorkout().getId());

        return studentWorkoutMapper.toCurrentWorkoutResponse(studentWorkout, workoutExercises);
    }


    private StudentWorkout getStudentWorkoutById(Long studentWorkoutId) {
        return studentWorkoutRepository.findById(studentWorkoutId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student workout not found with id: "
                                + studentWorkoutId
                ));
    }


    private User getStudentById(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + studentId
                ));
        if (!student.getRole().name().equals("STUDENT")) {
            throw new IllegalArgumentException("User is not a student with id: " + studentId);
        }
        return student;
    }


    private Workout getWorkoutById(Long workoutId) {
        return workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workout not found with id: " + workoutId
                ));
    }


    private void validateStudentWorkoutBelongsToStudent(StudentWorkout studentWorkout, Long studentId) {
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
            throw new IllegalArgumentException(
                    "Student does not belong to the same organization as the workout"
            );
        }
    }


    private void validateStudentWorkoutDoesNotAlreadyExist(Long studentId, Long workoutId) {
        boolean alreadyExists = studentWorkoutRepository.existsByStudentIdAndWorkoutId(studentId, workoutId);

        if (alreadyExists) {
            throw new DuplicateResourceException(
                    "Student already has this workout assigned"
            );
        }
    }
}