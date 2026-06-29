ALTER TABLE workout_exercises
DROP CONSTRAINT IF EXISTS chk_workout_exercises_reps;

ALTER TABLE workout_exercises
ALTER COLUMN reps TYPE VARCHAR(50)
USING reps::VARCHAR;