package org.gwozdz1uu.heyobackend.service;

import lombok.RequiredArgsConstructor;
import org.gwozdz1uu.heyobackend.dto.ChatMessageDTO;
import org.gwozdz1uu.heyobackend.dto.ConversationDTO;
import org.gwozdz1uu.heyobackend.model.ChatMessage;
import org.gwozdz1uu.heyobackend.model.Notification;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.notification.service.NotificationService;
import org.gwozdz1uu.heyobackend.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public List<ConversationDTO> getConversations(User user) {
        // Get all friends - conversations should include all friends, even without messages
        User currentUser = userService.findById(user.getId());
        List<User> allFriends = new ArrayList<>(currentUser.getFriends());
        
        // Get partners from both directions (where user is sender and where user is receiver)
        // This ensures we have up-to-date information for users who already have messages
        List<User> partnersAsReceiver = chatMessageRepository.findConversationPartnersAsReceiver(user);
        List<User> partnersAsSender = chatMessageRepository.findConversationPartnersAsSender(user);
        
        // Combine and deduplicate by user ID - friends take precedence
        // If a friend already has messages, use their message-based info (for online status, etc.)
        Map<Long, User> partnersMap = new HashMap<>();
        // First add all friends
        allFriends.forEach(f -> partnersMap.put(f.getId(), f));
        // Then update with message partners (they may have more recent online status)
        partnersAsReceiver.forEach(p -> partnersMap.put(p.getId(), p));
        partnersAsSender.forEach(p -> partnersMap.put(p.getId(), p));
        
        List<User> partners = new ArrayList<>(partnersMap.values());

        return partners.stream()
                .map(partner -> {
                    List<ChatMessage> messages = chatMessageRepository.findConversation(user, partner);
                    ChatMessage lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);
                    
                    long unreadCount = messages.stream()
                            .filter(m -> m.getReceiver().getId().equals(user.getId()) && !m.isRead())
                            .count();

                    return ConversationDTO.builder()
                            .partnerId(partner.getId())
                            .partnerUsername(partner.getUsername())
                            .partnerAvatarUrl(partner.getAvatarUrl())
                            .partnerOnline(partner.isOnline())
                            .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                            .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt() : null)
                            .unreadCount((int) unreadCount)
                            .build();
                })
                .sorted(Comparator.comparing(ConversationDTO::getLastMessageAt, 
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<ChatMessageDTO> getConversation(User user, Long partnerId) {
        User partner = userService.findById(partnerId);
        return chatMessageRepository.findConversation(user, partner)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatMessageDTO sendMessage(User sender, Long receiverId, String content) {
        User receiver = userService.findById(receiverId);
        
        // Check if users are friends (only friends can message each other)
        User currentUser = userService.findById(sender.getId());
        if (!currentUser.getFriends().contains(receiver)) {
            throw new RuntimeException("You can only message your friends");
        }
        
        // Check if this is the first message (new chat)
        List<ChatMessage> existingMessages = chatMessageRepository.findConversation(sender, receiver);
        boolean isNewChat = existingMessages.isEmpty();

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        message = chatMessageRepository.save(message);
        
        // Send notification for new message
        String messageText = sender.getUsername() + " wysłał Ci wiadomość: " + 
                (content.length() > 50 ? content.substring(0, 50) + "..." : content);
        notificationService.createNotification(
                receiver,
                sender,
                Notification.NotificationType.NEW_MESSAGE,
                messageText,
                message.getId()
        );
        
        // Send notification for new chat creation
        if (isNewChat) {
            String chatMessageText = sender.getUsername() + " rozpoczął z Tobą czat";
            notificationService.createNotification(
                    receiver,
                    sender,
                    Notification.NotificationType.NEW_CHAT,
                    chatMessageText,
                    sender.getId()
            );
        }
        
        return toDTO(message);
    }

    @Transactional
    public void markAsRead(User user, Long partnerId) {
        User partner = userService.findById(partnerId);
        chatMessageRepository.markAsRead(partner, user);
    }

    public int getUnreadCount(User user) {
        return chatMessageRepository.countByReceiverAndReadFalse(user);
    }

    /**
     * Search conversations by partner username
     */
    public List<ConversationDTO> searchConversations(User user, String query) {
        List<ConversationDTO> allConversations = getConversations(user);
        String lowerQuery = query.toLowerCase();
        
        return allConversations.stream()
                .filter(conv -> conv.getPartnerUsername().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    /**
     * Create a new chat with a friend (if not already exists)
     * This ensures conversations are automatically created between friends
     */
    @Transactional
    public ConversationDTO createChatWithFriend(User user, Long friendId) {
        User friend = userService.findById(friendId);
        User currentUser = userService.findById(user.getId());
        
        // Verify they are friends
        if (!currentUser.getFriends().contains(friend)) {
            throw new RuntimeException("You can only create chats with your friends");
        }
        
        // Check if conversation already exists
        List<ChatMessage> existingMessages = chatMessageRepository.findConversation(user, friend);
        
        // If conversation already exists, return it as DTO
        if (!existingMessages.isEmpty()) {
            return getConversations(user).stream()
                    .filter(conv -> conv.getPartnerId().equals(friendId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
        }
        
        // Send notification for new chat creation
        String messageText = user.getUsername() + " rozpoczął z Tobą czat";
        notificationService.createNotification(
                friend,
                user,
                Notification.NotificationType.NEW_CHAT,
                messageText,
                user.getId()
        );
        
        // Return empty conversation DTO (will be populated when first message is sent)
        return ConversationDTO.builder()
                .partnerId(friend.getId())
                .partnerUsername(friend.getUsername())
                .partnerAvatarUrl(friend.getAvatarUrl())
                .partnerOnline(friend.isOnline())
                .unreadCount(0)
                .build();
    }

    /**
     * Get friends who don't have a conversation yet
     */
    public List<org.gwozdz1uu.heyobackend.dto.UserDTO> getFriendsWithoutChat(User user) {
        User currentUser = userService.findById(user.getId());
        
        // Get partners from both directions
        List<User> partnersAsReceiver = chatMessageRepository.findConversationPartnersAsReceiver(user);
        List<User> partnersAsSender = chatMessageRepository.findConversationPartnersAsSender(user);
        
        // Combine and deduplicate by user ID
        Set<Long> partnerIds = new HashSet<>();
        partnersAsReceiver.forEach(p -> partnerIds.add(p.getId()));
        partnersAsSender.forEach(p -> partnerIds.add(p.getId()));
        
        return currentUser.getFriends().stream()
                .filter(friend -> !partnerIds.contains(friend.getId()))
                .map(userService::toDTO)
                .collect(Collectors.toList());
    }

    private ChatMessageDTO toDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .senderAvatarUrl(message.getSender().getAvatarUrl())
                .receiverId(message.getReceiver().getId())
                .receiverUsername(message.getReceiver().getUsername())
                .content(message.getContent())
                .read(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
