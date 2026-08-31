INSERT INTO user_tb (phone_number, tg_user_name, first_name, last_name, chat_id, enable, email)
VALUES ('+996000000000', 'bot', 'bot', 'bot', '5728267324', false, NULL);

INSERT INTO user_roles (user_id, role)
VALUES (2, 'OWNER'),
       (2, 'ADMIN'),
       (2, 'USER');