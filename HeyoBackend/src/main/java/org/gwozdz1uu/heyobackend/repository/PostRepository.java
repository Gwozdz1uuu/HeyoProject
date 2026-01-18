package org.gwozdz1uu.heyobackend.repository;

import org.gwozdz1uu.heyobackend.model.Post;
import org.gwozdz1uu.heyobackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.author IN :users ORDER BY p.createdAt DESC")
    Page<Post> findByAuthorInOrderByCreatedAtDesc(@Param("users") List<User> users, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.author.id = :userId ORDER BY p.createdAt DESC")
    Page<Post> findByAuthorIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
}
