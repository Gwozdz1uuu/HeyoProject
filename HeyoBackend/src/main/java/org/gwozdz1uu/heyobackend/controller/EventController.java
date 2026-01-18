package org.gwozdz1uu.heyobackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.gwozdz1uu.heyobackend.dto.EventCreateRequest;
import org.gwozdz1uu.heyobackend.dto.EventDTO;
import org.gwozdz1uu.heyobackend.model.User;
import org.gwozdz1uu.heyobackend.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<EventDTO>> getUpcomingEvents(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(eventService.getUpcomingEvents(user, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventService.getEvent(id, user));
    }

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(
            @Valid @RequestBody EventCreateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventService.createEvent(request, user));
    }

    @PostMapping("/{id}/interested")
    public ResponseEntity<EventDTO> toggleInterested(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventService.toggleInterested(id, user));
    }

    @PostMapping("/{id}/participate")
    public ResponseEntity<EventDTO> toggleParticipating(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventService.toggleParticipating(id, user));
    }
}
