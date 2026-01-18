package org.gwozdz1uu.heyobackend.repository;

import org.gwozdz1uu.heyobackend.model.Comment;
import org.gwozdz1uu.heyobackend.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);
    int countByPost(Post post);
}
