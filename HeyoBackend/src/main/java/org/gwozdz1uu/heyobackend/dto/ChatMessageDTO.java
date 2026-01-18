package org.gwozdz1uu.heyobackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageDTO {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private String senderAvatarUrl;
    private Long receiverId;
    private String receiverUsername;
    private String content;
    private boolean read;
    private LocalDateTime createdAt;
}
