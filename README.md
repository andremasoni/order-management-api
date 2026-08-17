# Order Management API

REST API for managing orders, built with Java 21 and Spring Boot 3.3. Orders move
through a status workflow that the API enforces, and every route except
authentication requires a JWT.

## Tech stack

- Java 21, Spring Boot 3.3
- Spring Web, Spring Data JPA, Spring Security, Bean Validation
- PostgreSQL 16 with Flyway migrations
- JWT (JJWT 0.12), BCrypt
- springdoc-openapi (Swagger UI), Spring Boot Actuator
- Docker and Docker Compose, Maven, Lombok

## Domain rule: the status workflow

An order is created as `CREATED` and can only move along allowed edges. Anything else
is rejected with `400` and an explicit message.

```
CREATED ──> PAID ──> PROCESSING ──> SHIPPED ──> DELIVERED
   │          │           │
   └──────────┴───────────┴────────> CANCELED
```

`DELIVERED` and `CANCELED` are terminal. The rule lives in `OrderService`, not in the
controller, so it holds no matter which entry point calls it.

## Endpoints

| Method | Path | Auth | Description |
| ------ | ---- | ---- | ----------- |
| POST | `/auth/register` | public | Create a `ROLE_USER` account |
| POST | `/auth/login` | public | Exchange credentials for a JWT |
| POST | `/orders` | Bearer | Create an order |
| GET | `/orders` | Bearer | List orders, paginated, newest first |
| GET | `/orders/{id}` | Bearer | Get one order |
| PUT | `/orders/{id}/status` | Bearer | Move the order to a new status |
| DELETE | `/orders/{id}` | Bearer + `ROLE_ADMIN` | Cancel an order |

`GET /orders` accepts `page`, `size` and `sort` (default `size=20`, `sort=createdAt,desc`)
and answers with an explicit envelope:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

Swagger UI: `http://localhost:8080/swagger-ui.html`
OpenAPI document: `http://localhost:8080/api-docs`
Health check: `http://localhost:8080/actuator/health`

## Configuration

| Variable | Required | Description |
| -------- | -------- | ----------- |
| `JWT_SECRET` | yes | Base64 key of at least 256 bits used to sign tokens |
| `SPRING_DATASOURCE_URL` | no | Defaults to `jdbc:postgresql://localhost:5432/orders_db` |
| `DB_USERNAME` / `DB_PASSWORD` | no | Database credentials |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | no | When both are set, an admin account is created at startup |

There is no default `JWT_SECRET`: the application refuses to start without one. A
signing key committed to a public repository is not a secret, and an API that boots
with a known key silently accepts forged tokens.

```bash
cp .env.example .env
echo "JWT_SECRET=$(openssl rand -base64 32)" >> .env
```

`/auth/register` always creates `ROLE_USER`, so the admin-only route needs the
bootstrap variables above to be reachable on a fresh database.

## Running with Docker Compose

```bash
cp .env.example .env      # then fill in JWT_SECRET
docker compose up --build
```

Compose starts PostgreSQL, waits for its health check, then starts the API on
`http://localhost:8080`.

## Running locally

```bash
docker compose up -d postgres
export JWT_SECRET=$(openssl rand -base64 32)
mvn spring-boot:run
```

## Database schema

Flyway owns the schema and applies `src/main/resources/db/migration/V1__init.sql` at
startup. Hibernate runs with `ddl-auto: validate`, so the application fails fast when
the entity mapping and the migrated schema disagree, instead of altering tables on its
own the way `ddl-auto: update` does.

## Request examples

```bash
# register and log in
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"andre","password":"secret123"}'

TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"andre","password":"secret123"}' | jq -r .token)

# create an order
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"customerName":"ACME Ltda","totalAmount":1250.00}'

# list, paginated
curl "http://localhost:8080/orders?page=0&size=10" -H "Authorization: Bearer $TOKEN"

# advance the status
curl -X PUT http://localhost:8080/orders/{id}/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"PAID"}'
```

## Project layout

```
src/main/java/com/company/ordermanagement/
├── config/      # security, OpenAPI, admin bootstrap
├── controller/  # HTTP layer
├── dto/         # request and response contracts
├── entity/      # JPA entities and enums
├── exception/   # domain exceptions and @RestControllerAdvice
├── repository/  # Spring Data JPA
├── security/    # JWT provider, filter, UserDetailsService
└── service/     # business rules and transactions
src/main/resources/db/migration/  # Flyway migrations
```

## Error responses

```json
{
  "status": 400,
  "message": "Invalid status transition: DELIVERED -> PAID",
  "timestamp": "2026-08-17T12:00:00"
}
```

| Status | When |
| ------ | ---- |
| 400 | Validation failure or forbidden status transition |
| 401 | Missing, malformed or expired token |
| 403 | Valid token without the required role |
| 404 | Order id does not exist |
| 409 | Username already registered |

## Known limitations

- No automated tests yet. `OrderService` transitions (unit tests with a mocked
  repository) and the authenticated routes (`@SpringBootTest` with Testcontainers)
  are the natural starting points.
- No refresh token: the client logs in again when the access token expires.
- An order has a customer name and a total, not order items. Adding a line item
  entity is the next step for the domain.
