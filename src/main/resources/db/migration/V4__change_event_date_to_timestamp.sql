ALTER TABLE events
    ALTER COLUMN event_date TYPE TIMESTAMP WITHOUT TIME ZONE
    USING event_date::timestamp;