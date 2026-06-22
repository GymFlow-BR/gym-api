-- ============================================================
-- GymFlow - Development Seed Data
-- ============================================================
-- Script para inserir dados básicos de desenvolvimento.
--
-- Uso recomendado:
-- - Ambiente local
-- - Testes via Postman
-- - Testes via Swagger
-- - Validação inicial do frontend
--
-- Não usar em produção.
-- ============================================================


-- ============================================================
-- Organization
-- ============================================================

INSERT INTO organizations (
    organization_name,
    organization_type,
    organization_email,
    organization_phone,
    active
)
SELECT
    'GymFlow Academy Dev',
    'ACADEMY',
    'dev@gymflow.com',
    '24999999999',
    true
WHERE NOT EXISTS (
    SELECT 1
    FROM organizations
    WHERE organization_email = 'dev@gymflow.com'
);


-- ============================================================
-- Users
-- ============================================================

INSERT INTO users (
    organization_id,
    user_name,
    user_email,
    password_hash,
    role,
    active
)
SELECT
    o.organization_id,
    'Professor Dev',
    'teacher.dev@gymflow.com',
    'dev-password',
    'TEACHER',
    true
FROM organizations o
WHERE o.organization_email = 'dev@gymflow.com'
AND NOT EXISTS (
    SELECT 1
    FROM users
    WHERE user_email = 'teacher.dev@gymflow.com'
);

INSERT INTO users (
    organization_id,
    user_name,
    user_email,
    password_hash,
    role,
    active
)
SELECT
    o.organization_id,
    'Aluno Dev',
    'student.dev@gymflow.com',
    'dev-password',
    'STUDENT',
    true
FROM organizations o
WHERE o.organization_email = 'dev@gymflow.com'
AND NOT EXISTS (
    SELECT 1
    FROM users
    WHERE user_email = 'student.dev@gymflow.com'
);


-- ============================================================
-- Student Profile
-- ============================================================

INSERT INTO student_profiles (
    user_id,
    birth_date,
    student_phone,
    goal,
    notes
)
SELECT
    u.user_id,
    DATE '2000-01-01',
    '24988888888',
    'Hypertrophy',
    'Perfil de aluno criado para testes locais.'
FROM users u
WHERE u.user_email = 'student.dev@gymflow.com'
AND NOT EXISTS (
    SELECT 1
    FROM student_profiles sp
    WHERE sp.user_id = u.user_id
);


-- ============================================================
-- Exercises
-- ============================================================

INSERT INTO exercises (
    organization_id,
    exercise_name,
    muscle_group,
    description,
    equipment_name,
    image_url,
    video_url,
    active
)
SELECT
    o.organization_id,
    'Supino reto',
    'Peito',
    'Exercício para fortalecimento do peitoral.',
    'Barra',
    'https://example.com/supino-reto.png',
    'https://example.com/supino-reto.mp4',
    true
FROM organizations o
WHERE o.organization_email = 'dev@gymflow.com'
AND NOT EXISTS (
    SELECT 1
    FROM exercises e
    WHERE e.organization_id = o.organization_id
    AND e.exercise_name = 'Supino reto'
);

INSERT INTO exercises (
    organization_id,
    exercise_name,
    muscle_group,
    description,
    equipment_name,
    image_url,
    video_url,
    active
)
SELECT
    o.organization_id,
    'Agachamento livre',
    'Pernas',
    'Exercício composto para membros inferiores.',
    'Barra',
    'https://example.com/agachamento-livre.png',
    'https://example.com/agachamento-livre.mp4',
    true
FROM organizations o
WHERE o.organization_email = 'dev@gymflow.com'
AND NOT EXISTS (
    SELECT 1
    FROM exercises e
    WHERE e.organization_id = o.organization_id
    AND e.exercise_name = 'Agachamento livre'
);

INSERT INTO exercises (
    organization_id,
    exercise_name,
    muscle_group,
    description,
    equipment_name,
    image_url,
    video_url,
    active
)
SELECT
    o.organization_id,
    'Remada curvada',
    'Costas',
    'Exercício para fortalecimento das costas.',
    'Barra',
    'https://example.com/remada-curvada.png',
    'https://example.com/remada-curvada.mp4',
    true
FROM organizations o
WHERE o.organization_email = 'dev@gymflow.com'
AND NOT EXISTS (
    SELECT 1
    FROM exercises e
    WHERE e.organization_id = o.organization_id
    AND e.exercise_name = 'Remada curvada'
);


-- ============================================================
-- Workout
-- ============================================================

INSERT INTO workouts (
    teacher_id,
    workout_name,
    status
)
SELECT
    u.user_id,
    'Treino A - Dev',
    'ACTIVE'
FROM users u
WHERE u.user_email = 'teacher.dev@gymflow.com'
AND NOT EXISTS (
    SELECT 1
    FROM workouts w
    WHERE w.teacher_id = u.user_id
    AND w.workout_name = 'Treino A - Dev'
);


-- ============================================================
-- Workout Exercises
-- ============================================================

INSERT INTO workout_exercises (
    workout_id,
    exercise_id,
    exercise_order,
    sets,
    reps,
    recommended_load,
    rest_time_seconds,
    notes
)
SELECT
    w.workout_id,
    e.exercise_id,
    1,
    4,
    10,
    40.00,
    60,
    'Manter controle do movimento.'
FROM workouts w
JOIN users teacher ON teacher.user_id = w.teacher_id
JOIN exercises e ON e.organization_id = teacher.organization_id
WHERE w.workout_name = 'Treino A - Dev'
AND e.exercise_name = 'Supino reto'
AND NOT EXISTS (
    SELECT 1
    FROM workout_exercises we
    WHERE we.workout_id = w.workout_id
    AND we.exercise_id = e.exercise_id
);

INSERT INTO workout_exercises (
    workout_id,
    exercise_id,
    exercise_order,
    sets,
    reps,
    recommended_load,
    rest_time_seconds,
    notes
)
SELECT
    w.workout_id,
    e.exercise_id,
    2,
    4,
    12,
    60.00,
    90,
    'Atenção à postura durante a execução.'
FROM workouts w
JOIN users teacher ON teacher.user_id = w.teacher_id
JOIN exercises e ON e.organization_id = teacher.organization_id
WHERE w.workout_name = 'Treino A - Dev'
AND e.exercise_name = 'Agachamento livre'
AND NOT EXISTS (
    SELECT 1
    FROM workout_exercises we
    WHERE we.workout_id = w.workout_id
    AND we.exercise_id = e.exercise_id
);

INSERT INTO workout_exercises (
    workout_id,
    exercise_id,
    exercise_order,
    sets,
    reps,
    recommended_load,
    rest_time_seconds,
    notes
)
SELECT
    w.workout_id,
    e.exercise_id,
    3,
    3,
    12,
    35.00,
    60,
    'Evitar impulso excessivo.'
FROM workouts w
JOIN users teacher ON teacher.user_id = w.teacher_id
JOIN exercises e ON e.organization_id = teacher.organization_id
WHERE w.workout_name = 'Treino A - Dev'
AND e.exercise_name = 'Remada curvada'
AND NOT EXISTS (
    SELECT 1
    FROM workout_exercises we
    WHERE we.workout_id = w.workout_id
    AND we.exercise_id = e.exercise_id
);


-- ============================================================
-- Student Workout
-- ============================================================

INSERT INTO student_workouts (
    student_id,
    workout_id,
    assigned_at,
    status
)
SELECT
    student.user_id,
    w.workout_id,
    CURRENT_TIMESTAMP,
    'ACTIVE'
FROM users student
JOIN workouts w ON w.workout_name = 'Treino A - Dev'
JOIN users teacher ON teacher.user_id = w.teacher_id
WHERE student.user_email = 'student.dev@gymflow.com'
AND student.organization_id = teacher.organization_id
AND NOT EXISTS (
    SELECT 1
    FROM student_workouts sw
    WHERE sw.student_id = student.user_id
    AND sw.workout_id = w.workout_id
);


-- ============================================================
-- Validation queries
-- ============================================================

SELECT 'Organization created' AS result, organization_id, organization_name
FROM organizations
WHERE organization_email = 'dev@gymflow.com';

SELECT 'Teacher created' AS result, user_id, user_name, role
FROM users
WHERE user_email = 'teacher.dev@gymflow.com';

SELECT 'Student created' AS result, user_id, user_name, role
FROM users
WHERE user_email = 'student.dev@gymflow.com';

SELECT 'Student profile created' AS result, profile_id, user_id, goal
FROM student_profiles
WHERE user_id = (
    SELECT user_id
    FROM users
    WHERE user_email = 'student.dev@gymflow.com'
);

SELECT 'Exercises created' AS result, exercise_id, exercise_name, active
FROM exercises
WHERE organization_id = (
    SELECT organization_id
    FROM organizations
    WHERE organization_email = 'dev@gymflow.com'
);

SELECT 'Workout created' AS result, workout_id, workout_name, status
FROM workouts
WHERE workout_name = 'Treino A - Dev';

SELECT 'Workout exercises created' AS result, workout_exercise_id, workout_id, exercise_id, exercise_order
FROM workout_exercises
WHERE workout_id = (
    SELECT workout_id
    FROM workouts
    WHERE workout_name = 'Treino A - Dev'
);

SELECT 'Student workout created' AS result, student_workout_id, student_id, workout_id, status
FROM student_workouts
WHERE student_id = (
    SELECT user_id
    FROM users
    WHERE user_email = 'student.dev@gymflow.com'
);