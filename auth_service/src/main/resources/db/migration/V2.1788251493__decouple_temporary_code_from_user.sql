DELETE FROM temporary_code_tb;

DROP INDEX IF EXISTS uq_active_code_per_user;

ALTER TABLE temporary_code_tb
    DROP CONSTRAINT IF EXISTS temporary_code_tb_id_user_db_fkey,
    DROP COLUMN IF EXISTS id_user_db,
    ADD COLUMN phone_number varchar NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_active_code_per_phone
    ON temporary_code_tb (phone_number) WHERE enable = true;