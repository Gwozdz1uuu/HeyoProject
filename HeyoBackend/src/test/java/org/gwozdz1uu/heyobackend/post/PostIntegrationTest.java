package org.gwozdz1uu.heyobackend.post;

import org.gwozdz1uu.heyobackend.post.controller.PostController;
import org.gwozdz1uu.heyobackend.post.dto.PostCreateRequest;
import org.gwozdz1uu.heyobackend.post.dto.PostDTO;
import org.gwozdz1uu.heyobackend.post.service.PostService;
import org.gwozdz1uu.heyobackend.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PostIntegrationTest {

    @Autowired(required = false)
    private PostController postController;

    @Autowired(required = false)
    private PostService postService;

    @MockBean
    private PostRepository postRepository;

    @Test
    void testPostControllerIsLoaded() {
        assertNotNull(postController, "PostController should be loaded in Spring context");
    }

    @Test
    void testPostServiceIsLoaded() {
        assertNotNull(postService, "PostService should be loaded in Spring context");
    }

    @Test
    void testPostComponentsAreProperlyWired() {
        assertAll(
                () -> assertNotNull(postController, "PostController should be available"),
                () -> assertNotNull(postService, "PostService should be available")
        );
    }

    @Test
    void testPostCreateRequestDtoStructure() {
        PostCreateRequest request = new PostCreateRequest();
        request.setContent("Test content");
        request.setImageUrl("https://example.com/image.jpg");

        assertAll(
                () -> assertEquals("Test content", request.getContent()),
                () -> assertEquals("https://example.com/image.jpg", request.getImageUrl()),
                () -> assertNotNull(request, "PostCreateRequest should be instantiable")
        );
    }

    @Test
    void testPostDTOStructure() {
        PostDTO postDTO = PostDTO.builder()
                .id(1L)
                .authorId(1L)
                .content("Test content")
                .imageUrl("https://example.com/image.jpg")
                .likesCount(5)
                .commentsCount(3)
                .likedByCurrentUser(false)
                .build();

        assertAll(
                () -> assertEquals(1L, postDTO.getId()),
                () -> assertEquals("Test content", postDTO.getContent()),
                () -> assertEquals("https://example.com/image.jpg", postDTO.getImageUrl()),
                () -> assertEquals(5, postDTO.getLikesCount()),
                () -> assertEquals(3, postDTO.getCommentsCount()),
                () -> assertFalse(postDTO.isLikedByCurrentUser())
        );
    }
}
