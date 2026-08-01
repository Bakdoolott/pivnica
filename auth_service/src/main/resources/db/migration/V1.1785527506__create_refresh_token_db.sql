CREATE TABLE IF NOT EXISTS refresh_token_tb(
    id bigserial primary key,
    user_id bigint not null references user_tb(id),
    token_hash varchar not null unique,
    expires_at timestamp not null,
    revoked boolean not null default false,
    created_at timestamp not null default now()
);
