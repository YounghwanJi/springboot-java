-- 테스트용 사용자 데이터
-- 비밀번호: "password123" (BCrypt 해시)

-- 기존 데이터 삭제 (외래키 순서 고려)
TRUNCATE TABLE refresh_tokens CASCADE;
TRUNCATE TABLE users CASCADE;

-- ADMIN 사용자 (ID: 1)
INSERT INTO users (id, role, email, password, name, phone_number, status, created_at, updated_at)
VALUES (1,
        'ADMIN',
        'admin@test.com',
        '$2a$10$BCuf8uL.9jKI2hCQJlpKKOK4HIFJJYkF5sVUoyOqoPP8zyGLka08S',  -- password123
        'Admin User',  -- 실제로는 암호화되어 저장됨
        '010-1234-5678',  -- 실제로는 암호화되어 저장됨
        'ACTIVE',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- USER 사용자 (ID: 2)
INSERT INTO users (id, role, email, password, name, phone_number, status, created_at, updated_at)
VALUES (2,
        'USER',
        'user@test.com',
        '$2a$10$BCuf8uL.9jKI2hCQJlpKKOK4HIFJJYkF5sVUoyOqoPP8zyGLka08S',  -- password123
        'Test User',
        '010-9876-5432',
        'ACTIVE',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- INACTIVE 사용자 (ID: 3)
INSERT INTO users (id, role, email, password, name, phone_number, status, created_at, updated_at)
VALUES (3,
        'USER',
        'inactive@test.com',
        '$2a$10$BCuf8uL.9jKI2hCQJlpKKOK4HIFJJYkF5sVUoyOqoPP8zyGLka08S',  -- password123
        'Inactive User',
        '010-5555-5555',
        'INACTIVE',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- MANAGER 사용자 (ID: 4)
INSERT INTO users (id, role, email, password, name, phone_number, status, created_at, updated_at)
VALUES (4,
        'MANAGER',
        'manager@test.com',
        '$2a$10$BCuf8uL.9jKI2hCQJlpKKOK4HIFJJYkF5sVUoyOqoPP8zyGLka08S',  -- password123
        'Manager User',
        '010-1111-2222',
        'ACTIVE',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- ID 시퀀스 재설정 (새로운 테스트 데이터는 100부터 시작)
ALTER SEQUENCE users_id_seq RESTART WITH 100;
ALTER SEQUENCE refresh_tokens_id_seq RESTART WITH 100;
