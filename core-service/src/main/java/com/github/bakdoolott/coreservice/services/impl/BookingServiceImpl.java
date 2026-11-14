package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.config.BookingProperties;
import com.github.bakdoolott.coreservice.exceptions.ConflictException;
import com.github.bakdoolott.coreservice.exceptions.LogicException;
import com.github.bakdoolott.coreservice.exceptions.NotFoundException;
import com.github.bakdoolott.coreservice.mappers.BookingMapper;
import com.github.bakdoolott.coreservice.models.Booking;
import com.github.bakdoolott.coreservice.models.BookingPrice;
import com.github.bakdoolott.coreservice.models.BookingPriceLine;
import com.github.bakdoolott.coreservice.models.Tables;
import com.github.bakdoolott.coreservice.models.dto.BookingCancelDto;
import com.github.bakdoolott.coreservice.models.dto.BookingCreateDto;
import com.github.bakdoolott.coreservice.models.dto.response.BookingCancelResponse;
import com.github.bakdoolott.coreservice.models.dto.response.BookingResponse;
import com.github.bakdoolott.coreservice.models.enums.*;
import com.github.bakdoolott.coreservice.repositories.BookingRepo;
import com.github.bakdoolott.coreservice.repositories.TableRepo;
import com.github.bakdoolott.coreservice.services.BookingPriceService;
import com.github.bakdoolott.coreservice.services.BookingService;
import com.github.bakdoolott.coreservice.services.SpecialDayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {

    private static final Set<BookingStatus> BLOCKING_STATUSES = EnumSet.of(
            BookingStatus.PENDING,
            BookingStatus.AWAITING_CONFIRMATION,
            BookingStatus.CONFIRMED,
            BookingStatus.SEATED
    );

    private static final Set<BookingStatus> CANCELLABLE_BY_ADMIN = EnumSet.of(
            BookingStatus.PENDING,
            BookingStatus.AWAITING_CONFIRMATION,
            BookingStatus.CONFIRMED,
            BookingStatus.SEATED
    );

    private final BookingRepo bookingRepo;
    private final TableRepo tableRepo;
    private final BookingMapper bookingMapper;
    private final BookingProperties bookingProperties;
    private final SpecialDayService specialDayService;
    private final BookingPriceService bookingPriceService;

    public BookingServiceImpl(BookingRepo bookingRepo,
                              TableRepo tableRepo,
                              BookingMapper bookingMapper,
                              BookingProperties bookingProperties,
                              SpecialDayService specialDayService,
                              BookingPriceService bookingPriceService) {
        this.bookingRepo = bookingRepo;
        this.tableRepo = tableRepo;
        this.bookingMapper = bookingMapper;
        this.bookingProperties = bookingProperties;
        this.specialDayService = specialDayService;
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

        validateSchedule(arrivalAt, now);
        validateArrivalTime(arrivalAt);

        if (specialDayService.isClosed(bookingDate)) {
            throw new LogicException("В этот день заведение закрыто");
        }

        DayType dayType = specialDayService.resolveDayType(bookingDate);
        boolean isFreeBooking = (dayType == DayType.WEEKDAY);

        List<Long> requestedIds = dto.getTableIds().stream().distinct().sorted().toList();
        if (requestedIds.size() > bookingProperties.getMaxTablesPerBooking()) {
            throw new LogicException("Слишком много столиков для одной брони");
        }

        List<Tables> tables = tableRepo.findEnabledByIdsAndHallForUpdate(requestedIds, dto.getHallId());
        if (tables.size() != requestedIds.size()) {
            throw new NotFoundException("Некоторые столики не найдены");
        }

        boolean depositRequired = !isFreeBooking;
        Map<TableType, BookingPrice> activePrices = depositRequired
                ? bookingPriceService.getActivePricesAt(arrivalAt)
                : Map.of();

        int totalCapacity = 0;
        BigDecimal totalDeposit = BigDecimal.ZERO;
        List<BookingPriceLine> pendingLines = new ArrayList<>();

        for (Tables table : tables) {
            if (table.getTableState() == TableState.UNAVAILABLE) {
                throw new ConflictException("Столик " + table.getTableNumber() + " недоступен");
            }
            if (table.getTableType() == TableType.BAR) {
                throw new LogicException("Место за баром не бронируется");
            }
            totalCapacity += table.getTableType().getCapacity();

            if (depositRequired) {
                BookingPrice applied = activePrices.get(table.getTableType());
                if (applied == null) {
                    throw new LogicException("Цена для " + table.getTableType() + " не установлена");
                }
                totalDeposit = totalDeposit.add(applied.getPrice());
                pendingLines.add(BookingPriceLine.of(null, table.getId(), table.getTableType(), applied));
            }
        }

        if (dto.getGuestCount() > totalCapacity) {
            throw new LogicException("Гостей больше, чем мест за выбранными столами");
        }

        List<Long> busyIds = bookingRepo.findBusyTableIdsForDate(
                requestedIds, bookingDate, BLOCKING_STATUSES);
        if (!busyIds.isEmpty()) {
            throw new ConflictException("Эти столики уже заняты на этот вечер");
        }

        String phone = normalizePhone(dto.getPhoneNumber());

        if (isFreeBooking) {
            bookingRepo.lockFreeBookingUser(userId);
            Long phoneLockedId = Long.parseLong(phone.replace("+", ""));
            bookingRepo.lockFreeBookingPhone(phoneLockedId);

            long userCount = bookingRepo.countFreeActiveByUserId(
                    userId, BookingStatus.CONFIRMED, now.toLocalDate());
            long phoneCount = bookingRepo.countFreeActiveByPhone(
                    phone, BookingStatus.CONFIRMED, now.toLocalDate());

            if (userCount >= bookingProperties.getMaxFreeActiveBookingPerUser()
                    || phoneCount >= bookingProperties.getMaxFreeActiveBookingPerPhone()) {
                throw new ConflictException("Вы превысили лимит бесплатных броней");
            }
        }

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setUserName(dto.getUserName().trim());
        booking.setPhoneNumber(phone);
        booking.setTables(new HashSet<>(tables));
        booking.setBookingDate(bookingDate);
        booking.setDateTime(arrivalAt);
        booking.setGuestCount(dto.getGuestCount());
        if (dto.getComment() != null && !dto.getComment().isBlank()) {
            booking.setComment(dto.getComment().trim());
        }
        booking.setCreatedAt(now);
        booking.setDayType(dayType);
        booking.setTotalAmount(formatMoney(totalDeposit));
        booking.setEnable(true);

        if (isFreeBooking) {
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setPaymentStatus(PaymentStatus.NOT_REQUIRED);
            booking.setConfirmedAt(now);
            booking.setHoldUntil(computeNoShowDeadline(arrivalAt, bookingDate));
        } else {
            booking.setBookingStatus(BookingStatus.PENDING);
            booking.setPaymentStatus(PaymentStatus.PENDING);
            booking.setHoldUntil(now.plusMinutes(bookingProperties.getPaymentTimeoutMinutes()));
        }

        for (BookingPriceLine line : pendingLines) {
            booking.addPriceLine(line);
        }

        Booking saved = bookingRepo.save(booking);
        log.info("Booking created: id={}, userId={}, status={}, totalAmount={}, free={}",
                saved.getId(), userId, saved.getBookingStatus(), saved.getTotalAmount(), isFreeBooking);
        return bookingMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public BookingResponse markPaymentSent(Long userId, Long bookingId) {
        Booking booking = bookingRepo.findOwnByIdForUpdate(bookingId, userId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());

        if (booking.getBookingStatus() == BookingStatus.EXPIRED) {
            throw new LogicException("Время на оплату истекло. Создайте новую бронь");
        }
        if (booking.getBookingStatus() == BookingStatus.AWAITING_CONFIRMATION) {
            return bookingMapper.toResponse(booking);
        }
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new LogicException("Отметить оплату можно только для неоплаченной брони");
        }
        if (now.isAfter(booking.getHoldUntil())) {
            throw new LogicException("Время на оплату истекло");
        }

        booking.setBookingStatus(BookingStatus.AWAITING_CONFIRMATION);
        booking.setPaymentSentAt(now);

        Booking saved = bookingRepo.save(booking);
        log.info("Payment marked as sent: bookingId={}, userId={}", bookingId, userId);
        return bookingMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingResponse> getAwaitingConfirmation() {
        return bookingRepo.findByStatusForAdmin(BookingStatus.AWAITING_CONFIRMATION)
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public BookingResponse confirmByAdmin(Long adminId, Long bookingId) {
        Booking booking = bookingRepo.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            return bookingMapper.toResponse(booking);
        }
        if (booking.getBookingStatus() != BookingStatus.AWAITING_CONFIRMATION) {
            throw new LogicException("Подтвердить можно только бронь в статусе AWAITING_CONFIRMATION");
        }

        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());

        LocalDateTime deadline = computeNoShowDeadline(
                booking.getDateTime(), booking.getBookingDate());
        if (!now.isBefore(deadline)) {
            throw new LogicException(
                    "Время брони уже прошло. Оформите возврат вручную");
        }

        List<Long> tableIds = booking.getTables().stream()
                .map(Tables::getId)
                .toList();
        List<Long> busyIds = bookingRepo.findBusyTableIdsForDateExcluding(
                tableIds, booking.getBookingDate(), BookingStatus.CONFIRMED, bookingId);
        if (!busyIds.isEmpty()) {
            throw new ConflictException(
                    "Столики уже заняты другим подтверждённым бронированием: " + busyIds);
        }

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setConfirmedBy(adminId);
        booking.setConfirmedAt(now);
        booking.setHoldUntil(deadline);

        Booking saved = bookingRepo.save(booking);
        log.info("Booking confirmed: bookingId={}, adminId={}", bookingId, adminId);
        return bookingMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public BookingResponse rejectByAdmin(Long adminId, Long bookingId,
                                         String reason, boolean moneyReceived) {
        Booking booking = bookingRepo.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        if (booking.getBookingStatus() != BookingStatus.AWAITING_CONFIRMATION) {
            throw new LogicException("Отклонить можно только бронь в статусе AWAITING_CONFIRMATION");
        }
        if (reason == null || reason.isBlank()) {
            throw new LogicException("Укажите причину отклонения");
        }

        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());
        booking.setBookingStatus(BookingStatus.REJECTED);
        booking.setRejectionReason(reason.trim());
        booking.setRejectedBy(adminId);
        booking.setRejectedAt(now);
        booking.setEnable(false);

        if (moneyReceived) {
            booking.setPaymentStatus(PaymentStatus.REFUND_REQUESTED);
            booking.setRefundAmount(booking.getTotalAmount());
            booking.setRefundPercent(100);
            booking.setRefundRequested(true);
            booking.setRefundRequestedAt(now);
        } else {
            booking.setPaymentStatus(PaymentStatus.REJECTED);
            booking.setRefundAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            booking.setRefundPercent(0);
        }

        Booking saved = bookingRepo.save(booking);
        log.warn("Booking rejected: bookingId={}, adminId={}, reason={}, moneyReceived={}",
                bookingId, adminId, reason, moneyReceived);
        return bookingMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public BookingResponse markArrived(Long adminId, Long bookingId) {
        Booking booking = bookingRepo.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new LogicException("Отметить приход можно только у подтверждённой брони");
        }

        booking.setBookingStatus(BookingStatus.SEATED);
        log.info("Guest arrived: bookingId={}, adminId={}", bookingId, adminId);
        return bookingMapper.toResponse(bookingRepo.save(booking));
    }

    @Transactional
    @Override
    public BookingResponse markDeparted(Long adminId, Long bookingId) {
        Booking booking = bookingRepo.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        if (booking.getBookingStatus() != BookingStatus.SEATED) {
            throw new LogicException("Гость ещё не отмечен как пришедший");
        }

        booking.setBookingStatus(BookingStatus.COMPLETED);
        booking.setEnable(false);
        log.info("Guest departed: bookingId={}, adminId={}", bookingId, adminId);
        return bookingMapper.toResponse(bookingRepo.save(booking));
    }


    @Transactional
    @Override
    public BookingResponse cancelOwnBooking(Long userId, Long bookingId, String reason) {
        Booking booking = bookingRepo.findOwnByIdForUpdate(bookingId, userId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());
        BookingStatus status = booking.getBookingStatus();

        if (status == BookingStatus.CANCELLED) {
            throw new ConflictException("Бронь уже отменена");
        }
        if (status == BookingStatus.EXPIRED) {
            throw new LogicException("Бронь уже истекла");
        }
        if (status == BookingStatus.REJECTED) {
            throw new LogicException("Бронь отклонена администратором");
        }
        if (status == BookingStatus.AWAITING_CONFIRMATION) {
            throw new LogicException(
                    "Оплата проверяется администратором. Дождитесь решения или свяжитесь с клубом");
        }
        if (status == BookingStatus.SEATED) {
            throw new LogicException("Гость уже сидит за столом, отмена невозможна");
        }
        if (status == BookingStatus.COMPLETED) {
            throw new LogicException("Вечер уже завершён");
        }

        if (status == BookingStatus.PENDING) {
            applySimpleCancellation(booking, userId, reason, now);
            return bookingMapper.toResponse(bookingRepo.save(booking));
        }

        if (status == BookingStatus.CONFIRMED) {
            BookingCancelResponse policy = buildCancelResponse(booking, now);
            if (!policy.cancellable()) {
                throw new LogicException(policy.message());
            }
            applyCancellation(booking, userId, reason, now, policy);
            return bookingMapper.toResponse(bookingRepo.save(booking));
        }

        throw new LogicException("Отмена невозможна в статусе " + status);
    }

    @Transactional
    @Override
    public BookingResponse cancelBooking(Long adminId, Long bookingId, BookingCancelDto dto) {
        Booking booking = bookingRepo.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь с ID " + bookingId + " не найдена"));

        if (!CANCELLABLE_BY_ADMIN.contains(booking.getBookingStatus())) {
            throw new ConflictException(
                    "Бронь в статусе " + booking.getBookingStatus() + " нельзя отменить");
        }

        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());

        if (booking.getPaymentStatus() != PaymentStatus.PAID) {
            applySimpleCancellation(booking, adminId, dto.reason(), now);
            return bookingMapper.toResponse(bookingRepo.save(booking));
        }

        BookingCancelResponse policy = buildCancelResponse(booking, now);
        if (dto.fullRefund()) {
            BigDecimal total = booking.getTotalAmount();
            policy = new BookingCancelResponse(
                    policy.bookingId(), true, policy.cancelDeadline(),
                    total, total, 100, "Полный возврат по решению администрации");
        }
        applyCancellation(booking, adminId, dto.reason(), now, policy);
        return bookingMapper.toResponse(bookingRepo.save(booking));
    }

    @Transactional
    @Override
    public BookingCancelResponse requestRefund(Long userId, Long bookingId) {
        Booking booking = bookingRepo.findOwnByIdForUpdate(bookingId, userId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        if (booking.getBookingStatus() != BookingStatus.CANCELLED) {
            throw new LogicException("Сначала отмените бронь");
        }
        if (booking.getPaymentStatus() != PaymentStatus.PAID) {
            throw new LogicException("Возврат возможен только для оплаченной брони");
        }
        if (booking.getRefundAmount() == null || booking.getRefundAmount().signum() <= 0) {
            throw new LogicException("Возврат по этой брони не предусмотрен");
        }
        if (booking.isRefundRequested()) {
            throw new ConflictException("Заявка уже создана");
        }

        booking.setRefundRequested(true);
        booking.setRefundRequestedAt(LocalDateTime.now(bookingProperties.getClubZone()));
        booking.setPaymentStatus(PaymentStatus.REFUND_REQUESTED);
        bookingRepo.save(booking);

        String message = "Заявка принята. Возврат в течение "
                + bookingProperties.getRefundWorkingDays() + " рабочих дней";
        return new BookingCancelResponse(
                booking.getId(), true, booking.getHoldUntil(),
                booking.getTotalAmount(), booking.getRefundAmount(),
                booking.getRefundPercent(), message);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingResponse> getRefundRequests() {
        return bookingRepo.findRefundRequests().stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public BookingResponse markRefunded(Long adminId, Long bookingId) {
        Booking booking = bookingRepo.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        if (booking.getPaymentStatus() != PaymentStatus.REFUND_REQUESTED) {
            throw new LogicException("Возврат не был запрошен");
        }

        booking.setPaymentStatus(PaymentStatus.REFUNDED);
        Booking saved = bookingRepo.save(booking);
        log.info("Refund marked as done: bookingId={}, adminId={}", bookingId, adminId);
        return bookingMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public BookingResponse markNoShow(Long adminId, Long bookingId) {
        Booking booking = bookingRepo.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new LogicException("Отметить неявку можно только у подтверждённой брони");
        }

        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());
        if (now.isBefore(booking.getHoldUntil())) {
            throw new LogicException("Время ожидания ещё не истекло");
        }

        booking.setBookingStatus(BookingStatus.NO_SHOW);
        booking.setEnable(false);
        booking.setCancelledAt(now);
        booking.setCancelledBy(adminId);
        booking.setRefundAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        booking.setRefundPercent(0);

        return bookingMapper.toResponse(bookingRepo.save(booking));
    }

    @Transactional(readOnly = true)
    @Override
    public BookingCancelResponse getCancelResponse(Long userId, Long bookingId) {
        Booking booking = bookingRepo.findOwnById(bookingId, userId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));
        return buildCancelResponse(booking, LocalDateTime.now(bookingProperties.getClubZone()));
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingResponse> getBookingsForNight(LocalDate date) {
        return bookingRepo.findAllForNight(date).stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Override
    public byte[] exportBookingsForNight(LocalDate date) {
        List<Booking> bookings = bookingRepo.findAllForNight(date);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Дата;Время;Столики;Гость;Телефон;Гостей;Сумма;Статус;Оплата;Комментарий\n");

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        for (Booking b : bookings) {
            String tables = b.getTables().stream()
                    .map(Tables::getTableNumber)
                    .sorted()
                    .map(String::valueOf)
                    .reduce((x, y) -> x + ", " + y)
                    .orElse("");

            csv.append(b.getBookingDate()).append(';')
                    .append(b.getDateTime().format(timeFormat)).append(';')
                    .append(csvCell(tables)).append(';')
                    .append(csvCell(b.getUserName())).append(';')
                    .append('"').append(b.getPhoneNumber()).append('"').append(';')  // телефон без csvCell
                    .append(b.getGuestCount()).append(';')
                    .append(formatMoney(b.getTotalAmount())).append(';')
                    .append(b.getBookingStatus()).append(';')
                    .append(b.getPaymentStatus()).append(';')
                    .append(csvCell(b.getComment())).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private LocalDateTime computeNoShowDeadline(LocalDateTime arrivalAt, LocalDate bookingDate) {
        return bookingDate.atTime(bookingProperties.getArrivalDeadline());
    }

    private BookingCancelResponse buildCancelResponse(Booking booking, LocalDateTime now) {
        BigDecimal total = booking.getTotalAmount();
        BigDecimal zero = formatMoney(BigDecimal.ZERO);
        LocalDateTime deadline = booking.getHoldUntil() != null
                ? booking.getHoldUntil()
                : booking.getDateTime();

        BookingStatus status = booking.getBookingStatus();

        if (status == BookingStatus.CANCELLED) {
            return new BookingCancelResponse(booking.getId(), false, deadline,
                    total, zero, 0, "Бронь уже отменена");
        }
        if (status == BookingStatus.EXPIRED) {
            return new BookingCancelResponse(booking.getId(), false, deadline,
                    total, zero, 0, "Бронь истекла");
        }
        if (status == BookingStatus.REJECTED) {
            return new BookingCancelResponse(booking.getId(), false, deadline,
                    total, zero, 0, "Бронь отклонена администратором");
        }
        if (status == BookingStatus.NO_SHOW) {
            return new BookingCancelResponse(booking.getId(), false, deadline,
                    total, zero, 0, "Неявка: депозит не возвращается");
        }
        if (status == BookingStatus.SEATED) {
            return new BookingCancelResponse(booking.getId(), false, deadline,
                    total, zero, 0, "Гость уже сидит за столом");
        }
        if (status == BookingStatus.COMPLETED) {
            return new BookingCancelResponse(booking.getId(), false, deadline,
                    total, zero, 0, "Вечер уже завершён");
        }

        if (booking.getPaymentStatus() != PaymentStatus.PAID) {
            return new BookingCancelResponse(booking.getId(), true, deadline,
                    total, zero, 0, "Оплата не подтверждена — возврат не требуется");
        }

        if (now.isAfter(deadline)) {
            return new BookingCancelResponse(booking.getId(), false, deadline,
                    total, zero, 0, "Время вышло, депозит не возвращается");
        }

        Duration timeToArrival = Duration.between(now, booking.getDateTime());
        long minutesToArrival = timeToArrival.toMinutes();
        long minuteSinceCreation = Duration.between(booking.getCreatedAt(), now).toMinutes();

        boolean isAccidental = (minuteSinceCreation < bookingProperties.getCancellationGraceMinutes())
                && (minutesToArrival >= bookingProperties.getCancellationGraceBeforeHours() * 60L);

        int percent;
        if (total.signum() == 0 || isAccidental) {
            percent = 100;
        } else if (minutesToArrival >= bookingProperties.getRefundFullBeforeHours() * 60L) {
            percent = 100;
        } else {
            percent = bookingProperties.getRefundPartialPercent();
        }

        BigDecimal refund = total.multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        String message = "К возврату " + refund + ". Срок возврата до "
                + bookingProperties.getRefundWorkingDays() + " рабочих дней";
        return new BookingCancelResponse(booking.getId(), true, deadline,
                total, refund, percent, message);
    }

    private void applySimpleCancellation(Booking booking, Long actorId, String reason, LocalDateTime now) {
        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setEnable(false);
        booking.setCancelReason(reason == null || reason.isBlank() ? null : reason.trim());
        booking.setCancelledAt(now);
        booking.setCancelledBy(actorId);
        booking.setRefundAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        booking.setRefundPercent(0);
        log.info("Booking {} cancelled without refund calculation", booking.getId());
    }

    private void applyCancellation(Booking booking, Long actorId, String reason,
                                   LocalDateTime now, BookingCancelResponse policy) {
        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setEnable(false);
        booking.setCancelledAt(now);
        booking.setCancelledBy(actorId);
        booking.setRefundAmount(policy.refundAmount());
        booking.setRefundPercent(policy.refundPercent());
        booking.setCancelReason(reason == null || reason.isBlank() ? null : reason.trim());
    }

    private void validateArrivalTime(LocalDateTime arrivalAt) {
        LocalTime time = arrivalAt.toLocalTime();
        if (time.isBefore(bookingProperties.getOpenTime())
                || time.isAfter(bookingProperties.getArrivalDeadline())) {
            throw new LogicException("Бронь принимается с "
                    + bookingProperties.getOpenTime() + " до "
                    + bookingProperties.getArrivalDeadline());
        }
    }

    private void validateSchedule(LocalDateTime arrivalAt, LocalDateTime now) {
        LocalDate bookingDate = arrivalAt.toLocalDate();
        LocalDate today = now.toLocalDate();

        if (bookingDate.isBefore(today)) {
            throw new LogicException("Нельзя забронировать на прошедшую дату");
        }
        if (bookingDate.isAfter(today.plusDays(bookingProperties.getMaxDepthDays()))) {
            throw new LogicException("Бронирование доступно максимум на "
                    + bookingProperties.getMaxDepthDays() + " дней вперёд");
        }
        if (arrivalAt.isBefore(now.plusMinutes(bookingProperties.getMinLeadMinutes()))) {
            throw new LogicException("Бронь принимается минимум за "
                    + bookingProperties.getMinLeadMinutes() + " минут до начала");
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new LogicException("Номер телефона обязателен");
        }
        String digitsOnly = phone.replaceAll("[^0-9]", "");

        if (digitsOnly.length() == 9) {
            digitsOnly = "996" + digitsOnly;
        }

        else if (digitsOnly.length() == 10 && digitsOnly.startsWith("0")) {
            digitsOnly = "996" + digitsOnly.substring(1);
        }

        if (digitsOnly.length() < 9 || digitsOnly.length() > 15) {
            throw new LogicException("Некорректный номер телефона");
        }
        return "+" + digitsOnly;
    }

    private BigDecimal formatMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String csvCell(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String cleaned = value.replace("\"", "\"\"").replaceAll("[\\r\\n]+", " ");
        if (cleaned.matches("^[=+\\-@\t].*")) {
            cleaned = "'" + cleaned;
        }
        return "\"" + cleaned + "\"";
    }
}