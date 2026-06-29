package br.com.gymflow.api.event.listeners;

import br.com.gymflow.api.event.StudentWorkoutExerciseCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StudentWorkoutExerciseCompletedEventListener {

    @EventListener
    public void handle(StudentWorkoutExerciseCompletedEvent event) {
        log.info(
                "Student {} completed exercise {} from workout {} at {}",
                event.studentId(),
                event.exerciseId(),
                event.workoutId(),
                event.completedAt()
        );
    }
}