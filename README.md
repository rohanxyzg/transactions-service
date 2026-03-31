# Transactions Service

A REST API for managing cardholder accounts and financial transactions, built with Java 17 and Spring Boot 3.

---

## Overview

Each cardholder has an **account** identified by a unique document number. Every operation a cardholder performs generates a **transaction** linked to their account and an **operation type**.

The operation type determines how the amount is stored:

| ID | Description               | Amount stored |
|----|---------------------------|---------------|
| 1  | Normal Purchase           | Negative      |
| 2  | Purchase with Installments | Negative     |
| 3  | Withdrawal                | Negative      |
| 4  | Credit Voucher            | Positive      |

The `amount` in a request is always a positive value — the service enforces the correct sign. The account **balance** is updated atomically with every transaction using a pessimistic write lock, so concurrent requests on the same account never produce incorrect results.

---

## Prerequisites

| To do | You need |
|-------|----------|
| Run the app | Docker + Docker Compose |
| Run tests locally | Java 17 + Maven |

---

## Quick Start

```bash
git clone https://github.com/rohanxyzg/transactions-service.git
cd transactions-service
chmod +x run.sh
./run.sh
```

The script builds the Docker image, starts PostgreSQL, runs Flyway migrations, and waits until the app is healthy.

| URL | What |
|-----|------|
| http://localhost:8080/swagger-ui.html | Interactive API docs |
| http://localhost:8080/api-docs | OpenAPI JSON spec |

---

## API Reference

### `POST /accounts` — Create an account

**Request**
```json
{ "document_number": "12345678900" }
```

**Response `201 Created`**
```json
{
  "account_id": 1,
  "document_number": "12345678900",
  "balance": 0
}
```

> `document_number` must be exactly 11 digits.

---

### `GET /accounts/{accountId}` — Get an account

**Response `200 OK`**
```json
{
  "account_id": 1,
  "document_number": "12345678900",
  "balance": -50.00
}
```

---

### `POST /transactions` — Create a transaction

**Request**
```json
{ "account_id": 1, "operation_type_id": 4, "amount": 123.45 }
```

**Response `201 Created`**
```json
{
  "transaction_id": 1,
  "account_id": 1,
  "operation_type_id": 4,
  "amount": 123.45,
  "event_date": "2024-01-05T09:34:18.589322Z"
}
```

> `amount` must be between `0.01` and `999999.99`. `event_date` is set by the server.

---

## Try it with curl

### Happy path

```bash
# 1. Create an account
curl -s -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"document_number":"12345678900"}' | jq

# 2. Check the account (balance starts at 0)
curl -s http://localhost:8080/accounts/1 | jq

# 3. Make a purchase — stored as -50.00, balance becomes -50.00
curl -s -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"account_id":1,"operation_type_id":1,"amount":50.00}' | jq

# 4. Apply a credit voucher — stored as +123.45, balance becomes 73.45
curl -s -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"account_id":1,"operation_type_id":4,"amount":123.45}' | jq

# 5. Check balance again — should be 73.45
curl -s http://localhost:8080/accounts/1 | jq
```

### Error cases

```bash
# Duplicate document number → 409 Conflict
curl -s -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"document_number":"12345678900"}' | jq

# Account not found → 404 Not Found
curl -s http://localhost:8080/accounts/9999 | jq

# Invalid document (not 11 digits) → 422 Unprocessable Entity
curl -s -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"document_number":"123"}' | jq

# Zero amount → 422 Unprocessable Entity
curl -s -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"account_id":1,"operation_type_id":1,"amount":0}' | jq

# Non-existent operation type → 404 Not Found
curl -s -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"account_id":1,"operation_type_id":99,"amount":10.00}' | jq
```

---

## Error Response Format

All errors return a consistent JSON body — no stack traces, no Spring defaults.

```json
{
  "timestamp": "2024-01-01T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "An account already exists for document number: 12345678900",
  "path": "/accounts"
}
```

| Status | When |
|--------|------|
| `400`  | Invalid path parameter type (e.g. `/accounts/abc`) |
| `404`  | Account or operation type not found |
| `409`  | Document number already registered |
| `422`  | Validation failure — missing or invalid fields |
| `503`  | DB lock contention under heavy load — safe to retry |
| `500`  | Unexpected server error |

---

## Running Tests

Unit and integration tests use H2 in-memory — no Docker needed.

```bash
# Requires Java 17 + Maven locally
./run.sh test
```

Includes a concurrency test (`ConcurrentTransactionTest`) that fires 10 threads simultaneously against the same account and asserts the final balance is exactly correct — proving the pessimistic lock works.

---

## All Commands

```bash
./run.sh          # build and start everything (default)
./run.sh down     # stop containers
./run.sh logs     # follow app logs live
./run.sh test     # run unit + integration tests (needs Java 17 + Maven)
./run.sh clean    # stop containers and wipe the database volume
```

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/transactions_db` | JDBC connection URL |
| `DB_USER` | `transactions` | Database username |
| `DB_PASSWORD` | `transactions` | Database password |
| `PORT` | `8080` | Server port |
| `HIKARI_MAX_POOL` | `20` | Max DB connection pool size |

---

## Design Decisions

**Amount sign enforcement in the service layer**
The caller always sends a positive amount. The service applies the sign based on the operation type. This ensures the rule is enforced in exactly one place, regardless of what the client sends.

**Pessimistic write lock on transactions**
`createTransaction` acquires a `SELECT FOR UPDATE` lock on the account row before reading the balance. This serialises concurrent transactions on the same account — without it, two simultaneous requests could both read the same balance, compute independently, and one would overwrite the other (lost update).

**Two-layer duplicate prevention**
Account creation checks `existsByDocumentNumber` first (fast path, clean 409 message), then relies on the database unique constraint as the hard guarantee if two requests race past the check simultaneously. The `DataIntegrityViolationException` is caught and returned as 409, not 500.

**Flyway for schema management**
`ddl-auto: validate` — Hibernate never touches the schema at runtime. Flyway owns all DDL through versioned, reviewable migration scripts.

---

## Project Structure

```
src/main/java/com/example/transactions/
├── controller/     HTTP layer — request mapping, input validation
├── service/        Business logic — sign rules, locking, balance updates
├── repository/     Spring Data JPA interfaces
├── domain/         JPA entities — Account, OperationType, Transaction
├── dto/            Immutable request/response records
└── exception/      Typed exceptions + global error handler

src/main/resources/
└── db/migration/   Flyway versioned SQL migrations
    ├── V1__create_tables.sql
    ├── V2__seed_operation_types.sql
    └── V3__add_balance_to_accounts.sql
```

---

## Tech Stack

| | |
|-|-|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Persistence | Spring Data JPA + Hibernate + PostgreSQL 16 |
| Migrations | Flyway |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Connection Pool | HikariCP |
| Test DB | H2 (in-memory) |
| Infrastructure | Docker + Docker Compose |
