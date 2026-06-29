# GymFlow API - Endpoints principais do MVP

## Objetivo

Este documento descreve os principais endpoints da API do GymFlow que serão consumidos pelo frontend do MVP.

A documentação tem como foco facilitar a integração entre frontend e backend, apresentando os endpoints disponíveis, permissões necessárias e exemplos básicos de request e response.

## Convenções

Base URL local:

`http://localhost:8080`

Autenticação:

A maioria dos endpoints exige autenticação via JWT.

Enviar o token no header:

`Authorization: Bearer {token}`

## Perfis de acesso

### ADMIN

Usuário administrador de uma organização.

Pode gerenciar recursos da própria organização.

### TEACHER

Professor ou personal vinculado a uma organização.

Pode gerenciar exercícios, treinos e alunos da própria organização.

### STUDENT

Aluno vinculado a uma organização.

Pode visualizar o próprio treino atual e atualizar o próprio progresso.

## Regras gerais de acesso

* Recursos são restritos por organização.
* `ADMIN` e `TEACHER` acessam apenas dados da própria organização.
* `STUDENT` acessa apenas os próprios dados.
* Apenas `STUDENT` pode marcar ou desmarcar exercícios como concluídos.
* Responses nunca devem expor `passwordHash`.

---

## 1. Autenticação

### Login

Método: `POST`

Endpoint: `/api/auth/login`

Permissão: `Público`

Descrição:

Autentica o usuário e retorna um token JWT.

Request:

{
"email": "[teacher.dev@gymflow.com](mailto:teacher.dev@gymflow.com)",
"password": "123456"
}

Response:

{
"token": "jwt-token",
"userId": 1,
"organizationId": 1,
"name": "Professor Dev",
"email": "[teacher.dev@gymflow.com](mailto:teacher.dev@gymflow.com)",
"role": "TEACHER"
}

---

## 2. Exercícios

### Criar exercício

Método: `POST`

Endpoint: `/api/exercises`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Cria um exercício dentro da organização do usuário autenticado.

Request:

{
"exerciseName": "Supino reto",
"muscleGroup": "Peito",
"equipmentName": "Barra",
"description": "Exercício para peitoral.",
"imageUrl": "https://example.com/image.png",
"videoUrl": "https://example.com/video.mp4"
}

Response:

{
"exerciseId": 1,
"exerciseName": "Supino reto",
"muscleGroup": "Peito",
"equipmentName": "Barra",
"description": "Exercício para peitoral.",
"imageUrl": "https://example.com/image.png",
"videoUrl": "https://example.com/video.mp4",
"active": true,
"createdAt": "2026-06-28T20:30:00",
"updatedAt": "2026-06-28T20:30:00"
}

### Listar exercícios

Método: `GET`

Endpoint: `/api/exercises`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Lista exercícios ativos da organização do usuário autenticado.

### Buscar exercício por ID

Método: `GET`

Endpoint: `/api/exercises/{exerciseId}`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Busca um exercício específico da organização do usuário autenticado.

### Atualizar exercício

Método: `PATCH`

Endpoint: `/api/exercises/{exerciseId}`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Atualiza parcialmente um exercício.

### Remover exercício

Método: `DELETE`

Endpoint: `/api/exercises/{exerciseId}`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Inativa um exercício.

---

## 3. Treinos

### Criar treino

Método: `POST`

Endpoint: `/api/workouts`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Cria um treino modelo reutilizável.

Request:

{
"workoutName": "Treino A"
}

Response:

{
"workoutId": 1,
"teacherId": 2,
"workoutName": "Treino A",
"status": "ACTIVE",
"createdAt": "2026-06-28T20:30:00",
"updatedAt": "2026-06-28T20:30:00"
}

### Listar treinos

Método: `GET`

Endpoint: `/api/workouts`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Lista treinos ativos da organização do usuário autenticado.

### Buscar treino por ID

Método: `GET`

Endpoint: `/api/workouts/{workoutId}`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Busca um treino específico da organização do usuário autenticado.

### Atualizar treino

Método: `PATCH`

Endpoint: `/api/workouts/{workoutId}`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Atualiza parcialmente um treino.

### Remover treino

Método: `DELETE`

Endpoint: `/api/workouts/{workoutId}`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Inativa um treino.

---

## 4. Exercícios do treino

### Adicionar exercício ao treino

Método: `POST`

Endpoint: `/api/workouts/{workoutId}/exercises`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Adiciona um exercício a um treino modelo.

Request:

{
"exerciseId": 1,
"exerciseOrder": 1,
"sets": 4,
"reps": "8-12",
"recommendedLoad": 40.00,
"restTimeSeconds": 60,
"notes": "Manter controle do movimento."
}

Response:

{
"id": 1,
"workoutId": 1,
"exerciseId": 1,
"exerciseName": "Supino reto",
"muscleGroup": "Peito",
"equipmentName": "Barra",
"exerciseOrder": 1,
"sets": 4,
"reps": "8-12",
"recommendedLoad": 40.00,
"restTimeSeconds": 60,
"notes": "Manter controle do movimento.",
"createdAt": "2026-06-28T20:30:00",
"updatedAt": "2026-06-28T20:30:00"
}

### Listar exercícios do treino

Método: `GET`

Endpoint: `/api/workouts/{workoutId}/exercises`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Lista os exercícios vinculados a um treino, ordenados por `exerciseOrder`.

### Buscar exercício do treino por ID

Método: `GET`

Endpoint: `/api/workouts/{workoutId}/exercises/{workoutExerciseId}`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Busca um exercício específico dentro de um treino.

### Atualizar exercício do treino

Método: `PATCH`

Endpoint: `/api/workouts/{workoutId}/exercises/{workoutExerciseId}`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Atualiza parcialmente um exercício vinculado ao treino.

### Remover exercício do treino

Método: `DELETE`

Endpoint: `/api/workouts/{workoutId}/exercises/{workoutExerciseId}`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Remove um exercício do treino.

---

## 5. Treinos atribuídos ao aluno

### Atribuir treino ao aluno

Método: `POST`

Endpoint: `/api/students/{studentId}/workouts`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Atribui um treino modelo a um aluno.

Regra importante:

Ao atribuir um novo treino ativo, outros treinos ativos do aluno devem ser desativados.

Response:

{
"studentWorkoutId": 1,
"studentId": 3,
"studentName": "Aluno Teste",
"workoutId": 1,
"workoutName": "Treino A",
"assignedAt": "2026-06-28T20:30:00",
"status": "ACTIVE",
"createdAt": "2026-06-28T20:30:00",
"updatedAt": "2026-06-28T20:30:00"
}

### Listar treinos do aluno

Método: `GET`

Endpoint: `/api/students/{studentId}/workouts`

Permissões: `ADMIN`, `TEACHER`, `STUDENT`

Descrição:

Lista os treinos atribuídos ao aluno.

### Buscar treino atribuído por ID

Método: `GET`

Endpoint: `/api/students/{studentId}/workouts/{studentWorkoutId}`

Permissões: `ADMIN`, `TEACHER`, `STUDENT`

Descrição:

Busca uma atribuição específica de treino do aluno.

### Atualizar treino atribuído

Método: `PATCH`

Endpoint: `/api/students/{studentId}/workouts/{studentWorkoutId}`

Permissões: `ADMIN`, `TEACHER`

Descrição:

Atualiza o status de um treino atribuído ao aluno.

---

## 6. Treino atual do aluno

### Visualizar treino atual

Método: `GET`

Endpoint: `/api/students/{studentId}/workouts/current`

Permissões: `ADMIN`, `TEACHER`, `STUDENT`

Descrição:

Retorna o treino ativo atual do aluno com os exercícios ordenados.

Response:

{
"studentId": 3,
"studentWorkoutId": 1,
"workoutId": 1,
"workoutName": "Treino A",
"assignedAt": "2026-06-28T20:30:00",
"status": "ACTIVE",
"exercises": [
{
"workoutExerciseId": 1,
"exerciseId": 1,
"exerciseName": "Supino reto",
"equipmentName": "Barra",
"muscleGroup": "Peito",
"description": "Exercício para peitoral.",
"exerciseOrder": 1,
"sets": 4,
"reps": "8-12",
"recommendedLoad": 40.00,
"restTimeSeconds": 60,
"notes": "Manter controle do movimento.",
"imageUrl": "https://example.com/image.png",
"videoUrl": "https://example.com/video.mp4"
}
]
}

---

## 7. Progresso do treino atual

### Visualizar progresso do treino atual

Método: `GET`

Endpoint: `/api/students/{studentId}/workouts/current/progress`

Permissões: `ADMIN`, `TEACHER`, `STUDENT`

Descrição:

Retorna o progresso do aluno no treino atual.

Response:

{
"studentId": 3,
"studentWorkoutId": 1,
"workoutId": 1,
"workoutName": "Treino A",
"totalExercises": 3,
"completedExercises": 1,
"progressPercentage": 33,
"exercises": [
{
"workoutExerciseId": 1,
"exerciseId": 1,
"exerciseName": "Supino reto",
"exerciseOrder": 1,
"completed": true,
"completedAt": "2026-06-28T20:30:00"
}
]
}

### Marcar exercício como concluído

Método: `PATCH`

Endpoint: `/api/students/{studentId}/workouts/current/exercises/{workoutExerciseId}/complete`

Permissões: `STUDENT`

Descrição:

Marca um exercício do treino atual como concluído.

Response:

{
"studentWorkoutId": 1,
"workoutExerciseId": 1,
"completed": true,
"completedAt": "2026-06-28T20:30:00"
}

### Desmarcar exercício como concluído

Método: `PATCH`

Endpoint: `/api/students/{studentId}/workouts/current/exercises/{workoutExerciseId}/uncomplete`

Permissões: `STUDENT`

Descrição:

Desmarca um exercício concluído do treino atual.

Response:

{
"studentWorkoutId": 1,
"workoutExerciseId": 1,
"completed": false,
"completedAt": null
}

---

## 8. Responses de erro

### 401 Unauthorized

Quando o usuário não envia token ou envia token inválido.

Response:

{
"status": 401,
"error": "Unauthorized",
"message": "Authentication is required to access this resource",
"path": "/api/exercises"
}

### 403 Forbidden

Quando o usuário está autenticado, mas não tem permissão para acessar o recurso.

Response:

{
"status": 403,
"error": "Forbidden",
"message": "Access denied",
"path": "/api/students/2/workouts/current"
}

### 404 Not Found

Quando o recurso solicitado não existe ou não pertence ao contexto permitido.

Response:

{
"status": 404,
"error": "Not Found",
"message": "Resource not found",
"path": "/api/workouts/99"
}

### 400 Bad Request

Quando a request possui dados inválidos.

Response:

{
"status": 400,
"error": "Bad Request",
"message": "Validation failed",
"path": "/api/exercises"
}
