package org.gwozdz1uu.heyobackend.service;

import lombok.RequiredArgsConstructor;
import org.gwozdz1uu.heyobackend.dto.ProfileCreateRequest;
import org.gwozdz1uu.heyobackend.dto.ProfileDTO;
import org.gwozdz1uu.heyobackend.model.Interest;
import org.gwozdz1uu.heyobackend.model.Profile;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.repository.InterestRepository;
import org.gwozdz1uu.heyobackend.repository.ProfileRepository;
import org.gwozdz1uu.heyobackend.repository.UserRepository;
import org.gwozdz1uu.heyobackend.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final JwtService jwtService;

    public ProfileDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUserId(userId)
                .orElse(Profile.builder().user(user).build());

        return toDTO(user, profile);
    }

    @Transactional
    public ProfileDTO completeProfile(User user, ProfileCreateRequest request) {
        // Reload user with all collections to avoid lazy initialization issues
        User loadedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Profile profile = profileRepository.findByUserId(loadedUser.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setNickname(request.getNickname());

        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            loadedUser.setAvatarUrl(request.getAvatarUrl());
            loadedUser = userRepository.save(loadedUser);
        }

        // Set interests
        if (request.getInterestIds() != null && !request.getInterestIds().isEmpty()) {
            Set<Interest> interests = request.getInterestIds().stream()
                    .map(interestId -> interestRepository.findById(interestId)
                            .orElseThrow(() -> new RuntimeException("Interest not found: " + interestId)))
                    .collect(Collectors.toSet());
            profile.setInterests(interests);
        }

        profile = profileRepository.save(profile);
        
        // Initialize lazy collections within transaction
        loadedUser.getFriends().size();
        loadedUser.getPosts().size();
        
        return toDTO(loadedUser, profile);
    }

    @Transactional
    public ProfileDTO updateProfile(User user, ProfileDTO dto) {
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElse(Profile.builder().user(user).build());

        // Update username if provided and different
        boolean usernameChanged = false;
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            // Check uniqueness - exclude current user
            User existingUser = userRepository.findByUsername(dto.getUsername()).orElse(null);
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                throw new RuntimeException("Username already exists");
            }
            user.setUsername(dto.getUsername());
            user = userRepository.save(user);
            usernameChanged = true;
        }

        if (dto.getFirstName() != null) profile.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) profile.setLastName(dto.getLastName());
        if (dto.getNickname() != null) profile.setNickname(dto.getNickname());
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
        // Reload user to get updated username
        user = userRepository.findById(user.getId()).orElseThrow(() -> new RuntimeException("User not found"));
        
        ProfileDTO result = toDTO(user, profile);
        
        // Generate new token if username was changed
        if (usernameChanged) {
            String newToken = jwtService.generateToken(user);
            result.setNewToken(newToken);
        }
        
        return result;
    }

    private ProfileDTO toDTO(User user, Profile profile) {
        // Safely get collection sizes, handling potential lazy initialization
        int friendsCount = 0;
        int postsCount = 0;
        
        try {
            friendsCount = user.getFriends() != null ? user.getFriends().size() : 0;
        } catch (Exception e) {
            // Collection not initialized, use 0
            friendsCount = 0;
        }
        
        try {
            postsCount = user.getPosts() != null ? user.getPosts().size() : 0;
        } catch (Exception e) {
            // Collection not initialized, use 0
            postsCount = 0;
        }
        
        return ProfileDTO.builder()
                .id(profile.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .nickname(profile.getNickname())
                .bio(profile.getBio())
                .dateOfBirth(profile.getDateOfBirth())
                .location(profile.getLocation())
                .website(profile.getWebsite())
                .phoneNumber(profile.getPhoneNumber())
                .friendsCount(friendsCount)
                .postsCount(postsCount)
                .build();
    }
}
