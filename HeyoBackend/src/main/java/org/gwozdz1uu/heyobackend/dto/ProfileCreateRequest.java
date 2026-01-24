package org.gwozdz1uu.heyobackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ProfileCreateRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    private String nickname;
    
    private String avatarUrl;
    
    private List<Long> interestIds;
}
