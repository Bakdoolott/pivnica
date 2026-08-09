package com.github.bakdoolott.coreservice.service;
import com.github.bakdoolott.coreservice.dto.EventAdminDTO;
import com.github.bakdoolott.coreservice.dto.EventBannerDTO;
import com.github.bakdoolott.coreservice.dto.EventCreateRequest;

import java.util.List;

public interface EventService {
    List<EventBannerDTO> getActiveBanners();
    List<EventAdminDTO> getAllForAdmin();
    EventAdminDTO createEvent(EventCreateRequest request, String createdBy);
    EventAdminDTO publishEvent(Long id);
    EventAdminDTO unpublishEvent(Long id);
EventAdminDTO updateEvent(Long id, EventCreateRequest request);
    void deleteEvent(Long id);
}
