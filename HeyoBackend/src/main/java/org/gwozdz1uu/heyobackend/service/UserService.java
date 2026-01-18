package org.gwozdz1uu.heyobackend.service;

import lombok.RequiredArgsConstructor;
import org.gwozdz1uu.heyobackend.dto.UserDTO;
import org.gwozdz1uu.heyobackend.model.Notification;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.notification.service.NotificationService;
import org.gwozdz1uu.heyobackend.repository.NotificationRepository;
import org.gwozdz1uu.heyobackend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    /**
     * Load user by username or email for Spring Security authentication
     * Allows users to log in with either their username or email
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<UserDTO> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCase(query)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void setOnlineStatus(User user, boolean online) {
        user.setOnline(online);
        if (!online) {
            user.setLastSeen(LocalDateTime.now());
        }
        userRepository.save(user);
    }

    @Transactional
    public List<UserDTO> getFriends(User user) {
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return currentUser.getFriends().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Long> getFriendIds(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getFriends().stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void sendFriendRequest(User user, Long friendId) {
        if (user.getId().equals(friendId)) {
            throw new RuntimeException("Cannot send friend request to yourself");
        }

        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getFriends().contains(friend)) {
            throw new RuntimeException("User is already your friend");
        }

        // Check if there's already a pending friend request
        List<Notification> existingRequests = notificationRepository.findByUserAndTypeAndActor(
                friend, Notification.NotificationType.FRIEND_REQUEST, currentUser);
        if (!existingRequests.isEmpty()) {
            throw new RuntimeException("Friend request already sent");
        }

        // Send friend request notification
        String message = currentUser.getUsername() + " wysłał Ci zaproszenie do znajomych";
        notificationService.createNotification(
                friend,
                currentUser,
                Notification.NotificationType.FRIEND_REQUEST,
                message,
                currentUser.getId() // referenceId stores the sender's ID
        );
    }

    @Transactional
    public void acceptFriendRequest(User user, Long notificationId) {
        // Find notification and verify it belongs to the current user
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new RuntimeException("Notification not found or not authorized"));

        if (notification.getType() != Notification.NotificationType.FRIEND_REQUEST) {
            throw new RuntimeException("Invalid notification type");
        }

        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Use referenceId to get sender ID (stored when friend request was sent)
        Long senderId = notification.getReferenceId();
        if (senderId == null) {
            throw new RuntimeException("Invalid sender ID in notification");
        }

        // Load sender from database
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        if (currentUser.getFriends().contains(sender)) {
            // Already friends, just delete the notification
            notificationRepository.delete(notification);
            return;
        }

        // Add bidirectional friendship
        currentUser.getFriends().add(sender);
        sender.getFriends().add(currentUser);
        userRepository.save(currentUser);
        userRepository.save(sender);

        // Send acceptance notification to sender
        String message = currentUser.getUsername() + " zaakceptował Twoje zaproszenie do znajomych";
        notificationService.createNotification(
                sender,
                currentUser,
                Notification.NotificationType.FRIEND_REQUEST_ACCEPTED,
                message,
                currentUser.getId()
        );

        // Delete the friend request notification
        notificationRepository.delete(notification);
    }

    @Transactional
    public void declineFriendRequest(User user, Long notificationId) {
        // Find notification and verify it belongs to the current user
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new RuntimeException("Notification not found or not authorized"));

        if (notification.getType() != Notification.NotificationType.FRIEND_REQUEST) {
            throw new RuntimeException("Invalid notification type");
        }

        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Use referenceId to get sender ID (stored when friend request was sent)
        Long senderId = notification.getReferenceId();
        if (senderId == null) {
            throw new RuntimeException("Invalid sender ID in notification");
        }

        // Load sender from database
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Send decline notification to sender
        String message = currentUser.getUsername() + " odrzucił Twoje zaproszenie do znajomych";
        notificationService.createNotification(
                sender,
                currentUser,
                Notification.NotificationType.FRIEND_REQUEST_DECLINED,
                message,
                currentUser.getId()
        );

        // Delete the friend request notification
        notificationRepository.delete(notification);
    }

    @Transactional
    public void removeFriend(User user, Long friendId) {
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        if (!currentUser.getFriends().contains(friend)) {
            throw new RuntimeException("User is not your friend");
        }

        // Remove bidirectional friendship
        currentUser.getFriends().remove(friend);
        friend.getFriends().remove(currentUser);
        
        userRepository.save(currentUser);
        userRepository.save(friend);
    }

    public UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .online(user.isOnline())
                .lastSeen(user.getLastSeen() != null ? user.getLastSeen().toString() : null)
                .build();
    }
}
