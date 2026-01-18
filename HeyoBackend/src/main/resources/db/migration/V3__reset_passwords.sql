-- =====================================================
-- Reset user passwords to known value for development
-- Password: password123
-- BCrypt hash: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
-- =====================================================

UPDATE users 
SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG'
WHERE username IN ('admin', 'john_doe', 'jane_smith', 'mike_wilson');
