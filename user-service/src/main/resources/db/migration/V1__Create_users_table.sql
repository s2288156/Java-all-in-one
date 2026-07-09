CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keycloak_id VARCHAR(36) UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    username VARCHAR(255) NOT NULL,
    created_time DATETIME,
    updated_time DATETIME,
    INDEX idx_email (email),
    INDEX idx_keycloak_id (keycloak_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
