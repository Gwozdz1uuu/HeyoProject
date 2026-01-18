package org.gwozdz1uu.heyobackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private boolean online;
    private String lastSeen;
}
