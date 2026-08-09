package com.github.bakdoolott.coreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventBannerDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
}