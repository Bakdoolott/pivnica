package com.github.bakdoolott.coreservice.event;

import com.github.bakdoolott.coreservice.event.enums.EventStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 255)
    String title;

    String description;

    @Column(name = "image_url")
    String imageUrl;

    @Column(name = "image_name")
    String imageName;

    @Column(name = "starts_at", nullable = false)
    LocalDateTime startsAt;

    @Column(name = "ends_at")
    LocalDateTime endsAt;

    @Column(name = "created_by")
    String createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status",nullable = false)
    EventStatus eventStatus;
}
