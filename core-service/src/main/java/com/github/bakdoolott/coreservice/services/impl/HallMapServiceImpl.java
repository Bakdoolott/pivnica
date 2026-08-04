package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.config.BookingProperties;
import com.github.bakdoolott.coreservice.exceptions.LogicExceptions;
import com.github.bakdoolott.coreservice.exceptions.NotFoundExceptions;
import com.github.bakdoolott.coreservice.mappers.TableMapper;
import com.github.bakdoolott.coreservice.models.Hall;
import com.github.bakdoolott.coreservice.models.Tables;
import com.github.bakdoolott.coreservice.models.dto.HallMapDto;
import com.github.bakdoolott.coreservice.models.dto.TableOnDateDto;
import com.github.bakdoolott.coreservice.repositories.HallRepo;
import com.github.bakdoolott.coreservice.repositories.TableRepo;
import com.github.bakdoolott.coreservice.services.HallMapService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class HallMapServiceImpl implements HallMapService {

    private final HallRepo hallRepo;
    private final TableRepo tableRepo;
    private final TableMapper tableMapper;
    private final BookingProperties bookingProperties;

    public HallMapServiceImpl(HallRepo hallRepo, TableRepo tableRepo, TableMapper tableMapper, BookingProperties bookingProperties) {
        this.hallRepo = hallRepo;
        this.tableRepo = tableRepo;
        this.tableMapper = tableMapper;
        this.bookingProperties = bookingProperties;
    }


    @Transactional(readOnly = true)
    @Override
    public HallMapDto getHallMap(Long hallId, LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(bookingProperties.getMaxDepthDays());
        LocalDate target;
        if (date != null) {
            target = date;
        } else {
            target = today;
        }
        if(target.isBefore(today)){
            throw new LogicExceptions("Нельзя выбрать прошедшую дату");
        }
        if(target.isAfter(maxDate)){
            throw new LogicExceptions("Бронирование доступно максимум на " + bookingProperties + " дней вперёд");
        }

        Hall hall = hallRepo.findById(hallId)
                .orElseThrow(() -> new NotFoundExceptions("Зал с ID " + hallId + " не найден"));

        if (!hall.isEnable()) {
            throw new NotFoundExceptions("Зал с ID " + hallId + " недоступен");
        }

        List<Tables> tables = tableRepo.findByHallIdAndEnableTrueOrderByTableNumberAsc(hallId);
        List<TableOnDateDto> tableDtos = tableMapper.tablesToTableOnDateDtoList(tables);

        return new HallMapDto(hall.getId(), hall.getHallNumber(), hall.getFloor(),target,today,maxDate, tableDtos);
    }
}