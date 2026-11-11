package com.github.bakdoolott.coreservice.services;
import com.github.bakdoolott.coreservice.models.dto.request.EventUpdateRequest;
import com.github.bakdoolott.coreservice.models.dto.response.EventResponse;
import com.github.bakdoolott.coreservice.models.dto.response.EventBannerDTO;
import com.github.bakdoolott.coreservice.models.dto.request.EventCreateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EventService {
    List<EventBannerDTO> getActiveBanners();
    List<EventResponse> getAllForAdmin();
    EventResponse createEvent(EventCreateRequest request, String createdBy);
    EventResponse publishEvent(Long id);
    EventResponse unpublishEvent(Long id);
EventResponse updateEvent(Long id, EventUpdateRequest request);
    void deleteEvent(Long id);
    void uploadImage(Long id, MultipartFile file);
    EventResponse getEventById(Long id);
    EventResponse getEventByIdForAdmin(Long id);
}
