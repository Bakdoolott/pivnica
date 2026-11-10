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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.github.bakdoolott.coreservice.models.Event;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService{
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private FileStorageService fileStorageService;
    @Value("${app.upload-dir}")
    private String uploadDir;


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
    @Override
    @Transactional
    public void uploadImage(Long id, MultipartFile file) {
        // 1. Находим событие по id или выбрасываем ваш кастомный NotFoundException
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id " + id + " не найдено"));

        // 2. Если у события уже была картинка, удаляем старый файл с диска
        String oldImageName = event.getImageName();
        if (oldImageName != null && !oldImageName.isEmpty()) {
            try {
                Path oldFilePath = Paths.get(uploadDir).resolve(oldImageName).toAbsolutePath().normalize();
                Files.deleteIfExists(oldFilePath);
            } catch (IOException e) {
                // Логируем ошибку, но не прерываем транзакцию, чтобы загрузка нового файла всё равно сработала
                // Если у вас нет логгера, можно временно оставить System.err или выкинуть RuntimeException
                System.err.println("Не удалось удалить старый файл: " + oldImageName);
            }
        }

        // 3. Вызываем метод store из FileStorageService для сохранения нового файла
        String newImageName = fileStorageService.store(file);

        // 4. Записываем результаты в сущность
        event.setImageName(newImageName);
        event.setImageUrl("/uploads/" + newImageName);

        // 5. Сохраняем обновленное событие
        eventRepository.save(event);
    }
}
