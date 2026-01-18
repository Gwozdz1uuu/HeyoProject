package org.gwozdz1uu.heyobackend.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gwozdz1uu.heyobackend.dto.CommentDTO;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.post.dto.PostCreateRequest;
import org.gwozdz1uu.heyobackend.post.dto.PostDTO;
import org.gwozdz1uu.heyobackend.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@DisplayName("PostController Tests")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    private PostDTO postDTO;
    private PostCreateRequest postCreateRequest;

    @BeforeEach
    void setUp() {
        postDTO = PostDTO.builder()
                .id(1L)
                .authorId(1L)
                .authorUsername("author")
                .authorAvatarUrl("https://example.com/avatar.jpg")
                .content("Test post content")
                .imageUrl("https://example.com/image.jpg")
                .likesCount(5)
                .commentsCount(3)
                .likedByCurrentUser(false)
                .createdAt(LocalDateTime.now())
                .build();

        postCreateRequest = new PostCreateRequest();
        postCreateRequest.setContent("New post content");
        postCreateRequest.setImageUrl("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("Should create post with content and image URL")
    @WithMockUser
    void testCreatePost_WithContentAndImageUrl() throws Exception {
        // Arrange
        when(postService.createPost(any(PostCreateRequest.class), any(User.class)))
                .thenReturn(postDTO);

        // Act & Assert
        mockMvc.perform(post("/api/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Test post content"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/image.jpg"))
                .andExpect(jsonPath("$.authorId").value(1L));

        verify(postService).createPost(any(PostCreateRequest.class), any(User.class));
    }

    @Test
    @DisplayName("Should create post without image URL")
    @WithMockUser
    void testCreatePost_WithoutImageUrl() throws Exception {
        // Arrange
        PostCreateRequest requestWithoutImage = new PostCreateRequest();
        requestWithoutImage.setContent("Text only post");
        requestWithoutImage.setImageUrl(null);

        PostDTO postWithoutImage = PostDTO.builder()
                .id(2L)
                .authorId(1L)
                .authorUsername("author")
                .content("Text only post")
                .imageUrl(null)
                .likesCount(0)
                .commentsCount(0)
                .likedByCurrentUser(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(postService.createPost(any(PostCreateRequest.class), any(User.class)))
                .thenReturn(postWithoutImage);

        // Act & Assert
        mockMvc.perform(post("/api/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithoutImage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Text only post"))
                .andExpect(jsonPath("$.imageUrl").doesNotExist());

        verify(postService).createPost(any(PostCreateRequest.class), any(User.class));
    }

    @Test
    @DisplayName("Should get post by ID")
    @WithMockUser
    void testGetPost() throws Exception {
        // Arrange
        when(postService.getPost(1L, any(User.class))).thenReturn(postDTO);

        // Act & Assert
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Test post content"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/image.jpg"));

        verify(postService).getPost(1L, any(User.class));
    }

    @Test
    @DisplayName("Should get feed with pagination")
    @WithMockUser
    void testGetFeed() throws Exception {
        // Arrange
        Page<PostDTO> postPage = new PageImpl<>(Arrays.asList(postDTO), PageRequest.of(0, 20), 1);
        when(postService.getFeed(any(User.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(postPage);

        // Act & Assert
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].content").value("Test post content"));

        verify(postService).getFeed(any(User.class), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Should get user posts")
    @WithMockUser
    void testGetUserPosts() throws Exception {
        // Arrange
        Page<PostDTO> postPage = new PageImpl<>(Arrays.asList(postDTO), PageRequest.of(0, 20), 1);
        when(postService.getUserPosts(eq(1L), any(User.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(postPage);

        // Act & Assert
        mockMvc.perform(get("/api/posts/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].authorId").value(1L));

        verify(postService).getUserPosts(eq(1L), any(User.class), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Should like post")
    @WithMockUser
    void testLikePost() throws Exception {
        // Arrange
        PostDTO likedPost = PostDTO.builder()
                .id(1L)
                .likesCount(6)
                .likedByCurrentUser(true)
                .build();

        when(postService.likePost(1L, any(User.class))).thenReturn(likedPost);

        // Act & Assert
        mockMvc.perform(post("/api/posts/1/like")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesCount").value(6))
                .andExpect(jsonPath("$.likedByCurrentUser").value(true));

        verify(postService).likePost(1L, any(User.class));
    }

    @Test
    @DisplayName("Should add comment to post")
    @WithMockUser
    void testAddComment() throws Exception {
        // Arrange
        CommentDTO commentDTO = CommentDTO.builder()
                .id(1L)
                .postId(1L)
                .authorId(1L)
                .authorUsername("author")
                .content("Test comment")
                .createdAt(LocalDateTime.now())
                .build();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("content", "Test comment");

        when(postService.addComment(eq(1L), eq("Test comment"), any(User.class)))
                .thenReturn(commentDTO);

        // Act & Assert
        mockMvc.perform(post("/api/posts/1/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Test comment"))
                .andExpect(jsonPath("$.postId").value(1L));

        verify(postService).addComment(eq(1L), eq("Test comment"), any(User.class));
    }

    @Test
    @DisplayName("Should get comments for post")
    @WithMockUser
    void testGetComments() throws Exception {
        // Arrange
        CommentDTO comment1 = CommentDTO.builder()
                .id(1L)
                .content("Comment 1")
                .build();

        CommentDTO comment2 = CommentDTO.builder()
                .id(2L)
                .content("Comment 2")
                .build();

        when(postService.getComments(1L)).thenReturn(Arrays.asList(comment1, comment2));

        // Act & Assert
        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].content").value("Comment 1"))
                .andExpect(jsonPath("$[1].content").value("Comment 2"));

        verify(postService).getComments(1L);
    }

    @Test
    @DisplayName("Should delete post")
    @WithMockUser
    void testDeletePost() throws Exception {
        // Arrange
        doNothing().when(postService).deletePost(1L, any(User.class));

        // Act & Assert
        mockMvc.perform(delete("/api/posts/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(postService).deletePost(1L, any(User.class));
    }

    @Test
    @DisplayName("Should validate image URL in request")
    @WithMockUser
    void testCreatePost_WithValidImageUrl() throws Exception {
        // Arrange
        PostCreateRequest requestWithLongUrl = new PostCreateRequest();
        requestWithLongUrl.setContent("Post with valid image URL");
        requestWithLongUrl.setImageUrl("https://example.com/images/very/long/path/to/image.jpg");

        when(postService.createPost(any(PostCreateRequest.class), any(User.class)))
                .thenReturn(postDTO);

        // Act & Assert
        mockMvc.perform(post("/api/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithLongUrl)))
                .andExpect(status().isOk());

        verify(postService).createPost(any(PostCreateRequest.class), any(User.class));
    }

    @Test
    @DisplayName("Should handle empty content in request")
    @WithMockUser
    void testCreatePost_WithEmptyContent() throws Exception {
        // Arrange
        PostCreateRequest requestWithEmptyContent = new PostCreateRequest();
        requestWithEmptyContent.setContent("");
        requestWithEmptyContent.setImageUrl("https://example.com/image.jpg");

        PostDTO postWithEmptyContent = PostDTO.builder()
                .id(1L)
                .content("")
                .imageUrl("https://example.com/image.jpg")
                .build();

        when(postService.createPost(any(PostCreateRequest.class), any(User.class)))
                .thenReturn(postWithEmptyContent);

        // Act & Assert
        mockMvc.perform(post("/api/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithEmptyContent)))
                .andExpect(status().isOk());
    }
}
