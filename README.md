# GymFlow API

API backend do GymFlow, MVP de uma plataforma para academias, professores/personais e alunos.

## Tecnologias

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Flyway
- Docker Compose

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

## Status atual

Nesta etapa, o projeto possui:

- PostgreSQL configurado via Docker Compose
- Datasource configurado no `application.yml`
- Flyway configurado
- Aplicação subindo localmente com `mvn spring-boot:run`