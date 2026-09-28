-- Via Bank - MySQL 8+
CREATE DATABASE IF NOT EXISTS via_bank
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE via_bank;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    email VARCHAR(160) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    birth_date DATE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    address VARCHAR(180) NULL,
    city VARCHAR(100) NULL,
    state VARCHAR(2) NULL,
    zip_code VARCHAR(8) NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_cpf UNIQUE (cpf),
    CONSTRAINT uk_users_phone UNIQUE (phone)
) ENGINE=InnoDB;

CREATE TABLE bank_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    agency VARCHAR(8) NOT NULL DEFAULT '0001',
    account_number VARCHAR(20) NOT NULL,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    yield_label VARCHAR(120) NULL,
    version BIGINT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_bank_accounts_user UNIQUE (user_id),
    CONSTRAINT uk_bank_accounts_number UNIQUE (account_number),
    CONSTRAINT fk_bank_accounts_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE pix_keys (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    key_type VARCHAR(20) NOT NULL,
    key_value VARCHAR(180) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_pix_keys_value UNIQUE (key_value),
    CONSTRAINT fk_pix_keys_account FOREIGN KEY (account_id) REFERENCES bank_accounts(id),
    INDEX idx_pix_keys_account (account_id)
) ENGINE=InnoDB;

CREATE TABLE transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    counterparty_account_id BIGINT NULL,
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255) NULL,
    category VARCHAR(80) NULL,
    transfer_reference VARCHAR(36) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES bank_accounts(id),
    CONSTRAINT fk_transactions_counterparty FOREIGN KEY (counterparty_account_id) REFERENCES bank_accounts(id),
    INDEX idx_transactions_account_created (account_id, created_at),
    INDEX idx_transactions_transfer_ref (transfer_reference)
) ENGINE=InnoDB;

CREATE TABLE pix_favorites (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    pix_key VARCHAR(180) NOT NULL,
    bank_name VARCHAR(120) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_pix_favorites_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_pix_favorites_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE cards (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    brand VARCHAR(30) NOT NULL,
    last4 VARCHAR(4) NOT NULL,
    holder_name VARCHAR(120) NOT NULL,
    expiration VARCHAR(5) NOT NULL,
    blocked BOOLEAN NOT NULL DEFAULT FALSE,
    limit_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    used_limit DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    product_name VARCHAR(80) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_cards_account FOREIGN KEY (account_id) REFERENCES bank_accounts(id),
    INDEX idx_cards_account (account_id)
) ENGINE=InnoDB;

CREATE TABLE investment_products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255) NOT NULL,
    risk VARCHAR(20) NOT NULL,
    return_label VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE investments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    principal DECIMAL(19,2) NOT NULL,
    current_value DECIMAL(19,2) NOT NULL,
    monthly_return DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_investments_account FOREIGN KEY (account_id) REFERENCES bank_accounts(id),
    CONSTRAINT fk_investments_product FOREIGN KEY (product_id) REFERENCES investment_products(id),
    INDEX idx_investments_account (account_id)
) ENGINE=InnoDB;

CREATE TABLE user_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    compact_mode BOOLEAN NOT NULL DEFAULT FALSE,
    notifications BOOLEAN NOT NULL DEFAULT TRUE,
    hide_balance BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_user_settings_user UNIQUE (user_id),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE devices (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    user_agent VARCHAR(500) NULL,
    language VARCHAR(30) NULL,
    platform VARCHAR(100) NULL,
    screen_width INT NULL,
    screen_height INT NULL,
    timezone VARCHAR(80) NULL,
    last_seen_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_devices_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_devices_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE revoked_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    jti VARCHAR(36) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    revoked_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_revoked_tokens_jti UNIQUE (jti),
    INDEX idx_revoked_tokens_expires (expires_at)
) ENGINE=InnoDB;
