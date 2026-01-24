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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Page<EventDTO> getUserEvents(User user, Pageable pageable) {
        // Get all events related to user: created, participating, or interested
        Set<Event> userEvents = new HashSet<>();
        
        // Add events created by user
        userEvents.addAll(eventRepository.findByCreatorIdOrderByEventDateDesc(user.getId()));
        
        // Add events where user is participating
        Page<Event> participatingPage = eventRepository.findByParticipantsIdOrderByEventDateDesc(user.getId(), Pageable.unpaged());
        userEvents.addAll(participatingPage.getContent());
        
        // Add events where user is interested
        Page<Event> interestedPage = eventRepository.findByInterestedUsersIdOrderByEventDateDesc(user.getId(), Pageable.unpaged());
        userEvents.addAll(interestedPage.getContent());
        
        // Convert to sorted list (by event date descending)
        var sortedEvents = userEvents.stream()
                .sorted((e1, e2) -> e2.getEventDate().compareTo(e1.getEventDate()))
                .collect(Collectors.toList());
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sortedEvents.size());
        var paginatedEvents = sortedEvents.subList(start, end);
        
        // Convert to DTOs
        var eventDTOs = paginatedEvents.stream()
                .map(event -> toDTO(event, user))
                .collect(Collectors.toList());
        
        // Create Page manually
        return new org.springframework.data.domain.PageImpl<>(
                eventDTOs,
                pageable,
                sortedEvents.size()
        );
    }

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
        // Safely get collection sizes, handling potential null or lazy initialization
        int interestedCount = 0;
        int participantsCount = 0;
        boolean isInterested = false;
        boolean isParticipating = false;
        
        try {
            if (event.getInterestedUsers() != null) {
                interestedCount = event.getInterestedUsers().size();
                isInterested = event.getInterestedUsers().contains(currentUser);
            }
        } catch (Exception e) {
            // Collection not initialized or null, use defaults
            interestedCount = 0;
            isInterested = false;
        }
        
        try {
            if (event.getParticipants() != null) {
                participantsCount = event.getParticipants().size();
                isParticipating = event.getParticipants().contains(currentUser);
            }
        } catch (Exception e) {
            // Collection not initialized or null, use defaults
            participantsCount = 0;
            isParticipating = false;
        }
        
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
                .interestedCount(interestedCount)
                .participantsCount(participantsCount)
                .isInterested(isInterested)
                .isParticipating(isParticipating)
                .createdAt(event.getCreatedAt())
                .build();
    }
}
