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