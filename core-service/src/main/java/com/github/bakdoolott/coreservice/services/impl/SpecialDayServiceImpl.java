package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.config.BookingProperties;
import com.github.bakdoolott.coreservice.exceptions.ConflictException;
import com.github.bakdoolott.coreservice.exceptions.LogicException;
import com.github.bakdoolott.coreservice.exceptions.NotFoundException;
import com.github.bakdoolott.coreservice.mappers.SpecialDayMapper;
import com.github.bakdoolott.coreservice.models.SpecialDay;
import com.github.bakdoolott.coreservice.models.dto.SpecialDayCreateDto;
import com.github.bakdoolott.coreservice.models.dto.response.SpecialDayResponse;
import com.github.bakdoolott.coreservice.models.enums.DayType;
import com.github.bakdoolott.coreservice.repositories.SpecialDayRepo;
import com.github.bakdoolott.coreservice.services.SpecialDayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SpecialDayServiceImpl implements SpecialDayService {

    private final SpecialDayRepo specialDayRepo;
    private final SpecialDayMapper specialDayMapper;
    private final BookingProperties bookingProperties;

    public SpecialDayServiceImpl(SpecialDayRepo specialDayRepo, SpecialDayMapper specialDayMapper, BookingProperties bookingProperties) {
        this.specialDayRepo = specialDayRepo;
        this.specialDayMapper = specialDayMapper;
        this.bookingProperties = bookingProperties;
    }


    @Override
    @Transactional
    public SpecialDayResponse create(Long adminId, SpecialDayCreateDto dto) {
        if(specialDayRepo.findByDateAndEnableTrue(dto.date()).isPresent()){
            throw new ConflictException("На эту дату уже есть праздник или выходной");
        }
        if(dto.date().isBefore(LocalDate.now())) {
            throw new LogicException("Нельзя создать праздник в прошлом");
        }
        SpecialDay specialDay = specialDayMapper.toEntity(dto);
        specialDay.setCreatedBy(adminId);

        SpecialDay saved = specialDayRepo.save(specialDay);
        return specialDayMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SpecialDayResponse update(LocalDate date, SpecialDayCreateDto dto) {
        SpecialDay specialDay = specialDayRepo.findByDateAndEnableTrue(date)
                .orElseThrow(() -> new NotFoundException("Праздник на эту дату не найден"));

        specialDay.setDayType(dto.dayType());
        specialDay.setName(dto.name());
        specialDay.setClosed(dto.closed());

        SpecialDay saved = specialDayRepo.save(specialDay);
        return specialDayMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialDayResponse> getFutureDays() {
        List<SpecialDay> days = specialDayRepo.findAllFutureDays(LocalDate.now());
        List<SpecialDayResponse> responses = new ArrayList<>();
        for(SpecialDay day : days){
            responses.add(specialDayMapper.toResponse(day));
        }
        return responses;
    }

    @Override
    @Transactional
    public void deleteByDate(LocalDate date) {
        SpecialDay specialDay = specialDayRepo.findByDateAndEnableTrue(date)
                .orElseThrow(() -> new NotFoundException("Специальный день не найден"));

        specialDay.setEnable(false);
        specialDayRepo.save(specialDay);

    }

    @Override
    @Transactional(readOnly = true)
    public boolean isClosed(LocalDate date) {
        return specialDayRepo.existsByDateAndClosedTrueAndEnableTrue(date);
    }

    @Override
    @Transactional(readOnly = true)
    public DayType resolveDayType(LocalDate date) {
        Optional<SpecialDay> specialDay = specialDayRepo.findByDateAndEnableTrue(date);
        if(specialDay.isPresent()){
            return specialDay.get().getDayType();
        }
        if(bookingProperties.getPeakDays().contains(date.getDayOfWeek())){
            return DayType.WEEKENDS;
        }
        return DayType.WEEKDAY;
    }
}
