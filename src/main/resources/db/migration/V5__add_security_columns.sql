CREATE TABLE IF NOT EXISTS user_credentials (
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    CONSTRAINT pk_user_credentials PRIMARY KEY (user_id),
    CONSTRAINT fk_user_credentials_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(30) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT ck_user_roles_role CHECK (role IN ('USER', 'STAFF', 'ADMIN'))
);

CREATE INDEX IF NOT EXISTS idx_user_roles_user ON user_roles(user_id);

INSERT INTO user_roles (user_id, role)
SELECT id, 'USER'
FROM users
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles WHERE user_roles.user_id = users.id
);
