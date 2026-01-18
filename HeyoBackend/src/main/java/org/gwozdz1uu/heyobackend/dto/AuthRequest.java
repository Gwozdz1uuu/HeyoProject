package org.gwozdz1uu.heyobackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication request DTO for login
 * Supports login by username OR email
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    
    @NotBlank(message = "Username or email is required")
    private String username; // Can be username or email
    
    @NotBlank(message = "Password is required")
    private String password;
}
