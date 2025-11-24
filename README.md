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

---

# Test

## http test (IntelliJ) `/test/http`

| File                | Description                    |
|---------------------|--------------------------------|
| `001_root-api.http` | http test for `RootController` |

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

## DB (PostgreSQL)

### `users` table

```sql
CREATE TABLE users
(
    id           BIGSERIAL PRIMARY KEY,
    email        VARCHAR(50) NOT NULL UNIQUE,
    password     VARCHAR(50) NOT NULL,
    name         VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    status       VARCHAR(20) NOT NULL,
    created_at   TIMESTAMP   NOT NULL,
    updated_at   TIMESTAMP   NOT NULL
);

-- 이메일 검색 성능 향상을 위한 인덱스 (UNIQUE 제약조건으로 자동 생성되지만 명시적 표현)
-- CREATE INDEX idx_users_email ON users(email);

-- status로 검색이 많다면 인덱스 추가
-- CREATE INDEX idx_users_status ON users(status);

-- 생성일자로 정렬/검색이 많다면 인덱스 추가
-- CREATE INDEX idx_users_created_at ON users(created_at);
```
