# docker-compose commands

### 모든 서비스 시작

```bash
docker-compose up -d
```

### 특정 서비스만 시작

```bash
docker-compose up -d postgres
docker-compose up -d redis
```

### 특정 서비스만 재시작

```bash
docker-compose restart postgres
docker-compose restart redis
```

### 로그 확인

```bash
docker-compose logs -f
```

### 특정 서비스 로그만 확인

```bash
docker-compose logs -f postgres
docker-compose logs -f redis
```

### 상태 확인

```bash
docker-compose ps
```

### 리소스 사용량 확인

```bash
docker stats my-postgres my-redis
```

### 중지

```bash
docker-compose down
```

### 특정 서비스만 중지

```bash
docker-compose stop redis
```

### 데이터까지 삭제

```bash
docker-compose down -v
```

### 네트워크 확인

```bash
docker network ls
docker network inspect <network-name>
```

# 운영 시

- 비밀번호를 환경변수로 변경 `"mypassword"` -> `${MY_PASSWORD}` (`.env` 분리)

## Hyper-V의 가상 네트워킹이 port를 예약 중인지 확인. (이 경우 포트 변경 필요)

``` bash
netsh int ipv4 show excludedportrange protocol=tcp
```