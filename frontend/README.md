# Via Bank

Projeto full stack do **Via Bank**.

## Estrutura

```text
Via Bank/
├── index.html
├── login.html
├── styles.css
├── app.js
├── backend.js
└── backend/
    ├── pom.xml
    ├── database/
    ├── docs/
    └── src/main/
```

## Frontend

O frontend roda separado, por exemplo com Live Server ou:

```bash
python -m http.server 5500
```

Abra:

```text
http://localhost:5500/login.html
```

O arquivo `backend.js` aponta para:

```text
http://localhost:3000/api
```

## Backend

O backend foi feito com:

- Java 21
- Spring Boot
- Maven
- Spring Security
- JWT
- Spring Data JPA
- MySQL

Entre na pasta:

```bash
cd backend
```

Quando o banco MySQL estiver criado e configurado:

```bash
mvn spring-boot:run
```

A API sobe em:

```text
http://localhost:3000/api
```

## MySQL

O banco ainda não precisa existir. As queries estão em:

```text
backend/database/01_schema.sql
backend/database/02_seed_optional.sql
backend/database/03_useful_queries.sql
```

A conexão está em `backend/src/main/resources/application.properties` e usa `CHANGE_ME` como senha padrão.

## Fluxo principal

1. Usuário cria conta no `login.html`.
2. `POST /api/auth/register` cria usuário, conta, cartão, configurações e chaves PIX.
3. O backend devolve JWT.
4. O dashboard usa o token no header `Authorization`.
5. Um PIX usa `POST /api/pix/send`.
6. O backend bloqueia as contas envolvidas, valida o saldo, debita o remetente, credita o destinatário e grava as duas movimentações na mesma transação.
