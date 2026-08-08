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
        if (userId == null) {
            throw new AccessDeniedException("Пользователь не авторизован");
        }
        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());

        LocalDate bookingDate = dto.getDate();
        LocalDateTime arrivalAt = bookingDate.atTime(dto.getStartTime());
        LocalDateTime dayStart = resolveStartAt(bookingDate);
        LocalDateTime dayEnd = resolveEndAt(bookingDate);

        validateSchedule(arrivalAt, now);
        validateArrivalTime(arrivalAt, dayStart, dayEnd);

        List<Long> requestedIds = dto.getTableIds().stream()
                .distinct()
                .sorted()
                .toList();

        if (requestedIds.size() > bookingProperties.getMaxTablesPerBooking()) {
            throw new LogicException("Максимум столиков в одной брони: "
                    + bookingProperties.getMaxTablesPerBooking());
        }
        List<Tables> tables = tableRepo.findEnabledByIdsAndHallForUpdate(requestedIds, dto.getHallId());

        if (tables.size() != requestedIds.size()) {
            throw new NotFoundException("Столики не найдены в указанном зале");
        }
        boolean anyUnavailable = tables.stream()
                .anyMatch(t -> t.getTableState() == TableState.UNAVAILABLE);
        if (anyUnavailable) {
            throw new ConflictException("Один или несколько столиков недоступны");
        }

        int totalCapacity = tables.stream().mapToInt(Tables::getPlaceCount).sum();
        if (dto.getGuestCount() > totalCapacity) {
            throw new LogicException("Количество гостей превышает вместимость столиков");
        }

        List<Long> busyIds = bookingRepo.findBusyTableIdsAmong(requestedIds, dayStart,
                dayEnd, BookingStatus.CONFIRMED);
        if (!busyIds.isEmpty()) {
            throw new ConflictException("Cтолики уже забронированы");
        }
        BookingPrice currentPrice = bookingPriceService.getPriceAt(arrivalAt);

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setUserName(dto.getUserName().trim());
        booking.setPhoneNumber(dto.getPhoneNumber().replaceAll("[^0-9+]", ""));
        booking.setTables(new HashSet<>(tables));
        booking.setDateTime(arrivalAt);
        booking.setEndsAt(dayEnd);
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

    private void validateArrivalTime(LocalDateTime arrivalAt, LocalDateTime dayStart, LocalDateTime dayEnd) {
        if (arrivalAt.isBefore(dayStart) || !arrivalAt.isBefore(dayEnd)) {
            throw new LogicException("Время прихода должно быть в рабочие часы клуба");
        }
    }

    private void validateSchedule(LocalDateTime arrivalAt, LocalDateTime now) {
        LocalDate bookingDate = arrivalAt.toLocalDate();
        LocalDate today = now.toLocalDate();
        LocalDate maxDate = now.toLocalDate().plusDays(bookingProperties.getMaxDepthDays());

        if (bookingDate.isBefore(today)) {
            throw new LogicException("Нельзя забронировать на прошедшую дату");
        }

        if (arrivalAt.toLocalDate().isAfter(maxDate)) {
            throw new LogicException("Бронирование доступно максимум на "
                    + bookingProperties.getMaxDepthDays() + " дней вперёд");
        }
        if (arrivalAt.isBefore(now.plusMinutes(bookingProperties.getMinLeadMinutes()))) {
            throw new LogicException("Бронь принимается минимум за "
                    + bookingProperties.getMinLeadMinutes() + " минут до начала");
        }

    }

    private LocalDateTime resolveStartAt(LocalDate date) {
        return date.atTime(bookingProperties.getOpenTime());
    }

    private LocalDateTime resolveEndAt(LocalDate date) {
        LocalDate endDate = bookingProperties.isOvernight() ? date.plusDays(1) : date;
        return endDate.atTime(bookingProperties.getClosingTime());
    }


    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }
        String normalized = comment.trim();
        return normalized.isBlank() ? null : normalized;
    }

}
