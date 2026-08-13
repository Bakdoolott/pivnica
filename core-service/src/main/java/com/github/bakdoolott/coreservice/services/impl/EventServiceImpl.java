package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.models.dto.response.EventResponse;
import com.github.bakdoolott.coreservice.models.dto.response.EventBannerDTO;
import com.github.bakdoolott.coreservice.models.dto.request.EventCreateRequest;
import com.github.bakdoolott.coreservice.models.enums.EventStatus;
import com.github.bakdoolott.coreservice.services.EventService;
import com.github.bakdoolott.coreservice.repositories.EventRepository;
import com.github.bakdoolott.coreservice.mappers.EventMapper;
import com.github.bakdoolott.coreservice.exceptions.NotFoundException;
import jakarta.transaction.Transactional;
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
    public List<EventResponse> getAllForAdmin(){
        List<Event> events = eventRepository.findAll();
        return events.stream()
                .map(event -> eventMapper.toResponse(event))
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public EventResponse createEvent(EventCreateRequest request, String createdBy){
        Event event = eventMapper.toEntity(request, createdBy);
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }
    @Override
    @Transactional
    public EventResponse publishEvent(Long id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found Event " + id));
        event.setEventStatus(EventStatus.ENABLE);
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }
    @Override
    @Transactional
    public EventResponse unpublishEvent(Long id){
        Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found Event " + id));
        event.setEventStatus(EventStatus.UNAVAILABLE);
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }
    @Override
    @Transactional
    public void deleteEvent(Long id){
        Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found Event " + id));
        event.setEnable(false);
        eventRepository.save(event);
    }
    @Override
    @Transactional
    public EventResponse updateEvent(Long id, EventCreateRequest request){
        Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found Event " + id));
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEndsAt(request.getEndsAt());
        event.setStartsAt(request.getStartsAt());
        event.setImageName(request.getImageName());
        event.setImageUrl(request.getImageUrl());
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }
}
