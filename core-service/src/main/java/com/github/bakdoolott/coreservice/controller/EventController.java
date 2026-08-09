package com.github.bakdoolott.coreservice.controller;

import com.github.bakdoolott.coreservice.dto.EventAdminDTO;
import com.github.bakdoolott.coreservice.dto.EventBannerDTO;
import com.github.bakdoolott.coreservice.dto.EventCreateRequest;
import com.github.bakdoolott.coreservice.models.Event;
import com.github.bakdoolott.coreservice.service.EventService;
import com.github.bakdoolott.coreservice.service.impl.EventServiceImpl;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;
    @Autowired
    public EventController(EventService eventService){
        this.eventService = eventService;
    }
    @GetMapping("/banner")
    public List<EventBannerDTO> getActiveBanners(){
        List<EventBannerDTO> banner = eventService.getActiveBanners();
        return banner;
    }
    @GetMapping("/admin")
    public List<EventAdminDTO> getAllEvents(){
        List<EventAdminDTO> events = eventService.getAllForAdmin();
        return events;
    }
    @PostMapping("/admin")
    public ResponseEntity<EventAdminDTO> createEvent(@Valid @RequestBody EventCreateRequest request){
        String createdBy = SecurityContextHolder.getContext().getAuthentication().getName();
        EventAdminDTO saved = eventService.createEvent(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    @PatchMapping("/admin/{id}")
    public ResponseEntity<EventAdminDTO> updateEvent(@PathVariable Long id,@RequestBody EventCreateRequest request){
        EventAdminDTO saved = eventService.updateEvent(id, request);
        return ResponseEntity.ok().body(saved);
    }
    @PatchMapping("/admin/{id}/disable")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id){
       eventService.deleteEvent(id);
       return ResponseEntity.noContent().build();
    }
    @GetMapping("/admin/{id}/publish")
    public ResponseEntity<EventAdminDTO> publishEvent(@PathVariable Long id){
        EventAdminDTO event = eventService.publishEvent(id);
        return ResponseEntity.ok(event);
    }
    @GetMapping("/admin/{id}/unpublish")
    public ResponseEntity<EventAdminDTO> unpublishEvent(@PathVariable Long id){
        EventAdminDTO event = eventService.unpublishEvent(id);
        return ResponseEntity.ok(event);
    }


}
