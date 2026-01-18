package org.gwozdz1uu.heyobackend.notification.model;

import org.gwozdz1uu.heyobackend.model.Notification;
import org.gwozdz1uu.heyobackend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Notification Model Tests")
class NotificationModelTest {

    private User user;
    private User actor;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        actor = User.builder()
                .id(2L)
                .username("actoruser")
                .email("actor@example.com")
                .build();
    }

    @Test
    @DisplayName("Should create notification with builder")
    void testNotificationBuilder() {
        notification = Notification.builder()
                .user(user)
                .actor(actor)
                .type(Notification.NotificationType.NEW_LIKE)
                .message("Someone liked your post")
                .referenceId(123L)
                .read(false)
                .build();

        assertNotNull(notification);
        assertEquals(user, notification.getUser());
        assertEquals(actor, notification.getActor());
        assertEquals(Notification.NotificationType.NEW_LIKE, notification.getType());
        assertEquals("Someone liked your post", notification.getMessage());
        assertEquals(123L, notification.getReferenceId());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("Should create notification with default read status")
    void testNotificationDefaultReadStatus() {
        notification = Notification.builder()
                .user(user)
                .type(Notification.NotificationType.NEW_COMMENT)
                .message("New comment")
                .build();

        assertNotNull(notification);
        assertFalse(notification.isRead(), "Default read status should be false");
    }

    @Test
    @DisplayName("Should allow setting createdAt manually")
    void testNotificationCreatedAt() {
        LocalDateTime customTime = LocalDateTime.now();
        notification = Notification.builder()
                .user(user)
                .type(Notification.NotificationType.NEW_POST)
                .message("New post")
                .createdAt(customTime)
                .build();

        assertNotNull(notification.getCreatedAt(), "createdAt should be set");
        assertEquals(customTime, notification.getCreatedAt(), 
                "createdAt should match the set value");
    }

    @Test
    @DisplayName("Should handle all notification types")
    void testAllNotificationTypes() {
        Notification.NotificationType[] types = Notification.NotificationType.values();
        
        assertTrue(types.length > 0, "Should have notification types");
        
        for (Notification.NotificationType type : types) {
            notification = Notification.builder()
                    .user(user)
                    .type(type)
                    .message("Test message for " + type.name())
                    .build();
            
            assertEquals(type, notification.getType(), 
                    "Notification type should match: " + type.name());
        }
    }

    @Test
    @DisplayName("Should allow notification without actor")
    void testNotificationWithoutActor() {
        notification = Notification.builder()
                .user(user)
                .type(Notification.NotificationType.EVENT_REMINDER)
                .message("Event reminder")
                .build();

        assertNotNull(notification);
        assertNull(notification.getActor(), "Actor should be nullable");
        assertEquals(user, notification.getUser());
    }

    @Test
    @DisplayName("Should allow notification without referenceId")
    void testNotificationWithoutReferenceId() {
        notification = Notification.builder()
                .user(user)
                .type(Notification.NotificationType.BIRTHDAY)
                .message("Happy birthday!")
                .build();

        assertNotNull(notification);
        assertNull(notification.getReferenceId(), "referenceId should be nullable");
    }

    @Test
    @DisplayName("Should allow notification without message")
    void testNotificationWithoutMessage() {
        notification = Notification.builder()
                .user(user)
                .type(Notification.NotificationType.NEW_EVENT)
                .build();

        assertNotNull(notification);
        assertNull(notification.getMessage(), "Message should be nullable");
    }

    @Test
    @DisplayName("Should mark notification as read")
    void testMarkNotificationAsRead() {
        notification = Notification.builder()
                .user(user)
                .type(Notification.NotificationType.NEW_LIKE)
                .read(false)
                .build();

        assertFalse(notification.isRead(), "Initially should be unread");

        notification.setRead(true);
        assertTrue(notification.isRead(), "Should be marked as read");
    }

    @Test
    @DisplayName("Should create notification with all fields")
    void testNotificationWithAllFields() {
        LocalDateTime customTime = LocalDateTime.now().minusDays(1);

        notification = Notification.builder()
                .id(1L)
                .user(user)
                .actor(actor)
                .type(Notification.NotificationType.NEW_COMMENT)
                .message("User commented on your post")
                .referenceId(456L)
                .read(false)
                .createdAt(customTime)
                .build();

        assertNotNull(notification);
        assertEquals(1L, notification.getId());
        assertEquals(user, notification.getUser());
        assertEquals(actor, notification.getActor());
        assertEquals(Notification.NotificationType.NEW_COMMENT, notification.getType());
        assertEquals("User commented on your post", notification.getMessage());
        assertEquals(456L, notification.getReferenceId());
        assertFalse(notification.isRead());
        assertEquals(customTime, notification.getCreatedAt());
    }

    @Test
    @DisplayName("Should verify notification type enum values")
    void testNotificationTypeEnum() {
        Notification.NotificationType[] types = {
                Notification.NotificationType.NEW_POST,
                Notification.NotificationType.NEW_COMMENT,
                Notification.NotificationType.NEW_LIKE,
                Notification.NotificationType.NEW_FOLLOWER,
                Notification.NotificationType.NEW_EVENT,
                Notification.NotificationType.EVENT_REMINDER,
                Notification.NotificationType.BIRTHDAY,
                Notification.NotificationType.FRIEND_REQUEST
        };

        for (Notification.NotificationType type : types) {
            assertNotNull(type, "Notification type should not be null: " + type.name());
            assertNotNull(type.name(), "Notification type name should not be null");
        }

        assertEquals(8, Notification.NotificationType.values().length, 
                "Should have 8 notification types");
    }
}
