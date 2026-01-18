package org.gwozdz1uu.heyobackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConversationDTO {
    private Long id;
    private Long partnerId;
    private String partnerUsername;
    private String partnerAvatarUrl;
    private boolean partnerOnline;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
}
