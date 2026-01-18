package org.gwozdz1uu.heyobackend.post;

import org.gwozdz1uu.heyobackend.model.Post;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.post.dto.PostCreateRequest;
import org.gwozdz1uu.heyobackend.post.dto.PostDTO;
import org.gwozdz1uu.heyobackend.post.service.PostService;
import org.gwozdz1uu.heyobackend.repository.CommentRepository;
import org.gwozdz1uu.heyobackend.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Post Creation Flow Tests")
class PostCreationFlowTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private PostService postService;

    private User author;
    private PostCreateRequest request;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .id(1L)
                .username("author")
                .email("author@test.com")
                .password("password")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();

        request = new PostCreateRequest();
    }

    @Test
    @DisplayName("Should create post with text only - complete flow")
    void testCreatePostFlow_TextOnly() {
        // Arrange
        request.setContent("This is a text-only post");
        request.setImageUrl(null);

        Post savedPost = Post.builder()
                .id(1L)
                .author(author)
                .content("This is a text-only post")
                .imageUrl(null)
                .likes(new HashSet<>())
                .comments(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // Act
        PostDTO result = postService.createPost(request, author);

        // Assert - Verify complete flow
        assertNotNull(result, "PostDTO should not be null");
        assertEquals("This is a text-only post", result.getContent(), "Content should match");
        assertNull(result.getImageUrl(), "Image URL should be null for text-only post");
        assertEquals(author.getId(), result.getAuthorId(), "Author ID should match");
        assertEquals(author.getUsername(), result.getAuthorUsername(), "Author username should match");
        assertEquals(0, result.getLikesCount(), "New post should have 0 likes");
        assertEquals(0, result.getCommentsCount(), "New post should have 0 comments");
        assertFalse(result.isLikedByCurrentUser(), "New post should not be liked by current user");
        assertNotNull(result.getCreatedAt(), "Created at should be set");

        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Should create post with image URL - complete flow")
    void testCreatePostFlow_WithImageUrl() {
        // Arrange
        String imageUrl = "https://example.com/uploads/photo123.jpg";
        request.setContent("Check out this amazing photo!");
        request.setImageUrl(imageUrl);

        Post savedPost = Post.builder()
                .id(2L)
                .author(author)
                .content("Check out this amazing photo!")
                .imageUrl(imageUrl)
                .likes(new HashSet<>())
                .comments(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // Act
        PostDTO result = postService.createPost(request, author);

        // Assert - Verify complete flow with image
        assertNotNull(result, "PostDTO should not be null");
        assertEquals("Check out this amazing photo!", result.getContent(), "Content should match");
        assertEquals(imageUrl, result.getImageUrl(), "Image URL should match");
        assertEquals(author.getId(), result.getAuthorId(), "Author ID should match");
        assertNotNull(result.getCreatedAt(), "Created at should be set");

        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Should handle various image URL formats")
    void testCreatePostFlow_VariousImageUrlFormats() {
        String[] imageUrls = {
                "https://example.com/image.jpg",
                "http://example.com/image.png",
                "https://cdn.example.com/path/to/image.gif",
                "https://example.com/image.webp",
                "https://example.com/images/photo_2024_01_15.png"
        };

        for (String imageUrl : imageUrls) {
            request.setContent("Post with " + imageUrl);
            request.setImageUrl(imageUrl);

            Post savedPost = Post.builder()
                    .id(1L)
                    .author(author)
                    .content("Post with " + imageUrl)
                    .imageUrl(imageUrl)
                    .likes(new HashSet<>())
                    .comments(new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .build();

            when(postRepository.save(any(Post.class))).thenReturn(savedPost);

            PostDTO result = postService.createPost(request, author);

            assertEquals(imageUrl, result.getImageUrl(), 
                    "Image URL should be preserved: " + imageUrl);
        }
    }

    @Test
    @DisplayName("Should handle empty string image URL")
    void testCreatePostFlow_EmptyStringImageUrl() {
        // Arrange
        request.setContent("Post with empty image URL");
        request.setImageUrl("");

        Post savedPost = Post.builder()
                .id(1L)
                .author(author)
                .content("Post with empty image URL")
                .imageUrl("")
                .likes(new HashSet<>())
                .comments(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // Act
        PostDTO result = postService.createPost(request, author);

        // Assert
        assertEquals("", result.getImageUrl(), "Empty string image URL should be preserved");
    }

    @Test
    @DisplayName("Should preserve all post properties after creation")
    void testCreatePostFlow_PreserveAllProperties() {
        // Arrange
        request.setContent("Comprehensive test post");
        request.setImageUrl("https://example.com/comprehensive.jpg");

        Post savedPost = Post.builder()
                .id(100L)
                .author(author)
                .content("Comprehensive test post")
                .imageUrl("https://example.com/comprehensive.jpg")
                .likes(new HashSet<>())
                .comments(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // Act
        PostDTO result = postService.createPost(request, author);

        // Assert - Verify all properties
        assertAll(
                () -> assertEquals(100L, result.getId()),
                () -> assertEquals("Comprehensive test post", result.getContent()),
                () -> assertEquals("https://example.com/comprehensive.jpg", result.getImageUrl()),
                () -> assertEquals(author.getId(), result.getAuthorId()),
                () -> assertEquals(author.getUsername(), result.getAuthorUsername()),
                () -> assertEquals(author.getAvatarUrl(), result.getAuthorAvatarUrl()),
                () -> assertEquals(0, result.getLikesCount()),
                () -> assertEquals(0, result.getCommentsCount()),
                () -> assertFalse(result.isLikedByCurrentUser()),
                () -> assertNotNull(result.getCreatedAt())
        );
    }
}
