package org.gwozdz1uu.heyobackend.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDTO {
    private Long id;
    private String actorUsername;
    private String actorAvatarUrl;
    private String type;
    private String message;
    private Long referenceId;
    private boolean read;
    private LocalDateTime createdAt;
}
