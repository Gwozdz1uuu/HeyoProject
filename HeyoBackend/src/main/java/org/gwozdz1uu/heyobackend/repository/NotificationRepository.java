package org.gwozdz1uu.heyobackend.repository;

import org.gwozdz1uu.heyobackend.model.Notification;
import org.gwozdz1uu.heyobackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user);
    int countByUserAndReadFalse(User user);
    
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user = :user")
    void markAllAsRead(User user);
    
    List<Notification> findByUserAndTypeAndReferenceId(User user, Notification.NotificationType type, Long referenceId);
    List<Notification> findByUserAndTypeAndActor(User user, Notification.NotificationType type, User actor);
    
    @Query("SELECT n FROM Notification n WHERE n.id = :id AND n.user.id = :userId")
    java.util.Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
