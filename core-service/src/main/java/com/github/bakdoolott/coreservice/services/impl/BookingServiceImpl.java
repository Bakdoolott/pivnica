package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.config.BookingProperties;
import com.github.bakdoolott.coreservice.exceptions.ConflictException;
import com.github.bakdoolott.coreservice.exceptions.LogicException;
import com.github.bakdoolott.coreservice.exceptions.NotFoundException;
import com.github.bakdoolott.coreservice.mappers.BookingMapper;
import com.github.bakdoolott.coreservice.models.Booking;
import com.github.bakdoolott.coreservice.models.BookingPrice;
import com.github.bakdoolott.coreservice.models.Tables;
import com.github.bakdoolott.coreservice.models.dto.BookingCreateDto;
import com.github.bakdoolott.coreservice.models.dto.response.BookingResponse;
import com.github.bakdoolott.coreservice.models.enums.BookingStatus;
import com.github.bakdoolott.coreservice.models.enums.PaymentStatus;
import com.github.bakdoolott.coreservice.models.enums.TableState;
import com.github.bakdoolott.coreservice.repositories.BookingRepo;
import com.github.bakdoolott.coreservice.repositories.TableRepo;
import com.github.bakdoolott.coreservice.services.BookingPriceService;
import com.github.bakdoolott.coreservice.services.BookingService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.HashSet;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepo bookingRepo;
    private final TableRepo tableRepo;
    private final BookingMapper bookingMapper;
    private final BookingProperties bookingProperties;
    private final BookingPriceService bookingPriceService;

    public BookingServiceImpl(BookingRepo bookingRepo, TableRepo tableRepo, BookingMapper bookingMapper, BookingProperties bookingProperties, BookingPriceService bookingPriceService) {
        this.bookingRepo = bookingRepo;
        this.tableRepo = tableRepo;
        this.bookingMapper = bookingMapper;
        this.bookingProperties = bookingProperties;
        this.bookingPriceService = bookingPriceService;
    }


    @Transactional
    @Override
    public BookingResponse createBooking(Long userId, BookingCreateDto dto) {
        if (userId == null){
            throw new AccessDeniedException("Пользователь не авторизован");
        }
        if (dto.getHallId() == null){
            throw new NotFoundException("Холл не найден");
        }

        ZoneId clubZone = bookingProperties.getClubZone();
        ZonedDateTime nowZoned = ZonedDateTime.now(clubZone);
        LocalDateTime now = nowZoned.toLocalDateTime();

        LocalDate bookingDate = dto.getDate();
        LocalTime startTime = dto.getStartTime();
        validateBookingDate(bookingDate,nowZoned.toLocalDate());

        LocalDateTime startAt = bookingDate.atTime(startTime);
        LocalDateTime endAt = calculateEndAt(startAt,bookingProperties.getClosingTime());

        List<Long> requestedIds = dto.getTableIds().stream().distinct().toList();

        List<Tables> tables = tableRepo.findEnabledByIdsAndHallForUpdate(requestedIds,dto.getHallId());

        if(tables.size() != requestedIds.size()){
            throw new NotFoundException("Один или несколько столиков не найдены");
        }

        boolean anyUnavailable = tables.stream().anyMatch(t -> t.getTableState() == TableState.UNAVAILABLE);
        if (anyUnavailable) {
            throw new ConflictException("Один или несколько столиков временно недоступны");
        }

        int totalCapacity = tables.stream().mapToInt(Tables::getPlaceCount).sum();
        if(dto.getGuestCount() > totalCapacity){
            throw new LogicException("Количество гостей превышает вместимость столиков");
        }

        List<Long> busyIds = bookingRepo.findBusyTableIdsAmong(
                requestedIds,startAt,endAt, BookingStatus.CONFIRMED);
        if(!busyIds.isEmpty()){
            throw new ConflictException("Столики уже забронированы");
        }

        BookingPrice currentPrice = bookingPriceService.getPriceAt(startAt);

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setUserName(dto.getUserName());
        booking.setPhoneNumber(dto.getPhoneNumber());
        booking.setTables(new HashSet<>(tables));
        booking.setDateTime(startAt);
        booking.setEndsAt(endAt);
        booking.setGuestCount(dto.getGuestCount());
        booking.setComment(normalizeComment(dto.getComment()));
        booking.setCreatedAt(now);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setPrice(currentPrice);
        booking.setEnable(true);

        Booking saved = bookingRepo.save(booking);
        return bookingMapper.toResponse(saved);
    }

    private void validateBookingDate(LocalDate bookingDate, LocalDate today) {
        LocalDate maxDate = today.plusDays(bookingProperties.getMaxDepthDays());

        if(bookingDate.isBefore(today)){
            throw new LogicException("Нельзя создать бронь на прошедшую дату");
        }
        if(bookingDate.isAfter(maxDate)){
            throw new LogicException("Бронирование доступно максимум на: "
                    + bookingProperties.getMaxDepthDays() + " дней вперед");
        }


    }
    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }
        String normalized = comment.trim();
        return normalized.isBlank() ? null : normalized;
    }
    private LocalDateTime calculateEndAt(LocalDateTime startAt, LocalTime closingTime) {
        LocalDateTime sameDayClosing = startAt.toLocalDate().atTime(closingTime);
        if (startAt.toLocalTime().isBefore(closingTime)) {
            return sameDayClosing;
        } else {
            return startAt.toLocalDate().plusDays(1).atTime(closingTime);
        }
    }

}
