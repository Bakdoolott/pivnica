CREATE TABLE IF NOT EXISTS users(
    id bigserial primary key,
    name varchar,
    phone varchar unique not null,
    chat_id bigint not null
);

CREATE TABLE IF NOT EXISTS roles(
    id bigserial primary key,
    role_name varchar not null unique
);

INSERT INTO roles(role_name)
VALUES ('ADMIN'),
       ('USER');

CREATE TABLE IF NOT EXISTS m2m_users_roles(
    user_id bigint references users(id),
    role_id bigint references roles(id)
);