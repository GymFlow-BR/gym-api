# GymFlow API

API backend do GymFlow, MVP de uma plataforma para academias, professores/personais e alunos.

O objetivo do MVP é permitir que professores e administradores cadastrem exercícios, criem treinos modelo, associem exercícios aos treinos e atribuam treinos a alunos. O aluno deve conseguir visualizar seu treino atual e os detalhes dos exercícios.

## Tecnologias

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Flyway
- Docker Compose
- Bean Validation
- JPA/Hibernate
- Swagger/OpenAPI
- JUnit 5
- Mockito

## Pré-requisitos

Antes de rodar o projeto localmente, é necessário ter instalado:

- Java 21
- Maven
- Docker
- Docker Compose

## Como rodar localmente

### 1. Subir o PostgreSQL

Na raiz do projeto, execute:

```bash
docker compose up -d
```

Para verificar se o container está rodando:

```bash
docker compose ps
```

Para visualizar logs do banco:

```bash
docker compose logs
```

Configurações locais do banco:

```text
Database: gymflow_db
User: gymflow
Password: gymflow
Port: 5432
```

### 2. Validar o build

```bash
mvn clean install
```

### 3. Rodar a aplicação

```bash
mvn spring-boot:run
```

Se tudo estiver correto, a aplicação será iniciada em:

```text
http://localhost:8080
```

## Variáveis de ambiente

O projeto utiliza variáveis de ambiente para configurar banco de dados, porta da aplicação e JWT.

Existe um arquivo de exemplo na raiz do projeto:

`.env.example`

Para ambiente local, copie o arquivo `.env.example` para `.env`.

Comando:

`cp .env.example .env`

Variáveis disponíveis:

* `SERVER_PORT`: porta onde a aplicação será executada. Valor local sugerido: `8080`.
* `SPRING_DATASOURCE_URL`: URL de conexão com o PostgreSQL. Valor local sugerido: `jdbc:postgresql://localhost:5432/gymflow_db`.
* `SPRING_DATASOURCE_USERNAME`: usuário do banco de dados. Valor local sugerido: `gymflow`.
* `SPRING_DATASOURCE_PASSWORD`: senha do banco de dados. Valor local sugerido: `gymflow`.
* `JWT_SECRET`: chave usada para assinar tokens JWT. Valor local sugerido: `change-this-secret-in-production`.
* `JWT_EXPIRATION_HOURS`: tempo de expiração do token em horas. Valor local sugerido: `2`.
* `FRONTEND_URL`: origem local do frontend permitida pelo CORS. Valor local sugerido: http://localhost:5173.

Atenção: o valor de `JWT_SECRET` usado em produção deve ser forte, privado e diferente do valor de desenvolvimento.

Não versionar arquivos `.env` com credenciais reais. Apenas o `.env.example` deve ser mantido no repositório.

## Swagger/OpenAPI

A API possui documentação interativa com Swagger/OpenAPI.

Após subir a aplicação, acesse:

```text
http://localhost:8080/swagger-ui/index.html
```

A especificação OpenAPI em JSON fica disponível em:

```text
http://localhost:8080/v3/api-docs
```

O Swagger pode ser usado para visualizar endpoints, contratos de request/response e testar chamadas da API durante o desenvolvimento.

## Seed de desenvolvimento

O projeto possui um script de dados iniciais para facilitar testes locais.

Arquivo:

```text
docs/dev/dev-seed-data.sql
```

Esse script cria dados básicos para desenvolvimento:

- Organização de desenvolvimento
* Usuário administrador com role `ADMIN`
* Professor com role `TEACHER`
* Aluno com role `STUDENT`
- Perfil do aluno
- Exercícios ativos
- Workout modelo ativo
- Exercícios vinculados ao workout
- Workout ativo atribuído ao aluno

Com o PostgreSQL rodando via Docker, execute:

```bash
docker exec -i gymflow-postgres psql -U gymflow -d gymflow_db < docs/dev/dev-seed-data.sql
```

O seed é destinado apenas ao ambiente local de desenvolvimento.

## Testes

Para rodar todos os testes automatizados:

```bash
mvn clean test
```

Para validar o build completo:

```bash
mvn clean install
```

O backend possui testes unitários para os principais services do MVP:

- `OrganizationService`
- `ExerciseService`
- `WorkoutService`
- `WorkoutExerciseService`
- `StudentWorkoutService`

## Fluxos principais do MVP

O backend cobre os principais fluxos da primeira versão do GymFlow:

- Gerenciamento de organizações
- Cadastro e gerenciamento de exercícios
- Criação e gerenciamento de treinos modelo
- Associação de exercícios aos treinos
- Atribuição de treinos a alunos
- Visualização do treino atual do aluno
- Validações de regra de negócio
- Tratamento padronizado de erros

## Endpoints principais

Os endpoints podem ser consultados de forma completa pelo Swagger.

Principais grupos da API:

```text
/api/organizations
/api/exercises
/api/workouts
/api/workouts/{workoutId}/exercises
/api/students/{studentId}/workouts
```

> Os caminhos exatos e contratos atualizados devem ser conferidos no Swagger.

## Flyway

O projeto utiliza Flyway para controle de migrations do banco de dados.

As migrations devem ser criadas em:

```text
src/main/resources/db/migration
```

O padrão de nome deve seguir:

```text
V1__descricao_da_migration.sql
```

Exemplo:

```text
V2__add_active_to_exercises.sql
```

## Como parar a aplicação

Se a aplicação estiver rodando pelo terminal, pressione:

```text
Ctrl + C
```

## Como parar o banco local

Para parar o PostgreSQL sem apagar os dados:

```bash
docker compose down
```

Para parar o PostgreSQL e remover o volume local:

```bash
docker compose down -v
```

> Atenção: o comando com `-v` apaga os dados locais do banco.

## Estrutura básica do projeto

```text
src/main/java/br/com/gymflow/api
├── config
├── controller
├── domain
├── dto
├── exception
├── mapper
├── repository
└── service
```

```text
src/main/resources/db/migration
```

```text
docs/dev
```

## Observações de desenvolvimento

- Dados fake de desenvolvimento devem ficar em `docs/dev`, não em migrations Flyway.
- Migrations devem ser usadas para alterações estruturais do banco.
- O Swagger deve ser usado como referência principal dos contratos da API.
- Antes de abrir um Pull Request, rode `mvn clean test` e `mvn clean install`.

## Status atual

Nesta etapa, o projeto possui:

- PostgreSQL configurado via Docker Compose
- Datasource configurado para ambiente local
- Flyway configurado
- Swagger/OpenAPI disponível
- Seed de desenvolvimento disponível
- Testes unitários dos principais services
- Aplicação subindo localmente com `mvn spring-boot:run`