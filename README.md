# Information

> BoilerPlate for Spring Boot based on Java
>> [!WARNING] Before running this project, please follow the 'Setup' section to launch the docker images and set up the
> > database.

## Project Information

| Item        | Description     |     
|-------------|-----------------|
| Spring Boot | 3.5.8           |
| Language    | Java            |
| JDK         | 21              |
| Build Tool  | Gradle (Groovy) | 
| Packaging   | Jar             |
| Port        | 8080            | 

### Profiles

- Use `.yml`
- `local`, `dev`, `qa`, `stg`, `prd`

### Security

- Login: email and password
- Spring Security: Access token and Refresh token

---

# APIs

## Actuator

| URI              | Method | Description                                                                |
|------------------|--------|----------------------------------------------------------------------------|
| /actuator/health | GET    | Health check                                                               |
| /actuator/info   | GET    | Shows info properties (e.g., buildInfo, git info, or custom info.* fields) |

## Root

> Implement directly for cases where an actuator is not used.

| URI            | Method | Description                                                                |
|----------------|--------|----------------------------------------------------------------------------|
| /api/v1/health | GET    | Health check                                                               |
| /api/v1/info   | GET    | Shows info properties (e.g., buildInfo, git info, or custom info.* fields) |

## Users

| URI                | Method | Description                 |
|--------------------|--------|-----------------------------|
| /api/v1/users      | POST   | Register a new user         |
| /api/v1/users/{id} | GET    | Get user details            |
| /api/v1/users      | GET    | Get paginated list of users |
| /api/v1/users/{id} | PUT    | Update user profile         |
| /api/v1/users/{id} | DELETE | Remove user account         |

## Auth

| URI                  | Method | Description          |
|----------------------|--------|----------------------|
| /api/v1/auth/login   | POST   | Login and get tokens |
| /api/v1/auth/refresh | POST   | Get new tokens       |

## External Test

| URI                        | Method | Description                 |
|----------------------------|--------|-----------------------------|
| /api/v1/test/external      | POST   | Register a new item         |
| /api/v1/test/external/{id} | GET    | Get item details            |
| /api/v1/test/external      | GET    | Get paginated list of items |
| /api/v1/test/external/{id} | PUT    | Update item                 |
| /api/v1/test/external/{id} | DELETE | Remove item                 |

---

## Handle error(exception)

### Common

``` json
response: {
  Instant timestamp,
  int status,
  String errorCode,
  String message,
  String path,
  List<ValidationError> validationErrors
}

validationErrors: {
  String objectName,
  String field,
  String code,
  String message,
  Object rejectedValue
}
```

### User

    // 400
    EMAIL_TYPE_INVALID("USER_40001", HttpStatus.BAD_REQUEST,
            "EMAIL type is invalid."),

    // 409
    EMAIL_CONFLICT("USER_40901", HttpStatus.CONFLICT,
            "EMAIL is already in use."),

    // 404
    USER_NOT_FOUND("USER_40401", HttpStatus.NOT_FOUND,
            "User not found.");

---

# Test

## http test (IntelliJ) `/test/http`

| File                | Description                    |
|---------------------|--------------------------------|
| `001_root-api.http` | http test for `RootController` |
| `002_user-api.http` | http test for `UserController` |

## http request test (External)

- https://github.com/YounghwanJi/t-test-simple-server

| URI                          | Method | Description                          |
|------------------------------|--------|--------------------------------------|
| /api/items                   | POST   | Register a new item (201 Created)    |
| /api/items/{id}              | GET    | Get item details (200 OK)            |
| /api/items?limit=10&offset=0 | GET    | Get paginated list of items (200 OK) |
| /api/items/{id}              | PUT    | Update item (200 OK)                 |
| /api/items/{id}              | DELETE | Remove item (200 OK)                 |

## http-client.private.env.json

> Unversioned Files (in .gitignore)

``` json
{
  "dev": {
    "authToken": "dev-token-here"
  },
  "qa": {
    "authToken": "qa-token-here"
  },
  "stg": {
    "authToken": "stg-token-here"
  },
  "prd": {
    "authToken": "prd-token-here"
  }
}
```

---

# Setup

## Docker

### Starting the docker compose stack.

``` bash
# Use /docs/integration/docker-compose.yml
$ docker-compose up -d # run
$ docker compose down # stop
$ docker-compose down -v # stop and remove data
```

#### PostgreSQL as a single container

``` bash
 # Use /docs/progresql/docker-compose.yml
 $ docker-compose up -d # run
 $ docker compose down # stop
 $ docker-compose down -v # stop and remove data
 
 # connect to container
 $ docker exec -it my-postgres psql -U myuser -d mydb
 # connect to db directly
 $ psql -h localhost -p 5432 -U myuser -d mydb
```

#### Redis as a single container

``` bash
 # Use /docs/redis/docker-compose.yml
 $ docker-compose up -d # run
 $ docker compose down # stop
 $ docker-compose down -v # stop and remove data
 
 # connect to container
 $ docker exec -it my-redis sh
 # connect to redis directly
 $ docker exec -it my-redis redis-cli -a "myredispassword"
```

#### Hashicorp vault as a single container

``` bash
 # Use /docs/hashcorp_vault/docker-compose.yml
 $ docker-compose up -d # run
 $ docker compose down # stop
 $ docker-compose down -v # stop and remove data
```

## DB (PostgreSQL)

### `users` table

```sql
CREATE TABLE users
(
    id           BIGSERIAL PRIMARY KEY,
    role         VARCHAR(20)  NOT NULL DEFAULT 'USER',
    email        VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    name         VARCHAR(50)  NOT NULL,
    phone_number VARCHAR(20)  NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL
);

-- 이메일 검색 성능 향상을 위한 인덱스 (UNIQUE 제약조건으로 자동 생성되지만 명시적 표현)
-- CREATE INDEX idx_users_email ON users(email);

-- status로 검색이 많다면 인덱스 추가
-- CREATE INDEX idx_users_status ON users(status);

-- 생성일자로 정렬/검색이 많다면 인덱스 추가
-- CREATE INDEX idx_users_created_at ON users(created_at);
```

`20251221`: role 추가, password 길이 변경 (암호화)

```sql
ALTER TABLE users
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
ALTER TABLE users
ALTER
COLUMN password TYPE VARCHAR(255);
```

`20251221`: 양방향 암호화를 위한 컬럼 길이 변경.

```sql
ALTER TABLE public.users
ALTER
COLUMN email TYPE VARCHAR(255),
ALTER
COLUMN name TYPE VARCHAR(255),
ALTER
COLUMN phone_number TYPE VARCHAR(255);
```

### `refresh_tokens` table

```sql
CREATE TABLE refresh_tokens
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(500) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    CONSTRAINT fk_refresh_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);
```

---

# Documentation

## Swagger

### Entry point

- /api-docs
- /swagger-ui.html
- /swagger-ui/index.html

### Separation

- primary: User-API
- secondary: Dev-API

---

# ETC

## Logging

- logback (resources/logging/)
- rolling
    - common log - 200MB, total 100GB, 90 days
    - error log - 200MB, total 50GB, 180 days
- `type` by profile
    - `string`: local
    - `json`: dev, qa, stg, prd