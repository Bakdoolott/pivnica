package com.github.bakdoolott.coreservice.models;

import com.github.bakdoolott.coreservice.models.enums.EventStatus;
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
    @SequenceGenerator(
            name = "event_seq",
            sequenceName = "event_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_seq")
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

    @Column(nullable = false)
    boolean enable = true;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;
}
