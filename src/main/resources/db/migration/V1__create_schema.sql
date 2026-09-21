CREATE TABLE venues (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE artists (
    id BIGSERIAL PRIMARY KEY,
    stage_name VARCHAR(150) NOT NULL UNIQUE,
    country VARCHAR(100) NOT NULL,
    genre VARCHAR(80) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    event_code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    category VARCHAR(30) NOT NULL CHECK (category IN ('MUSIC','SPORTS','TECHNOLOGY','EDUCATION','CULTURE','ENTERTAINMENT')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('DRAFT','PUBLISHED','SOLD_OUT','CANCELLED','FINISHED')),
    event_date DATE NOT NULL,
    minimum_age INTEGER NOT NULL,
    venue_id BIGINT NOT NULL,
    CONSTRAINT fk_event_venue FOREIGN KEY (venue_id) REFERENCES venues(id)
);

CREATE TABLE event_artists (
    event_id BIGINT NOT NULL,
    artist_id BIGINT NOT NULL,
    PRIMARY KEY (event_id, artist_id),
    CONSTRAINT fk_event_artists_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_event_artists_artist FOREIGN KEY (artist_id) REFERENCES artists(id)
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    city VARCHAR(100),
    birth_date DATE,
    user_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    ticket_code VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(30) NOT NULL CHECK (type IN ('GENERAL','VIP','BACKSTAGE','STUDENT')),
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    status VARCHAR(30) NOT NULL CHECK (status IN ('RESERVED','PAID','CANCELLED','USED')),
    purchase_date TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    CONSTRAINT fk_ticket_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ticket_event FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE INDEX idx_events_venue ON events(venue_id);
CREATE INDEX idx_events_status_date ON events(status, event_date);
CREATE INDEX idx_tickets_user_status ON tickets(user_id, status);
CREATE INDEX idx_tickets_event_status ON tickets(event_id, status);
