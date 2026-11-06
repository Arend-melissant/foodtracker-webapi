# Project Agent Guidelines: Clean Java Architecture & API Standards

This document establishes the architectural, coding, and engineering standards for this repository. All AI agents and developers must strictly follow these instructions when writing, modifying, or refactoring Java code.

---

## 1. Core Principles & Clean Code

- **Clean Code & SOLID**:
  - Adhere to Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion principles.
  - Keep methods small, focused, and intention-revealing.
  - Maintain high cohesion within modules and loose coupling between components.
- **Naming Conventions**:
  - Classes and Interfaces: PascalCase (e.g., `CreateOrderUseCase`, `FoodItemRepository`).
  - Methods and Variables: camelCase (e.g., `calculateNutritionalValue`, `userRepository`).
  - Constants: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_ATTEMPTS`).
  - Use meaningful, descriptive domain names. Avoid vague acronyms or single-letter variable names.
- **Modern Java & Immutability**:
  - Prefer immutable data structures. Use Java records (`record`) or classes with `final` fields for Value Objects, DTOs, and Domain Events.
  - Avoid unnecessary mutable shared state.
- **Exception Handling & Logging**:
  - Never catch `Throwable` or `Exception` silently or use empty catch blocks.
  - Never use `System.out.println()` for production or service logging. Use standard logging facades (e.g., SLF4J / Logback) with appropriate log levels (`DEBUG`, `INFO`, `WARN`, `ERROR`).
  - Map exceptions to clear domain-specific exception types and translate them to HTTP status codes at the API adapter layer.

---

## 2. Hexagonal Architecture (Ports and Adapters)

Organize code strictly into decoupled layers where dependencies point solely inwards toward the business domain:

```
src/
├── domain/                  # Pure Business Logic (No frameworks, no DB dependencies)
│   ├── model/               # Entities, Value Objects, Aggregates
│   ├── exception/           # Core domain exceptions
│   └── event/               # Domain event records
├── application/             # Use Cases & Orchestration
│   ├── port/
│   │   ├── inbound/         # Primary / Driving ports (Use case interfaces)
│   │   └── outbound/        # Secondary / Driven ports (SPIs: Repositories, EventBus, External Services)
│   └── service/             # Application services implementing inbound ports
├── infrastructure/          # Adapters & Technical Details
│   ├── adapter/
│   │   ├── inbound/
│   │   │   └── rest/        # REST Controllers, DTOs, Serialization, Swagger/OpenAPI
│   │   └── outbound/
│   │       ├── persistence/ # Hibernate ORM entities, SQLite DB configuration, Repository adapters
│   │       ├── eventbus/    # Internal message bus implementation
│   │       └── auth/        # Google OAuth2 security & token verification filters
│   └── config/              # Dependency injection, bootstrap, and environment wiring
```

### Dependency Rules:
1. **Domain Layer**: Contains pure Java classes. Must have zero external framework dependencies (no Jakarta annotations for persistence, no Jackson/JAXB, no Spring/Hibernate).
2. **Application Layer**: Depends only on Domain. Defines inbound ports (use cases) and outbound ports (interfaces for DB, bus, etc.).
3. **Infrastructure Layer**: Implements outbound ports (Hibernate/SQLite adapter, Message Bus adapter) and drives inbound ports (REST controllers).

---

## 3. Strong Typing & Serialization/Deserialization (JSON & XML)

- **Always Use Strongly-Typed Classes**:
  - Never handle payloads as raw untyped `Map<String, Object>`, raw strings, `JsonObject`, or untyped XML DOM nodes in business logic.
  - Define explicit request DTOs and response DTOs for every endpoint.
- **Bi-Directional Serialization (JSON & XML)**:
  - Both JSON and XML must be fully supported across the API.
  - Model classes must be annotated for seamless serialization and deserialization in both formats.
  - Use declarative serialization:
    - Jakarta XML Binding (`jakarta.xml.bind.annotation.*` - `@XmlRootElement`, `@XmlElement`, `@XmlAccessorType`) for XML.
    - Jackson (`com.fasterxml.jackson.annotation.*` or Jakarta JSON Binding `jakarta.json.bind.annotation.*`) with support for XML/JSON modules.
  - Keep separate DTOs for API payloads and Domain Entities. Never expose database or internal domain entities directly in the API.

---

## 4. Modern Jakarta EE Standards

- **Use the `jakarta.*` Namespace**:
  - Always prefer the modern Jakarta EE package ecosystem over legacy `javax.*` packages:
    - **REST API**: `jakarta.ws.rs.*` (JAX-RS)
    - **Persistence**: `jakarta.persistence.*` (JPA)
    - **XML Binding**: `jakarta.xml.bind.*` (JAXB)
    - **JSON Binding / Processing**: `jakarta.json.*`, `jakarta.json.bind.*` (JSON-P / JSON-B)
    - **Validation**: `jakarta.validation.*` (Bean Validation - `@NotNull`, `@Size`, `@Valid`)
    - **Dependency Injection & Lifecycle**: `jakarta.inject.*`, `jakarta.annotation.*`, `jakarta.enterprise.*`

---

## 5. Internal Message Bus

- **Decoupled Asynchronous & Event-Driven Workflows**:
  - Use an internal message bus / event publisher to decouple side effects (notifications, audit logging, analytics, cache updates, cross-aggregate reactions) from core transactions.
  - Define an outbound port interface in the application layer:
    ```java
    package com.example.application.port.outbound;

    public interface EventPublisherPort {
        <T> void publish(T event);
    }
    ```
  - Implement using a robust internal bus (such as an in-memory event bus, Guava `EventBus`, or a lightweight asynchronous reactor/executor pattern).
  - All domain events must be immutable records (e.g., `record FoodLoggedEvent(UUID id, String userId, Instant timestamp)`).

---

## 6. RESTful Web API & Content Negotiation

- **Endpoints & Resource Modeling**:
  - Follow RESTful resource naming conventions (plural nouns, hierarchical relationships, e.g., `/api/v1/foods`, `/api/v1/foods/{id}`).
  - Standard HTTP verbs:
    - `GET`: Read resource(s) - idempotent, safe.
    - `POST`: Create resource - returns `201 Created` with `Location` header.
    - `PUT`: Complete resource replacement - idempotent.
    - `PATCH`: Partial update.
    - `DELETE`: Remove resource - returns `204 No Content`.
- **Content Negotiation (JSON & XML)**:
  - Endpoints must declare support for both media types:
    ```java
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    ```
  - Respect the incoming `Content-Type` and `Accept` request headers.
- **Consistent Error Handling**:
  - Use standard HTTP status codes (`400`, `401`, `403`, `404`, `422`, `500`).
  - Format error responses according to RFC 7807 (Problem Details for HTTP APIs) in both JSON and XML representations.

---

## 7. OpenAPI / Swagger Dev Endpoint

- **API Documentation & Testing**:
  - Expose an interactive OpenAPI / Swagger UI interface for developer testing and interactive verification (e.g., `/swagger` or `/swagger-ui/index.html`).
  - Generate OpenAPI v3 specifications dynamically using OpenAPI/Swagger annotations (e.g., `@OpenAPIDefinition`, `@Operation`, `@ApiResponse`, `@Parameter`).
  - Include schemas, field descriptions, validation constraints, and sample request/response payloads.
  - Configure Swagger UI to allow token authorization (Bearer Token / OAuth2) for testing authenticated endpoints directly in the browser.

---

## 8. Data Storage & ORM: SQLite + Hibernate ORM

- **Database**:
  - Use SQLite for data storage (via `org.xerial:sqlite-jdbc`).
- **ORM Configuration (Hibernate ORM / Jakarta Persistence)**:
  - *Note on naming: In the Java ecosystem, the ORM is Hibernate ORM (NHibernate is its .NET sister project). Use Hibernate ORM implementing the Jakarta Persistence standard (`jakarta.persistence`).*
  - Configure the Hibernate dialect for SQLite (e.g., `org.hibernate.community.dialect.SQLiteDialect` or custom SQLite dialect).
  - Use clean JPA mappings (`@Entity`, `@Table`, `@Id`, `@Column`) inside the infrastructure persistence adapter.
  - Map persistence entities to clean domain models before passing data back into application use cases.
  - Manage database transactions cleanly with appropriate isolation and transaction boundary demarcation.

---

## 9. Security & Authentication: Google OAuth2

- **Authentication Flow**:
  - Secure all protected REST endpoints using Google OAuth2 Bearer Tokens (JWT / Google ID Tokens).
  - Verify tokens on incoming requests:
    - Validate signature, expiry (`exp`), issuer (`https://accounts.google.com`), and audience (`aud` matching the configured Google Client ID).
    - Use the Google Auth Library (`com.google.auth:google-auth-library-oauth2-http` or `google-api-client`) or a standard JWT validator verifying Google's public JWKS (`https://www.googleapis.com/oauth2/v3/certs`).
- **Request Context & Authorization**:
  - Implement a `ContainerRequestFilter` (or HTTP security filter) that parses the `Authorization: Bearer <token>` header, validates the identity, and injects a `SecurityContext` containing user details (email, sub/user ID, roles).
  - Dev Endpoints & Swagger: Expose public access to OpenAPI/Swagger UI endpoints and health checks while enforcing OAuth2 verification on all domain endpoints.

