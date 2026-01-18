package org.gwozdz1uu.heyobackend.post.dto;

import lombok.Data;

@Data
public class PostCreateRequest {
    private String content;
    private String imageUrl;
}
