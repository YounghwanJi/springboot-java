CREATE TABLE users
(
    id           BIGSERIAL PRIMARY KEY,
    role         VARCHAR(20)  NOT NULL DEFAULT 'USER',
    email        VARCHAR(255)  NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    name         VARCHAR(255)  NOT NULL,
    phone_number VARCHAR(255)  NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL
);

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