package org.gwozdz1uu.heyobackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private LocalDateTime eventDate;
    private String location;
    private String hashtags;
    private Long creatorId;
    private String creatorUsername;
    private int interestedCount;
    private int participantsCount;
    private boolean isInterested;
    private boolean isParticipating;
    private LocalDateTime createdAt;
}
