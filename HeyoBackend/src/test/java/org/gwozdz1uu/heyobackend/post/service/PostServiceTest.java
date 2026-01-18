package org.gwozdz1uu.heyobackend.post.service;

import org.gwozdz1uu.heyobackend.dto.CommentDTO;
import org.gwozdz1uu.heyobackend.model.Comment;
import org.gwozdz1uu.heyobackend.model.Post;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.post.dto.PostCreateRequest;
import org.gwozdz1uu.heyobackend.post.dto.PostDTO;
import org.gwozdz1uu.heyobackend.repository.CommentRepository;
import org.gwozdz1uu.heyobackend.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Tests")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private org.gwozdz1uu.heyobackend.notification.service.NotificationService notificationService;

    @InjectMocks
    private PostService postService;

    private User author;
    private User currentUser;
    private Post post;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .id(1L)
                .username("author")
                .email("author@test.com")
                .password("password")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();

        currentUser = User.builder()
                .id(2L)
                .username("currentUser")
                .email("user@test.com")
                .password("password")
                .build();

        post = Post.builder()
                .id(1L)
                .author(author)
                .content("Test post content")
                .imageUrl("https://example.com/image.jpg")
                .likes(new HashSet<>())
                .comments(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create post with content only")
    void testCreatePost_WithContentOnly() {
        // Arrange
        PostCreateRequest request = new PostCreateRequest();
        request.setContent("New post content");
        request.setImageUrl(null);

        Post savedPost = Post.builder()
                .id(1L)
                .author(author)
                .content("New post content")
                .imageUrl(null)
                .likes(new HashSet<>())
                .comments(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // Act
        PostDTO result = postService.createPost(request, author);

        // Assert
        assertNotNull(result);
        assertEquals("New post content", result.getContent());
        assertNull(result.getImageUrl());
        assertEquals(author.getId(), result.getAuthorId());
        assertEquals(author.getUsername(), result.getAuthorUsername());
        
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("Should create post with content and image URL")
    void testCreatePost_WithContentAndImageUrl() {
        // Arrange
        PostCreateRequest request = new PostCreateRequest();
        request.setContent("Post with image");
        request.setImageUrl("https://example.com/test-image.png");

        Post savedPost = Post.builder()
                .id(1L)
                .author(author)
                .content("Post with image")
                .imageUrl("https://example.com/test-image.png")
                .likes(new HashSet<>())
                .comments(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // Act
        PostDTO result = postService.createPost(request, author);

        // Assert
        assertNotNull(result);
        assertEquals("Post with image", result.getContent());
        assertEquals("https://example.com/test-image.png", result.getImageUrl());
        assertEquals(author.getId(), result.getAuthorId());
        
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post capturedPost = postCaptor.getValue();
        assertEquals("Post with image", capturedPost.getContent());
        assertEquals("https://example.com/test-image.png", capturedPost.getImageUrl());
        assertEquals(author, capturedPost.getAuthor());
    }

    @Test
    @DisplayName("Should handle different image URL formats")
    void testCreatePost_WithDifferentImageUrlFormats() {
        // Test various URL formats
        String[] imageUrls = {
                "https://example.com/image.jpg",
                "http://example.com/image.png",
                "https://example.com/image.gif",
                "https://cdn.example.com/uploads/image.webp"
        };

        for (String imageUrl : imageUrls) {
            PostCreateRequest request = new PostCreateRequest();
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

            assertEquals(imageUrl, result.getImageUrl());
        }
    }

    @Test
    @DisplayName("Should get post by ID")
    void testGetPost() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // Act
        PostDTO result = postService.getPost(1L, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(post.getId(), result.getId());
        assertEquals(post.getContent(), result.getContent());
        assertEquals(post.getImageUrl(), result.getImageUrl());
        assertEquals(author.getId(), result.getAuthorId());
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when post not found")
    void testGetPost_NotFound() {
        // Arrange
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            postService.getPost(999L, currentUser);
        }, "Post not found");
    }

    @Test
    @DisplayName("Should get feed with pagination")
    void testGetFeed() {
        // Arrange
        List<Post> posts = Arrays.asList(post);
        Page<Post> postPage = new PageImpl<>(posts, PageRequest.of(0, 20), 1);
        
        when(postRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(postPage);

        // Act
        Page<PostDTO> result = postService.getFeed(currentUser, PageRequest.of(0, 20));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(post.getContent(), result.getContent().get(0).getContent());
        verify(postRepository).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get user posts")
    void testGetUserPosts() {
        // Arrange
        List<Post> posts = Arrays.asList(post);
        Page<Post> postPage = new PageImpl<>(posts, PageRequest.of(0, 20), 1);
        
        when(postRepository.findByAuthorIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class))).thenReturn(postPage);

        // Act
        Page<PostDTO> result = postService.getUserPosts(1L, currentUser, PageRequest.of(0, 20));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(author.getId(), result.getContent().get(0).getAuthorId());
        verify(postRepository).findByAuthorIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should like post when not liked")
    void testLikePost_AddLike() {
        // Arrange
        post.setLikes(new HashSet<>());
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        // Act
        PostDTO result = postService.likePost(1L, currentUser);

        // Assert
        assertNotNull(result);
        verify(postRepository).findById(1L);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("Should unlike post when already liked")
    void testLikePost_RemoveLike() {
        // Arrange
        post.setLikes(new HashSet<>(Collections.singletonList(currentUser)));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        // Act
        PostDTO result = postService.likePost(1L, currentUser);

        // Assert
        assertNotNull(result);
        verify(postRepository).findById(1L);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("Should add comment to post")
    void testAddComment() {
        // Arrange
        Comment comment = Comment.builder()
                .id(1L)
                .post(post)
                .author(currentUser)
                .content("Test comment")
                .createdAt(LocalDateTime.now())
                .build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        // Act
        CommentDTO result = postService.addComment(1L, "Test comment", currentUser);

        // Assert
        assertNotNull(result);
        assertEquals("Test comment", result.getContent());
        assertEquals(currentUser.getId(), result.getAuthorId());
        verify(postRepository).findById(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should get comments for post")
    void testGetComments() {
        // Arrange
        Comment comment1 = Comment.builder()
                .id(1L)
                .post(post)
                .author(currentUser)
                .content("Comment 1")
                .createdAt(LocalDateTime.now())
                .build();

        Comment comment2 = Comment.builder()
                .id(2L)
                .post(post)
                .author(author)
                .content("Comment 2")
                .createdAt(LocalDateTime.now().plusMinutes(1))
                .build();

        when(commentRepository.findByPostIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(comment2, comment1));

        // Act
        List<CommentDTO> result = postService.getComments(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(commentRepository).findByPostIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("Should delete post by author")
    void testDeletePost_ByAuthor() {
        // Arrange
        post.setAuthor(author);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        doNothing().when(postRepository).delete(any(Post.class));

        // Act
        postService.deletePost(1L, author);

        // Assert
        verify(postRepository).findById(1L);
        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("Should throw exception when deleting post by non-author")
    void testDeletePost_NotAuthorized() {
        // Arrange
        post.setAuthor(author);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            postService.deletePost(1L, currentUser);
        }, "Not authorized to delete this post");

        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("Should correctly map Post to PostDTO with image URL")
    void testPostDTO_MappingWithImageUrl() {
        // Arrange
        post.setImageUrl("https://example.com/image.jpg");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // Act
        PostDTO result = postService.getPost(1L, currentUser);

        // Assert
        assertEquals(post.getImageUrl(), result.getImageUrl());
        assertEquals(post.getContent(), result.getContent());
        assertEquals(post.getLikesCount(), result.getLikesCount());
        assertEquals(post.getCommentsCount(), result.getCommentsCount());
    }

    @Test
    @DisplayName("Should correctly map Post to PostDTO without image URL")
    void testPostDTO_MappingWithoutImageUrl() {
        // Arrange
        post.setImageUrl(null);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // Act
        PostDTO result = postService.getPost(1L, currentUser);

        // Assert
        assertNull(result.getImageUrl());
        assertEquals(post.getContent(), result.getContent());
    }
}
