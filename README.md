# GymFlow API

Backend do **GymFlow**, MVP de uma plataforma para academias, personal trainers, professores e alunos.

O objetivo do MVP é permitir que uma organização fitness gerencie alunos, exercícios, treinos modelo e atribuições semanais de treino. O aluno acessa sua área para visualizar os treinos atribuídos, consultar detalhes dos exercícios e registrar o progresso do treino.

---

## Status do projeto

MVP funcional validado.

Fluxos principais implementados:

- Cadastro de organização.
- Criação automática do primeiro administrador.
- Login com sessão via cookie `HttpOnly`.
- Logout.
- Consulta do usuário autenticado.
- Alteração de senha.
- Cadastro e gerenciamento de professores.
- Cadastro e gerenciamento de alunos.
- Cadastro e gerenciamento de exercícios.
- Upload de imagem e vídeo de exercício.
- Criação e gerenciamento de treinos modelo.
- Associação de exercícios aos treinos.
- Atribuição de treinos a alunos por dia da semana.
- Visualização da rotina semanal do aluno.
- Visualização do treino do dia.
- Visualização de detalhes de treino específico.
- Registro de progresso do aluno nos exercícios.
- Controle de acesso por perfil e organização.

---

## Tecnologias

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL
- Flyway
- Maven
- Docker Compose
- Bean Validation
- JPA/Hibernate
- Swagger/OpenAPI
- JUnit 5
- Mockito

---

## Perfis de acesso

### ADMIN

Administrador de uma organização.

Pode gerenciar os principais recursos da própria organização, incluindo professores, alunos, exercícios e treinos.

### TEACHER

Professor ou personal trainer vinculado a uma organização.

Pode gerenciar alunos, exercícios, treinos e atribuições de treino dentro da própria organização.

### STUDENT

Aluno vinculado a uma organização.

Pode visualizar os próprios treinos e registrar o próprio progresso.

---

## Modelo multi-organização

O GymFlow utiliza o modelo `Organization` para representar academias e profissionais autônomos.

Tipos iniciais suportados:

```txt
ACADEMY
PERSONAL
```

Todos os usuários pertencem a uma organização.

No MVP, o perfil `ADMIN` representa um administrador da própria organização, não um administrador global da plataforma.

Regras gerais:

- `ADMIN` acessa recursos da própria organização.
- `TEACHER` acessa recursos da própria organização.
- `STUDENT` acessa apenas os próprios dados.
- Recursos de uma organização não devem ser acessados por outra organização.

---

## Pré-requisitos

Antes de rodar o projeto localmente, instale:

- Java 21
- Maven
- Docker
- Docker Compose

---

## Como rodar localmente

### 1. Subir o PostgreSQL

Na raiz do projeto:

```bash
docker compose up -d
```

Verificar containers:

```bash
docker compose ps
```

Ver logs do banco:

```bash
docker compose logs postgres
```

Configuração local padrão:

```txt
Database: gymflow_db
User: gymflow
Password: gymflow
Port: 5432
```

Essas credenciais são apenas para desenvolvimento local.

---

### 2. Configurar variáveis de ambiente

O projeto usa variáveis de ambiente para banco, autenticação, CORS, cookie e upload.

Crie um arquivo `.env` a partir do exemplo:

```bash
cp .env.example .env
```

Variáveis principais:

```txt
SERVER_PORT=8080

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gymflow_db
SPRING_DATASOURCE_USERNAME=gymflow
SPRING_DATASOURCE_PASSWORD=gymflow

JWT_SECRET=change-this-secret-in-production
JWT_EXPIRATION_HOURS=2

FRONTEND_URL=http://localhost:5173

AUTH_COOKIE_NAME=gymflow_session
AUTH_COOKIE_SECURE=false
AUTH_COOKIE_SAME_SITE=Lax
AUTH_COOKIE_MAX_AGE_SECONDS=7200

CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret

GYMFLOW_UPLOAD_IMAGE_MAX_SIZE=5242880
GYMFLOW_UPLOAD_VIDEO_MAX_SIZE=52428800
```

Atenção:

- Não versionar `.env`.
- Não usar segredos reais no repositório.
- Em produção, `JWT_SECRET` deve ser forte e privado.
- Em produção com HTTPS, `AUTH_COOKIE_SECURE` deve ser `true`.

---

### 3. Rodar migrations

As migrations são executadas automaticamente pelo Flyway ao subir a aplicação.

Local das migrations:

```txt
src/main/resources/db/migration
```

Padrão de nome:

```txt
V1__descricao_da_migration.sql
```

---

### 4. Rodar a aplicação

```bash
mvn spring-boot:run
```

A aplicação ficará disponível em:

```txt
http://localhost:8080
```

---

## Seed de desenvolvimento

O projeto possui um seed para facilitar testes locais.

Arquivo:

```txt
docs/dev/dev-seed-data.sql
```

Executar com o PostgreSQL rodando via Docker:

```bash
docker exec -i gymflow-postgres psql -U gymflow -d gymflow_db < docs/dev/dev-seed-data.sql
```

Credenciais de desenvolvimento:

```txt
admin.dev@gymflow.com / 123456
teacher.dev@gymflow.com / 123456
student.dev@gymflow.com / 123456
```

O seed cria:

- Organização de desenvolvimento.
- Usuário administrador.
- Professor.
- Aluno.
- Perfil do aluno.
- Exercícios de exemplo.
- Treino modelo ativo.
- Exercícios vinculados ao treino.
- Treino atribuído ao aluno.

O seed é destinado apenas ao ambiente local de desenvolvimento.

---

## Autenticação

A autenticação do GymFlow usa JWT armazenado em cookie `HttpOnly`.

Fluxo principal:

1. O frontend envia `POST /api/auth/login`.
2. O backend valida as credenciais.
3. O backend gera um JWT.
4. O backend envia o JWT em cookie `HttpOnly`.
5. O frontend não armazena token em `localStorage`.
6. As próximas requisições autenticadas usam o cookie automaticamente.
7. O endpoint `/api/auth/me` retorna o usuário autenticado.
8. O endpoint `/api/auth/logout` limpa o cookie.

Endpoints principais:

```txt
POST  /api/auth/login
GET   /api/auth/me
POST  /api/auth/logout
PATCH /api/auth/change-password
```

---

## Swagger/OpenAPI

Com a aplicação rodando, acesse:

```txt
http://localhost:8080/swagger-ui/index.html
```

Especificação OpenAPI:

```txt
http://localhost:8080/v3/api-docs
```

Use o Swagger como referência técnica dos contratos atualizados da API.

---

## Testes

Rodar todos os testes:

```bash
mvn clean test
```

Validar build completo:

```bash
mvn clean install
```

Antes de abrir Pull Request, execute pelo menos:

```bash
mvn clean test
```

---

## Principais grupos de endpoints

```txt
/api/auth
/api/organizations
/api/users
/api/users/by-organization/{organizationId}/by-role
/api/exercises
/api/exercises/{exerciseId}/image
/api/exercises/{exerciseId}/video
/api/workouts
/api/workouts/{workoutId}/exercises
/api/students/{studentId}/workouts
/api/students/{studentId}/workouts/current
/api/students/{studentId}/workouts/current/progress
/api/students/{studentId}/workouts/{studentWorkoutId}/details
/api/students/{studentId}/workouts/{studentWorkoutId}/progress
```

---

## Fluxos principais do MVP

### Organização

- Criar organização.
- Criar primeiro administrador automaticamente.

### Usuários

- Listar usuários por organização e perfil.
- Criar alunos.
- Criar professores.
- Editar usuários.
- Inativar usuários.

### Exercícios

- Criar exercício.
- Listar exercícios.
- Editar exercício.
- Inativar exercício.
- Reativar exercício.
- Fazer upload de imagem.
- Fazer upload de vídeo.

### Treinos

- Criar treino modelo.
- Listar treinos.
- Editar treino.
- Inativar treino.
- Adicionar exercícios ao treino.
- Remover exercícios do treino.

### Atribuição semanal

- Atribuir treino a aluno por dia da semana.
- Manter apenas um treino ativo por aluno e dia da semana.
- Permitir o mesmo treino modelo em dias diferentes.
- Listar rotina semanal do aluno.
- Inativar atribuições.

### Área do aluno

- Visualizar treino do dia.
- Visualizar rotina semanal.
- Visualizar detalhes de treino específico.
- Marcar exercício como concluído.
- Desmarcar exercício concluído.
- Consultar progresso do treino.

---

## Tratamento de erros

A API utiliza respostas padronizadas para erros.

Exemplos de status:

```txt
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
500 Internal Server Error
```

Responses de erro não devem expor detalhes internos sensíveis.

---

## Estrutura básica

```txt
src/main/java/br/com/gymflow/api
├── auth
├── config
├── controller
├── domain
├── dto
├── exception
├── mapper
├── repository
├── security
└── service
```

```txt
src/main/resources
├── application.yml
└── db/migration
```

```txt
docs
├── architecture.md
├── api-endpoints.md
└── dev/dev-seed-data.sql
```

---

## Fora do escopo do MVP

- Deploy em produção.
- Pagamentos.
- Marketplace de profissionais.
- Login social.
- Convite por e-mail.
- Reset de senha por e-mail.
- Notificações.
- Relatórios avançados.
- App mobile nativo.
- Integração com mensageria externa.
- Administração global da plataforma.

---

## Observações de desenvolvimento

- Dados fake de desenvolvimento devem ficar em `docs/dev`.
- Migrations Flyway devem ser usadas apenas para estrutura e alterações de schema.
- Seed local não deve ser usado em produção.
- Segredos reais devem ser configurados por variáveis de ambiente.
- O frontend deve enviar requisições com credenciais habilitadas para permitir uso do cookie HttpOnly.