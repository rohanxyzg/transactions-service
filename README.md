# Transactions Service

A REST API for managing cardholder accounts and financial transactions.

## Overview

Each account belongs to a cardholder identified by a document number. Transactions are linked to an account and an operation type. The operation type determines the sign of the stored amount:

| ID | Description              | Sign stored |
|----|--------------------------|-------------|
| 1  | Normal Purchase          | Negative    |
| 2  | Purchase with Installments | Negative  |
| 3  | Withdrawal               | Negative    |
| 4  | Credit Voucher           | Positive    |

The `amount` field in a request is always treated as an absolute value — the service applies the correct sign.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/)

That's it. No local JDK or Maven needed to run.

## Quick Start

```bash
git clone https://github.com/rohanxyzg/transactions-service.git
cd transactions-service
chmod +x run.sh
./run.sh
```

Once ready:

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/api-docs | OpenAPI JSON spec |

## API Endpoints

### Create Account

```
POST /accounts
```

Request:
```json
{ "document_number": "12345678900" }
```

Response `201 Created`:
```json
{ "account_id": 1, "document_number": "12345678900" }
```

---

### Get Account

```
GET /accounts/{accountId}
```

Response `200 OK`:
```json
{ "account_id": 1, "document_number": "12345678900" }
```

---

### Create Transaction

```
POST /transactions
```

Request:
```json
{ "account_id": 1, "operation_type_id": 4, "amount": 123.45 }
```

Response `201 Created`:
```json
{
  "transaction_id": 1,
  "account_id": 1,
  "operation_type_id": 4,
  "amount": 123.45,
  "event_date": "2024-01-05T09:34:18.589Z"
}
```

## Example with curl

```bash
# Create account
curl -s -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"document_number":"12345678900"}' | jq

# Get account
curl -s http://localhost:8080/accounts/1 | jq

# Create a debit transaction (stored as -50.00)
curl -s -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"account_id":1,"operation_type_id":1,"amount":50.00}' | jq

# Create a credit transaction (stored as +123.45)
curl -s -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"account_id":1,"operation_type_id":4,"amount":123.45}' | jq
```

## Error Responses

All errors return a consistent JSON body:

```json
{
  "timestamp": "2024-01-01T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Account not found with id: 99",
  "path": "/accounts/99"
}
```

| Status | Cause |
|--------|-------|
| 404    | Account or operation type not found |
| 409    | Document number already registered |
| 422    | Validation failure (missing/invalid fields) |
| 500    | Unexpected server error |

## Running Tests

Tests use H2 in-memory database and do not require Docker.

```bash
./run.sh test
# or directly:
mvn test
```

## Other Commands

```bash
./run.sh down    # stop containers
./run.sh logs    # follow app logs
./run.sh clean   # stop and remove volumes
```

## Environment Variables

| Variable      | Default                                        | Description         |
|---------------|------------------------------------------------|---------------------|
| `DB_URL`      | `jdbc:postgresql://localhost:5432/transactions_db` | JDBC URL        |
| `DB_USER`     | `transactions`                                 | Database username   |
| `DB_PASSWORD` | `transactions`                                 | Database password   |
| `PORT`        | `8080`                                         | Server port         |

## Project Structure

```
src/main/java/com/example/transactions/
├── controller/      HTTP layer — request/response mapping
├── service/         Business logic — sign enforcement, validation
├── repository/      Spring Data JPA interfaces
├── domain/          JPA entities (Account, OperationType, Transaction)
├── dto/             Request/response records
└── exception/       Custom exceptions + global error handler
```

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Data JPA + Hibernate
- PostgreSQL 16
- SpringDoc OpenAPI (Swagger UI)
- H2 (tests only)
- Docker + Docker Compose
