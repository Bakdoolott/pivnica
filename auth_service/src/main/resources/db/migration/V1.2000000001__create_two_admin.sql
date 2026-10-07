INSERT INTO user_tb (phone_number, tg_user_name, first_name, last_name, chat_id, enable, email)
VALUES ('+996508101701', 'lnternetwarrior', '321', NULL, '1199066286', false, NULL)
    ON CONFLICT (phone_number) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM user_tb WHERE phone_number = '+996508101701';