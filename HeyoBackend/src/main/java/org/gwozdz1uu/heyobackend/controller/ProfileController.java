package org.gwozdz1uu.heyobackend.controller;

import lombok.RequiredArgsConstructor;
import org.gwozdz1uu.heyobackend.dto.ProfileDTO;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileDTO> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.getProfile(user.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileDTO> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody ProfileDTO dto) {
        return ResponseEntity.ok(profileService.updateProfile(user, dto));
    }
}
