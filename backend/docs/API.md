# Contrato da API - Via Bank

Base URL usada pelo frontend:

```text
http://localhost:3000/api
```

Todos os endpoints, exceto cadastro e login, exigem:

```http
Authorization: Bearer <accessToken>
```

## Autenticação

### POST /auth/register
Cria usuário, conta, configurações, cartão de débito e chaves PIX padrão.

Body:
```json
{
  "name": "Maria Silva",
  "cpf": "12345678901",
  "email": "maria@email.com",
  "phone": "11999999999",
  "birthDate": "2000-01-01",
  "password": "senha123"
}
```

### POST /auth/login
Body:
```json
{
  "email": "maria@email.com",
  "password": "senha123"
}
```

### GET /auth/me
Retorna o usuário da sessão.

### POST /auth/logout
Revoga o JWT atual.

## Perfil

- `GET /profile`
- `PUT /profile`

## Conta

- `GET /account`
- `GET /account/balance`
- `POST /account/deposit` - endpoint de desenvolvimento, controlado por `DEMO_DEPOSIT`.

## PIX

- `GET /pix/keys`
- `POST /pix/keys`
- `DELETE /pix/keys/{id}`
- `POST /pix/send`
- `GET /pix/favorites`
- `POST /pix/favorites`

### POST /pix/send
```json
{
  "key": "destino@email.com",
  "amount": 50.00,
  "description": "Pagamento"
}
```

## Extrato

- `GET /transactions?period=30d&page=1&limit=50`
- `GET /transactions/{id}`

Períodos aceitos: `30d`, `90d` ou ano (`2026`).

## Analytics

- `GET /analytics/spending?period=30d`
- `GET /financial-health`

## Cartões

- `GET /cards`
- `GET /cards/{id}`
- `PATCH /cards/{id}/status`
- `PATCH /cards/{id}/limit`
- `GET /cards/{id}/virtual`

A API não expõe PAN completo nem CVV.

## Investimentos

- `GET /investments/products`
- `GET /investments/portfolio`
- `POST /investments`
- `POST /investments/{id}/redeem`

## Preferências

- `GET /settings`
- `PUT /settings`

## Dispositivos

- `POST /devices`

## Compatibilidade

- `POST /sync/dashboard`

Esse endpoint existe apenas para compatibilidade com o frontend. O backend ignora saldo, cartões, transações e investimentos enviados pelo navegador, porque dados financeiros não podem ter o frontend como fonte da verdade.
