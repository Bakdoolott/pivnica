INSERT INTO user_tb (phone_number, tg_user_name, first_name, last_name, chat_id, enable, email)
VALUES ('+996000000000', 'bot', 'bot', 'bot', '5728267324', false, NULL);

INSERT INTO user_roles (user_id, role)
VALUES (2, 'OWNER'),
       (2, 'ADMIN'),
       (2, 'USER');

INSERT INTO temporary_code_tb(id_user_db, code, created_at, expires_at, enable)
VALUES (2, '1234', '2026-08-31 14:50:56.187364' ,'2026-08-31 14:52:56.187364', 'true');