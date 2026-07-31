CREATE TABLE IF NOT EXISTS hall_tb (
                                       id BIGSERIAL PRIMARY KEY,
                                       floor INT NOT NULL UNIQUE,
                                       hall_number VARCHAR(255) NOT NULL UNIQUE,
    hall_status VARCHAR(20) NOT NULL DEFAULT 'ENABLE'
    );

CREATE TABLE IF NOT EXISTS table_tb (
                                        id BIGSERIAL PRIMARY KEY,
                                        table_number INT NOT NULL,
                                        x INT,
                                        y INT,
                                        place_county INT NOT NULL,
                                        id_hall_tb BIGINT NOT NULL REFERENCES hall_tb(id) ON DELETE CASCADE,
    table_status VARCHAR(20) DEFAULT 'FREE'
    );


CREATE TABLE IF NOT EXISTS book_tb (
                                       id BIGSERIAL PRIMARY KEY,
                                       user_id BIGINT NOT NULL,
                                       date_time TIMESTAMP NOT NULL,
                                       payment_status VARCHAR(20) NOT NULL DEFAULT 'FAILED',
    id_table_tb BIGINT NOT NULL REFERENCES table_tb(id) ON DELETE CASCADE,
    user_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL
    );



CREATE TABLE IF NOT EXISTS event (
                                     id BIGSERIAL PRIMARY KEY,
                                     title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    image_name VARCHAR(255),
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP,
    created_by VARCHAR(255),
    event_status VARCHAR(20) NOT NULL DEFAULT 'ENABLE'
    );

CREATE INDEX idx_table_hall ON table_tb(id_hall_tb);
CREATE INDEX idx_booking_table ON book_tb(id_table_tb);
CREATE INDEX idx_booking_user ON book_tb(user_id);
CREATE INDEX idx_event_starts ON event(starts_at);