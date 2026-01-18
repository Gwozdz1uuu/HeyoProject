package org.gwozdz1uu.heyobackend.post.controller;

import lombok.RequiredArgsConstructor;
import org.gwozdz1uu.heyobackend.dto.CommentDTO;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.post.dto.PostCreateRequest;
import org.gwozdz1uu.heyobackend.post.dto.PostDTO;
import org.gwozdz1uu.heyobackend.post.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<Page<PostDTO>> getFeed(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(postService.getFeed(user, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostDTO>> getUserPosts(
            @PathVariable Long userId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(postService.getUserPosts(userId, user, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(postService.getPost(id, user));
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(
            @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(postService.createPost(request, user));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<PostDTO> likePost(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(postService.likePost(id, user));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(postService.addComment(id, body.get("content"), user));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getComments(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        postService.deletePost(id, user);
        return ResponseEntity.noContent().build();
    }
}
