package org.gwozdz1uu.heyobackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gwozdz1uu.heyobackend.dto.AuthRequest;
import org.gwozdz1uu.heyobackend.dto.AuthResponse;
import org.gwozdz1uu.heyobackend.dto.RegisterRequest;
import org.gwozdz1uu.heyobackend.model.Profile;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.repository.ProfileRepository;
import org.gwozdz1uu.heyobackend.repository.UserRepository;
import org.gwozdz1uu.heyobackend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Generate username from email (part before @)
        String baseUsername = request.getEmail().split("@")[0];

        // Ensure minimum length required by User.username constraints (3–50)
        if (baseUsername.length() < 3) {
            StringBuilder sb = new StringBuilder(baseUsername);
            while (sb.length() < 3) {
                sb.append('x');
            }
            baseUsername = sb.toString();
        }
        if (baseUsername.length() > 50) {
            baseUsername = baseUsername.substring(0, 50);
        }

        String username = baseUsername;
        int counter = 1;

        // Ensure username is unique and still fits into 50 characters
        while (userRepository.existsByUsername(username)) {
            String suffix = String.valueOf(counter);
            int maxBaseLength = 50 - suffix.length();
            String truncatedBase = baseUsername.length() > maxBaseLength
                    ? baseUsername.substring(0, maxBaseLength)
                    : baseUsername;
            username = truncatedBase + suffix;
            counter++;

            // Safety check to prevent infinite loop
            if (counter > 10000) {
                throw new RuntimeException("Unable to generate unique username");
            }
        }

        User user = User.builder()
                .username(username)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .avatarUrl("https://i.pravatar.cc/100?u=" + username)
                .build();

        user = userRepository.save(user);

        // Create empty profile
        Profile profile = Profile.builder()
                .user(user)
                .build();
        profileRepository.save(profile);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .profileCompleted(false)
                .build();
    }

    /**
     * Authenticate user and generate JWT token
     * Supports login by username or email
     */
    public AuthResponse login(AuthRequest request) {
        log.debug("Login attempt for user: {}", request.getUsername());
        
        // Check if user exists first for debugging
        var existingUser = userRepository.findByUsernameOrEmail(request.getUsername());
        if (existingUser.isEmpty()) {
            log.warn("User not found: {}", request.getUsername());
        } else {
            log.debug("User found: {}, checking password...", existingUser.get().getUsername());
            boolean matches = passwordEncoder.matches(request.getPassword(), existingUser.get().getPassword());
            log.debug("Password matches: {}", matches);
        }
        
        // Authenticate using Spring Security (uses UserDetailsService which supports both username and email)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Find user by username or email
        User user = userRepository.findByUsernameOrEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user);
        log.info("Login successful for user: {}", user.getUsername());

        // Check if profile is completed (has firstName and lastName)
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        boolean profileCompleted = profile != null && 
                profile.getFirstName() != null && !profile.getFirstName().isEmpty() &&
                profile.getLastName() != null && !profile.getLastName().isEmpty();

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .profileCompleted(profileCompleted)
                .build();
    }
}
