-- =====================================================
-- Migration: Add NEW_MESSAGE and NEW_CHAT to notifications.type
-- Version: 6
-- =====================================================

-- Alter the ENUM to include the new chat notification types
ALTER TABLE notifications 
MODIFY COLUMN type ENUM(
    'NEW_POST', 
    'NEW_COMMENT', 
    'NEW_LIKE', 
    'NEW_FOLLOWER', 
    'NEW_EVENT', 
    'EVENT_REMINDER', 
    'BIRTHDAY', 
    'FRIEND_REQUEST',
    'FRIEND_REQUEST_ACCEPTED',
    'FRIEND_REQUEST_DECLINED',
    'NEW_MESSAGE',
    'NEW_CHAT'
) NOT NULL;
