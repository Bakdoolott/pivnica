package com.github.bakdoolott.coreservice.services;

import com.github.bakdoolott.coreservice.models.dto.HallDto;
import com.github.bakdoolott.coreservice.models.dto.request.CreateHallRequest;
import com.github.bakdoolott.coreservice.models.dto.request.UpdateHallRequest;

import java.util.List;

public interface HallService {
    HallDto create(CreateHallRequest request);
    HallDto update(UpdateHallRequest request);
    HallDto findById(Long id);
    List<HallDto> getAll();
    void deleteById(Long id);
    Boolean isExistById(Long id);
}
