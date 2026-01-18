package org.gwozdz1uu.heyobackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventCreateRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    private String imageUrl;
    
    @NotNull(message = "Event date is required")
    private LocalDateTime eventDate;
    
    private String location;
    private String hashtags;
}
