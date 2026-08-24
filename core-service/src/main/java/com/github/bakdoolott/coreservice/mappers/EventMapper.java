package com.github.bakdoolott.coreservice.mappers;

import com.github.bakdoolott.coreservice.models.dto.response.EventResponse;
import com.github.bakdoolott.coreservice.models.dto.response.EventBannerDTO;
import com.github.bakdoolott.coreservice.models.dto.request.EventCreateRequest;
import com.github.bakdoolott.coreservice.models.Event;
import com.github.bakdoolott.coreservice.models.enums.EventStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EventMapper {
    public EventBannerDTO toBannerDTO(Event event){
        return EventBannerDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .imageUrl(event.getImageUrl())
                .startsAt((event.getStartsAt()))
                .endsAt(event.getEndsAt())
                .build();
    }
    public EventResponse toResponse(Event event){
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .imageName(event.getImageName())
                .imageUrl(event.getImageUrl())
                .createdBy(event.getCreatedBy())
                .createdAt(event.getCreatedAt())
                .eventStatus(event.getEventStatus())
                .startsAt(event.getStartsAt())
                .endsAt(event.getEndsAt())
                .enable(event.isEnable())
                .build();
    }
    public Event toEntity(EventCreateRequest request, String createdBy){
        Event event =  new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setImageUrl(request.getImageUrl());
        event.setImageName(request.getImageName());
        event.setStartsAt(request.getStartsAt());
        event.setEndsAt(request.getEndsAt());
        event.setEventStatus(EventStatus.UNAVAILABLE);
        event.setEnable(true);
        event.setCreatedAt(LocalDateTime.now());
        event.setCreatedBy(createdBy);
        return event;
    }

}