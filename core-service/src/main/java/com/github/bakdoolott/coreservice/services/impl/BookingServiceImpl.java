package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.config.BookingProperties;
import com.github.bakdoolott.coreservice.exceptions.ConflictException;
import com.github.bakdoolott.coreservice.exceptions.LogicException;
import com.github.bakdoolott.coreservice.exceptions.NotFoundException;
import com.github.bakdoolott.coreservice.mappers.BookingMapper;
import com.github.bakdoolott.coreservice.models.Booking;
import com.github.bakdoolott.coreservice.models.Tables;
import com.github.bakdoolott.coreservice.models.dto.BookingCancelDto;
import com.github.bakdoolott.coreservice.models.dto.BookingCreateDto;
import com.github.bakdoolott.coreservice.models.dto.response.BookingCancelResponse;
import com.github.bakdoolott.coreservice.models.dto.response.BookingResponse;
import com.github.bakdoolott.coreservice.models.enums.*;
import com.github.bakdoolott.coreservice.repositories.BookingRepo;
import com.github.bakdoolott.coreservice.repositories.TableRepo;
import com.github.bakdoolott.coreservice.services.BookingService;
import com.github.bakdoolott.coreservice.services.SpecialDayService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepo bookingRepo;
    private final TableRepo tableRepo;
    private final BookingMapper bookingMapper;
    private final BookingProperties bookingProperties;
    private final SpecialDayService specialDayService;

    public BookingServiceImpl(BookingRepo bookingRepo, TableRepo tableRepo, BookingMapper bookingMapper, BookingProperties bookingProperties, SpecialDayService specialDayService) {
        this.bookingRepo = bookingRepo;
        this.tableRepo = tableRepo;
        this.bookingMapper = bookingMapper;
        this.bookingProperties = bookingProperties;
        this.specialDayService = specialDayService;
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
            throw new LogicException("В этот день заведении закрыто");
        }

        DayType dayType = specialDayService.resolveDayType(bookingDate);

        List<Long> requestedIds = dto.getTableIds().stream().distinct().toList();
        if (requestedIds.size() > bookingProperties.getMaxTablesPerBooking()) {
            throw new LogicException("Слишком много столиков для одной брони");
        }

        List<Tables> tables = tableRepo.findEnabledByIdsAndHallForUpdate(requestedIds, dto.getHallId());
        if (tables.size() != requestedIds.size()) {
            throw new NotFoundException("Некоторые столики не найдены");
        }
        int totalCapacity = 0;
        BigDecimal totalDeposit = BigDecimal.ZERO;

        for (Tables table : tables) {
            if (table.getTableState() == TableState.UNAVAILABLE) {
                throw new ConflictException("Столик " + table.getTableNumber() + " недоступен");
            }
            if (table.getTableType() == TableType.BAR) {
                throw new LogicException("Место за баром не бронируется");
            }
            totalCapacity += table.getTableType().getCapacity();

            if(dayType != DayType.WEEKDAY){
                totalDeposit = totalDeposit.add(table.getTableType().getPrice());
            }

        }
        if (dto.getGuestCount() > totalCapacity) {
            throw new LogicException("Гостей больше, чем мест за выбранными столами");
        }
        List<Long> busyIds = bookingRepo.findBusyTableIdsForDate(requestedIds, bookingDate, BookingStatus.CONFIRMED);
        if (!busyIds.isEmpty()) {
            throw new ConflictException("Эти столики уже заняты на этот вечер");
        }

        boolean isFreeBooking = (dayType == DayType.WEEKDAY);
        String phone = normalizePhone(dto.getPhoneNumber());

        if (isFreeBooking) {

            bookingRepo.lockFreeBookingUser(userId);

            Long phoneLockedId = Long.parseLong(phone.replace("+",""));
            bookingRepo.lockFreeBookingPhone(phoneLockedId);

            long userCount = bookingRepo.countFreeActiveByUserId(userId, BookingStatus.CONFIRMED, now.toLocalDate());
            long phoneCount = bookingRepo.countFreeActiveByPhone(phone, BookingStatus.CONFIRMED, now.toLocalDate());

            if (userCount >= bookingProperties.getMaxFreeActiveBookingPerUser() || phoneCount >= bookingProperties.getMaxFreeActiveBookingPerPhone()) {
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
        if(dto.getComment() != null && !dto.getComment().isBlank()){
            booking.setComment(dto.getComment().trim());
            }
        booking.setCreatedAt(now);
        booking.setDayType(dayType);
        booking.setTotalAmount(formatMoney(totalDeposit));

        LocalDateTime graceTime = arrivalAt.plusMinutes(bookingProperties.getArrivalGraceMinutes());
        LocalDateTime hardDeadLine = bookingDate.atTime(bookingProperties.getArrivalDeadline());
         if (graceTime.isBefore(hardDeadLine)) {
                booking.setHoldUntil(graceTime);
         } else {
                booking.setHoldUntil(hardDeadLine);
         }
         booking.setBookingStatus(BookingStatus.CONFIRMED);

         if (isFreeBooking) {
                booking.setPaymentStatus(PaymentStatus.NOT_REQUIRED);
         } else {
                booking.setPaymentStatus(PaymentStatus.PAID);
         }

         booking.setEnable(true);

         bookingRepo.save(booking);
         return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long adminId, Long bookingId, BookingCancelDto dto) {
        Booking booking = bookingRepo.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь с ID: " + bookingId + " не найдена"));

        if(booking.getBookingStatus() == BookingStatus.CANCELLED){
            throw new ConflictException("Бронь с ID: " + bookingId + " уже отменена");
        }
        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());
        if (booking.getBookingDate().isBefore(now.toLocalDate())){
            throw new LogicException("Нельзя отменить прошедшую бронь");
        }
        BookingCancelResponse policy = buildCancelResponse(booking,now);
        if(dto.fullRefund()){
            BigDecimal total = booking.getTotalAmount();
            policy = new BookingCancelResponse(policy.bookingId(),true,policy.cancelDeadline()
                    ,total,total,100,"Полный возврат по решению администрации");
        }
        applyCancellation(booking,adminId,dto.reason(),now,policy);
        return bookingMapper.toResponse(bookingRepo.save(booking));
    }

    @Override
    @Transactional
    public BookingResponse cancelOwnBooking(Long userId, Long bookingId, String reason) {
        Booking booking = bookingRepo.findOwnByIdForUpdate(bookingId,userId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());

        if(booking.getBookingStatus() == BookingStatus.CANCELLED){
            throw new ConflictException("Бронь уже отменена");
        }
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new LogicException("Отменить можно только подтвержденную бронь");
        }
        BookingCancelResponse response = buildCancelResponse(booking,now);
        if(!response.cancellable()){
            throw new LogicException(response.message());
        }
        applyCancellation(booking,userId,reason,now,response);

        return bookingMapper.toResponse(bookingRepo.save(booking));
    }

    @Override
    @Transactional
    public BookingCancelResponse requestRefund(Long userId, Long bookingId){
        Booking booking = bookingRepo.findOwnByIdForUpdate(bookingId, userId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        if(booking.getBookingStatus() != BookingStatus.CANCELLED){
            throw new LogicException("Сначала отмените бронь");
        }
        if(booking.getRefundAmount() == null){
            throw new LogicException("Сумма возврата не зафиксирована");
        }
        if(booking.isRefundRequested()){
            throw new ConflictException("Заявка уже создана");
        }
        booking.setRefundRequested(true);
        booking.setRefundRequestedAt(LocalDateTime.now(bookingProperties.getClubZone()));
        bookingRepo.save(booking);

        String message = "Заявка принята. Возврат в течении "
                + bookingProperties.getRefundWorkingDays() + " рабочих дней" ;
        return new BookingCancelResponse(
                booking.getId(),
                true,
                booking.getHoldUntil(),
                booking.getTotalAmount(),
                booking.getRefundAmount(),
                booking.getRefundPercent(),
                message
        );
    }


    @Override
    public byte[] exportBookingsForNight(LocalDate date) {
        List<Booking> bookings = bookingRepo.findAllForNight(date);

        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Дата;Время;Столики;Гость;Телефон;Гостей;Сумма;Статус;Оплата;Комментарий\n");

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        for(Booking b : bookings){
            StringBuilder tableStr = new StringBuilder();
            for (Tables t : b.getTables()){
                if(!tableStr.isEmpty()) tableStr.append(", ");
                tableStr.append(t.getTableNumber());
            }

            csv.append(b.getBookingDate()).append(';')
                    .append(b.getDateTime().format(timeFormat)).append(';')
                    .append(csvCell(tableStr.toString())).append(';')
                    .append(csvCell(b.getUserName())).append(';')
                    .append(csvCell(b.getPhoneNumber())).append(';')
                    .append(b.getGuestCount()).append(';')
                    .append(formatMoney(b.getTotalAmount())).append(';')
                    .append(b.getBookingStatus()).append(';')
                    .append(b.getPaymentStatus()).append(';')
                    .append(csvCell(b.getComment())).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingCancelResponse getCancelResponse(Long userId, Long bookingId) {
        Booking booking = bookingRepo.findOwnById(bookingId,userId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        return buildCancelResponse(booking,LocalDateTime.now(bookingProperties.getClubZone()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsForNight(LocalDate date) {
        List<Booking> bookings = bookingRepo.findAllForNight(date);
        List<BookingResponse> responses = new ArrayList<>();
        for(Booking booking : bookings){
            responses.add(bookingMapper.toResponse(booking));
        }
        return responses;
    }

    @Override
    @Transactional
    public BookingResponse markNoShow(Long adminId, Long bookingId) {
        Booking booking = bookingRepo.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new LogicException("Отметить неявку можно только у подтверждённой брони");
        }
        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());

        if(now.isBefore(booking.getHoldUntil())){
            throw new LogicException("Время ожидания еще не истекло");
        }

        booking.setBookingStatus(BookingStatus.NO_SHOW);
        booking.setEnable(false);
        booking.setCancelledAt(now);
        booking.setCancelledBy(adminId);
        booking.setRefundAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        booking.setRefundPercent(0);


        return bookingMapper.toResponse(bookingRepo.save(booking));
    }


    private BookingCancelResponse buildCancelResponse(Booking booking, LocalDateTime now) {
        BigDecimal total = booking.getTotalAmount();
        BigDecimal zero = formatMoney(BigDecimal.ZERO);
        LocalDateTime deadline = booking.getDateTime();
        if(booking.getHoldUntil() != null){
            deadline = booking.getHoldUntil();
        }

        if(booking.getBookingStatus() == BookingStatus.CANCELLED){
            return new BookingCancelResponse(booking.getId(), false,deadline,total,zero,0, "Бронь уже отменена");
        }
        if(now.isAfter(deadline)) {
            return new BookingCancelResponse(booking.getId(), false,deadline,total,zero,0,"Время вышло,депозит не возвращается");
        }

        Duration timeToArrival = Duration.between(now,booking.getDateTime());
        long minutesToArrival = timeToArrival.toMinutes();
        long minuteSinceCreation = Duration.between(booking.getCreatedAt(),now).toMinutes();

        boolean isAccidental = (minuteSinceCreation < bookingProperties.getCancellationGraceMinutes())
                && (minutesToArrival >= bookingProperties.getCancellationGraceBeforeHours() * 60L);

        int percent;
        if(total.signum()==0 || isAccidental){
            percent = 100;
        } else if (minutesToArrival >= bookingProperties.getRefundFullBeforeHours() * 60L){
            percent = 100;
        }else {
            percent = bookingProperties.getRefundPartialPercent();
        }
        BigDecimal refund = total.multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);

        String message = "К возврату " + refund + " Срок возврата до " + bookingProperties.getRefundWorkingDays() + " дней";

        return new BookingCancelResponse(booking.getId(), true,deadline,total,refund,percent, message);

    }

    private void validateArrivalTime(LocalDateTime arrivalAt) {
        LocalTime time = arrivalAt.toLocalTime();

        if(time.isBefore(bookingProperties.getOpenTime()) || time.isAfter(bookingProperties.getArrivalDeadline())){
            throw new LogicException("Бронь принимается с " +
                    bookingProperties.getOpenTime() + " до " +
                    bookingProperties.getArrivalDeadline());
        }
    }

    private void applyCancellation(Booking booking, Long id, String reason, LocalDateTime now,BookingCancelResponse policy) {
        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setEnable(false);

        booking.setCancelledAt(now);
        booking.setCancelledBy(id);
        booking.setRefundAmount(policy.refundAmount());
        booking.setRefundPercent(policy.refundPercent());
        if(reason != null && !reason.isBlank()){
            booking.setCancelReason(reason.trim());
        }else {
            booking.setCancelReason(null);
        }
    }


    private void validateSchedule(LocalDateTime arrivalAt, LocalDateTime now) {
        LocalDate bookingDate = arrivalAt.toLocalDate();
        LocalDate today = now.toLocalDate();

        if (bookingDate.isBefore(today)) {
            throw new LogicException("Нельзя забронировать на прошедшую дату");
        }
        if(bookingDate.isAfter(today.plusDays(bookingProperties.getMaxDepthDays()))){
            throw new LogicException("Бронирование доступно максимум на " +
                    bookingProperties.getMaxDepthDays() + " дней вперед");
        }
        if (arrivalAt.isBefore(now.plusMinutes(bookingProperties.getMinLeadMinutes()))) {
            throw new LogicException("Бронь принимается минимум за "
                    + bookingProperties.getMinLeadMinutes() + " минут до начала");
        }

    }
    private String normalizePhone(String phone){
        if(phone == null || phone.isBlank()){
            throw new LogicException("Номер телефона обязателен");
        }
        String digitsOnly = phone.replaceAll("[^0-9]", "");

        if(digitsOnly.startsWith("0") && digitsOnly.length() == 10){
            digitsOnly = "996" + digitsOnly.substring(1);
        }
        if(digitsOnly.length() < 9 || digitsOnly.length() > 15){
            throw new LogicException("Некорректный номер телефона");
        }
        return "+" + digitsOnly;
    }
    private BigDecimal formatMoney(BigDecimal amount){
        return amount.setScale(2,RoundingMode.HALF_UP);
    }

    private String csvCell(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String cleaned = value.replace("\"", "\"\"").replaceAll("[\\r\\n]+", " ");
        if(cleaned.matches("^[=+\\-@t\r].*")){
            cleaned = "'" + cleaned;
        }
        return "\"" + cleaned + "\"";
    }

}
