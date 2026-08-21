# GymFlow API - Endpoints principais do MVP

## Objetivo

Este documento descreve os principais endpoints da API do GymFlow consumidos pelo frontend do MVP.

A documentação tem como foco facilitar a integração entre frontend e backend, apresentando endpoints disponíveis, permissões necessárias e exemplos básicos de request e response.

Para contratos completos e atualizados, consulte também o Swagger/OpenAPI da aplicação.

---

## Base URL local

```txt
http://localhost:8080
```

---

## Autenticação

O GymFlow utiliza JWT armazenado em cookie `HttpOnly`.

Após o login, o backend envia o token em um cookie seguro para o navegador. O frontend não precisa armazenar token manualmente e não deve usar `localStorage` para autenticação.

Em chamadas autenticadas feitas pelo frontend, é necessário enviar credenciais habilitadas:

```ts
credentials: "include"
```

---

## Perfis de acesso

### ADMIN

Administrador de uma organização.

Pode gerenciar recursos da própria organização, incluindo professores, alunos, exercícios e treinos.

### TEACHER

Professor ou personal vinculado a uma organização.

Pode gerenciar alunos, exercícios, treinos e atribuições dentro da própria organização.

### STUDENT

Aluno vinculado a uma organização.

Pode visualizar os próprios treinos e registrar o próprio progresso.

---

## Regras gerais de acesso

- Recursos são restritos por organização.
- `ADMIN` e `TEACHER` acessam apenas dados da própria organização.
- `STUDENT` acessa apenas os próprios dados.
- Apenas `STUDENT` pode marcar ou desmarcar exercícios como concluídos.
- Responses nunca devem expor `passwordHash`.
- O cookie de autenticação deve ser tratado apenas pelo backend/browser.

---

## 1. Autenticação

### Login

Método: `POST`

Endpoint:

```txt
/api/auth/login
```

Permissão: `Público`

Descrição:

Autentica o usuário e cria uma sessão via cookie `HttpOnly`.

Request:

```json
{
  "email": "teacher.dev@gymflow.com",
  "password": "123456"
}
```

Response:

```json
{
  "userId": 2,
  "organizationId": 1,
  "name": "Professor Dev",
  "email": "teacher.dev@gymflow.com",
  "role": "TEACHER"
}
```

Observação:

O JWT não é retornado no body. Ele é enviado pelo backend no cookie `HttpOnly`.

---

### Usuário autenticado

Método: `GET`

Endpoint:

```txt
/api/auth/me
```

Permissão: `Autenticado`

Descrição:

Retorna os dados do usuário autenticado com base no cookie de sessão.

Response:

```json
{
  "userId": 2,
  "organizationId": 1,
  "name": "Professor Dev",
  "email": "teacher.dev@gymflow.com",
  "role": "TEACHER"
}
```

---

### Logout

Método: `POST`

Endpoint:

```txt
/api/auth/logout
```

Permissão: `Autenticado`

Descrição:

Encerra a sessão atual e limpa o cookie de autenticação.

Response:

```txt
204 No Content
```

---

### Alterar senha

Método: `PATCH`

Endpoint:

```txt
/api/auth/change-password
```

Permissão: `Autenticado`

Descrição:

Altera a senha do usuário autenticado.

Request:

```json
{
  "currentPassword": "123456",
  "newPassword": "novaSenha123",
  "confirmNewPassword": "novaSenha123"
}
```

Response:

```txt
204 No Content
```

---

## 2. Organizações

### Criar organização

Método: `POST`

Endpoint:

```txt
/api/organizations
```

Permissão: `Público`

Descrição:

Cria uma nova organização e o primeiro usuário administrador.

Request:

```json
{
  "organizationName": "GymFlow Academy",
  "organizationType": "ACADEMY",
  "organizationEmail": "contato@gymflow.com",
  "organizationPhone": "21999999999",
  "adminName": "Administrador Dev",
  "adminEmail": "admin.dev@gymflow.com",
  "password": "123456"
}
```

Response:

```json
{
  "organizationId": 1,
  "organizationName": "GymFlow Academy",
  "organizationType": "ACADEMY",
  "organizationEmail": "contato@gymflow.com",
  "adminId": 1,
  "adminName": "Administrador Dev",
  "adminEmail": "admin.dev@gymflow.com"
}
```

---

## 3. Usuários

### Listar usuários por organização e perfil

Método: `GET`

Endpoint:

```txt
/api/users/by-organization/{organizationId}/by-role?role=STUDENT
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Lista usuários de uma organização filtrando pelo perfil.

Regras principais:

- `ADMIN` pode listar `TEACHER` e `STUDENT`.
- `TEACHER` pode listar apenas `STUDENT`.
- Usuários só podem listar dados da própria organização.

Response:

```json
[
  {
    "id": 3,
    "organizationId": 1,
    "organizationName": "GymFlow Academy Dev",
    "name": "Aluno Dev",
    "email": "student.dev@gymflow.com",
    "role": "STUDENT",
    "active": true,
    "createdAt": "2026-08-21T10:00:00"
  }
]
```

---

### Criar usuário

Método: `POST`

Endpoint:

```txt
/api/users
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Cria um novo usuário dentro da organização do usuário autenticado.

Regras principais:

- `ADMIN` pode criar professores e alunos.
- `TEACHER` pode criar alunos.
- `TEACHER` não pode criar professores nem admins.
- `STUDENT` não pode criar usuários.

Request:

```json
{
  "name": "Aluno Novo",
  "email": "aluno.novo@gymflow.com",
  "password": "123456",
  "role": "STUDENT"
}
```

Response:

```json
{
  "id": 3,
  "organizationId": 1,
  "organizationName": "GymFlow Academy Dev",
  "name": "Aluno Novo",
  "email": "aluno.novo@gymflow.com",
  "role": "STUDENT",
  "active": true,
  "createdAt": "2026-08-21T10:00:00"
}
```

---

### Atualizar usuário

Método: `PATCH`

Endpoint:

```txt
/api/users/{id}
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Atualiza parcialmente um usuário.

Regras principais:

- `ADMIN` pode editar usuários da própria organização.
- `TEACHER` pode editar dados básicos de alunos da própria organização.
- `TEACHER` não pode alterar status `active`.
- `TEACHER` não pode editar professores ou admins.

Request:

```json
{
  "name": "Aluno Atualizado",
  "email": "aluno.atualizado@gymflow.com",
  "active": true
}
```

Response:

```json
{
  "id": 3,
  "organizationId": 1,
  "organizationName": "GymFlow Academy Dev",
  "name": "Aluno Atualizado",
  "email": "aluno.atualizado@gymflow.com",
  "role": "STUDENT",
  "active": true,
  "createdAt": "2026-08-21T10:00:00"
}
```

---

### Inativar usuário

Método: `DELETE`

Endpoint:

```txt
/api/users/{id}
```

Permissão: `ADMIN`

Descrição:

Inativa um usuário da própria organização.

Response:

```txt
204 No Content
```

---

## 4. Exercícios

### Criar exercício

Método: `POST`

Endpoint:

```txt
/api/exercises
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Cria um exercício dentro da organização do usuário autenticado.

Request:

```json
{
  "exerciseName": "Supino reto",
  "muscleGroup": "Peito",
  "equipmentName": "Barra",
  "description": "Exercício para peitoral.",
  "imageUrl": "https://example.com/image.png",
  "videoUrl": "https://example.com/video.mp4"
}
```

Response:

```json
{
  "exerciseId": 1,
  "exerciseName": "Supino reto",
  "muscleGroup": "Peito",
  "equipmentName": "Barra",
  "description": "Exercício para peitoral.",
  "imageUrl": "https://example.com/image.png",
  "videoUrl": "https://example.com/video.mp4",
  "active": true,
  "createdAt": "2026-08-21T10:00:00",
  "updatedAt": "2026-08-21T10:00:00"
}
```

---

### Listar exercícios

Método: `GET`

Endpoint:

```txt
/api/exercises
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Lista exercícios ativos e inativos da organização do usuário autenticado.

---

### Buscar exercício por ID

Método: `GET`

Endpoint:

```txt
/api/exercises/{exerciseId}
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Busca um exercício específico da organização do usuário autenticado.

---

### Atualizar exercício

Método: `PATCH`

Endpoint:

```txt
/api/exercises/{exerciseId}
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Atualiza parcialmente um exercício.

---

### Inativar exercício

Método: `DELETE`

Endpoint:

```txt
/api/exercises/{exerciseId}
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Inativa um exercício.

---

### Upload de imagem do exercício

Método: `POST`

Endpoint:

```txt
/api/exercises/{exerciseId}/image
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Faz upload da imagem de demonstração do exercício.

Formato:

```txt
multipart/form-data
```

Campo:

```txt
file
```

---

### Upload de vídeo do exercício

Método: `POST`

Endpoint:

```txt
/api/exercises/{exerciseId}/video
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Faz upload do vídeo de demonstração do exercício.

Formato:

```txt
multipart/form-data
```

Campo:

```txt
file
```

---

## 5. Treinos

### Criar treino

Método: `POST`

Endpoint:

```txt
/api/workouts
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Cria um treino modelo reutilizável dentro da organização do usuário autenticado.

Request:

```json
{
  "workoutName": "Treino A"
}
```

Response:

```json
{
  "workoutId": 1,
  "teacherId": 2,
  "workoutName": "Treino A",
  "status": "ACTIVE",
  "createdAt": "2026-08-21T10:00:00",
  "updatedAt": "2026-08-21T10:00:00"
}
```

---

### Listar treinos

Método: `GET`

Endpoint:

```txt
/api/workouts
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Lista treinos ativos e inativos da organização do usuário autenticado.

---

### Buscar treino por ID

Método: `GET`

Endpoint:

```txt
/api/workouts/{workoutId}
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Busca um treino específico da organização do usuário autenticado.

---

### Atualizar treino

Método: `PATCH`

Endpoint:

```txt
/api/workouts/{workoutId}
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Atualiza parcialmente um treino.

---

### Inativar treino

Método: `DELETE`

Endpoint:

```txt
/api/workouts/{workoutId}
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Inativa um treino.

---

## 6. Exercícios do treino

### Adicionar exercício ao treino

Método: `POST`

Endpoint:

```txt
/api/workouts/{workoutId}/exercises
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Adiciona um exercício a um treino modelo.

Request:

```json
{
  "exerciseId": 1,
  "exerciseOrder": 1,
  "sets": 4,
  "reps": "8-12",
  "recommendedLoad": 40.0,
  "restTimeSeconds": 60,
  "notes": "Manter controle do movimento."
}
```

Response:

```json
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
  "recommendedLoad": 40.0,
  "restTimeSeconds": 60,
  "notes": "Manter controle do movimento.",
  "createdAt": "2026-08-21T10:00:00",
  "updatedAt": "2026-08-21T10:00:00"
}
```

---

### Listar exercícios do treino

Método: `GET`

Endpoint:

```txt
/api/workouts/{workoutId}/exercises
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Lista os exercícios vinculados a um treino, ordenados por `exerciseOrder`.

---

### Buscar exercício do treino por ID

Método: `GET`

Endpoint:

```txt
/api/workouts/{workoutId}/exercises/{workoutExerciseId}
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Busca um exercício específico dentro de um treino.

---

### Atualizar exercício do treino

Método: `PATCH`

Endpoint:

```txt
/api/workouts/{workoutId}/exercises/{workoutExerciseId}
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Atualiza parcialmente um exercício vinculado ao treino.

---

### Remover exercício do treino

Método: `DELETE`

Endpoint:

```txt
/api/workouts/{workoutId}/exercises/{workoutExerciseId}
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Remove um exercício do treino.

---

## 7. Treinos atribuídos ao aluno

### Atribuir treino ao aluno

Método: `POST`

Endpoint:

```txt
/api/students/{studentId}/workouts
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Atribui um treino modelo a um aluno em um dia específico da semana.

Regra importante:

- Um aluno pode ter vários treinos ativos na semana.
- Um aluno pode ter apenas um treino ativo por dia da semana.
- O mesmo treino modelo pode ser atribuído ao mesmo aluno em dias diferentes.

Request:

```json
{
  "workoutId": 1,
  "weekDay": "MONDAY"
}
```

Valores aceitos para `weekDay`:

```txt
MONDAY
TUESDAY
WEDNESDAY
THURSDAY
FRIDAY
SATURDAY
SUNDAY
```

Response:

```json
{
  "studentWorkoutId": 1,
  "studentId": 3,
  "studentName": "Aluno Dev",
  "workoutId": 1,
  "workoutName": "Treino A",
  "teacherName": "Professor Dev",
  "assignedAt": "2026-08-21T10:00:00",
  "weekDay": "MONDAY",
  "status": "ACTIVE",
  "createdAt": "2026-08-21T10:00:00",
  "updatedAt": "2026-08-21T10:00:00"
}
```

---

### Listar treinos do aluno

Método: `GET`

Endpoint:

```txt
/api/students/{studentId}/workouts
```

Permissões: `ADMIN`, `TEACHER`, `STUDENT`

Descrição:

Lista todos os treinos atribuídos ao aluno.

---

### Buscar treino atribuído por ID

Método: `GET`

Endpoint:

```txt
/api/students/{studentId}/workouts/{studentWorkoutId}
```

Permissões: `ADMIN`, `TEACHER`, `STUDENT`

Descrição:

Busca uma atribuição específica de treino do aluno.

---

### Buscar detalhes de treino específico

Método: `GET`

Endpoint:

```txt
/api/students/{studentId}/workouts/{studentWorkoutId}/details
```

Permissões: `ADMIN`, `TEACHER`, `STUDENT`

Descrição:

Retorna os detalhes completos de um treino específico atribuído ao aluno, incluindo exercícios.

---

### Atualizar treino atribuído

Método: `PATCH`

Endpoint:

```txt
/api/students/{studentId}/workouts/{studentWorkoutId}
```

Permissões: `ADMIN`, `TEACHER`

Descrição:

Atualiza o status de um treino atribuído ao aluno.

Request:

```json
{
  "status": "INACTIVE"
}
```

---

## 8. Treino atual do aluno

### Visualizar treino atual

Método: `GET`

Endpoint:

```txt
/api/students/{studentId}/workouts/current
```

Permissões: `ADMIN`, `TEACHER`, `STUDENT`

Descrição:

Retorna o treino ativo do aluno para o dia atual do servidor.

Response:

```json
{
  "studentId": 3,
  "studentWorkoutId": 1,
  "workoutId": 1,
  "workoutName": "Treino A",
  "teacherName": "Professor Dev",
  "assignedAt": "2026-08-21T10:00:00",
  "weekDay": "MONDAY",
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
      "recommendedLoad": 40.0,
      "restTimeSeconds": 60,
      "notes": "Manter controle do movimento.",
      "imageUrl": "https://example.com/image.png",
      "videoUrl": "https://example.com/video.mp4"
    }
  ]
}
```

---

## 9. Progresso do treino atual

### Visualizar progresso do treino atual

Método: `GET`

Endpoint:

```txt
/api/students/{studentId}/workouts/current/progress
```

Permissões: `ADMIN`, `TEACHER`, `STUDENT`

Descrição:

Retorna o progresso do aluno no treino atual do dia.

Response:

```json
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
      "completedAt": "2026-08-21T10:00:00"
    }
  ]
}
```

---

### Marcar exercício do treino atual como concluído

Método: `PATCH`

Endpoint:

```txt
/api/students/{studentId}/workouts/current/exercises/{workoutExerciseId}/complete
```

Permissão: `STUDENT`

Descrição:

Marca um exercício do treino atual como concluído.

---

### Desmarcar exercício do treino atual

Método: `PATCH`

Endpoint:

```txt
/api/students/{studentId}/workouts/current/exercises/{workoutExerciseId}/uncomplete
```

Permissão: `STUDENT`

Descrição:

Desmarca um exercício concluído do treino atual.

---

## 10. Progresso de treino específico

### Visualizar progresso de treino específico

Método: `GET`

Endpoint:

```txt
/api/students/{studentId}/workouts/{studentWorkoutId}/progress
```

Permissões: `ADMIN`, `TEACHER`, `STUDENT`

Descrição:

Retorna o progresso de um treino específico ativo atribuído ao aluno.

---

### Marcar exercício de treino específico como concluído

Método: `PATCH`

Endpoint:

```txt
/api/students/{studentId}/workouts/{studentWorkoutId}/exercises/{workoutExerciseId}/complete
```

Permissão: `STUDENT`

Descrição:

Marca como concluído um exercício de um treino específico ativo.

---

### Desmarcar exercício de treino específico

Método: `PATCH`

Endpoint:

```txt
/api/students/{studentId}/workouts/{studentWorkoutId}/exercises/{workoutExerciseId}/uncomplete
```

Permissão: `STUDENT`

Descrição:

Desmarca como concluído um exercício de um treino específico ativo.

---

## 11. Responses de erro

### 400 Bad Request

Quando a request possui dados inválidos.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/exercises"
}
```

---

### 401 Unauthorized

Quando o usuário não está autenticado ou a sessão expirou.

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication is required",
  "path": "/api/exercises"
}
```

---

### 403 Forbidden

Quando o usuário está autenticado, mas não tem permissão para acessar o recurso.

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/students/2/workouts/current"
}
```

---

### 404 Not Found

Quando o recurso solicitado não existe ou não pertence ao contexto permitido.

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found",
  "path": "/api/workouts/99"
}
```

---

### 409 Conflict

Quando ocorre conflito de regra de negócio.

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Email already in use",
  "path": "/api/users"
}
```

---

## Observações

- Este documento resume os principais endpoints usados pelo frontend.
- O Swagger/OpenAPI deve ser usado como fonte de referência para contratos completos.
- O frontend deve usar `credentials: "include"` nas requisições autenticadas.
- O backend é responsável por criar, validar e limpar o cookie de autenticação.