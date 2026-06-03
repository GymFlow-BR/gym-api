# GymFlow API

API backend do GymFlow, MVP de uma plataforma para academias, professores/personais e alunos.

## Objetivo do MVP

Permitir que professores/admins cadastrem exercícios, criem treinos modelo, associem exercícios aos treinos e atribuam treinos a alunos.

O aluno deve conseguir visualizar seu treino atual e os detalhes dos exercícios.

## Tecnologias

- Java 21
- Spring Boot
- Maven
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway
- Bean Validation
- Lombok

## Pré-requisitos

Antes de rodar o projeto, é necessário ter instalado:

- Java 21
- Maven
- Docker
- Docker Compose

## Como rodar localmente

### 1. Subir o banco PostgreSQL

Na raiz do projeto, execute:

```bash
docker compose up -d