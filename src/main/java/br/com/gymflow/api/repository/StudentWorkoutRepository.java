package br.com.gymflow.api.repository;

import br.com.gymflow.api.domain.StudentWorkout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentWorkoutRepository extends JpaRepository<StudentWorkout, Long> {
    List<StudentWorkout> findAllByStudentId(Long studentId);
}
