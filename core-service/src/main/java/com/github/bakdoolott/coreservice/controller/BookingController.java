package com.github.bakdoolott.coreservice.controller;

import com.github.bakdoolott.coreservice.models.dto.BookingCancelDto;
import com.github.bakdoolott.coreservice.models.dto.BookingCreateDto;
import com.github.bakdoolott.coreservice.models.dto.response.BookingResponse;
import com.github.bakdoolott.coreservice.security.AuthenticatedUserResolver;
import com.github.bakdoolott.coreservice.services.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/core/booking")
@Tag(name = "Бронирования")
public class BookingController {

    private final BookingService bookingService;
    private final AuthenticatedUserResolver userResolver;

    public BookingController(BookingService bookingService, AuthenticatedUserResolver userResolver) {
        this.bookingService = bookingService;
        this.userResolver = userResolver;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cоздание брони")
    public ResponseEntity<BookingResponse> create(Authentication authentication,
                                                  @Valid @RequestBody BookingCreateDto request) {
        Long userId = userResolver.getUserId(authentication);
        return new ResponseEntity<>(bookingService.createBooking(userId, request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @Operation(summary = "Брони на указанную дату")
    public ResponseEntity<List<BookingResponse>> listByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(bookingService.getBookingsForNight(date));
    }

    @PostMapping("{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @Operation(summary = "Отмена брони администратором")
    public ResponseEntity<BookingResponse> cancel(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody BookingCancelDto request) {
        Long adminId = userResolver.getUserId(authentication);
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
}
