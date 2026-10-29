# mgs-claude-code

A Spring Boot 4 REST API for managing people (CRUD), backed by an in-memory H2 database.

## Requirements

- Java 17+

## Running

```powershell
.\mvnw.cmd spring-boot:run
```

The app starts on `http://localhost:8080`.

> H2 is in-memory: all data is lost when the app stops.

## API documentation

While the app is running:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI spec (JSON):** http://localhost:8080/v3/api-docs

## Endpoints

| Method | Path                | Description         | Responses               |
|--------|---------------------|---------------------|-------------------------|
| GET    | `/api/persons`      | List all people     | 200                     |
| GET    | `/api/persons/{id}` | Get a person        | 200, 404                |
| POST   | `/api/persons`      | Create a person     | 201, 400, 409           |
| PUT    | `/api/persons/{id}` | Update a person     | 200, 400, 404, 409      |
| DELETE | `/api/persons/{id}` | Delete a person     | 204, 404                |
| GET    | `/api/health`       | Health check        | 200, 503                |

`/api/health` is the only health check endpoint. It returns `{"status":"UP","timestamp":"..."}`, aggregates the Spring Boot Actuator health checks (including the database), and returns `503` when any of them is down. Actuator's own HTTP endpoints (`/actuator/**`) are disabled via `management.server.port=-1`.

### Request body (POST / PUT)

```json
{
  "firstName": "Alice",
  "lastName": "Smith",
  "email": "alice@example.com",
  "birthDate": "1990-05-01"
}
```

| Field       | Rules                                   |
|-------------|-----------------------------------------|
| `firstName` | Required, max 100 characters            |
| `lastName`  | Required, max 100 characters            |
| `email`     | Required, valid email, unique, max 255  |
| `birthDate` | Optional, `yyyy-MM-dd`, must be in past |

Responses return the same fields plus `id`. A `400` means a field failed validation; a `409` means the email is already used by another person.

### Example

```powershell
curl.exe -X POST http://localhost:8080/api/persons `
  -H "Content-Type: application/json" `
  -d '{\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"email\":\"alice@example.com\",\"birthDate\":\"1990-05-01\"}'
```

## Project structure

```
src/main/java/com/mgs/claudecode/
├── config/       OpenAPI configuration
├── controller/   REST endpoints
├── service/      Business logic
├── repository/   Spring Data JPA repositories
├── entity/       JPA entities
├── dto/          Request/response records
└── exception/    Exceptions mapped to HTTP status codes
```

## Tests

```powershell
.\mvnw.cmd test
```
