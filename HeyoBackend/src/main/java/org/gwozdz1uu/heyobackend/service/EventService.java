package org.gwozdz1uu.heyobackend.service;

import lombok.RequiredArgsConstructor;
import org.gwozdz1uu.heyobackend.dto.EventCreateRequest;
import org.gwozdz1uu.heyobackend.dto.EventDTO;
import org.gwozdz1uu.heyobackend.model.Event;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Page<EventDTO> getUpcomingEvents(User currentUser, Pageable pageable) {
        return eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable)
                .map(event -> toDTO(event, currentUser));
    }

    public EventDTO getEvent(Long eventId, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return toDTO(event, currentUser);
    }

    @Transactional
    public EventDTO createEvent(EventCreateRequest request, User creator) {
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .eventDate(request.getEventDate())
                .location(request.getLocation())
                .hashtags(request.getHashtags())
                .creator(creator)
                .build();

        event = eventRepository.save(event);
        return toDTO(event, creator);
    }

    @Transactional
    public EventDTO toggleInterested(Long eventId, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getInterestedUsers().contains(user)) {
            event.getInterestedUsers().remove(user);
        } else {
            event.getInterestedUsers().add(user);
        }

        event = eventRepository.save(event);
        return toDTO(event, user);
    }

    @Transactional
    public EventDTO toggleParticipating(Long eventId, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getParticipants().contains(user)) {
            event.getParticipants().remove(user);
        } else {
            event.getParticipants().add(user);
        }

        event = eventRepository.save(event);
        return toDTO(event, user);
    }

    private EventDTO toDTO(Event event, User currentUser) {
        return EventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .imageUrl(event.getImageUrl())
                .eventDate(event.getEventDate())
                .location(event.getLocation())
                .hashtags(event.getHashtags())
                .creatorId(event.getCreator().getId())
                .creatorUsername(event.getCreator().getUsername())
                .interestedCount(event.getInterestedUsers().size())
                .participantsCount(event.getParticipants().size())
                .isInterested(event.getInterestedUsers().contains(currentUser))
                .isParticipating(event.getParticipants().contains(currentUser))
                .createdAt(event.getCreatedAt())
                .build();
    }
}
