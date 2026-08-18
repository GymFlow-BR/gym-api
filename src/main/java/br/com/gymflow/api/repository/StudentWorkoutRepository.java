package br.com.gymflow.api.repository;

import br.com.gymflow.api.domain.StudentWorkout;
import br.com.gymflow.api.domain.enums.WeekDay;
import br.com.gymflow.api.domain.enums.WorkoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentWorkoutRepository extends JpaRepository<StudentWorkout, Long> {

    List<StudentWorkout> findAllByStudentId(Long studentId);

    Optional<StudentWorkout> findFirstByStudentIdAndStatusOrderByAssignedAtDesc(
            Long studentId,
            WorkoutStatus status
    );

    Optional<StudentWorkout> findFirstByStudentIdAndStatusAndWeekDay(
            Long studentId,
            WorkoutStatus status,
            WeekDay weekDay
    );


    List<StudentWorkout> findAllByStudentIdAndStatus(Long studentId, WorkoutStatus status);

    boolean existsByStudentIdAndWeekDayAndStatus(
            Long studentId,
            WeekDay weekDay,
            WorkoutStatus status
    );

    boolean existsByStudentIdAndWeekDayAndStatusAndIdNot(
            Long studentId,
            WeekDay weekDay,
            WorkoutStatus status,
            Long studentWorkoutId
    );
}