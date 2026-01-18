-- =====================================================
-- Migration: Add FRIEND_REQUEST_ACCEPTED and FRIEND_REQUEST_DECLINED to notifications.type
-- Version: 5
-- =====================================================

-- Alter the ENUM to include the new friend request notification types
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
    'FRIEND_REQUEST_DECLINED'
) NOT NULL;
