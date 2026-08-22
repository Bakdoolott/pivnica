package com.github.bakdoolott.coreservice.controller;

import com.github.bakdoolott.coreservice.models.dto.BookingCancelDto;
import com.github.bakdoolott.coreservice.models.dto.BookingCreateDto;
import com.github.bakdoolott.coreservice.models.dto.response.BookingCancelResponse;
import com.github.bakdoolott.coreservice.models.dto.response.BookingResponse;
import com.github.bakdoolott.coreservice.services.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/core/booking")
@Tag(name = "Бронирования")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cоздание брони")
    public ResponseEntity<BookingResponse> create(@AuthenticationPrincipal Long userId,
                                                  @Valid @RequestBody BookingCreateDto request) {

        return new ResponseEntity<>(bookingService.createBooking(userId, request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @Operation(summary = "Брони на указанную дату")
    public ResponseEntity<List<BookingResponse>> listByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(bookingService.getBookingsForNight(date));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @Operation(summary = "Отмена брони администратором")
    public ResponseEntity<BookingResponse> cancel(@AuthenticationPrincipal Long adminId,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody BookingCancelDto request) {

        return ResponseEntity.ok(bookingService.cancelBooking(adminId, id, request));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @Operation(summary = "Выгрузка списка броней")
    public ResponseEntity<byte[]> export(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        byte[] body = bookingService.exportBookingsForNight(date);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"bookings-" + date + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }
    @GetMapping("/{id}/cancellation")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Условия отмены брони",
            description = "Показывает можно ли отменить")
    public ResponseEntity<BookingCancelResponse> cancellationPolicy(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        return ResponseEntity.ok(bookingService.getCancelResponse(userId, id));
    }

    @PostMapping("/{id}/cancel-guest")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Отмена своей брони")
    public ResponseEntity<BookingResponse> cancelOwn(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestParam(required = false) @Size(max = 300) String reason) {

        return ResponseEntity.ok(bookingService.cancelOwnBooking(userId, id, reason));
    }
    
    @PostMapping("/{id}/refund")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Запрос на возврат средств",description = "Создает заявку на возврат после отмены брони")
    public ResponseEntity<BookingCancelResponse> requestRefund(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id){

        return ResponseEntity.ok(bookingService.requestRefund(userId,id));
    }

    @PostMapping("/{id}/no-show")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @Operation(summary = "Отметить неявку гостя")
    public ResponseEntity<BookingResponse> markNoShow(@AuthenticationPrincipal Long adminId, @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.markNoShow(adminId, id));
    }
}
