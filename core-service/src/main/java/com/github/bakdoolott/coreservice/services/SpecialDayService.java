package com.github.bakdoolott.coreservice.services;

import com.github.bakdoolott.coreservice.models.dto.SpecialDayCreateDto;
import com.github.bakdoolott.coreservice.models.dto.response.SpecialDayResponse;
import com.github.bakdoolott.coreservice.models.enums.DayType;

import java.time.LocalDate;
import java.util.List;

public interface SpecialDayService {
    SpecialDayResponse create(Long adminId, SpecialDayCreateDto dto);
    SpecialDayResponse update(LocalDate date,SpecialDayCreateDto dto);
    List<SpecialDayResponse> getFutureDays();
    void deleteByDate(LocalDate date);
    boolean isClosed(LocalDate date);
    DayType resolveDayType(LocalDate date);

}
