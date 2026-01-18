package org.gwozdz1uu.heyobackend.repository;

import org.gwozdz1uu.heyobackend.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findAllByOrderByEventDateAsc(Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.eventDate > :now ORDER BY e.eventDate ASC")
    Page<Event> findUpcomingEvents(LocalDateTime now, Pageable pageable);
    
    List<Event> findByCreatorIdOrderByEventDateDesc(Long creatorId);
    
    @Query("SELECT e FROM Event e WHERE e.title LIKE %:query% OR e.description LIKE %:query%")
    List<Event> searchEvents(String query);
}
