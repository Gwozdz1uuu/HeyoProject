package org.gwozdz1uu.heyobackend.websocket.chat.controller;

import org.gwozdz1uu.heyobackend.dto.ChatMessageDTO;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.service.ChatService;
import org.gwozdz1uu.heyobackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatService chatService;

    @Mock
    private UserService userService;

    @Mock
    private Principal principal;

    @InjectMocks
    private ChatWebSocketController chatWebSocketController;

    private User sender;
    private User friend;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .id(1L)
                .username("sender")
                .email("sender@test.com")
                .password("password")
                .friends(new HashSet<>())
                .build();

        friend = User.builder()
                .id(3L)
                .username("friend")
                .email("friend@test.com")
                .password("password")
                .build();

        sender.getFriends().add(friend);
    }

    @Test
    void testSendMessage_ShouldSendToReceiverAndSender() {
        // Arrange
        when(principal.getName()).thenReturn("sender");
        when(userService.findByUsername("sender")).thenReturn(sender);

        Map<String, Object> payload = new HashMap<>();
        payload.put("receiverId", 2L);
        payload.put("content", "Hello, receiver!");

        ChatMessageDTO messageDTO = ChatMessageDTO.builder()
                .id(1L)
                .senderId(1L)
                .senderUsername("sender")
                .receiverId(2L)
                .content("Hello, receiver!")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(chatService.sendMessage(sender, 2L, "Hello, receiver!")).thenReturn(messageDTO);

        // Act
        chatWebSocketController.sendMessage(payload, principal);

        // Assert
        verify(userService).findByUsername("sender");
        verify(chatService).sendMessage(sender, 2L, "Hello, receiver!");
        
        // Verify message sent to receiver
        verify(messagingTemplate).convertAndSendToUser(
                eq("2"),
                eq("/queue/messages"),
                eq(messageDTO)
        );
        
        // Verify confirmation sent to sender
        verify(messagingTemplate).convertAndSendToUser(
                eq("1"),
                eq("/queue/messages"),
                eq(messageDTO)
        );
    }

    @Test
    void testTyping_ShouldNotifyReceiver() {
        // Arrange
        when(principal.getName()).thenReturn("sender");
        when(userService.findByUsername("sender")).thenReturn(sender);

        Map<String, Object> payload = new HashMap<>();
        payload.put("receiverId", 2L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);

        // Act
        chatWebSocketController.typing(payload, principal);

        // Assert
        verify(userService).findByUsername("sender");
        
        verify(messagingTemplate).convertAndSendToUser(
                eq("2"),
                eq("/queue/typing"),
                captor.capture()
        );

        Map<String, Object> sentData = captor.getValue();
        assertEquals(1L, sentData.get("userId"));
        assertEquals("sender", sentData.get("username"));
    }

    @Test
    void testSetOnline_ShouldUpdateStatusAndNotifyFriends() {
        // Arrange
        when(principal.getName()).thenReturn("sender");
        when(userService.findByUsername("sender")).thenReturn(sender);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);

        // Act
        chatWebSocketController.setOnline(principal);

        // Assert
        verify(userService).findByUsername("sender");
        verify(userService).setOnlineStatus(sender, true);
        
        // Verify notification sent to friend
        verify(messagingTemplate).convertAndSendToUser(
                eq("3"),
                eq("/queue/status"),
                captor.capture()
        );

        Map<String, Object> sentData = captor.getValue();
        assertEquals(1L, sentData.get("userId"));
        assertEquals(true, sentData.get("online"));
    }

    @Test
    void testSetOffline_ShouldUpdateStatusAndNotifyFriends() {
        // Arrange
        when(principal.getName()).thenReturn("sender");
        when(userService.findByUsername("sender")).thenReturn(sender);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);

        // Act
        chatWebSocketController.setOffline(principal);

        // Assert
        verify(userService).findByUsername("sender");
        verify(userService).setOnlineStatus(sender, false);
        
        // Verify notification sent to friend
        verify(messagingTemplate).convertAndSendToUser(
                eq("3"),
                eq("/queue/status"),
                captor.capture()
        );

        Map<String, Object> sentData = captor.getValue();
        assertEquals(1L, sentData.get("userId"));
        assertEquals(false, sentData.get("online"));
    }

    @Test
    void testSetOnline_WithNoFriends_ShouldNotSendNotifications() {
        // Arrange
        sender.setFriends(new HashSet<>());
        when(principal.getName()).thenReturn("sender");
        when(userService.findByUsername("sender")).thenReturn(sender);

        // Act
        chatWebSocketController.setOnline(principal);

        // Assert
        verify(userService).findByUsername("sender");
        verify(userService).setOnlineStatus(sender, true);
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    void testSendMessage_WithInvalidPayload_ShouldHandleGracefully() {
        // Arrange
        when(principal.getName()).thenReturn("sender");
        when(userService.findByUsername("sender")).thenReturn(sender);

        Map<String, Object> payload = new HashMap<>();
        payload.put("receiverId", "invalid");
        payload.put("content", "Hello!");

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> {
            chatWebSocketController.sendMessage(payload, principal);
        });
    }
}
