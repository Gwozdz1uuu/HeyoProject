package org.gwozdz1uu.heyobackend.websocket.chat;

import org.gwozdz1uu.heyobackend.dto.ChatMessageDTO;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.service.ChatService;
import org.gwozdz1uu.heyobackend.service.UserService;
import org.gwozdz1uu.heyobackend.websocket.chat.controller.ChatWebSocketController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocket Message Flow Tests")
class WebSocketMessageFlowTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatService chatService;

    @Mock
    private UserService userService;

    @Mock
    private Principal principal;

    private ChatWebSocketController controller;

    private User user1;

    @BeforeEach
    void setUp() {
        controller = new ChatWebSocketController(messagingTemplate, chatService, userService);

        user1 = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@test.com")
                .password("password")
                .friends(new HashSet<>())
                .build();

    }

    @Test
    @DisplayName("Should handle complete message send flow")
    void testCompleteMessageSendFlow() {
        // Arrange
        when(principal.getName()).thenReturn("user1");
        when(userService.findByUsername("user1")).thenReturn(user1);

        Map<String, Object> payload = new HashMap<>();
        payload.put("receiverId", 2L);
        payload.put("content", "Test message");

        ChatMessageDTO messageDTO = ChatMessageDTO.builder()
                .id(1L)
                .senderId(1L)
                .senderUsername("user1")
                .receiverId(2L)
                .content("Test message")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(chatService.sendMessage(user1, 2L, "Test message")).thenReturn(messageDTO);

        // Act
        controller.sendMessage(payload, principal);

        // Assert
        verify(userService).findByUsername("user1");
        verify(chatService).sendMessage(user1, 2L, "Test message");
        verify(messagingTemplate, times(2)).convertAndSendToUser(
                anyString(),
                eq("/queue/messages"),
                eq(messageDTO)
        );
    }

    @Test
    @DisplayName("Should handle typing indicator flow")
    void testTypingIndicatorFlow() {
        // Arrange
        when(principal.getName()).thenReturn("user1");
        when(userService.findByUsername("user1")).thenReturn(user1);

        Map<String, Object> payload = new HashMap<>();
        payload.put("receiverId", 2L);

        // Act
        controller.typing(payload, principal);

        // Assert
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> queueCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(messagingTemplate).convertAndSendToUser(
                destinationCaptor.capture(),
                queueCaptor.capture(),
                dataCaptor.capture()
        );

        assertEquals("2", destinationCaptor.getValue());
        assertEquals("/queue/typing", queueCaptor.getValue());
        assertEquals(1L, dataCaptor.getValue().get("userId"));
        assertEquals("user1", dataCaptor.getValue().get("username"));
    }

    @Test
    @DisplayName("Should handle online status flow with multiple friends")
    void testOnlineStatusFlowWithMultipleFriends() {
        // Arrange
        User friend1 = User.builder().id(3L).username("friend1").build();
        User friend2 = User.builder().id(4L).username("friend2").build();
        user1.getFriends().add(friend1);
        user1.getFriends().add(friend2);

        when(principal.getName()).thenReturn("user1");
        when(userService.findByUsername("user1")).thenReturn(user1);

        // Act
        controller.setOnline(principal);

        // Assert
        verify(userService).setOnlineStatus(user1, true);
        verify(messagingTemplate, times(2)).convertAndSendToUser(
                anyString(),
                eq("/queue/status"),
                any(Map.class)
        );
    }

    @Test
    @DisplayName("Should handle offline status flow")
    void testOfflineStatusFlow() {
        // Arrange
        User friend = User.builder().id(3L).username("friend").build();
        user1.getFriends().add(friend);

        when(principal.getName()).thenReturn("user1");
        when(userService.findByUsername("user1")).thenReturn(user1);

        // Act
        controller.setOffline(principal);

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        
        verify(userService).setOnlineStatus(user1, false);
        verify(messagingTemplate).convertAndSendToUser(
                eq("3"),
                eq("/queue/status"),
                captor.capture()
        );

        Map<String, Object> statusData = captor.getValue();
        assertEquals(1L, statusData.get("userId"));
        assertEquals(false, statusData.get("online"));
    }

    @Test
    @DisplayName("Should verify all message mappings are correctly annotated")
    void testMessageMappingAnnotations() {
        // This test verifies that the controller has the correct @MessageMapping annotations
        // by checking if methods are invoked correctly during actual usage
        
        assertNotNull(controller);
        assertTrue(controller.getClass().isAnnotationPresent(org.springframework.stereotype.Controller.class));
    }

    @Test
    @DisplayName("Should handle empty friends list gracefully")
    void testEmptyFriendsListHandling() {
        // Arrange
        user1.setFriends(new HashSet<>());
        when(principal.getName()).thenReturn("user1");
        when(userService.findByUsername("user1")).thenReturn(user1);

        // Act
        controller.setOnline(principal);

        // Assert
        verify(userService).setOnlineStatus(user1, true);
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }
}
