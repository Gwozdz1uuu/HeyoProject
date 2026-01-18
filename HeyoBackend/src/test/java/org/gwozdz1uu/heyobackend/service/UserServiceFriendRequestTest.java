package org.gwozdz1uu.heyobackend.service;

import org.gwozdz1uu.heyobackend.model.Notification;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.notification.service.NotificationService;
import org.gwozdz1uu.heyobackend.repository.NotificationRepository;
import org.gwozdz1uu.heyobackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Friend Request Tests")
class UserServiceFriendRequestTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    private User sender;
    private User receiver;
    private Notification friendRequestNotification;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .id(1L)
                .username("sender")
                .email("sender@test.com")
                .password("password")
                .friends(new HashSet<>())
                .build();

        receiver = User.builder()
                .id(2L)
                .username("receiver")
                .email("receiver@test.com")
                .password("password")
                .friends(new HashSet<>())
                .build();

        friendRequestNotification = Notification.builder()
                .id(10L)
                .user(receiver)
                .actor(sender)
                .type(Notification.NotificationType.FRIEND_REQUEST)
                .message("sender wysłał Ci zaproszenie do znajomych")
                .referenceId(sender.getId()) // referenceId stores sender's ID
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should send friend request successfully")
    void testSendFriendRequest_Success() {
        // Arrange
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(notificationRepository.findByUserAndTypeAndActor(
                receiver, Notification.NotificationType.FRIEND_REQUEST, sender))
                .thenReturn(Collections.emptyList());
        doNothing().when(notificationService).createNotification(any(), any(), any(), any(), any());

        // Act
        assertDoesNotThrow(() -> userService.sendFriendRequest(sender, receiver.getId()));

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<User> actorCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Notification.NotificationType> typeCaptor = ArgumentCaptor.forClass(Notification.NotificationType.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> referenceIdCaptor = ArgumentCaptor.forClass(Long.class);

        verify(notificationService).createNotification(
                userCaptor.capture(),
                actorCaptor.capture(),
                typeCaptor.capture(),
                messageCaptor.capture(),
                referenceIdCaptor.capture()
        );

        assertEquals(receiver.getId(), userCaptor.getValue().getId());
        assertEquals(sender.getId(), actorCaptor.getValue().getId());
        assertEquals(Notification.NotificationType.FRIEND_REQUEST, typeCaptor.getValue());
        assertEquals(sender.getId(), referenceIdCaptor.getValue());
    }

    @Test
    @DisplayName("Should throw error when sending friend request to yourself")
    void testSendFriendRequest_ToYourself() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.sendFriendRequest(sender, sender.getId());
        });
        assertEquals("Cannot send friend request to yourself", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw error when friend request already sent")
    void testSendFriendRequest_AlreadySent() {
        // Arrange
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(notificationRepository.findByUserAndTypeAndActor(
                receiver, Notification.NotificationType.FRIEND_REQUEST, sender))
                .thenReturn(Collections.singletonList(friendRequestNotification));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.sendFriendRequest(sender, receiver.getId());
        });
        assertEquals("Friend request already sent", exception.getMessage());
    }

    @Test
    @DisplayName("Should accept friend request successfully")
    void testAcceptFriendRequest_Success() {
        // Arrange
        when(notificationRepository.findByIdAndUserId(10L, receiver.getId())).thenReturn(Optional.of(friendRequestNotification));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        doNothing().when(notificationService).createNotification(any(), any(), any(), any(), any());
        doNothing().when(notificationRepository).delete(any(Notification.class));

        // Act
        assertDoesNotThrow(() -> userService.acceptFriendRequest(receiver, 10L));

        // Assert - verify friendship was added
        assertTrue(receiver.getFriends().contains(sender));
        assertTrue(sender.getFriends().contains(receiver));

        // Verify acceptance notification was sent
        ArgumentCaptor<User> notificationUserCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<User> notificationActorCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Notification.NotificationType> typeCaptor = ArgumentCaptor.forClass(Notification.NotificationType.class);

        verify(notificationService, times(1)).createNotification(
                notificationUserCaptor.capture(),
                notificationActorCaptor.capture(),
                typeCaptor.capture(),
                any(),
                any()
        );

        assertEquals(sender.getId(), notificationUserCaptor.getValue().getId());
        assertEquals(receiver.getId(), notificationActorCaptor.getValue().getId());
        assertEquals(Notification.NotificationType.FRIEND_REQUEST_ACCEPTED, typeCaptor.getValue());

        // Verify friend request notification was deleted
        verify(notificationRepository).delete(friendRequestNotification);
    }

    @Test
    @DisplayName("Should throw error when accepting notification not belonging to user")
    void testAcceptFriendRequest_NotAuthorized() {
        // Arrange
        User otherUser = User.builder().id(999L).build();
        when(notificationRepository.findByIdAndUserId(10L, otherUser.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.acceptFriendRequest(otherUser, 10L);
        });
        assertEquals("Notification not found or not authorized", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw error when accepting invalid notification type")
    void testAcceptFriendRequest_InvalidType() {
        // Arrange
        Notification wrongNotification = Notification.builder()
                .id(10L)
                .user(receiver)
                .type(Notification.NotificationType.NEW_LIKE)
                .referenceId(sender.getId())
                .build();
        when(notificationRepository.findByIdAndUserId(10L, receiver.getId())).thenReturn(Optional.of(wrongNotification));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.acceptFriendRequest(receiver, 10L);
        });
        assertEquals("Invalid notification type", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw error when notification has null referenceId")
    void testAcceptFriendRequest_NullReferenceId() {
        // Arrange
        Notification notificationWithoutRef = Notification.builder()
                .id(10L)
                .user(receiver)
                .type(Notification.NotificationType.FRIEND_REQUEST)
                .referenceId(null)
                .build();
        when(notificationRepository.findByIdAndUserId(10L, receiver.getId())).thenReturn(Optional.of(notificationWithoutRef));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.acceptFriendRequest(receiver, 10L);
        });
        assertEquals("Invalid sender ID in notification", exception.getMessage());
    }

    @Test
    @DisplayName("Should decline friend request successfully")
    void testDeclineFriendRequest_Success() {
        // Arrange
        when(notificationRepository.findByIdAndUserId(10L, receiver.getId())).thenReturn(Optional.of(friendRequestNotification));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        doNothing().when(notificationService).createNotification(any(), any(), any(), any(), any());
        doNothing().when(notificationRepository).delete(any(Notification.class));

        // Act
        assertDoesNotThrow(() -> userService.declineFriendRequest(receiver, 10L));

        // Assert - verify friendship was NOT added
        assertFalse(receiver.getFriends().contains(sender));
        assertFalse(sender.getFriends().contains(receiver));

        // Verify decline notification was sent
        ArgumentCaptor<User> notificationUserCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<User> notificationActorCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Notification.NotificationType> typeCaptor = ArgumentCaptor.forClass(Notification.NotificationType.class);

        verify(notificationService, times(1)).createNotification(
                notificationUserCaptor.capture(),
                notificationActorCaptor.capture(),
                typeCaptor.capture(),
                any(),
                any()
        );

        assertEquals(sender.getId(), notificationUserCaptor.getValue().getId());
        assertEquals(receiver.getId(), notificationActorCaptor.getValue().getId());
        assertEquals(Notification.NotificationType.FRIEND_REQUEST_DECLINED, typeCaptor.getValue());

        // Verify friend request notification was deleted
        verify(notificationRepository).delete(friendRequestNotification);
    }

    @Test
    @DisplayName("Should handle accept when already friends")
    void testAcceptFriendRequest_AlreadyFriends() {
        // Arrange - make them already friends
        receiver.getFriends().add(sender);
        sender.getFriends().add(receiver);

        when(notificationRepository.findByIdAndUserId(10L, receiver.getId())).thenReturn(Optional.of(friendRequestNotification));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        doNothing().when(notificationRepository).delete(any(Notification.class));

        // Act
        assertDoesNotThrow(() -> userService.acceptFriendRequest(receiver, 10L));

        // Assert - notification should be deleted, but no new notification sent
        verify(notificationRepository).delete(friendRequestNotification);
        verify(notificationService, never()).createNotification(any(), any(), any(), any(), any());
    }
}
