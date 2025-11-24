# Information

> BoilerPlate for Spring Boot based on Java

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

# Test

## http test (IntelliJ) `/test/http`

| File                | Description                    |
|---------------------|--------------------------------|
| `001_root-api.http` | http test for `RootController` |
