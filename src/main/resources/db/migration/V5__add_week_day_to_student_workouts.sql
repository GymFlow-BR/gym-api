ALTER TABLE student_workouts
    ADD COLUMN week_day VARCHAR(20);

UPDATE student_workouts
SET week_day = 'MONDAY'
WHERE week_day IS NULL;

ALTER TABLE student_workouts
    ALTER COLUMN week_day SET NOT NULL;

ALTER TABLE student_workouts
    ADD CONSTRAINT chk_student_workouts_week_day
        CHECK (
            week_day IN (
                         'MONDAY',
                         'TUESDAY',
                         'WEDNESDAY',
                         'THURSDAY',
                         'FRIDAY',
                         'SATURDAY',
                         'SUNDAY'
                )
            );

CREATE UNIQUE INDEX uk_student_workouts_active_day
    ON student_workouts (student_id, week_day)
    WHERE status = 'ACTIVE';