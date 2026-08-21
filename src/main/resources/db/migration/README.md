# Modelagem do Banco de Dados - GymFlow API

## Objetivo

Este documento descreve a modelagem atual do banco de dados do **GymFlow API** para o MVP.

A estrutura foi criada para permitir que academias, personal trainers e professores gerenciem alunos, exercícios, treinos modelo, atribuições semanais de treino e progresso do aluno.

---

## Tecnologias

- PostgreSQL
- Flyway
- SQL
- JPA/Hibernate

---

## Organização das migrations

As migrations ficam em:

```txt
src/main/resources/db/migration
```

O padrão de nome segue o formato:

```txt
V1__descricao_da_migration.sql
```

Exemplo:

```txt
V1__create_initial_tables.sql
V2__add_exercise_media_fields.sql
V3__add_student_workout_progress.sql
```

---

## Conceito principal

O GymFlow usa uma modelagem multi-organização.

A entidade central é `organizations`.

Uma organização pode representar:

```txt
ACADEMY
PERSONAL
```

Essa decisão permite que o MVP atenda tanto academias quanto profissionais autônomos.

---

## Tabelas principais

### organizations

Representa uma organização dentro do GymFlow.

Pode ser uma academia ou um personal trainer/profissional autônomo.

Responsabilidades:

- Agrupar usuários.
- Isolar dados por organização.
- Definir o contexto de exercícios, treinos e alunos.

Campos conceituais principais:

```txt
organization_id
organization_name
organization_type
organization_email
organization_phone
active
created_at
updated_at
```

---

### users

Representa os usuários do sistema.

Perfis disponíveis:

```txt
ADMIN
TEACHER
STUDENT
```

Todos os usuários pertencem a uma organização.

Responsabilidades:

- Representar administradores.
- Representar professores/personais.
- Representar alunos.
- Controlar autenticação e autorização.

Campos conceituais principais:

```txt
user_id
organization_id
user_name
user_email
password_hash
role
active
created_at
updated_at
```

Regras principais:

- O e-mail do usuário deve ser único.
- `password_hash` nunca deve ser exposto em responses.
- `ADMIN`, `TEACHER` e `STUDENT` acessam apenas dados permitidos pelo perfil.
- `STUDENT` acessa apenas os próprios dados.

---

### exercises

Representa exercícios cadastrados por uma organização.

Responsabilidades:

- Armazenar dados básicos do exercício.
- Armazenar grupo muscular.
- Armazenar equipamento.
- Armazenar descrição.
- Armazenar URL de imagem.
- Armazenar URL de vídeo.
- Permitir inativação sem remoção física.

Campos conceituais principais:

```txt
exercise_id
organization_id
exercise_name
muscle_group
equipment_name
description
image_url
video_url
active
created_at
updated_at
```

Regras principais:

- Exercícios pertencem a uma organização.
- Exercícios podem ser inativados.
- A listagem administrativa pode exibir exercícios ativos e inativos.
- O frontend decide filtrar visualmente por status.

---

### workouts

Representa um treino modelo reutilizável.

Um `Workout` não pertence diretamente a um aluno.

Responsabilidades:

- Representar um treino base.
- Permitir reutilização do mesmo treino para vários alunos.
- Permitir associação de vários exercícios.

Campos conceituais principais:

```txt
workout_id
organization_id
teacher_id
workout_name
status
created_at
updated_at
```

Status disponíveis:

```txt
ACTIVE
INACTIVE
ARCHIVED
```

Regras principais:

- Treinos pertencem a uma organização.
- Treinos são modelos reutilizáveis.
- Um treino pode ser atribuído a vários alunos.
- Um treino pode ser atribuído ao mesmo aluno em diferentes dias da semana.
- A inativação do treino não deve apagar o histórico.

---

### workout_exercises

Representa os exercícios vinculados a um treino modelo.

Responsabilidades:

- Definir quais exercícios fazem parte de um treino.
- Definir ordem dos exercícios.
- Definir séries, repetições, carga recomendada, descanso e observações.

Campos conceituais principais:

```txt
workout_exercise_id
workout_id
exercise_id
exercise_order
sets
reps
recommended_load
rest_time_seconds
notes
created_at
updated_at
```

Regras principais:

- Um treino pode ter vários exercícios.
- Um exercício pode estar em vários treinos.
- Os exercícios do treino são ordenados por `exercise_order`.
- A ordem não deve se repetir dentro do mesmo treino.
- `reps` é textual para permitir valores como `8-12`, `até a falha` ou observações similares.

---

### student_workouts

Representa a atribuição de um treino modelo para um aluno.

Essa tabela não representa o treino em si. Ela representa o vínculo entre aluno, treino e dia da semana.

Responsabilidades:

- Atribuir um treino modelo a um aluno.
- Definir o dia da semana da atribuição.
- Controlar status da atribuição.
- Permitir rotina semanal do aluno.

Campos conceituais principais:

```txt
student_workout_id
student_id
workout_id
assigned_by
assigned_at
week_day
status
created_at
updated_at
```

Valores de `week_day`:

```txt
MONDAY
TUESDAY
WEDNESDAY
THURSDAY
FRIDAY
SATURDAY
SUNDAY
```

Status disponíveis:

```txt
ACTIVE
INACTIVE
ARCHIVED
```

Regras principais:

- Um aluno pode ter vários treinos ativos na semana.
- Um aluno pode ter apenas um treino ativo por dia da semana.
- O mesmo treino modelo pode ser atribuído ao mesmo aluno em dias diferentes.
- Ao atribuir um novo treino ativo para um dia da semana, outro treino ativo daquele aluno no mesmo dia deve ser inativado.
- A rotina semanal do aluno é derivada das atribuições ativas.

---

### student_workout_exercise_progress

Representa o progresso do aluno nos exercícios de um treino atribuído.

O progresso pertence a um `StudentWorkout`, e não diretamente ao `Workout`.

Essa decisão é importante porque o `Workout` é um modelo reutilizável, enquanto cada aluno precisa ter seu próprio progresso individual.

Campos conceituais principais:

```txt
student_workout_exercise_progress_id
student_workout_id
workout_exercise_id
completed
completed_at
created_at
updated_at
```

Regras principais:

- Cada aluno registra progresso individualmente.
- O progresso está associado ao treino atribuído.
- Marcar um exercício como concluído atualiza `completed` e `completed_at`.
- Desmarcar um exercício concluído limpa `completed_at`.
- Apenas o aluno dono do treino pode marcar ou desmarcar progresso.

---

## Relacionamentos principais

```txt
organizations 1:N users
organizations 1:N exercises
organizations 1:N workouts

users (ADMIN/TEACHER) 1:N workouts
users (STUDENT) 1:N student_workouts

workouts 1:N workout_exercises
exercises 1:N workout_exercises

workouts 1:N student_workouts
student_workouts 1:N student_workout_exercise_progress
workout_exercises 1:N student_workout_exercise_progress
```

---

## Fluxo conceitual do treino

```txt
ADMIN/TEACHER cria exercícios
        ↓
ADMIN/TEACHER cria um treino modelo
        ↓
ADMIN/TEACHER adiciona exercícios ao treino
        ↓
ADMIN/TEACHER atribui o treino a um aluno em um dia da semana
        ↓
STUDENT visualiza o treino do dia ou a rotina semanal
        ↓
STUDENT marca exercícios como concluídos
```

---

## Por que Workout não possui student_id

`Workout` representa um modelo de treino reutilizável.

Se `Workout` tivesse `student_id`, cada treino ficaria preso a um único aluno, impedindo reutilização.

Por isso, a relação com aluno acontece por meio de `student_workouts`.

Essa separação permite:

- Criar um treino uma vez.
- Reutilizar o treino para vários alunos.
- Atribuir o mesmo treino em dias diferentes.
- Manter progresso individual por aluno.
- Evoluir futuramente para histórico, relatórios e ciclos de treino.

---

## Por que StudentWorkout possui week_day

O MVP evoluiu de “um treino ativo por aluno” para “rotina semanal”.

Com `week_day`, o sistema permite que o aluno tenha treinos diferentes ao longo da semana.

Exemplo:

```txt
MONDAY    -> Treino A
TUESDAY   -> Cardio
WEDNESDAY -> Treino B
FRIDAY    -> Treino C
```

Regra atual:

```txt
Um aluno pode ter apenas um treino ativo por dia da semana.
```

Mas pode ter vários treinos ativos na semana.

---

## Segurança e isolamento por organização

A modelagem foi pensada para evitar vazamento de dados entre organizações.

Regras principais:

- Usuários pertencem a uma organização.
- Exercícios pertencem a uma organização.
- Treinos pertencem a uma organização.
- Alunos só podem receber treinos da própria organização.
- Professores e admins só acessam dados da própria organização.
- Alunos só acessam os próprios treinos.

---

## Seed de desenvolvimento

O seed local fica em:

```txt
docs/dev/dev-seed-data.sql
```

Ele cria dados fictícios para facilitar testes locais:

- Organização de desenvolvimento.
- Admin.
- Professor.
- Aluno.
- Exercícios.
- Treino modelo.
- Exercícios vinculados ao treino.
- Treino atribuído ao aluno.

Credenciais locais:

```txt
admin.dev@gymflow.com / 123456
teacher.dev@gymflow.com / 123456
student.dev@gymflow.com / 123456
```

Essas credenciais são apenas para ambiente local.

---

## Fora do escopo da modelagem do MVP

- Pagamentos.
- Marketplace.
- Nutricionistas.
- Planos de assinatura.
- Convites por e-mail.
- Reset de senha por e-mail.
- Notificações.
- Histórico avançado de treinos.
- Relatórios analíticos.
- Multi-unidades complexas.
- Administração global da plataforma.

---

## Observações

- Migrations Flyway devem ser usadas para alterações estruturais.
- Seeds de desenvolvimento não devem ser versionados como migrations obrigatórias.
- Dados reais não devem ser colocados no seed.
- Alterações de regra de negócio devem ser acompanhadas de testes.
- Alterações de schema devem ser acompanhadas de migration.