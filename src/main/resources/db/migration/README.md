# Modelagem Inicial do Banco de Dados

Este documento descreve a estrutura inicial do banco de dados do GymFlow API.
A modelagem foi adaptada para ser flexível, atendendo tanto academias tradicionais quanto personal trainers autônomos e pequenos estúdios através do conceito de **Organizações**.

## Tecnologias
* PostgreSQL
* Flyway
* SQL

## Arquivo da Migration
`src/main/resources/db/migration/V1__create_initial_tables.sql`

## Tabelas Criadas
* **organizations**: armazena as organizações parceiras (academias ou personal trainers);
* **users**: armazena os usuários do sistema (administradores, professores e alunos);
* **student_profiles**: armazena dados complementares e históricos dos alunos;
* **exercises**: armazena os exercícios cadastrados por cada organização;
* **workouts**: armazena os treinos modelo;
* **student_workouts**: relaciona os alunos com os treinos recebidos;
* **workout_exercises**: relaciona quais exercícios fazem parte de cada treino.

## Regras Principais da Modelagem
* Uma organização pode ter vários usuários;
* Um usuário pertence obrigatoriamente a uma organização;
* Uma organização pode ser do tipo **ACADEMY** (Academia) ou **PERSONAL** (Personal Trainer);
* Um usuário pode ter a função (*role*) de `ADMIN`, `TEACHER` ou `STUDENT`;
* Exercícios pertencem a uma organização específica;
* Organizações e exercícios possuem um campo `active` (verdadeiro/falso) para permitir desativação no futuro.

## Fluxo dos Treinos Modelo
Os treinos não são criados direto para um aluno específico. O professor cria um "treino modelo" reutilizável e depois o associa a quantos alunos quiser.

Professor cria o Treino Modelo ➔ Adiciona os Exercícios ao Treino ➔ Atribui o Treino para um ou mais Alunos

## Relacionamentos Principais
* organizations (1) ➔ (N) users
* organizations (1) ➔ (N) exercises
* users (1) ➔ (1) student_profiles
* users (TEACHER/ADMIN) (1) ➔ (N) workouts
* workouts (1) ➔ (N) workout_exercises
* exercises (1) ➔ (N) workout_exercises
* users (STUDENT) (N) ➔ (N) workouts *via tabela student_workouts*

## Validações Diretas no Banco
* O tipo da organização aceita apenas `ACADEMY` ou `PERSONAL`;
* A função (*role*) do usuário aceita apenas `ADMIN`, `TEACHER` ou `STUDENT`;
* O status dos treinos aceita apenas `ACTIVE`, `INACTIVE` ou `ARCHIVED`;
* Quantidade de séries (*sets*), repetições (*reps*) e a ordem do exercício devem ser maiores que zero;
* Não podem existir dois exercícios com o mesmo nome dentro da mesma organização.