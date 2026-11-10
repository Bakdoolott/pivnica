package com.github.bakdoolott.coreservice.controller;

import com.github.bakdoolott.coreservice.models.dto.request.UpdatePaymentQrDto;
import com.github.bakdoolott.coreservice.models.dto.response.PaymentQrResponse;
import com.github.bakdoolott.coreservice.services.PaymentQrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/core/admin/payment-qr")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OWNER')")
@Tag(name = "Админ: QR-код оплаты")
public class PaymentQrAdminController {

    private final PaymentQrService service;

    @GetMapping
    @Operation(summary = "Все QR-коды (история)")
    public List<PaymentQrResponse> list() {
        return service.getAll();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Загрузить новый QR (старый деактивируется)")
    public ResponseEntity<PaymentQrResponse> upload(
            @AuthenticationPrincipal Long adminId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String description) {

        PaymentQrResponse response = service.upload(adminId, file, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/external")
    @Operation(summary = "Добавить QR по внешнему URL")
    public ResponseEntity<PaymentQrResponse> external(
            @AuthenticationPrincipal Long adminId,
            @RequestParam String imageUrl,
            @RequestParam(required = false) String description) {

        PaymentQrResponse response = service.createFromUrl(adminId, imageUrl, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Обновить описание QR")
    public ResponseEntity<PaymentQrResponse> update(
            @AuthenticationPrincipal Long adminId,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentQrDto dto) {

        return ResponseEntity.ok(service.update(adminId, id, dto));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Сделать QR активным (другие деактивируются)")
    public ResponseEntity<PaymentQrResponse> activate(
            @AuthenticationPrincipal Long adminId,
            @PathVariable Long id) {

        return ResponseEntity.ok(service.activate(adminId, id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Деактивировать QR")
    public ResponseEntity<Void> disable(
            @AuthenticationPrincipal Long adminId,
            @PathVariable Long id) {

        service.disable(adminId, id);
        return ResponseEntity.noContent().build();
    }
}