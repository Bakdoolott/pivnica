INSERT INTO user_tb (phone_number, tg_user_name, first_name, last_name, chat_id, enable, email)
VALUES ('+996508101701', 'lnternetwarrior', '321', NULL, '1199066286', false, NULL)
    RETURNING id;

-- 2. Привязываем роль ADMIN (подставьте полученный id вместо <NEW_USER_ID>)
INSERT INTO user_roles (user_id, role)
VALUES (2, 'ADMIN');