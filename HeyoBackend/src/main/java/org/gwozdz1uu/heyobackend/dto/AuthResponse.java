package org.gwozdz1uu.heyobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response DTO containing JWT token and user info
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    
    @Builder.Default
    private String type = "Bearer";
    
    private Long userId;
    private String username;
    private String email;
    private String avatarUrl;
    
    @Builder.Default
    private boolean profileCompleted = false;
}
