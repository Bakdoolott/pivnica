package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.config.BookingProperties;
import com.github.bakdoolott.coreservice.exceptions.ConflictException;
import com.github.bakdoolott.coreservice.exceptions.LogicException;
import com.github.bakdoolott.coreservice.exceptions.NotFoundException;
import com.github.bakdoolott.coreservice.mappers.TableMapper;
import com.github.bakdoolott.coreservice.models.Hall;
import com.github.bakdoolott.coreservice.models.Tables;
import com.github.bakdoolott.coreservice.models.dto.HallMapDto;
import com.github.bakdoolott.coreservice.models.dto.TableOnDateDto;
import com.github.bakdoolott.coreservice.models.enums.HallStatus;
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
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Map;

@Slf4j
@Service
public class HallMapServiceImpl implements HallMapService {

    private final HallRepo hallRepo;
    private final TableRepo tableRepo;
    private final TableMapper tableMapper;
    private final BookingRepo bookingRepo;
    private final BookingProperties bookingProperties;

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

        ZonedDateTime nowZoned = ZonedDateTime.now(clubZone);
        LocalDate today = nowZoned.toLocalDate();
        LocalDateTime now = nowZoned.toLocalDateTime();

        LocalDate targetDate;
        if (date != null) {
            targetDate = date;
        } else {
            targetDate = today;
        }

        LocalDate maxDate = today.plusDays(bookingProperties.getMaxDepthDays());
        validateDate(targetDate,today,maxDate);

        Hall hall = hallRepo.findById(hallId)
                .orElseThrow(() -> new NotFoundException("Зал с ID " + hallId + " не найден"));

        if(!hall.isEnable() || hall.getHallStatus() == HallStatus.UNAVAILABLE){
            throw new ConflictException("Зал с ID " + hallId + " временно недоступен");
        }

        List<Tables> tables = tableRepo.findByHallIdAndEnableTrueOrderByTableNumberAsc(hallId);
        List<TableOnDateDto> tableOnDateDtos = tableMapper.tablesToTableOnDateDtoList(tables);

        NightInterval night = resolveNightInterval(targetDate);

        Set<Long> bookedTablesIds = bookingRepo.findBookedTableIds(hallId,night.start(),
                night.end(),now);

        applyTableStatuses(tables,tableOnDateDtos,bookedTablesIds);

        return new HallMapDto(hall.getId(), hall.getHallNumber(),hall.getFloor(),
                targetDate,today,maxDate,tableOnDateDtos);
    }

    private void validateDate(
            LocalDate targetDate,
            LocalDate today,
            LocalDate maxDate
    ) {
        if (targetDate.isBefore(today)) {
            throw new LogicException("Нельзя выбрать прошедшую дату");
        }
        if (targetDate.isAfter(maxDate)) {
            throw new LogicException("Бронирование доступно максимум на " +
                    bookingProperties.getMaxDepthDays() + " дней");
        }
    }

    private void applyTableStatuses(List<Tables> tables,
                                    List<TableOnDateDto> tableDtos,
                                    Set<Long>bookedTableIds){
        Map<Long,Tables> tablesMap = tables.stream()
                .collect(Collectors.toMap(Tables::getId, Function.identity()));
        for(TableOnDateDto dto : tableDtos){
            Tables entity = tablesMap.get(dto.getId());
            if(entity==null){
                log.error("Cтолик с ID {} не найден", dto.getId());

                throw new IllegalStateException("Ошибка соответствия столиков");
            }
            if(entity.getTableState() == TableState.UNAVAILABLE){
                if(bookedTableIds.contains(entity.getId())){
                    log.warn("Активная бронь недоступного столика ID: {}",entity.getId());
                }
                dto.setTableStatus(TableStatus.UNAVAILABLE);
            }else {
                dto.setTableStatus(bookedTableIds.contains(entity.getId())
                        ? TableStatus.BOOKED
                        : TableStatus.FREE);
            }
        }
    }
    private NightInterval resolveNightInterval(LocalDate targetDate){
        LocalTime startTime = bookingProperties.getDefaultStartTime();
        LocalTime closingTime = bookingProperties.getClosingTime();

        LocalDateTime nightStart = targetDate.atTime(startTime);
        LocalDateTime nightEnd;

        if(closingTime.isBefore(startTime) || closingTime.equals(startTime)){
            nightEnd = targetDate.plusDays(1).atTime(closingTime);
        }else {
            nightEnd = targetDate.atTime(closingTime);
        }
        return new NightInterval(nightStart,nightEnd);
    }

    private record NightInterval(LocalDateTime start, LocalDateTime end) {}
}
