CREATE TABLE IF NOT EXISTS user_tb(
     id bigserial primary key,
     phone_number varchar unique not null,
     tg_user_name varchar,
     first_name varchar,
     last_name varchar,
     chat_id bigint not null,
     enable boolean not null default true
 );

CREATE TABLE IF NOT EXISTS user_roles(
    user_id bigint not null references user_tb(id),
    role varchar not null
);

CREATE TABLE IF NOT EXISTS temporary_code_tb(
    id bigserial primary key,
    id_user_db bigint not null references user_tb(id),
    code varchar not null,
    created_at timestamp not null default now(),
    expires_at timestamp not null,
    attempts int not null default 0,
    enable boolean not null default true
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_active_code_per_user
    ON temporary_code_tb (id_user_db) WHERE enable = true;