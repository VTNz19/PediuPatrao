# Dashboard Administrativo — PediuPatrão

Painel para o responsável pelo estabelecimento acompanhar vendas, pedidos e tendências.
Tela: **`/dashboard`** (tela inicial após o login de ADMIN/GERENTE). Dados vindos da API **`/api/dashboard/**`**.

## 1. Requisitos

### Funcionais

| Id | Requisito | Como foi atendido |
|----|-----------|-------------------|
| RF01 | Indicadores gerais de vendas | Cards: faturamento (com variação % vs. período anterior), pedidos válidos/total, ticket médio, clientes atendidos, descontos, em andamento, cancelados e taxa de cancelamento — `GET /resumo` |
| RF02 | Análise de vendas (gráficos) | Faturamento por dia/semana/mês (`/vendas`), pedidos por hora do dia (`/vendas/por-hora`), receita por categoria (`/vendas/por-categoria`) |
| RF03 | Situação dos pedidos | Gráfico de rosca por status + tabela dos últimos pedidos (`/pedidos/por-status`, `/pedidos/recentes`) |
| RF04 | Produtos mais vendidos | Ranking por quantidade e receita (`/produtos/mais-vendidos`) |
| RF05 | Filtros de consulta | Período (de/até), status, tipo de produto e agrupamento; botões Aplicar/Limpar. Opções de status e tipo vêm de `/filtros` |
| RF06 | Integração com a API | A página só consome a API via `fetch` (sem dados embutidos no HTML); mesma sessão/CSRF do sistema |
| RF07 | Atualização e tratamento de dados | Atualização automática a cada 60 s (liga/desliga), indicação de "atualizado às"; mensagens para 400/401/403/falha de rede; estados vazios; valores arredondados em 2 casas; pedidos sem data ou itens nulos são tolerados |

### Não funcionais

| Id | Requisito |
|----|-----------|
| RNF01 | **Segurança:** `/dashboard` e `/api/dashboard/**` restritos a ADMIN e GERENTE (URL em `SecurityConfig` + `@PreAuthorize` no `DashboardService`). Atendente é redirecionado para `/pedidos` |
| RNF02 | **Somente leitura:** a API usa apenas `GET`; não altera dados nem exige CSRF |
| RNF03 | **Desempenho:** a consulta ao MongoDB é limitada ao período pedido (`dataHoraEntrada` em `[de, ate+1)`); período máximo de 366 dias, padrão de 30 dias; `limite` máximo 50 |
| RNF04 | **Consistência:** mesma regra de negócio em todos os endpoints (abaixo) |
| RNF05 | **Usabilidade:** layout responsivo (Bootstrap), valores em R$ (pt-BR) |
| RNF06 | **Manutenibilidade:** regras concentradas em `DashboardService`; contratos em `DashboardDtos`; erros padronizados `{"erro": "..."}` |
| RNF07 | **Testabilidade:** 25 testes automatizados novos (serviço e contrato HTTP) sem depender do MongoDB |

### Regras de negócio das métricas

- O período filtra pela **data de entrada** do pedido.
- **Faturamento** = soma de `valorTotal` (já com desconto) dos pedidos **não cancelados**.
- Pedidos `CANCELADO` aparecem só nas contagens (total, cancelados, taxa e gráfico de status); ficam fora de faturamento, ticket médio, séries, ranking e categorias.
- **Ticket médio** = faturamento ÷ pedidos válidos.
- **Em andamento** = status não terminal (RECEBIDO, EM_PREPARACAO, PRONTO, SAIU_PARA_ENTREGA_RETIRADO).
- **Variação** compara com o período imediatamente anterior de mesma duração; `null` se ele não teve faturamento.
- **Receita do ranking/categoria** = quantidade × preço unitário (antes do desconto do pedido).
- O filtro `tipo` vale para o ranking de produtos; os demais endpoints ignoram esse parâmetro.

## 2. API

Base: `/api/dashboard` · Autenticação: sessão (login do sistema) · Perfis: ADMIN, GERENTE · Formato: JSON.

**Parâmetros comuns** (todos opcionais, exceto onde indicado):

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `de` | `yyyy-MM-dd` | Data inicial. Padrão: 29 dias antes de `ate` |
| `ate` | `yyyy-MM-dd` | Data final (inclusive). Padrão: hoje |
| `status` | string | `RECEBIDO`, `EM_PREPARACAO`, `PRONTO`, `SAIU_PARA_ENTREGA_RETIRADO`, `FINALIZADO` ou `CANCELADO` (sem diferenciar maiúsculas) |

### Endpoints

| Método | Rota | Parâmetros extras | Resposta |
|--------|------|-------------------|----------|
| GET | `/filtros` | — | Status, tipos de produto e agrupamentos disponíveis |
| GET | `/resumo` | — | Indicadores gerais (RF01) |
| GET | `/vendas` | `agrupamento` = `dia` (padrão), `semana`, `mes` | Série de faturamento (RF02) |
| GET | `/vendas/por-hora` | — | 24 faixas horárias (RF02) |
| GET | `/vendas/por-categoria` | — | Receita por tipo de produto (RF02) |
| GET | `/pedidos/por-status` | — | Quantidade e % por status (RF03) |
| GET | `/pedidos/recentes` | `limite` 1–50 (padrão 10) | Últimos pedidos (RF03) |
| GET | `/produtos/mais-vendidos` | `tipo`, `limite` 1–50 (padrão 5) | Ranking (RF04) |

### Exemplos

**`GET /api/dashboard/resumo?de=2025-10-01&ate=2025-10-31`**

```json
{
  "de": "2025-10-01",
  "ate": "2025-10-31",
  "totalPedidos": 42,
  "pedidosValidos": 39,
  "faturamento": 3120.5,
  "ticketMedio": 80.01,
  "descontoTotal": 145.0,
  "pedidosCancelados": 3,
  "taxaCancelamento": 7.14,
  "pedidosEmAndamento": 2,
  "clientesAtendidos": 27,
  "faturamentoPeriodoAnterior": 2800.0,
  "pedidosPeriodoAnterior": 36,
  "variacaoFaturamento": 11.45
}
```

**`GET /api/dashboard/vendas?de=2025-10-01&ate=2025-10-03&agrupamento=dia`**

```json
[
  { "periodo": "2025-10-01", "rotulo": "01/10", "faturamento": 210.0, "pedidos": 3 },
  { "periodo": "2025-10-02", "rotulo": "02/10", "faturamento": 0.0,   "pedidos": 0 },
  { "periodo": "2025-10-03", "rotulo": "03/10", "faturamento": 95.5,  "pedidos": 1 }
]
```

**`GET /api/dashboard/vendas/por-hora`** → `[ { "hora": 0, "faturamento": 0.0, "pedidos": 0 }, ... 24 itens ... ]`

**`GET /api/dashboard/vendas/por-categoria`**

```json
[ { "tipo": "Pizza", "quantidade": 51, "receita": 2040.0 },
  { "tipo": "Bebida", "quantidade": 38, "receita": 304.0 } ]
```

**`GET /api/dashboard/pedidos/por-status`**

```json
[ { "status": "RECEBIDO", "quantidade": 1, "percentual": 2.38 },
  { "status": "EM_PREPARACAO", "quantidade": 1, "percentual": 2.38 },
  { "status": "PRONTO", "quantidade": 0, "percentual": 0.0 },
  { "status": "SAIU_PARA_ENTREGA_RETIRADO", "quantidade": 0, "percentual": 0.0 },
  { "status": "FINALIZADO", "quantidade": 37, "percentual": 88.1 },
  { "status": "CANCELADO", "quantidade": 3, "percentual": 7.14 } ]
```

**`GET /api/dashboard/pedidos/recentes?limite=2`**

```json
[ { "id": "671f...", "cliente": "Maria", "status": "FINALIZADO", "valorTotal": 100.0,
    "dataHoraEntrada": "2025-10-31T19:42:10" } ]
```

**`GET /api/dashboard/produtos/mais-vendidos?tipo=Pizza&limite=3`**

```json
[ { "produtoId": "671a...", "nome": "Pizza Mussarela", "tipo": "Pizza", "quantidade": 30, "receita": 1200.0 } ]
```

**`GET /api/dashboard/filtros`**

```json
{ "status": ["RECEBIDO", "EM_PREPARACAO", "PRONTO", "SAIU_PARA_ENTREGA_RETIRADO", "FINALIZADO", "CANCELADO"],
  "tipos": ["Bebida", "Esfiha", "Pizza"],
  "agrupamentos": ["dia", "semana", "mes"] }
```

### Erros

| HTTP | Quando | Corpo |
|------|--------|-------|
| 400 | Data fora de `yyyy-MM-dd` | `{"erro": "Parâmetro 'de' inválido: '31/01/2025'. Esperado: yyyy-MM-dd."}` |
| 400 | `de` posterior a `ate` | `{"erro": "A data inicial (de) não pode ser posterior à data final (ate)."}` |
| 400 | Período > 366 dias | `{"erro": "O período máximo de consulta é de 366 dias."}` |
| 400 | `status` desconhecido | `{"erro": "Status inválido: XPTO. Valores aceitos: RECEBIDO, ..."}` |
| 400 | `agrupamento` desconhecido | `{"erro": "Agrupamento inválido: ano. Valores aceitos: [dia, semana, mes]."}` |
| 400 | `limite` fora de 1–50 ou não numérico | `{"erro": "O limite deve estar entre 1 e 50."}` |
| 401/302 | Sem login | Redireciona para `/login` |
| 403 | Perfil ATENDENTE | Acesso negado |

## 3. Decisões de projeto

- **Um endpoint por visão** (em vez de um único "dashboard"): cada gráfico carrega de forma independente, pode ser reutilizado e testado isoladamente, e os mesmos filtros valem para todos.
- **Agregação em Java sobre consulta por período no MongoDB**: o volume de uma pizzaria por período limitado (≤ 366 dias) é pequeno, e as regras (cancelados, desconto, categoria vinda do catálogo) ficam legíveis e testáveis. Se o volume crescer, o ponto de evolução é o *aggregation pipeline* do MongoDB, sem mudar o contrato da API.
- **Séries completas**: dias/semanas/meses sem venda voltam com zero, para o gráfico não distorcer a tendência.
- **Categoria pelo catálogo**: `ItemPedido` não guarda o tipo; ele é obtido de `Produto` (itens de produtos removidos caem em "Outros").
- **Acesso restrito a ADMIN/GERENTE**: dados financeiros não são relevantes para o atendente.

## 4. Testes

`./mvnw test` — **34 testes, 0 falhas** (25 novos para o dashboard + 9 já existentes). Execução em 2026-11-10:

```
Tests run: 7,  Failures: 0, Errors: 0 -- DashboardControllerTest
Tests run: 18, Failures: 0, Errors: 0 -- DashboardServiceTest
Tests run: 7,  Failures: 0, Errors: 0 -- PedidoServiceTest
Tests run: 2,  Failures: 0, Errors: 0 -- DiffUtilsTest
Tests run: 34, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Cenário | Teste |
|---------|-------|
| Faturamento, ticket médio, desconto, cancelados, taxa, em andamento, clientes | `resumoCalculaIndicadoresIgnorandoCancelados` |
| Filtro por status | `resumoComFiltroDeStatus` |
| Período sem dados (sem divisão por zero) | `resumoVazioNaoDividePorZero` |
| Série por dia (com dias vazios), por mês | `vendasPorDia*`, `vendasAgrupadasPorMes*` |
| 24 faixas horárias; cancelado não conta | `vendasPorHoraRetorna24Faixas` |
| Status com percentual | `pedidosPorStatusTrazTodosOsStatusComPercentual` |
| Ranking: ordem, exclusão de cancelados, filtro de tipo, limite | `ranking*` |
| Receita por categoria | `categoriasSomamReceitaPorTipo` |
| Pedidos recentes | `pedidosRecentesMaisNovoPrimeiroELimite` |
| Períodos padrão | `semParametrosUsaUltimos30Dias` |
| Erros 400 (data, período invertido/longo, status, agrupamento, limite) | `rejeita*`, `DashboardControllerTest` |

### Teste de integração manual (MongoDB real, 2026-11-10)

Aplicação + MongoDB 7 em execução, login via formulário e chamadas com `curl`:

| Chamada | Resultado |
|---------|-----------|
| `GET /api/dashboard/resumo` (gerente01) | 200 — 11 pedidos, 9 válidos, faturamento 515,95, taxa de cancelamento 18,18% |
| `GET /produtos/mais-vendidos?limite=3` | 200 — Refrigerante (9), Esfiha (6), Pizza Mussarela (5) |
| `GET /vendas/por-categoria` | 200 — Pizza 306,00 · Lanche 102,00 · Bebida 63,00 · Esfiha 51,00 |
| `GET /pedidos/por-status` | 200 — todos os 6 status com percentual |
| `resumo?de=31/01/2025` · `status=XPTO` · `agrupamento=ano` · `de>ate` | 400 com `{"erro": ...}` |
| `/api/dashboard/resumo` como atendente01 | 403; `/` redireciona para `/pedidos` |

Este teste revelou um defeito que os testes unitários (com repositório simulado) não pegam: o Spring Data MongoDB
não aceita duas condições no mesmo campo em query derivada (`>=` e `<` em `dataHoraEntrada`). Corrigido com `@Query` explícita.

**Não coberto por teste automatizado:** a renderização da tela no navegador (ver roteiro abaixo).

### Roteiro de teste manual (com MongoDB ativo)

1. `docker run -d --name mongo -p 27017:27017 mongo:7` e `mvnw.cmd spring-boot:run`.
2. Login `gerente01` / `teste123`; criar alguns pedidos em **Pedidos → Novo** e avançar/cancelar alguns.
3. Abrir **Dashboard**: cards e gráficos devem refletir os pedidos; testar filtros de data, status, tipo e agrupamento.
4. Login `atendente01`: `/` leva a `/pedidos` e `/api/dashboard/resumo` retorna 403.
5. Chamar `/api/dashboard/resumo?de=31/01/2025` logado: deve retornar 400 com mensagem.

## 5. Fluxo Git (gitflow)

`main` (estável) ← `develop` ← `feature/dashboard-api` (esta implementação).
