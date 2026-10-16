# 🍕 PediuPatrão

Sistema web de gestão de pedidos para pizzarias, com controle de acesso por perfil, fluxo de
status do pedido (recebimento → finalização), cancelamentos/descontos restritos ao gerente e
trilha de auditoria das alterações.

Projeto acadêmico (UMC).

## Tecnologias

- Java 21
- Spring Boot 3.4.4 (Web, Security, Data MongoDB, Thymeleaf)
- MongoDB
- Thymeleaf + Layout Dialect + Spring Security extras
- AdminLTE (interface)
- Lombok
- Maven (wrapper incluso)
- JUnit 5 / Spring Boot Test

## Pré-requisitos

- JDK 21
- MongoDB rodando em `localhost:27017` (ex.: via Docker)

```bash
docker run -d --name mongo -p 27017:27017 mongo:7
```

## Como executar

```bash
./mvnw spring-boot:run        # Linux/macOS
mvnw.cmd spring-boot:run      # Windows
```

A aplicação sobe em **http://localhost:8081** (porta definida em `application.properties`).

### Configuração

`src/main/resources/application.properties`:

| Propriedade                    | Padrão                       | Descrição                    |
|--------------------------------|------------------------------|------------------------------|
| `server.port`                  | `8081`                       | Porta HTTP                   |
| `spring.data.mongodb.uri`      | `mongodb://localhost:27017`  | URI do MongoDB               |
| `spring.data.mongodb.database` | `pizzaria`                   | Nome do banco                |

Para usar um cluster MongoDB Atlas, troque `spring.data.mongodb.uri` pela sua string de
conexão. **Não versione credenciais** — prefira variáveis de ambiente
(`SPRING_DATA_MONGODB_URI`).

### Dados iniciais (seed)

Na primeira execução o `DataInitializer` cria automaticamente:

| Usuário       | Senha      | Perfil    |
|---------------|------------|-----------|
| `admin`       | `teste123` | ADMIN     |
| `gerente01`   | `teste123` | GERENTE   |
| `atendente01` | `teste123` | ATENDENTE |

Além de 4 produtos (Pizza Mussarela, Pizza Calabresa, Refrigerante Lata, Esfiha de Carne) e
1 cliente de teste, caso o banco esteja vazio. **Troque essas senhas fora de ambiente de teste.**

## Funcionalidades

- **Usuários e perfis (RF01):** cadastro e edição de usuários, restrito ao ADMIN.
- **Pedidos com itens (RF02):** o valor total é sempre calculado no servidor a partir do
  catálogo; o preço nunca é aceito do cliente.
- **Fluxo de status (RF03):**
  `RECEBIDO → EM_PREPARACAO → PRONTO → SAIU_PARA_ENTREGA_RETIRADO → FINALIZADO`,
  sem pular etapas e sem alterar pedidos finalizados/cancelados. Responsável e data/hora são
  registrados automaticamente.
- **Cancelamento e desconto (RF04):** exclusivos do GERENTE. Cancelamento exige justificativa;
  desconto é percentual, limitado a 20%. Ambos só são permitidos antes da saída para
  entrega/retirada.
- **Auditoria (RF05):** criação/alteração de pedidos e clientes gera registros imutáveis
  (usuário, perfil, data/hora, operação, campos antes/depois, justificativa).
- **Clientes e produtos:** cadastro com endereço completo; catálogo de produtos.

## Principais alterações realizadas

- Perfis ADMIN, GERENTE e ATENDENTE, com autorização por URL (`SecurityConfig`) e por método
  de serviço (`@PreAuthorize`), e proteção contra atribuição indevida de privilégios.
- Entidade `Pedido` evoluída com itens, quantidades, preços, desconto, total calculado no
  servidor, responsável e data/hora de entrada e saída.
- Máquina de estados do pedido com bloqueio de transições inválidas e de alterações após
  cancelamento ou finalização.
- Cancelamento (com justificativa) e desconto (máx. 20%) exclusivos do GERENTE, só antes da
  saída para entrega/retirada.
- Auditoria persistente no MongoDB (`RegistroAuditoria`, `AuditoriaRepository`,
  `AuditoriaService`) para pedidos e clientes, consultável apenas por ADMIN e GERENTE e sem
  operações de edição ou exclusão.
- CSRF habilitado em formulários e chamadas à API.
- Testes unitários das regras de pedido e do `DiffUtils`.

## Perfis e permissões

| Ação                                 | ADMIN | GERENTE | ATENDENTE |
|--------------------------------------|:-----:|:-------:|:---------:|
| Gerenciar usuários e perfis          |  ✅   |         |           |
| Cadastrar/editar clientes            |       |         |    ✅     |
| Excluir clientes                     |  ✅   |         |           |
| Criar pedidos / avançar status       |       |   ✅    |    ✅     |
| Consultar pedidos                    |  ✅   |   ✅    |    ✅     |
| Cancelar pedido / aplicar desconto   |       |   ✅    |           |
| Gerenciar catálogo de produtos       |  ✅   |         |           |
| Consultar catálogo                   |  ✅   |   ✅    |    ✅     |
| Consultar auditoria                  |  ✅   |   ✅    |           |

A autorização é aplicada em dois níveis: por URL (`SecurityConfig`) e por método de serviço
(`@PreAuthorize`) — chamadas diretas à API com o perfil errado retornam `403 Forbidden`.

## Rotas principais

**Telas (Thymeleaf)**

| Rota                                  | Descrição                       |
|---------------------------------------|---------------------------------|
| `/login`, `/`                         | Login e página inicial          |
| `/pedidos`, `/pedidos/novo`           | Listagem e criação de pedidos   |
| `/clientes`, `/clientes/novo`         | Clientes                        |
| `/produtos`                           | Catálogo                        |
| `/usuarios`                           | Usuários (ADMIN)                |
| `/auditoria`                          | Auditoria (ADMIN/GERENTE)       |

**API REST**

| Método e rota                         | Descrição                       |
|---------------------------------------|---------------------------------|
| `PUT /api/pedidos/{id}/avancar`       | Avança o status                 |
| `PUT /api/pedidos/{id}/cancelar`      | Cancela (GERENTE)               |
| `PUT /api/pedidos/{id}/desconto`      | Aplica desconto (GERENTE)       |
| `PUT/DELETE /api/clientes/{id}`       | Atualiza / remove cliente       |
| `PUT/DELETE /api/produtos/{id}`       | Atualiza / remove produto       |
| `GET/PUT/DELETE /api/usuarios/{id}`   | Gerencia usuário (ADMIN)        |

### CSRF

CSRF está habilitado (cookie `XSRF-TOKEN`). Os formulários Thymeleaf enviam o token
automaticamente. Para testar a API via curl/Postman em `PUT`/`POST`/`DELETE`:

```bash
curl -c cookies.txt -b cookies.txt http://localhost:8081/ -u gerente01:teste123
TOKEN=$(grep XSRF-TOKEN cookies.txt | awk '{print $7}')
curl -b cookies.txt -H "X-XSRF-TOKEN: $TOKEN" -X PUT \
  http://localhost:8081/api/pedidos/<id>/desconto \
  -H "Content-Type: application/json" -d '{"percentual": 10}'
```

## Estrutura do projeto

```
src/main/java/com/umc/pediupatrao/
├── config/       # SecurityConfig, DataInitializer, MongoInitConfig
├── controller/   # Telas e endpoints REST
├── entity/       # Pedido, ItemPedido, Cliente, Produto, Usuario, Perfil, StatusPedido, RegistroAuditoria...
├── exception/    # NegocioException, ApiExceptionHandler
├── repository/   # Repositórios Spring Data MongoDB
├── service/      # Regras de negócio e @PreAuthorize
└── util/         # DiffUtils (comparação de campos para auditoria)
src/main/resources/
├── templates/    # Páginas Thymeleaf
└── static/       # CSS e AdminLTE
```

## Testes

```bash
./mvnw test
```

Cobrem a máquina de estados do pedido (transições inválidas, bloqueio de cancelamento/desconto
após a saída, limite de 20%, cálculo do total) e o `DiffUtils` usado na auditoria.

## Repositório

https://github.com/VTNz19/PediuPatrao
