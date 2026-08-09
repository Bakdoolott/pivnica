package com.github.bakdoolott.coreservice.repository;

import com.github.bakdoolott.coreservice.models.Event;
import com.github.bakdoolott.coreservice.models.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByEnableTrueAndEventStatusAndEndsAtAfterOrderByStartsAtAsc(
            EventStatus eventStatus,
            LocalDateTime now
    );
}