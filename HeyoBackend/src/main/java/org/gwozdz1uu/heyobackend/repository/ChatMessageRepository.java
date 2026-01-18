package org.gwozdz1uu.heyobackend.repository;

import org.gwozdz1uu.heyobackend.model.ChatMessage;
import org.gwozdz1uu.heyobackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender = :user1 AND m.receiver = :user2) OR " +
            "(m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.createdAt ASC")
    List<ChatMessage> findConversation(@Param("user1") User user1, @Param("user2") User user2);
    
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender = :user1 AND m.receiver = :user2) OR " +
            "(m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.createdAt DESC")
    Page<ChatMessage> findConversationPaged(@Param("user1") User user1, @Param("user2") User user2, Pageable pageable);
    
    @Modifying
    @Query("UPDATE ChatMessage m SET m.read = true WHERE m.sender = :sender AND m.receiver = :receiver AND m.read = false")
    void markAsRead(@Param("sender") User sender, @Param("receiver") User receiver);
    
    int countByReceiverAndReadFalse(User receiver);
    
    @Query("SELECT DISTINCT m.receiver FROM ChatMessage m WHERE m.sender = :user")
    List<User> findConversationPartnersAsReceiver(@Param("user") User user);
    
    @Query("SELECT DISTINCT m.sender FROM ChatMessage m WHERE m.receiver = :user")
    List<User> findConversationPartnersAsSender(@Param("user") User user);
}
