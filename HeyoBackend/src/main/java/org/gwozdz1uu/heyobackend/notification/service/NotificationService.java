package org.gwozdz1uu.heyobackend.notification.service;

import lombok.RequiredArgsConstructor;
import org.gwozdz1uu.heyobackend.model.Notification;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.notification.dto.NotificationDTO;
import org.gwozdz1uu.heyobackend.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Page<NotificationDTO> getNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::toDTO);
    }

    public int getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    public void createNotification(User user, User actor, Notification.NotificationType type, String message, Long referenceId) {
        Notification notification = Notification.builder()
                .user(user)
                .actor(actor)
                .type(type)
                .message(message)
                .referenceId(referenceId)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .actorUsername(notification.getActor() != null ? notification.getActor().getUsername() : null)
                .actorAvatarUrl(notification.getActor() != null ? notification.getActor().getAvatarUrl() : null)
                .type(notification.getType().name())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
