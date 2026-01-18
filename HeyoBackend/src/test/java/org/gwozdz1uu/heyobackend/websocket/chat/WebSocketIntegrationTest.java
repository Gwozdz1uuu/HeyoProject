package org.gwozdz1uu.heyobackend.websocket.chat;

import org.gwozdz1uu.heyobackend.service.ChatService;
import org.gwozdz1uu.heyobackend.service.UserService;
import org.gwozdz1uu.heyobackend.websocket.chat.controller.ChatWebSocketController;
import org.gwozdz1uu.heyobackend.websocket.config.WebSocketConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {

    @Autowired(required = false)
    private WebSocketConfig webSocketConfig;

    @Autowired(required = false)
    private ChatWebSocketController chatWebSocketController;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @MockBean
    private ChatService chatService;

    @MockBean
    private UserService userService;

    @Test
    void testWebSocketConfigIsLoaded() {
        assertNotNull(webSocketConfig, "WebSocketConfig should be loaded in Spring context");
    }

    @Test
    void testChatWebSocketControllerIsLoaded() {
        assertNotNull(chatWebSocketController, "ChatWebSocketController should be loaded in Spring context");
    }

    @Test
    void testWebSocketComponentsAreProperlyWired() {
        assertAll(
                () -> assertNotNull(webSocketConfig, "WebSocketConfig should be available"),
                () -> assertNotNull(chatWebSocketController, "ChatWebSocketController should be available"),
                () -> assertNotNull(messagingTemplate, "SimpMessagingTemplate should be available"),
                () -> assertNotNull(chatService, "ChatService should be available"),
                () -> assertNotNull(userService, "UserService should be available")
        );
    }

    @Test
    void testWebSocketConfigurationAnnotations() {
        assertNotNull(webSocketConfig);
        
        // Verify the class has @Configuration and @EnableWebSocketMessageBroker annotations
        // by checking if it implements the required interface
        assertTrue(webSocketConfig instanceof org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer,
                "WebSocketConfig should implement WebSocketMessageBrokerConfigurer");
    }

    @Test
    void testChatWebSocketControllerHasCorrectAnnotations() {
        assertNotNull(chatWebSocketController);
        
        // Verify the controller has @Controller annotation
        org.springframework.stereotype.Controller controllerAnnotation = 
                chatWebSocketController.getClass().getAnnotation(org.springframework.stereotype.Controller.class);
        assertNotNull(controllerAnnotation, "ChatWebSocketController should have @Controller annotation");
    }
}
