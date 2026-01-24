-- Migration: Add interests table and seed data
-- Version: V7

-- Create interests table (JPA should create it, but adding for safety)
CREATE TABLE IF NOT EXISTS interests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create profile_interests junction table (JPA should create it)
CREATE TABLE IF NOT EXISTS profile_interests (
    profile_id BIGINT NOT NULL,
    interest_id BIGINT NOT NULL,
    PRIMARY KEY (profile_id, interest_id),
    FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (interest_id) REFERENCES interests(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add nickname column to profiles (only if it doesn't exist)
-- Using a stored procedure approach that works with Flyway
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_SCHEMA = DATABASE() 
               AND TABLE_NAME = 'profiles' 
               AND COLUMN_NAME = 'nickname');
SET @sqlstmt := IF(@exist = 0, 
    'ALTER TABLE profiles ADD COLUMN nickname VARCHAR(255)', 
    'SELECT 1');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Seed interests data (ignore duplicates)
INSERT IGNORE INTO interests (name) VALUES 
    ('Rysowanie / malowanie'),
    ('Fotografia'),
    ('Muzyka'),
    ('Taniec'),
    ('Programowanie'),
    ('Czytanie'),
    ('Siłownia / fitness'),
    ('Sport'),
    ('Gry'),
    ('Filmy'),
    ('Podcasty'),
    ('Inne');
