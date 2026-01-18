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
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .avatarUrl("https://i.pravatar.cc/100?u=" + request.getUsername())
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

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
