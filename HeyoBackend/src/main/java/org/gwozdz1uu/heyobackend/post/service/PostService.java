package org.gwozdz1uu.heyobackend.post.service;

import lombok.RequiredArgsConstructor;
import org.gwozdz1uu.heyobackend.dto.CommentDTO;
import org.gwozdz1uu.heyobackend.model.Comment;
import org.gwozdz1uu.heyobackend.model.Notification;
import org.gwozdz1uu.heyobackend.model.Post;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.notification.service.NotificationService;
import org.gwozdz1uu.heyobackend.repository.CommentRepository;
import org.gwozdz1uu.heyobackend.repository.PostRepository;
import org.gwozdz1uu.heyobackend.post.dto.PostCreateRequest;
import org.gwozdz1uu.heyobackend.post.dto.PostDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    public Page<PostDTO> getFeed(User currentUser, Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(post -> toDTO(post, currentUser));
    }

    public Page<PostDTO> getUserPosts(Long userId, User currentUser, Pageable pageable) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId, pageable)
                .map(post -> toDTO(post, currentUser));
    }

    public PostDTO getPost(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return toDTO(post, currentUser);
    }

    @Transactional
    public PostDTO createPost(PostCreateRequest request, User author) {
        Post post = Post.builder()
                .author(author)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .build();

        post = postRepository.save(post);
        return toDTO(post, author);
    }

    @Transactional
    public PostDTO likePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        boolean wasLiked = post.getLikes().contains(user);
        if (wasLiked) {
            post.getLikes().remove(user);
        } else {
            post.getLikes().add(user);
            
            // Create notification only when liking (not unliking) and not for own posts
            if (!post.getAuthor().getId().equals(user.getId())) {
                String message = user.getUsername() + " polubił Twój post";
                notificationService.createNotification(
                        post.getAuthor(),
                        user,
                        Notification.NotificationType.NEW_LIKE,
                        message,
                        postId
                );
            }
        }

        post = postRepository.save(post);
        return toDTO(post, user);
    }

    @Transactional
    public CommentDTO addComment(Long postId, String content, User author) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .content(content)
                .build();

        comment = commentRepository.save(comment);
        
        // Create notification for comment (not for own posts)
        if (!post.getAuthor().getId().equals(author.getId())) {
            String message = author.getUsername() + " skomentował Twój post";
            notificationService.createNotification(
                    post.getAuthor(),
                    author,
                    Notification.NotificationType.NEW_COMMENT,
                    message,
                    postId
            );
        }
        
        return toCommentDTO(comment);
    }

    public List<CommentDTO> getComments(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId)
                .stream()
                .map(this::toCommentDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to delete this post");
        }

        postRepository.delete(post);
    }

    private PostDTO toDTO(Post post, User currentUser) {
        return PostDTO.builder()
                .id(post.getId())
                .authorId(post.getAuthor().getId())
                .authorUsername(post.getAuthor().getUsername())
                .authorAvatarUrl(post.getAuthor().getAvatarUrl())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .likedByCurrentUser(post.getLikes() != null && post.getLikes().contains(currentUser))
                .createdAt(post.getCreatedAt())
                .build();
    }

    private CommentDTO toCommentDTO(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .authorId(comment.getAuthor().getId())
                .authorUsername(comment.getAuthor().getUsername())
                .authorAvatarUrl(comment.getAuthor().getAvatarUrl())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
