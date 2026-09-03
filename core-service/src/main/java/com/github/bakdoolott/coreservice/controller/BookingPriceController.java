package com.github.bakdoolott.coreservice.controller;

import com.github.bakdoolott.coreservice.models.dto.PriceCreateDto;
import com.github.bakdoolott.coreservice.models.dto.PriceUpdateDto;
import com.github.bakdoolott.coreservice.models.dto.response.PriceResponse;
import com.github.bakdoolott.coreservice.models.enums.TableType;
import com.github.bakdoolott.coreservice.services.BookingPriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/core/booking-price")
@Tag(name = "Цены бронирования")
public class BookingPriceController {

    private final BookingPriceService service;

    public BookingPriceController(BookingPriceService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @Operation(summary = "Создание цены бронирования")
    public ResponseEntity<PriceResponse> create(@Valid @RequestBody PriceCreateDto request){
        return new ResponseEntity<>(service.createPrice(request), HttpStatus.CREATED);
    }

    @PatchMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @Operation(summary = "Обновление цены бронирования")
    public ResponseEntity<PriceResponse> update(@Valid @RequestBody PriceUpdateDto request){
        return  new ResponseEntity<>(service.updatePrice(request), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Действующие цены по всем типам столов")
    public ResponseEntity<List<PriceResponse>> current(){
        return ResponseEntity.ok(service.getCurrentPrices());
    }

    @GetMapping("/{tableType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Действующая цена по типу стола")
    public ResponseEntity<PriceResponse> current(@PathVariable TableType tableType){
        return ResponseEntity.ok(service.getCurrentPrice(tableType));
    }

    @GetMapping("/{tableType}/history")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "История цен по типу стола")
    public ResponseEntity<List<PriceResponse>> history(@PathVariable TableType tableType) {
        return ResponseEntity.ok(service.getHistory(tableType));
    }

}
