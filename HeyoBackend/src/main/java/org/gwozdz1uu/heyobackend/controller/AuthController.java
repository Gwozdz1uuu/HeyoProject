package org.gwozdz1uu.heyobackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.gwozdz1uu.heyobackend.dto.AuthRequest;
import org.gwozdz1uu.heyobackend.dto.AuthResponse;
import org.gwozdz1uu.heyobackend.dto.RegisterRequest;
import org.gwozdz1uu.heyobackend.repository.UserRepository;
import org.gwozdz1uu.heyobackend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    /**
     * Debug endpoint to check password encoding
     * Only for development - remove in production
     */
    @GetMapping("/debug/{username}")
    public ResponseEntity<Map<String, Object>> debugUser(@PathVariable String username) {
        Map<String, Object> result = new HashMap<>();
        
        var userOpt = userRepository.findByUsernameOrEmail(username);
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            result.put("found", true);
            result.put("username", user.getUsername());
            result.put("email", user.getEmail());
            result.put("passwordHash", user.getPassword());
            result.put("passwordHashLength", user.getPassword().length());
            // Test password matching
            result.put("testPassword123Match", passwordEncoder.matches("password123", user.getPassword()));
            result.put("newHash", passwordEncoder.encode("password123"));
        } else {
            result.put("found", false);
            result.put("searchedFor", username);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Debug endpoint to reset user password
     * Only for development - remove in production
     */
    @PostMapping("/debug/reset-password/{username}")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @PathVariable String username,
            @RequestParam(defaultValue = "password123") String newPassword) {
        Map<String, Object> result = new HashMap<>();
        
        var userOpt = userRepository.findByUsernameOrEmail(username);
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            String newHash = passwordEncoder.encode(newPassword);
            user.setPassword(newHash);
            userRepository.save(user);
            
            result.put("success", true);
            result.put("username", user.getUsername());
            result.put("newPasswordHash", newHash);
            result.put("verifyMatch", passwordEncoder.matches(newPassword, newHash));
        } else {
            result.put("success", false);
            result.put("error", "User not found: " + username);
        }
        
        return ResponseEntity.ok(result);
    }
}
