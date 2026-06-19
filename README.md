# Access Management Service

A REST API for role-based access management: users are created with one of a fixed set of roles (`ADMIN`, `MEMBER`, `GUEST`) and managed through a small, well-defined HTTP surface. The project is deliberately narrow in scope — the goal is a clean, well-reasoned backend service rather than a large feature set.

## Tech Stack

- **Java 21**, **Maven**
- **Spring Boot 4** (Web MVC, Data JPA, Validation, Actuator)
- **Hibernate 7** with an **H2** in-memory database
- **JUnit 5** for testing
- Structured JSON logging (ECS format) and **Micrometer** metrics

The code is organised **package-by-feature**: everything related to users lives under `user/`, with cross-cutting infrastructure (request filtering, exception handling, error responses) kept at the application root since it serves all features rather than any single one.

## Running It

```bash
git clone https://github.com/marius-acro/access-management-service.git
cd access-management-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The `dev` profile seeds a few example users and enables SQL logging. Without it the application starts with an empty database — which is the intended behaviour for tests and production-like runs, where seed data has no place.

Once running, the service is available at `http://localhost:8080`.

```bash
# List all users
curl http://localhost:8080/users

# Create a user
curl -i -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"email": "alice@example.com", "role": "MEMBER"}'

# Fetch one by id
curl http://localhost:8080/users/{id}

# Delete one
curl -i -X DELETE http://localhost:8080/users/{id}
```

## API

| Method   | Path          | Description        | Success | Errors                          |
| -------- | ------------- | ------------------ | ------- | ------------------------------- |
| `GET`    | `/users`      | List all users     | `200`   | —                               |
| `GET`    | `/users/{id}` | Fetch a user by id | `200`   | `404` not found, `400` bad id   |
| `POST`   | `/users`      | Create a user      | `201`   | `400` validation failed         |
| `DELETE` | `/users/{id}` | Delete a user      | `204`   | `404` not found, `400` bad id   |

Errors are returned as a consistent JSON shape (`error` plus a human-readable `message`, or a `fields` map for validation failures), produced centrally rather than per endpoint.

Two Actuator endpoints are exposed deliberately — `health` and `metrics` — rather than opening everything. Exposure is opt-in by name, not a blanket `*`.

## Design Decisions

The interesting part of a small service is not *what* it does but *why* it is built the way it is. A few decisions worth calling out:

### Roles are persisted by name, not by ordinal

`User.role` is annotated `@Enumerated(EnumType.STRING)`. By default JPA stores an enum by its ordinal position (`ADMIN` → `0`, `MEMBER` → `1`, …), which means reordering the enum or inserting a value in the middle silently reassigns the role of every existing row. In an access-management context that is the worst kind of bug: a harmless-looking code change quietly granting the wrong permissions. Storing the name instead makes the mapping independent of declaration order.

### The API model is decoupled from the persistence model

Input and output each pass through their own DTO — `CreateUserRequest` coming in, `UserResponse` going out — and the JPA entity is never serialised directly. Mapping happens at the controller boundary; the service works with domain objects and knows nothing about the HTTP representation. This keeps the two models free to evolve independently and ensures that adding a field to the entity (say, an internal flag) can never accidentally leak into an API response.

### Validation is layered, with one responsibility per constraint

The incoming `role` is kept as a `String` on the request DTO and validated with a custom `@ValidRole` constraint, rather than letting Jackson deserialise straight into the enum. Deserialising directly would turn an invalid role into a low-level parsing error; validating explicitly produces a clean `400` with a field-level message. Each constraint checks exactly one thing — `@NotNull` handles presence, `@ValidRole` handles the value — so an empty role and an invalid role are reported as distinct, precise errors.

### Observability is treated as a first-class, cross-cutting concern

Every request is tagged with a correlation ID (read from an `X-Correlation-Id` header or generated), propagated through the call stack via MDC and emitted on every log line. Logs are structured JSON in ECS format, so the correlation ID is a queryable field rather than buried in a message string. The correlation logic lives in a servlet filter at the edge — the service layer never sees it, because tracing context belongs to the boundary, not the business logic.

Metrics follow the same layering. Technical signals (request rate, latency, status) come for free from Actuator's built-in HTTP metrics. The one custom metric — a counter of users created, tagged by `outcome` — lives in the service, because "a user was created" is a piece of domain knowledge the framework cannot infer. The split is deliberate: each layer measures what it is positioned to know. A malformed request rejected by validation never reaches the service, so it is counted as an HTTP client error by the framework, not as a creation failure by the business metric — two different questions, answered at two different layers.

## Testing

Tests focus on observable behaviour at the HTTP boundary rather than on implementation detail. The controller tests exercise every endpoint across its full range of status codes — success, not-found, malformed id, and each validation failure case independently — using `MockMvc`. A repository test covers persistence round-tripping, and a set of contract tests verifies the correlation-ID filter (generation, propagation of an incoming id, uniqueness across requests).

```bash
./mvnw test
```

## What I'd Do Next

The current scope was chosen to keep the service small and coherent. The natural next steps, in roughly the order they'd matter:

- **Schema migrations.** Hibernate currently generates the schema (`create-drop`), which is fine for in-memory development but unacceptable for a real database. A versioned migration tool (Flyway or Liquibase) would own the schema and make changes auditable and reversible.
- **A uniqueness constraint on email.** Right now duplicate emails are allowed. Enforcing uniqueness at the database level, surfaced as a `409 Conflict`, is the obvious correctness improvement for a user store.
- **Richer observability.** The two hand-rolled success/failure counters could be replaced by a single Micrometer `Observation`, which captures duration, outcome and a trace span in one instrument — the same abstraction Spring uses internally — rather than counting outcomes by hand.

## AI Assistance

I used an AI assistant throughout this project, deliberately as a tutor rather than a code generator. Coming from a backend background in another language, I used it to explain Java and Spring concepts, to surface idioms I wouldn't have known to look for, and as a sparring partner to pressure-test my reasoning. The implementation, the architecture, and the design decisions documented above are my own — written by me, and chosen because I can justify them. Where the assistant suggested something I didn't understand or agree with, I pushed back or left it out.
