CREATE DATABASE auth_db;
CREATE DATABASE core_db;
-- CREATE DATABASE payment_db;


CREATE USER auth_user WITH PASSWORD 'auth_strong_pass_123';
CREATE USER core_user WITH PASSWORD 'core_strong_pass_456';
-- CREATE USER payment_user WITH PASSWORD 'payment_strong_pass_789';

GRANT CONNECT ON DATABASE auth_db TO auth_user;
GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;

GRANT CONNECT ON DATABASE core_db TO core_user;
GRANT ALL PRIVILEGES ON DATABASE core_db TO core_user;

-- GRANT CONNECT ON DATABASE payment_db TO payment_user;
-- GRANT ALL PRIVILEGES ON DATABASE payment_db TO payment_user;