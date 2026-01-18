package org.gwozdz1uu.heyobackend.websocket.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gwozdz1uu.heyobackend.dto.ChatMessageDTO;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.service.ChatService;
import org.gwozdz1uu.heyobackend.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserService userService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> payload, Principal principal) {
        log.info("[WebSocket] Received message from user: {}", principal.getName());
        
        try {
            User sender = userService.findByUsername(principal.getName());
            Long receiverId = Long.valueOf(payload.get("receiverId").toString());
            String content = payload.get("content").toString();

            log.info("[WebSocket] Sending message from {} to {}: {}", sender.getId(), receiverId, content);
            
            ChatMessageDTO message = chatService.sendMessage(sender, receiverId, content);

            log.info("[WebSocket] Message saved, broadcasting to users...");
            
            // Get receiver username for routing
            User receiver = userService.findById(receiverId);
            
            // Send to receiver - convertAndSendToUser uses username from Principal mapping
            messagingTemplate.convertAndSendToUser(
                    receiver.getUsername(),
                    "/queue/messages",
                    message
            );
            log.info("[WebSocket] ✓ Sent to receiver (username: {}) via convertAndSendToUser", receiver.getUsername());

            // Send confirmation to sender - convertAndSendToUser uses username from Principal mapping
            messagingTemplate.convertAndSendToUser(
                    sender.getUsername(),
                    "/queue/messages",
                    message
            );
            log.info("[WebSocket] ✓ Sent to sender (username: {}) via convertAndSendToUser", sender.getUsername());
            log.info("[WebSocket] ✓ Message broadcast complete");
        } catch (RuntimeException e) {
            log.error("[WebSocket] Error sending message: {}", e.getMessage());
            
            // Send error back to sender - use username not ID!
            User sender = userService.findByUsername(principal.getName());
            messagingTemplate.convertAndSendToUser(
                    sender.getUsername(),
                    "/queue/errors",
                    Map.of("error", e.getMessage(), "type", "MESSAGE_ERROR")
            );
        }
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload Map<String, Object> payload, Principal principal) {
        User sender = userService.findByUsername(principal.getName());
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        User receiver = userService.findById(receiverId);

        // Use username not ID for routing!
        messagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/typing",
                Map.of("userId", sender.getId(), "username", sender.getUsername())
        );
    }

    @MessageMapping("/user.online")
    public void setOnline(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        userService.setOnlineStatus(user, true);

        // Broadcast to all friends - use transactional method to avoid lazy loading
        List<Long> friendIds = userService.getFriendIds(user.getId());
        friendIds.forEach(friendId -> {
                User friend = userService.findById(friendId);
                // Use username not ID for routing!
                messagingTemplate.convertAndSendToUser(
                        friend.getUsername(),
                        "/queue/status",
                        Map.of("userId", user.getId(), "online", true)
                );
        });
    }

    @MessageMapping("/user.offline")
    public void setOffline(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        userService.setOnlineStatus(user, false);

        // Broadcast to all friends - use transactional method to avoid lazy loading
        List<Long> friendIds = userService.getFriendIds(user.getId());
        friendIds.forEach(friendId -> {
                User friend = userService.findById(friendId);
                // Use username not ID for routing!
                messagingTemplate.convertAndSendToUser(
                        friend.getUsername(),
                        "/queue/status",
                        Map.of("userId", user.getId(), "online", false)
                );
        });
    }
}
