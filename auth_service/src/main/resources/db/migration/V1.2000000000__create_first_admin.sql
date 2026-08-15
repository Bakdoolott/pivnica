INSERT INTO user_tb (phone_number, tg_user_name, first_name, last_name, chat_id, enable, email)
VALUES ('+996552209809', 'pascal1356', '123', NULL, '5728267324', false, NULL)
RETURNING id;

-- 2. Привязываем роль ADMIN (подставьте полученный id вместо <NEW_USER_ID>)
INSERT INTO user_roles (user_id, role)
VALUES (1, 'ADMIN');