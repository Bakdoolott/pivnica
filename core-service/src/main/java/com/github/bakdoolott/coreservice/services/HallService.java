package com.github.bakdoolott.coreservice.services;

import com.github.bakdoolott.coreservice.models.dto.HallDto;
import com.github.bakdoolott.coreservice.models.dto.request.CreateHallRequest;
import com.github.bakdoolott.coreservice.models.dto.request.UpdateHallRequest;
import com.github.bakdoolott.coreservice.models.dto.response.HallResponse;

import java.util.List;

public interface HallService {
    HallResponse create(CreateHallRequest request);
    HallResponse update(UpdateHallRequest request);
    HallResponse getById(Long id);
    HallDto findById(Long id);
    List<HallResponse> getAll();
    void deleteById(Long id);
    Boolean isExistById(Long id);
}
