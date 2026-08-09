package com.github.bakdoolott.coreservice.dto;

import com.github.bakdoolott.coreservice.models.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAdminDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String imageName;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private EventStatus eventStatus;
    private boolean enable;
}