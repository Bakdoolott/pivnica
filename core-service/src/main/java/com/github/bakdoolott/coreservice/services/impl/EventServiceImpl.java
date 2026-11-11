package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.models.dto.request.EventUpdateRequest;
import com.github.bakdoolott.coreservice.models.dto.response.EventResponse;
import com.github.bakdoolott.coreservice.models.dto.response.EventBannerDTO;
import com.github.bakdoolott.coreservice.models.dto.request.EventCreateRequest;
import com.github.bakdoolott.coreservice.models.enums.EventStatus;
import com.github.bakdoolott.coreservice.services.EventService;
import com.github.bakdoolott.coreservice.repositories.EventRepository;
import com.github.bakdoolott.coreservice.mappers.EventMapper;
import com.github.bakdoolott.coreservice.exceptions.NotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.github.bakdoolott.coreservice.models.Event;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService{
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final FileStorageService fileStorageService;


    @Override
    @Transactional
    public EventResponse createEvent(EventCreateRequest request, String createdBy) {
        String fileName = fileStorageService.store(request.getImage());

        Event event = eventMapper.toEntity(request, createdBy);
        event.setImageName(fileName);
        event.setImageUrl("/uploads/events/" + fileName);

        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }



    @Override
    @Transactional
    public void uploadImage(Long id, MultipartFile file) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not Found Event " + id));

        String oldImageName = event.getImageName();
        String newImageName = fileStorageService.store(file);

        event.setImageName(newImageName);
        event.setImageUrl("/uploads/events/" + newImageName);
        eventRepository.save(event);

        fileStorageService.delete(oldImageName);
    }

    @Override
    public List<EventBannerDTO> getActiveBanners(){
        List<Event> events = eventRepository.findByEnableTrueAndEventStatusAndEndsAtAfterOrderByStartsAtAsc(
                EventStatus.ENABLE, LocalDateTime.now()
        );
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
    public EventResponse updateEvent(Long id,EventUpdateRequest request){
        Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Not Found Event " + id));
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEndsAt(request.getEndsAt());
        event.setStartsAt(request.getStartsAt());
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }
    @Override
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .filter(e -> e.isEnable() && e.getEventStatus() == EventStatus.ENABLE)
                .orElseThrow(() -> new NotFoundException("Not Found Event " + id));
        return eventMapper.toResponse(event);
    }

    @Override
    public EventResponse getEventByIdForAdmin(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not Found Event " + id));
        return eventMapper.toResponse(event);
    }
}
