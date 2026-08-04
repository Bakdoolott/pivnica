package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.config.BookingProperties;
import com.github.bakdoolott.coreservice.exceptions.ConflictExceptions;
import com.github.bakdoolott.coreservice.exceptions.LogicExceptions;
import com.github.bakdoolott.coreservice.exceptions.NotFoundExceptions;
import com.github.bakdoolott.coreservice.mappers.TableMapper;
import com.github.bakdoolott.coreservice.models.Hall;
import com.github.bakdoolott.coreservice.models.Tables;
import com.github.bakdoolott.coreservice.models.dto.HallMapDto;
import com.github.bakdoolott.coreservice.models.dto.TableOnDateDto;
import com.github.bakdoolott.coreservice.models.enums.BookingStatus;
import com.github.bakdoolott.coreservice.models.enums.TableState;
import com.github.bakdoolott.coreservice.models.enums.TableStatus;
import com.github.bakdoolott.coreservice.repositories.BookingRepo;
import com.github.bakdoolott.coreservice.repositories.HallRepo;
import com.github.bakdoolott.coreservice.repositories.TableRepo;
import com.github.bakdoolott.coreservice.services.HallMapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class HallMapServiceImpl implements HallMapService {

    private final HallRepo hallRepo;
    private final TableRepo tableRepo;
    private final TableMapper tableMapper;
    private final BookingRepo bookingRepo;
    private final BookingProperties bookingProperties;

    private static final Set<BookingStatus> ACTIVE_STATUSES =
            EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    public HallMapServiceImpl(HallRepo hallRepo, TableRepo tableRepo, TableMapper tableMapper, BookingRepo bookingRepo, BookingProperties bookingProperties) {
        this.hallRepo = hallRepo;
        this.tableRepo = tableRepo;
        this.bookingRepo = bookingRepo;
        this.tableMapper = tableMapper;
        this.bookingProperties = bookingProperties;
    }


    @Transactional(readOnly = true)
    @Override
    public HallMapDto getHallMap(Long hallId, LocalDate date) {
        ZoneId clubZone = bookingProperties.getClubZone();

        ZonedDateTime now = ZonedDateTime.now(clubZone);
        LocalDate today = now.toLocalDate();
        LocalDateTime currentDateTime = now.toLocalDateTime();

        LocalDate targetDate;
        if (date != null) {
            targetDate = date;
        } else {
            targetDate = today;
        }

        LocalDate maxDate = today.plusDays(bookingProperties.getMaxDepthDays());

        validateDate(targetDate,today,maxDate);

        Hall hall = hallRepo.findById(hallId)
                .orElseThrow(() -> new NotFoundExceptions("Зал с ID " + hallId + " не найден"));
        if(!hall.isEnable()){
            throw new ConflictExceptions("Зал с ID " + hallId + " временно недоступен");
        }
        List<Tables> tables = tableRepo.findByHallIdAndEnableTrueOrderByTableNumberAsc(hallId);

        List<TableOnDateDto> tableOnDateDtos = tableMapper.tablesToTableOnDateDtoList(tables);

        LocalDateTime nightStart = targetDate.atTime(bookingProperties.getDefaultStartTime());

        LocalDateTime nightEnd = targetDate.plusDays(1).atTime(bookingProperties.getClosingTime());

        Set<Long> bookedTableIds = bookingRepo.findBookedTableIds(hallId,nightStart,nightEnd,ACTIVE_STATUSES,currentDateTime);

        applyTableStatuses(tableOnDateDtos,bookedTableIds);

        return new HallMapDto(hall.getId(), hall.getHallNumber(), hall.getFloor(), targetDate, today, maxDate, tableOnDateDtos);
    }

    private void validateDate(
            LocalDate targetDate,
            LocalDate today,
            LocalDate maxDate
    ) {
        if (targetDate.isBefore(today)) {
            throw new LogicExceptions("Нельзя выбрать прошедшую дату");
        }
        if (targetDate.isAfter(maxDate)) {
            throw new LogicExceptions("Бронирование доступно максимум на " +
                    bookingProperties.getMaxDepthDays() + " дней");
        }
    }

    private void applyTableStatuses(
            List<TableOnDateDto> tableDtos,
            Set<Long> bookedTableIds) {
        for (TableOnDateDto tableDto : tableDtos) {
            if (tableDto.getState() == TableState.UNAVAILABLE) {
                tableDto.setTableStatus(TableStatus.UNAVAILABLE);
                if (bookedTableIds.contains(tableDto.getId())) {
                    log.warn("Активная бронь на недоступном столике ID: {}", tableDto.getId());
                }
                continue;
            }
            TableStatus status;
            if (bookedTableIds.contains(tableDto.getId())) {
                status = TableStatus.BOOKED;
            } else {
                status = TableStatus.FREE;
            }
            tableDto.setTableStatus(status);

        }
    }
}
