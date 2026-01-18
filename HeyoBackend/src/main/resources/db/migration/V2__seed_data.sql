-- =====================================================
-- Heyo Social Media Application - Seed Data for Development
-- Version: 2
-- Password for all users: password123
-- BCrypt hash generated with strength 10
-- =====================================================

-- Clean existing data (for development only - remove in production)
DELETE FROM chat_messages;
DELETE FROM conversations;
DELETE FROM notifications;
DELETE FROM event_participants;
DELETE FROM event_interested;
DELETE FROM events;
DELETE FROM comments;
DELETE FROM post_likes;
DELETE FROM posts;
DELETE FROM user_friends;
DELETE FROM profiles;
DELETE FROM users;

-- Reset auto-increment
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE profiles AUTO_INCREMENT = 1;
ALTER TABLE posts AUTO_INCREMENT = 1;
ALTER TABLE comments AUTO_INCREMENT = 1;
ALTER TABLE events AUTO_INCREMENT = 1;
ALTER TABLE notifications AUTO_INCREMENT = 1;
ALTER TABLE conversations AUTO_INCREMENT = 1;
ALTER TABLE chat_messages AUTO_INCREMENT = 1;

-- -----------------------------------------------------
-- Sample Users (password: password123)
-- BCrypt hash: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
-- -----------------------------------------------------
INSERT INTO users (username, email, password, avatar_url, is_online, role, created_at, updated_at)
VALUES 
    ('admin', 'admin@heyo.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 
     'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', false, 'ADMIN', NOW(), NOW()),
    ('john_doe', 'john@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 
     'https://api.dicebear.com/7.x/avataaars/svg?seed=john', false, 'USER', NOW(), NOW()),
    ('jane_smith', 'jane@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 
     'https://api.dicebear.com/7.x/avataaars/svg?seed=jane', false, 'USER', NOW(), NOW()),
    ('mike_wilson', 'mike@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 
     'https://api.dicebear.com/7.x/avataaars/svg?seed=mike', false, 'USER', NOW(), NOW());

-- -----------------------------------------------------
-- Sample Profiles
-- -----------------------------------------------------
INSERT INTO profiles (user_id, first_name, last_name, bio, date_of_birth, location, website)
VALUES 
    (1, 'Admin', 'User', 'System administrator', '1990-01-01', 'System', NULL),
    (2, 'John', 'Doe', 'Software developer passionate about coding and technology.', 
     '1995-05-15', 'New York, USA', 'https://johndoe.dev'),
    (3, 'Jane', 'Smith', 'UX Designer | Coffee lover | Creative thinker', 
     '1993-08-22', 'San Francisco, USA', 'https://janesmith.design'),
    (4, 'Mike', 'Wilson', 'Tech enthusiast and gamer. Building the future one line of code at a time.', 
     '1998-12-03', 'Austin, USA', NULL);

-- -----------------------------------------------------
-- Sample Friendships
-- -----------------------------------------------------
INSERT INTO user_friends (user_id, friend_id)
VALUES 
    (2, 3), (3, 2),
    (2, 4), (4, 2),
    (3, 4), (4, 3);

-- -----------------------------------------------------
-- Sample Posts
-- -----------------------------------------------------
INSERT INTO posts (author_id, content, image_url, created_at, updated_at)
VALUES 
    (2, 'Just finished a great coding session! Built a new feature using Angular and Spring Boot. #coding #webdev', 
     NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (3, 'Designed a beautiful new dashboard today. Love when creativity meets functionality!', 
     'https://picsum.photos/seed/design/800/600', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (4, 'Anyone else excited about the new tech conference next month? Cant wait to attend!', 
     NULL, DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 12 HOUR)),
    (2, 'Working on Heyo - a new social media platform. Stay tuned for updates!', 
     'https://picsum.photos/seed/heyo/800/600', DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR));

-- -----------------------------------------------------
-- Sample Post Likes
-- -----------------------------------------------------
INSERT INTO post_likes (post_id, user_id)
VALUES 
    (1, 3), (1, 4),
    (2, 2), (2, 4),
    (3, 2), (3, 3),
    (4, 3), (4, 4);

-- -----------------------------------------------------
-- Sample Comments
-- -----------------------------------------------------
INSERT INTO comments (post_id, author_id, content, created_at)
VALUES 
    (1, 3, 'Great work! Angular and Spring Boot is a powerful combination.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (1, 4, 'Love it! Would love to see what you built.', DATE_SUB(NOW(), INTERVAL 23 HOUR)),
    (2, 2, 'This looks amazing! Can you share some design tips?', DATE_SUB(NOW(), INTERVAL 20 HOUR)),
    (4, 3, 'Excited to try it out! When is the beta launch?', DATE_SUB(NOW(), INTERVAL 5 HOUR));

-- -----------------------------------------------------
-- Sample Events
-- -----------------------------------------------------
INSERT INTO events (title, description, image_url, event_date, location, hashtags, creator_id, created_at)
VALUES 
    ('Tech Meetup 2026', 'Join us for an amazing tech meetup! We will discuss the latest trends in web development, AI, and more.',
     'https://picsum.photos/seed/meetup/800/400', DATE_ADD(NOW(), INTERVAL 30 DAY), 'Tech Hub, Downtown', 
     '#tech #meetup #developers #networking', 2, NOW()),
    ('Design Workshop', 'Learn the fundamentals of UX/UI design in this hands-on workshop.',
     'https://picsum.photos/seed/workshop/800/400', DATE_ADD(NOW(), INTERVAL 14 DAY), 'Creative Space, San Francisco',
     '#design #ux #ui #workshop', 3, NOW());

-- -----------------------------------------------------
-- Sample Event Interest/Participation
-- -----------------------------------------------------
INSERT INTO event_interested (event_id, user_id)
VALUES 
    (1, 3), (1, 4),
    (2, 2), (2, 4);

INSERT INTO event_participants (event_id, user_id)
VALUES 
    (1, 2),
    (2, 3);

-- -----------------------------------------------------
-- Sample Notifications
-- -----------------------------------------------------
INSERT INTO notifications (user_id, actor_id, type, message, reference_id, is_read, created_at)
VALUES 
    (2, 3, 'NEW_LIKE', 'Jane Smith liked your post', 1, false, DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (2, 4, 'NEW_COMMENT', 'Mike Wilson commented on your post', 1, false, DATE_SUB(NOW(), INTERVAL 23 HOUR)),
    (3, 2, 'NEW_LIKE', 'John Doe liked your post', 2, true, DATE_SUB(NOW(), INTERVAL 20 HOUR)),
    (2, 3, 'NEW_COMMENT', 'Jane Smith commented on your post', 4, false, DATE_SUB(NOW(), INTERVAL 5 HOUR));

-- -----------------------------------------------------
-- Sample Conversation and Messages
-- -----------------------------------------------------
INSERT INTO conversations (user1_id, user2_id, last_message_at, created_at)
VALUES 
    (2, 3, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO chat_messages (conversation_id, sender_id, receiver_id, content, is_read, created_at)
VALUES 
    (1, 2, 3, 'Hey Jane! How is the new design project going?', true, DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (1, 3, 2, 'Hi John! It is going great, thanks for asking. Almost done with the mockups.', true, DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (1, 2, 3, 'Awesome! Let me know if you need any help with the implementation.', true, DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (1, 3, 2, 'Will do! By the way, are you going to the Tech Meetup?', false, DATE_SUB(NOW(), INTERVAL 1 HOUR));
