CREATE TABLE student_workout_exercise_progress (

    progress_id BIGSERIAL PRIMARY KEY,
    student_workout_id  BIGINT NOT NULL,
    workout_exercise_id BIGINT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_progress_student_workout
        FOREIGN KEY (student_workout_id)
        REFERENCES student_workouts (student_workout_id),

    CONSTRAINT fk_progress_workout_exercise
        FOREIGN KEY (workout_exercise_id)
        REFERENCES workout_exercises (workout_exercise_id),

    CONSTRAINT uk_progress_student_workout_exercise
        UNIQUE (student_workout_id, workout_exercise_id)
);