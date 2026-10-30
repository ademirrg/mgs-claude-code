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

## Authentication

The Person endpoints require a JWT. Get one from `POST /api/auth/login` and send it as `Authorization: Bearer <token>`. Tokens are signed with HS256 and expire after 1 hour.

There are two built-in accounts:

| Username | Default password | Role    | Can do                              |
|----------|------------------|---------|-------------------------------------|
| `admin`  | `admin123`       | `ADMIN` | Read, create, update, delete people |
| `user`   | `user123`        | `USER`  | Read people                         |

The defaults are for local development only. Override them with environment variables:

| Variable         | Purpose                                     |
|------------------|---------------------------------------------|
| `JWT_SECRET`     | HS256 signing key, at least 32 bytes        |
| `ADMIN_PASSWORD` | Password for `admin`                        |
| `USER_PASSWORD`  | Password for `user`                         |

A missing, invalid or expired token returns `401`; a valid token without the required role returns `403`. `/api/auth/login`, `/api/health` and the API docs are public. In Swagger UI, click **Authorize** and paste the token.

```powershell
curl.exe -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"admin\",\"password\":\"admin123\"}'
# {"accessToken":"eyJ...","tokenType":"Bearer","expiresIn":3600}
```

## Endpoints

| Method | Path                | Description         | Role         | Responses                    |
|--------|---------------------|---------------------|--------------|------------------------------|
| POST   | `/api/auth/login`   | Get an access token | Public       | 200, 400, 401                |
| GET    | `/api/persons`      | List all people     | USER, ADMIN  | 200, 401, 403                |
| GET    | `/api/persons/{id}` | Get a person        | USER, ADMIN  | 200, 401, 403, 404           |
| POST   | `/api/persons`      | Create a person     | ADMIN        | 201, 400, 401, 403, 409      |
| PUT    | `/api/persons/{id}` | Update a person     | ADMIN        | 200, 400, 401, 403, 404, 409 |
| DELETE | `/api/persons/{id}` | Delete a person     | ADMIN        | 204, 401, 403, 404           |
| GET    | `/api/health`       | Health check        | Public       | 200, 503                     |

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
  -H "Authorization: Bearer <token>" `
  -H "Content-Type: application/json" `
  -d '{\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"email\":\"alice@example.com\",\"birthDate\":\"1990-05-01\"}'
```

## Postman collection

`docs/postman/` holds a Postman collection covering every endpoint, with test assertions, and a local environment. Import both into Postman, select **mgs-claude-code local**, and run the collection in order: **Auth** stores the admin and user tokens that later requests use.

To run it from the command line while the app is running:

```powershell
cd docs/postman
postman collection run mgs-claude-code.postman_collection.json -e mgs-claude-code-local.postman_environment.json
```

## Project structure

```
src/main/java/com/mgs/claudecode/
├── config/       OpenAPI and Spring Security/JWT configuration
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
