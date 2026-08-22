package com.github.bakdoolott.coreservice.services;
import com.github.bakdoolott.coreservice.models.dto.response.EventResponse;
import com.github.bakdoolott.coreservice.models.dto.response.EventBannerDTO;
import com.github.bakdoolott.coreservice.models.dto.request.EventCreateRequest;

import java.util.List;

public interface EventService {
    List<EventBannerDTO> getActiveBanners();
    List<EventResponse> getAllForAdmin();
    EventResponse createEvent(EventCreateRequest request, String createdBy);
    EventResponse publishEvent(Long id);
    EventResponse unpublishEvent(Long id);
EventResponse updateEvent(Long id, EventCreateRequest request);
    void deleteEvent(Long id);
}
