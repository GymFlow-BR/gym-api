# Modelagem Inicial do Banco de Dados

Este documento descreve a migration inicial do banco de dados do **GymFlow API**.

A estrutura foi criada para atender ao MVP do sistema, permitindo o cadastro de academias, usuários, exercícios, treinos modelo e atribuição de treinos para alunos.

## Tecnologias

- PostgreSQL
- Flyway
- SQL

## Migration

Arquivo principal:

```txt
src/main/resources/db/migration/V1__create_initial_tables.sql
```

## Tabelas Criadas

A migration inicial cria as seguintes tabelas:

- `academies`: armazena as academias cadastradas;
- `users`: armazena os usuários do sistema;
- `student_profiles`: armazena dados complementares dos alunos;
- `exercises`: armazena os exercícios cadastrados por academia;
- `workouts`: armazena os treinos modelo;
- `student_workouts`: relaciona alunos com treinos modelo;
- `workout_exercises`: relaciona exercícios com treinos.

## Regras Principais da Modelagem

A modelagem inicial segue estas regras:

- Uma academia pode ter vários usuários;
- Um usuário pertence a uma academia;
- Um usuário pode ter role `ADMIN`, `TEACHER` ou `STUDENT`;
- Exercícios pertencem a uma academia;
- Treinos são criados como modelos reutilizáveis;
- Um treino modelo pode ter vários exercícios;
- Um treino modelo pode ser atribuído a vários alunos;
- A atribuição entre aluno e treino é feita pela tabela `student_workouts`.

## Treinos Modelo

A tabela `workouts` representa os treinos modelo do sistema.

Ela não possui `student_id` diretamente, pois o objetivo é permitir que um professor/admin crie um treino uma vez e possa atribuí-lo para um ou mais alunos.

A relação entre aluno e treino é feita pela tabela `student_workouts`.

Fluxo esperado:

```txt
Professor/Admin cria um treino modelo
        ↓
Exercícios são adicionados ao treino
        ↓
O treino modelo é atribuído a um ou mais alunos
```

## Relacionamentos Principais

Os principais relacionamentos da modelagem são:

```txt
academies 1:N users
academies 1:N exercises
users 1:1 student_profiles
users (TEACHER / ADMIN) 1:N workouts
workouts 1:N workout_exercises
exercises 1:N workout_exercises
users N:N workouts via student_workouts
```

A tabela `student_workouts` permite que alunos recebam treinos modelo, enquanto `workout_exercises` define os exercícios de cada treino.

## Validações no Banco

A migration possui algumas validações importantes:

- `role` do usuário aceita apenas `ADMIN`, `TEACHER` ou `STUDENT`;
- `status` dos treinos aceita apenas `ACTIVE`, `INACTIVE` ou `ARCHIVED`;
- `sets`, `reps` e `exercise_order` precisam ser maiores que zero;
- exercícios não podem ter o mesmo nome dentro da mesma academia;
- o mesmo treino não pode ser atribuído mais de uma vez ao mesmo aluno.

## Validações Futuras

Algumas regras serão tratadas futuramente no backend, principalmente na camada de Service:

- Garantir que `student_id` seja um usuário com role `STUDENT`;
- Garantir que `teacher_id` seja um usuário com role `TEACHER` ou `ADMIN`;
- Definir se um aluno poderá ter um ou mais treinos ativos ao mesmo tempo;
- Atualizar automaticamente o campo `updated_at`.

## Status

A modelagem inicial do banco foi criada e revisada para o MVP do GymFlow.

A estrutura atual permite:

- cadastrar academias;
- cadastrar usuários;
- cadastrar exercícios;
- criar treinos modelo;
- associar exercícios aos treinos;
- atribuir treinos para alunos.

Essa migration serve como base para as próximas etapas do backend.