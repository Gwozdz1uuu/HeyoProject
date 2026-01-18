package org.gwozdz1uu.heyobackend.service;

import lombok.RequiredArgsConstructor;
import org.gwozdz1uu.heyobackend.dto.ProfileDTO;
import org.gwozdz1uu.heyobackend.model.Profile;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.repository.ProfileRepository;
import org.gwozdz1uu.heyobackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUserId(userId)
                .orElse(Profile.builder().user(user).build());

        return toDTO(user, profile);
    }

    @Transactional
    public ProfileDTO updateProfile(User user, ProfileDTO dto) {
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElse(Profile.builder().user(user).build());

        if (dto.getFirstName() != null) profile.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) profile.setLastName(dto.getLastName());
        if (dto.getBio() != null) profile.setBio(dto.getBio());
        if (dto.getDateOfBirth() != null) profile.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getLocation() != null) profile.setLocation(dto.getLocation());
        if (dto.getWebsite() != null) profile.setWebsite(dto.getWebsite());
        if (dto.getPhoneNumber() != null) profile.setPhoneNumber(dto.getPhoneNumber());

        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
            userRepository.save(user);
        }

        profile = profileRepository.save(profile);
        return toDTO(user, profile);
    }

    private ProfileDTO toDTO(User user, Profile profile) {
        return ProfileDTO.builder()
                .id(profile.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .bio(profile.getBio())
                .dateOfBirth(profile.getDateOfBirth())
                .location(profile.getLocation())
                .website(profile.getWebsite())
                .phoneNumber(profile.getPhoneNumber())
                .friendsCount(user.getFriends().size())
                .postsCount(user.getPosts().size())
                .build();
    }
}
