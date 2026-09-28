# Via Bank Backend

Backend REST do frontend **Via Bank**, feito com Java 21, Spring Boot 4.1.1, Maven e MySQL.

## Recursos

- Cadastro e login com BCrypt + JWT.
- Logout com revogação de token.
- Conta criada automaticamente no cadastro.
- Chaves PIX de CPF, e-mail e telefone criadas automaticamente.
- PIX entre usuários Via Bank com transação atômica.
- Lock pessimista das duas contas durante o PIX.
- Extrato paginado.
- Gastos por categoria.
- Cartões sem PAN/CVV.
- Perfil e preferências.
- Investimentos e resgates.
- CORS para o frontend em localhost:5500.
- API em localhost:3000.

## Banco de dados

A conexão está propositalmente sem uma senha válida:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/via_bank...
spring.datasource.username=root
spring.datasource.password=CHANGE_ME
```

Quando criar o MySQL:

1. Execute `database/01_schema.sql`.
2. Opcional: execute `database/02_seed_optional.sql`.
3. Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET`.

## Rodar

```bash
mvn clean spring-boot:run
```

API:

```text
http://localhost:3000/api
```

Frontend:

```text
http://localhost:5500/login.html
```

## PIX

`POST /api/pix/send`

```json
{
  "key": "destino@email.com",
  "amount": 25.50,
  "description": "Almoço"
}
```

Dentro de uma única transação o backend bloqueia as contas, verifica o saldo, debita o remetente, credita o destinatário e grava duas movimentações com a mesma `transferReference`. Se algo falhar, ocorre rollback.

## Importante

Este projeto é uma base educacional/portfólio. Um banco real ainda exige ledger contábil dedicado, idempotência forte, KYC/AML, antifraude, auditoria imutável, HSM/gestão de chaves, limites transacionais, reconciliação, observabilidade, segregação de funções e integração real ao arranjo Pix.
