package org.gwozdz1uu.heyobackend.controller;

import lombok.RequiredArgsConstructor;
import org.gwozdz1uu.heyobackend.dto.UserDTO;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.toDTO(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(userService.toDTO(user));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(userService.searchUsers(query));
    }

    @GetMapping("/friends")
    public ResponseEntity<List<UserDTO>> getFriends(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getFriends(user));
    }

    @PostMapping("/friends/{friendId}")
    public ResponseEntity<Void> sendFriendRequest(
            @PathVariable Long friendId,
            @AuthenticationPrincipal User user) {
        userService.sendFriendRequest(user, friendId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/friend-requests/{notificationId}/accept")
    public ResponseEntity<Void> acceptFriendRequest(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal User user) {
        userService.acceptFriendRequest(user, notificationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/friend-requests/{notificationId}/decline")
    public ResponseEntity<Void> declineFriendRequest(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal User user) {
        userService.declineFriendRequest(user, notificationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable Long friendId,
            @AuthenticationPrincipal User user) {
        userService.removeFriend(user, friendId);
        return ResponseEntity.ok().build();
    }
}
