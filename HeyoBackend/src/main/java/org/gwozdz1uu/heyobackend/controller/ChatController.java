package org.gwozdz1uu.heyobackend.controller;

import lombok.RequiredArgsConstructor;
import org.gwozdz1uu.heyobackend.dto.ChatMessageDTO;
import org.gwozdz1uu.heyobackend.dto.ConversationDTO;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatService.getConversations(user));
    }

    @GetMapping("/conversations/{partnerId}")
    public ResponseEntity<List<ChatMessageDTO>> getConversation(
            @PathVariable Long partnerId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatService.getConversation(user, partnerId));
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User user) {
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        String content = body.get("content").toString();
        return ResponseEntity.ok(chatService.sendMessage(user, receiverId, content));
    }

    @PostMapping("/conversations/{partnerId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long partnerId,
            @AuthenticationPrincipal User user) {
        chatService.markAsRead(user, partnerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of("count", chatService.getUnreadCount(user)));
    }

    @GetMapping("/conversations/search")
    public ResponseEntity<List<ConversationDTO>> searchConversations(
            @RequestParam String query,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatService.searchConversations(user, query));
    }

    @PostMapping("/conversations/create/{friendId}")
    public ResponseEntity<ConversationDTO> createChatWithFriend(
            @PathVariable Long friendId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatService.createChatWithFriend(user, friendId));
    }

    @GetMapping("/friends/without-chat")
    public ResponseEntity<List<org.gwozdz1uu.heyobackend.dto.UserDTO>> getFriendsWithoutChat(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatService.getFriendsWithoutChat(user));
    }
}
