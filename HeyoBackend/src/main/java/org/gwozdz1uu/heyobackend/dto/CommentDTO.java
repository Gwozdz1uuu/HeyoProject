package org.gwozdz1uu.heyobackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDTO {
    private Long id;
    private Long postId;
    private Long authorId;
    private String authorUsername;
    private String authorAvatarUrl;
    private String content;
    private LocalDateTime createdAt;
}
