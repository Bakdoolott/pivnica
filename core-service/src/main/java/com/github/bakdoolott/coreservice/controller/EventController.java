package com.github.bakdoolott.coreservice.controller;

import com.github.bakdoolott.coreservice.models.dto.response.EventResponse;
import com.github.bakdoolott.coreservice.models.dto.response.EventBannerDTO;
import com.github.bakdoolott.coreservice.models.dto.request.EventCreateRequest;
import com.github.bakdoolott.coreservice.services.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/core/event")
@Tag(name = "мероприятия")
public class EventController {
    private final EventService eventService;
    @Autowired
    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/banner")
    @Operation(summary = "получение баннеров мероприятий",
    description = "находит баннеры с незаконченными мероприятиями")
    public List<EventBannerDTO> getActiveBanners(){
        List<EventBannerDTO> banner = eventService.getActiveBanners();
        return banner;
    }
    @GetMapping("/admin")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OWNER')")
    @Operation(summary = "получение всех баннеров")
    public List<EventResponse> getAllEvents(){
        List<EventResponse> events = eventService.getAllForAdmin();
        return events;
    }
    @PostMapping("/admin")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OWNER')")
    @Operation(summary = "создание мероприятия")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventCreateRequest request){
        String createdBy = SecurityContextHolder.getContext().getAuthentication().getName();
        EventResponse saved = eventService.createEvent(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    @PatchMapping("/admin/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OWNER')")
    @Operation(summary = "редактирование мероприятия",
    description = "обновляет отдельные поля")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id, @Valid @RequestBody EventCreateRequest request){
        EventResponse saved = eventService.updateEvent(id, request);
        return ResponseEntity.ok().body(saved);
    }
    @PatchMapping("/admin/{id}/disable")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OWNER')")
    @Operation(summary = "удаление мероприятия")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id){
       eventService.deleteEvent(id);
       return ResponseEntity.noContent().build();
    }
    @GetMapping("/admin/{id}/publish")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OWNER')")
    @Operation(summary = "публикация мероприятия в афишу")
    public ResponseEntity<EventResponse> publishEvent(@PathVariable Long id){
        EventResponse event = eventService.publishEvent(id);
        return ResponseEntity.ok(event);
    }
    @GetMapping("/admin/{id}/unpublish")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OWNER')")
    @Operation(summary = "удаление мероприятия из афиши")
    public ResponseEntity<EventResponse> unpublishEvent(@PathVariable Long id){
        EventResponse event = eventService.unpublishEvent(id);
        return ResponseEntity.ok(event);
    }


}
