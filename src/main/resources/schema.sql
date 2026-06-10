-- SQL Database Schema for Suraksha Shield (Insurance Policy Optimizer)
-- Corresponding to MongoDB collections: admins, users, policies

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS policies_db;
USE policies_db;

-- 1. Create admins table
CREATE TABLE IF NOT EXISTS admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Create policies table
CREATE TABLE IF NOT EXISTS policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    premium INT NOT NULL CHECK (premium >= 0),
    coverage INT NOT NULL CHECK (coverage >= 0),
    risk_level VARCHAR(50) NOT NULL,
    provider VARCHAR(255) NOT NULL,
    created_by_id BIGINT NULL,
    CONSTRAINT fk_policy_created_by FOREIGN KEY (created_by_id) REFERENCES admins(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Create user_policies (Many-to-Many Join Table)
CREATE TABLE IF NOT EXISTS user_policies (
    user_id BIGINT NOT NULL,
    policy_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, policy_id),
    CONSTRAINT fk_user_policies_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_policies_policy FOREIGN KEY (policy_id) REFERENCES policies(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
