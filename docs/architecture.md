# Arquitetura do GymFlow

Este documento registra decisões importantes de arquitetura e modelagem adotadas no backend do GymFlow.

O objetivo é manter claro para a equipe por que determinadas entidades existem, como elas se relacionam e quais decisões foram tomadas para preparar o sistema para evolução futura.

---

## Modelo Organization

O GymFlow usa `Organization` como entidade base para representar diferentes tipos de clientes da plataforma.

Uma `Organization` representa a estrutura responsável por gerenciar usuários, exercícios, treinos e alunos dentro do sistema.

Inicialmente, uma organização pode representar:

- `ACADEMY`: academias
- `PERSONAL`: personal trainers ou profissionais autônomos

Essa decisão permite que o GymFlow atenda tanto academias tradicionais quanto profissionais independentes que desejam gerenciar seus próprios alunos e treinos.

---

## Por que usamos Organization em vez de Academy

Na primeira ideia do sistema, o modelo era pensado exclusivamente para academias.

Nesse cenário, faria sentido ter uma entidade chamada `Academy`.

Porém, durante a evolução do produto, identificamos que o GymFlow também pode atender personal trainers e outros profissionais fitness que não necessariamente pertencem a uma academia.

Por isso, o modelo foi generalizado para `Organization`.

Com essa decisão, evitamos limitar o sistema apenas a academias e deixamos a base preparada para atender diferentes tipos de organização no futuro.

Exemplos:

- Uma academia pode ter seus próprios professores, alunos, exercícios e treinos.
- Um personal trainer pode gerenciar seus próprios alunos, exercícios e treinos.
- Futuramente, outros tipos de organização podem ser adicionados sem remodelar toda a base principal do sistema.

---

## Tipos iniciais de organização

No MVP, os tipos iniciais de organização são:

```txt
ACADEMY
PERSONAL
```
## Segurança e escopo por organização

O GymFlow adota uma arquitetura multi-organização.

Todos os usuários pertencem a uma `Organization`, incluindo usuários com perfil `ADMIN`, `TEACHER` e `STUDENT`.

No MVP, o perfil `ADMIN` não representa um administrador global da plataforma. Ele representa um administrador dentro da própria organização.

Regras principais:

- `ADMIN` gerencia recursos da própria organização.
- `TEACHER` gerencia exercícios, treinos e alunos da própria organização.
- `STUDENT` acessa apenas os próprios dados.
- Recursos como exercícios, treinos e alunos não devem ser acessados entre organizações diferentes.

Essa decisão evita vazamento de dados entre academias, personais e demais organizações que possam existir futuramente.

## Progresso do aluno no treino

O progresso do aluno é registrado a partir da entidade `StudentWorkoutExerciseProgress`.

Essa entidade representa o progresso de um exercício específico dentro de um treino atribuído a um aluno.

O progresso pertence a um `StudentWorkout`, e não diretamente ao `Workout`, porque `Workout` representa um treino modelo reutilizável.

Essa decisão permite que vários alunos usem o mesmo treino modelo, mas cada um tenha seu próprio progresso individual.

Regra importante:

- `Workout` representa o modelo.
- `StudentWorkout` representa a atribuição do treino ao aluno.
- `StudentWorkoutExerciseProgress` representa o progresso do aluno em cada exercício daquele treino atribuído.

## Eventos internos

O GymFlow passou a utilizar eventos internos com Spring Events para desacoplar ações secundárias da regra principal de negócio.

Quando um aluno marca um exercício como concluído, o sistema publica um evento interno chamado `StudentWorkoutExerciseCompletedEvent`.

Neste MVP, o listener apenas registra um log informativo.

Essa decisão prepara o sistema para evoluções futuras, como:

- histórico de atividade;
- notificações;
- relatórios;
- gamificação;
- integração com mensageria externa.

Neste momento, não usamos RabbitMQ, Kafka ou Redis. O objetivo é manter o MVP simples, mas já com uma base arquitetural mais preparada.