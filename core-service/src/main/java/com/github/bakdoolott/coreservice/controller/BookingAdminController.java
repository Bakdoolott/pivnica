package com.github.bakdoolott.coreservice.controller;

import com.github.bakdoolott.coreservice.models.dto.response.BookingResponse;
import com.github.bakdoolott.coreservice.services.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/core/admin/booking")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OWNER')")
@Tag(name = "Админ: бронирования")
public class BookingAdminController {

    private final BookingService bookingService;

    @GetMapping("/awaiting-confirmation")
    @Operation(summary = "Брони, ожидающие подтверждения оплаты")
    public ResponseEntity<List<BookingResponse>> awaitingConfirmation() {
        return ResponseEntity.ok(bookingService.getAwaitingConfirmation());
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Подтвердить оплату брони")
    public ResponseEntity<BookingResponse> confirm(
            @AuthenticationPrincipal Long adminId,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmByAdmin(adminId, id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Отклонить бронь")
    public ResponseEntity<BookingResponse> reject(
            @AuthenticationPrincipal Long adminId,
            @PathVariable Long id,
            @Valid @RequestBody RejectBookingDto dto) {
        return ResponseEntity.ok(bookingService.rejectByAdmin(
                adminId, id, dto.reason(), dto.moneyReceived()));
    }

    @GetMapping("/refund-requests")
    @Operation(summary = "Заявки на возврат")
    public ResponseEntity<List<BookingResponse>> refundRequests() {
        return ResponseEntity.ok(bookingService.getRefundRequests());
    }

    @PostMapping("/{id}/refund-done")
    @Operation(summary = "Отметить возврат выполненным")
    public ResponseEntity<BookingResponse> markRefunded(
            @AuthenticationPrincipal Long adminId,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.markRefunded(adminId, id));
    }

    public record RejectBookingDto(
            @NotBlank(message = "Причина обязательна")
            @Size(max = 300)
            String reason,

            boolean moneyReceived
    ) {}
}