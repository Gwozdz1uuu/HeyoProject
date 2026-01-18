package org.gwozdz1uu.heyobackend.websocket.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebSocketConfigTest {

    @Autowired
    private WebSocketConfig webSocketConfig;

    @Test
    void testWebSocketConfigBeanExists() {
        assertNotNull(webSocketConfig, "WebSocketConfig bean should be created");
    }

    @Test
    void testWebSocketConfigImplementsInterface() {
        assertTrue(webSocketConfig instanceof org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer,
                "WebSocketConfig should implement WebSocketMessageBrokerConfigurer");
    }

    @Test
    void testWebSocketConfigHasCorrectAnnotations() {
        // Verify annotations exist at class level (not on proxy)
        assertTrue(WebSocketConfig.class.isAnnotationPresent(org.springframework.context.annotation.Configuration.class),
                "WebSocketConfig should have @Configuration annotation");
        
        assertTrue(WebSocketConfig.class.isAnnotationPresent(org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker.class),
                "WebSocketConfig should have @EnableWebSocketMessageBroker annotation");
    }

    @Test
    void testWebSocketConfigClassExists() {
        assertNotNull(WebSocketConfig.class, "WebSocketConfig class should exist");
        assertEquals("org.gwozdz1uu.heyobackend.websocket.config.WebSocketConfig", 
                WebSocketConfig.class.getName(),
                "WebSocketConfig should be in correct package");
    }
}
