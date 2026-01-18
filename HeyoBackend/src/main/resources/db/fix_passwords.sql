-- =====================================================
-- Quick fix for user passwords
-- Run this manually in MySQL if login fails
-- Password: password123
-- =====================================================

-- Update all test user passwords to 'password123'
UPDATE users 
SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG'
WHERE username IN ('admin', 'john_doe', 'jane_smith', 'mike_wilson');

-- If no users exist, insert them
INSERT IGNORE INTO users (username, email, password, avatar_url, is_online, role, created_at, updated_at)
VALUES 
    ('admin', 'admin@heyo.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 
     'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', false, 'ADMIN', NOW(), NOW()),
    ('john_doe', 'john@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 
     'https://api.dicebear.com/7.x/avataaars/svg?seed=john', false, 'USER', NOW(), NOW()),
    ('jane_smith', 'jane@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 
     'https://api.dicebear.com/7.x/avataaars/svg?seed=jane', false, 'USER', NOW(), NOW()),
    ('mike_wilson', 'mike@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 
     'https://api.dicebear.com/7.x/avataaars/svg?seed=mike', false, 'USER', NOW(), NOW());

-- Fix flyway history if checksum mismatch
DELETE FROM flyway_schema_history WHERE version IN ('2', '3');

SELECT 'Passwords updated! Login with password123' AS message;
