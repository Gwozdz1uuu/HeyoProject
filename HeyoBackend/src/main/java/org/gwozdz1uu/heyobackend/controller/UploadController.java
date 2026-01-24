package org.gwozdz1uu.heyobackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String frontendUrl;

    private static final String AVATAR_UPLOAD_DIR = "uploads/avatars/";
    private static final String EVENT_UPLOAD_DIR = "uploads/events/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp"};

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                response.put("error", "File size exceeds 5MB limit");
                return ResponseEntity.badRequest().body(response);
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                response.put("error", "Invalid filename");
                return ResponseEntity.badRequest().body(response);
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            boolean isValidExtension = false;
            for (String allowedExt : ALLOWED_EXTENSIONS) {
                if (extension.equals(allowedExt)) {
                    isValidExtension = true;
                    break;
                }
            }

            if (!isValidExtension) {
                response.put("error", "Invalid file type. Allowed: JPG, PNG, WebP");
                return ResponseEntity.badRequest().body(response);
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(AVATAR_UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return URL - relative path that will be served by WebMvcConfig
            String fileUrl = "/api/uploads/avatars/" + filename;
            response.put("url", fileUrl);
            response.put("filename", filename);

            log.info("Avatar uploaded successfully: {}", filename);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error uploading avatar", e);
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/event-image")
    public ResponseEntity<Map<String, String>> uploadEventImage(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                response.put("error", "File size exceeds 5MB limit");
                return ResponseEntity.badRequest().body(response);
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                response.put("error", "Invalid filename");
                return ResponseEntity.badRequest().body(response);
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            boolean isValidExtension = false;
            for (String allowedExt : ALLOWED_EXTENSIONS) {
                if (extension.equals(allowedExt)) {
                    isValidExtension = true;
                    break;
                }
            }

            if (!isValidExtension) {
                response.put("error", "Invalid file type. Allowed: JPG, PNG, WebP");
                return ResponseEntity.badRequest().body(response);
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(EVENT_UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return URL - relative path that will be served by WebMvcConfig
            String fileUrl = "/api/uploads/events/" + filename;
            response.put("url", fileUrl);
            response.put("filename", filename);

            log.info("Event image uploaded successfully: {}", filename);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error uploading event image", e);
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
