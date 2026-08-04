package com.github.bakdoolott.coreservice.services;

import com.github.bakdoolott.coreservice.models.dto.HallMapDto;

import java.time.LocalDate;

public interface HallMapService {
    HallMapDto getHallMap (Long hallId,LocalDate date);
}
