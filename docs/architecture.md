# Arquitetura do GymFlow

Este documento registra decisões importantes de arquitetura e modelagem adotadas no backend do **GymFlow API**.

O objetivo é manter claro por que determinadas entidades existem, como elas se relacionam e quais decisões foram tomadas para manter o MVP simples, funcional e preparado para evolução futura.

---

## Visão geral

O GymFlow é um MVP de uma plataforma para academias, personal trainers, professores e alunos.

A arquitetura atual permite que uma organização fitness gerencie:

- usuários;
- alunos;
- professores;
- exercícios;
- treinos modelo;
- exercícios vinculados aos treinos;
- atribuições semanais de treino;
- progresso do aluno.

O sistema foi construído com foco em separação por organização, controle de acesso por perfil e reutilização de treinos modelo.

---

## Modelo Organization

O GymFlow usa `Organization` como entidade base para representar diferentes tipos de clientes da plataforma.

Uma `Organization` representa a estrutura responsável por gerenciar usuários, exercícios, treinos e alunos dentro do sistema.

Inicialmente, uma organização pode representar:

```txt
ACADEMY
PERSONAL
```

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

---

## Segurança e escopo por organização

O GymFlow adota uma arquitetura multi-organização.

Todos os usuários pertencem a uma `Organization`, incluindo usuários com perfil `ADMIN`, `TEACHER` e `STUDENT`.

No MVP, o perfil `ADMIN` não representa um administrador global da plataforma. Ele representa um administrador dentro da própria organização.

Regras principais:

- `ADMIN` gerencia recursos da própria organização.
- `TEACHER` gerencia alunos, exercícios, treinos e atribuições da própria organização.
- `STUDENT` acessa apenas os próprios dados.
- Recursos como exercícios, treinos, alunos e professores não devem ser acessados entre organizações diferentes.

Essa decisão evita vazamento de dados entre academias, personais e demais organizações que possam existir futuramente.

---

## Perfis de acesso

### ADMIN

Representa o administrador de uma organização.

Responsabilidades principais:

- gerenciar professores;
- gerenciar alunos;
- gerenciar exercícios;
- gerenciar treinos;
- atribuir treinos a alunos;
- inativar usuários da própria organização.

### TEACHER

Representa um professor ou personal trainer vinculado a uma organização.

Responsabilidades principais:

- gerenciar alunos;
- gerenciar exercícios;
- gerenciar treinos;
- atribuir treinos a alunos.

Restrições principais:

- não pode criar professores;
- não pode criar admins;
- não pode alterar status ativo/inativo de usuários;
- não pode acessar dados de outra organização.

### STUDENT

Representa um aluno vinculado a uma organização.

Responsabilidades principais:

- visualizar o próprio treino do dia;
- visualizar a própria rotina semanal;
- visualizar detalhes de treinos atribuídos;
- marcar exercícios como concluídos;
- desmarcar exercícios concluídos.

Restrições principais:

- não pode acessar dados de outros alunos;
- não pode gerenciar exercícios;
- não pode gerenciar treinos;
- não pode criar usuários.

---

## Autenticação

A autenticação do GymFlow usa JWT armazenado em cookie `HttpOnly`.

A decisão foi tomada para evitar que o frontend precise armazenar o token manualmente em `localStorage`.

Fluxo de autenticação:

```txt
Usuário envia e-mail/senha
        ↓
Backend valida credenciais
        ↓
Backend gera JWT
        ↓
Backend envia JWT em cookie HttpOnly
        ↓
Frontend faz chamadas autenticadas com credentials: include
        ↓
Backend valida o cookie nas próximas requisições
```

Endpoints principais:

```txt
POST  /api/auth/login
GET   /api/auth/me
POST  /api/auth/logout
PATCH /api/auth/change-password
```

Regras principais:

- O token não é retornado no body do login.
- O frontend não deve salvar token em `localStorage`.
- O cookie de autenticação é criado e limpo pelo backend.
- O endpoint `/api/auth/me` é usado para recuperar a sessão após refresh.
- O logout limpa o cookie de autenticação.

---

## Treinos modelo

A entidade `Workout` representa um treino modelo reutilizável.

Um treino modelo não pertence diretamente a um aluno.

Essa decisão permite:

- criar um treino uma vez;
- reutilizar o treino para vários alunos;
- atribuir o mesmo treino a diferentes alunos;
- atribuir o mesmo treino ao mesmo aluno em dias diferentes;
- manter o progresso individual separado por aluno.

Regra importante:

```txt
Workout representa o modelo.
StudentWorkout representa a atribuição do treino a um aluno.
```

---

## Exercícios do treino

A entidade `WorkoutExercise` representa a relação entre um treino modelo e seus exercícios.

Ela armazena informações específicas daquele exercício dentro do treino, como:

- ordem;
- séries;
- repetições;
- carga recomendada;
- tempo de descanso;
- observações.

Essa decisão evita duplicar exercícios e permite que o mesmo exercício seja usado em vários treinos com configurações diferentes.

---

## Rotina semanal do aluno

O MVP evoluiu para permitir rotina semanal.

A entidade `StudentWorkout` representa a atribuição de um treino modelo a um aluno em um dia específico da semana.

O campo `weekDay` define o dia da semana da atribuição.

Valores suportados:

```txt
MONDAY
TUESDAY
WEDNESDAY
THURSDAY
FRIDAY
SATURDAY
SUNDAY
```

Regras principais:

- Um aluno pode ter vários treinos ativos na semana.
- Um aluno pode ter apenas um treino ativo por dia da semana.
- O mesmo treino modelo pode ser atribuído ao mesmo aluno em dias diferentes.
- Ao atribuir um novo treino ativo para o mesmo aluno e mesmo dia da semana, outro treino ativo daquele dia deve ser inativado.

Exemplo:

```txt
MONDAY    -> Treino A
TUESDAY   -> Cardio
WEDNESDAY -> Treino B
FRIDAY    -> Treino A
```

Essa decisão permite que o aluno tenha uma rotina semanal mais realista sem abandonar a simplicidade do MVP.

---

## Treino atual do aluno

O endpoint de treino atual retorna o treino ativo do aluno para o dia atual do servidor.

```txt
GET /api/students/{studentId}/workouts/current
```

Essa abordagem permite que a tela principal do aluno mostre automaticamente o treino do dia.

Quando não há treino ativo para o dia atual, a API retorna uma resposta apropriada para o frontend exibir um estado vazio.

---

## Detalhes de treino específico

Além do treino atual, o GymFlow permite consultar os detalhes completos de um treino específico atribuído ao aluno.

```txt
GET /api/students/{studentId}/workouts/{studentWorkoutId}/details
```

Essa decisão permite que o aluno acesse qualquer treino ativo da rotina semanal, mesmo que ele não seja o treino do dia.

---

## Progresso do aluno no treino

O progresso do aluno é registrado a partir da entidade `StudentWorkoutExerciseProgress`.

Essa entidade representa o progresso de um exercício específico dentro de um treino atribuído a um aluno.

O progresso pertence a um `StudentWorkout`, e não diretamente ao `Workout`, porque `Workout` representa um treino modelo reutilizável.

Essa decisão permite que vários alunos usem o mesmo treino modelo, mas cada um tenha seu próprio progresso individual.

Regra importante:

- `Workout` representa o modelo.
- `StudentWorkout` representa a atribuição do treino ao aluno.
- `StudentWorkoutExerciseProgress` representa o progresso do aluno em cada exercício daquele treino atribuído.

---

## Progresso do treino atual e de treino específico

O MVP suporta progresso em dois contextos:

### Treino atual

```txt
GET   /api/students/{studentId}/workouts/current/progress
PATCH /api/students/{studentId}/workouts/current/exercises/{workoutExerciseId}/complete
PATCH /api/students/{studentId}/workouts/current/exercises/{workoutExerciseId}/uncomplete
```

### Treino específico

```txt
GET   /api/students/{studentId}/workouts/{studentWorkoutId}/progress
PATCH /api/students/{studentId}/workouts/{studentWorkoutId}/exercises/{workoutExerciseId}/complete
PATCH /api/students/{studentId}/workouts/{studentWorkoutId}/exercises/{workoutExerciseId}/uncomplete
```

Essa separação permite que o aluno registre progresso tanto no treino do dia quanto em outros treinos ativos da rotina semanal.

---

## Upload de mídia dos exercícios

O MVP suporta upload de imagem e vídeo para exercícios.

Endpoints principais:

```txt
POST /api/exercises/{exerciseId}/image
POST /api/exercises/{exerciseId}/video
```

O upload permite que o frontend exiba mídia de apoio para o aluno durante a execução do treino.

As credenciais do serviço de upload devem ser configuradas por variáveis de ambiente e nunca devem ser versionadas no repositório.

---

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

---

## Tratamento de erros

A API utiliza tratamento padronizado de erros.

Cenários principais:

```txt
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
500 Internal Server Error
```

Objetivos:

- evitar vazamento de detalhes internos;
- facilitar exibição de mensagens no frontend;
- padronizar responses de erro;
- separar erro de validação, erro de permissão e erro de regra de negócio.

---

## Decisões fora do MVP

Algumas decisões foram mantidas fora do MVP para preservar foco e simplicidade.

Fora do escopo atual:

- marketplace de profissionais;
- pagamentos;
- assinatura de planos;
- convites por e-mail;
- reset de senha por e-mail;
- login social;
- notificações;
- relatórios avançados;
- mensageria externa;
- app mobile nativo;
- administração global da plataforma.

---

## Direção futura

A arquitetura atual permite evoluir para:

- marketplace de personais;
- programas de treino pagos;
- acompanhamento nutricional;
- notificações;
- relatórios de evolução;
- ciclos de treino;
- histórico avançado;
- gamificação;
- mensageria com RabbitMQ ou Kafka;
- deploy em produção;
- pipeline CI/CD.

Essas evoluções devem ser implementadas gradualmente, sem comprometer a simplicidade do MVP.