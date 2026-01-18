package org.gwozdz1uu.heyobackend.post.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostDTO {
    private Long id;
    private Long authorId;
    private String authorUsername;
    private String authorAvatarUrl;
    private String content;
    private String imageUrl;
    private int likesCount;
    private int commentsCount;
    private boolean likedByCurrentUser;
    private LocalDateTime createdAt;
}
