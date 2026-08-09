package com.github.bakdoolott.coreservice.service.impl;

import com.github.bakdoolott.coreservice.dto.EventAdminDTO;
import com.github.bakdoolott.coreservice.dto.EventBannerDTO;
import com.github.bakdoolott.coreservice.dto.EventCreateRequest;
import com.github.bakdoolott.coreservice.models.enums.EventStatus;
import com.github.bakdoolott.coreservice.service.EventService;
import com.github.bakdoolott.coreservice.repository.EventRepository;
import com.github.bakdoolott.coreservice.mapper.EventMapper;
import com.github.bakdoolott.coreservice.service.exceptions.EventNotFoundException;
import org.springframework.stereotype.Service;
import com.github.bakdoolott.coreservice.models.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class EventServiceImpl implements EventService{
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository, EventMapper eventMapper){
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }
    @Override
    public List<EventBannerDTO> getActiveBanners(){
        List<Event> events = eventRepository.findByEnableTrueAndEventStatusAndEndsAtAfterOrderByStartsAtAsc(EventStatus.ENABLE, LocalDateTime.now());
        return events.stream()
                .map(event -> eventMapper.toBannerDTO(event))
                .collect(Collectors.toList());
    }
    @Override
    public List<EventAdminDTO> getAllForAdmin(){
        List<Event> events = eventRepository.findAll();
        return events.stream()
                .map(event -> eventMapper.toAdminDTO(event))
                .collect(Collectors.toList());
    }
    @Override
    public EventAdminDTO createEvent(EventCreateRequest request, String createdBy){
        Event event = eventMapper.toEntity(request, createdBy);
        Event saved = eventRepository.save(event);
        return eventMapper.toAdminDTO(saved);
    }
    @Override
    public EventAdminDTO publishEvent(Long id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
        event.setEventStatus(EventStatus.ENABLE);
        Event saved = eventRepository.save(event);
        return eventMapper.toAdminDTO(saved);
    }
    @Override
    public EventAdminDTO unpublishEvent(Long id){
        Event event = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
        event.setEventStatus(EventStatus.UNAVAILABLE);
        Event saved = eventRepository.save(event);
        return eventMapper.toAdminDTO(saved);
    }
    @Override
    public void deleteEvent(Long id){
        Event event = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
        event.setEnable(false);
        eventRepository.save(event);
    }
    @Override
    public EventAdminDTO updateEvent(Long id, EventCreateRequest request){
        Event event = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEndsAt(request.getEndsAt());
        event.setStartsAt(request.getStartsAt());
        event.setImageName(request.getImageName());
        event.setImageUrl(request.getImageUrl());
        Event saved = eventRepository.save(event);
        return eventMapper.toAdminDTO(saved);
    }
}
