-- =====================================================
-- Heyo Social Media Application - Initial Database Schema
-- Version: 1
-- =====================================================

-- -----------------------------------------------------
-- Table: users
-- Main user table with authentication details
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    is_online BOOLEAN DEFAULT FALSE,
    last_seen DATETIME,
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for faster login lookups
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- -----------------------------------------------------
-- Table: profiles
-- Extended user profile information
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    bio TEXT,
    date_of_birth DATE,
    location VARCHAR(255),
    website VARCHAR(500),
    phone_number VARCHAR(20),
    
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_profiles_user_id UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table: user_friends
-- Self-referencing many-to-many for user friendships
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS user_friends (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT fk_user_friends_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_friends_friend FOREIGN KEY (friend_id) 
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for reverse lookups (find users who have this user as friend)
CREATE INDEX idx_user_friends_friend ON user_friends(friend_id);

-- -----------------------------------------------------
-- Table: posts
-- User posts/content
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    content TEXT,
    image_url VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) 
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for fetching user's posts and timeline queries
CREATE INDEX idx_posts_author ON posts(author_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);

-- -----------------------------------------------------
-- Table: post_likes
-- Many-to-many relationship for post likes
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS post_likes (
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (post_id, user_id),
    CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) 
        REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for checking if a user liked a post
CREATE INDEX idx_post_likes_user ON post_likes(user_id);

-- -----------------------------------------------------
-- Table: comments
-- Comments on posts
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) 
        REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) 
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for fetching comments by post
CREATE INDEX idx_comments_post ON comments(post_id);
CREATE INDEX idx_comments_author ON comments(author_id);

-- -----------------------------------------------------
-- Table: events
-- Events that users can create and join
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    event_date DATETIME NOT NULL,
    location VARCHAR(255),
    hashtags VARCHAR(500),
    creator_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_events_creator FOREIGN KEY (creator_id) 
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for event queries
CREATE INDEX idx_events_creator ON events(creator_id);
CREATE INDEX idx_events_event_date ON events(event_date);
CREATE INDEX idx_events_created_at ON events(created_at DESC);

-- -----------------------------------------------------
-- Table: event_interested
-- Users interested in an event
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS event_interested (
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (event_id, user_id),
    CONSTRAINT fk_event_interested_event FOREIGN KEY (event_id) 
        REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_interested_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_event_interested_user ON event_interested(user_id);

-- -----------------------------------------------------
-- Table: event_participants
-- Users participating in an event
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS event_participants (
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (event_id, user_id),
    CONSTRAINT fk_event_participants_event FOREIGN KEY (event_id) 
        REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_participants_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_event_participants_user ON event_participants(user_id);

-- -----------------------------------------------------
-- Table: notifications
-- User notifications
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    actor_id BIGINT,
    type ENUM('NEW_POST', 'NEW_COMMENT', 'NEW_LIKE', 'NEW_FOLLOWER', 
              'NEW_EVENT', 'EVENT_REMINDER', 'BIRTHDAY', 'FRIEND_REQUEST') NOT NULL,
    message TEXT,
    reference_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_actor FOREIGN KEY (actor_id) 
        REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for fetching user notifications
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- -----------------------------------------------------
-- Table: conversations
-- Chat conversations between two users
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    last_message_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_conversations_user1 FOREIGN KEY (user1_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversations_user2 FOREIGN KEY (user2_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_conversations_users UNIQUE (user1_id, user2_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for finding conversations by user
CREATE INDEX idx_conversations_user1 ON conversations(user1_id);
CREATE INDEX idx_conversations_user2 ON conversations(user2_id);
CREATE INDEX idx_conversations_last_message ON conversations(last_message_at DESC);

-- -----------------------------------------------------
-- Table: chat_messages
-- Individual chat messages
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_chat_messages_conversation FOREIGN KEY (conversation_id) 
        REFERENCES conversations(id) ON DELETE SET NULL,
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_receiver FOREIGN KEY (receiver_id) 
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for fetching messages
CREATE INDEX idx_chat_messages_conversation ON chat_messages(conversation_id);
CREATE INDEX idx_chat_messages_sender ON chat_messages(sender_id);
CREATE INDEX idx_chat_messages_receiver ON chat_messages(receiver_id);
CREATE INDEX idx_chat_messages_created_at ON chat_messages(created_at DESC);
