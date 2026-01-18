package org.gwozdz1uu.heyobackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProfileDTO {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String avatarUrl;
    private String firstName;
    private String lastName;
    private String bio;
    private LocalDate dateOfBirth;
    private String location;
    private String website;
    private String phoneNumber;
    private int friendsCount;
    private int postsCount;
}
