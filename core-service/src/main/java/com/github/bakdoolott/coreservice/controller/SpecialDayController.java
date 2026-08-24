package com.github.bakdoolott.coreservice.controller;

import com.github.bakdoolott.coreservice.models.dto.SpecialDayCreateDto;
import com.github.bakdoolott.coreservice.models.dto.response.SpecialDayResponse;
import com.github.bakdoolott.coreservice.services.SpecialDayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/core/special-days")
@Tag(name = "Специальные дни")
public class SpecialDayController {

    private final SpecialDayService specialDayService;


    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @Operation(summary = "Создание специального дня",description = "Праздник,Выходной,Нерабочий день")
    public ResponseEntity<SpecialDayResponse> createdSpecialDay(@AuthenticationPrincipal Long adminId,
                                                                @Valid @RequestBody SpecialDayCreateDto request){
        return new ResponseEntity<>(specialDayService.create(adminId, request), HttpStatus.CREATED);

    }

    @PutMapping("/{date}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @Operation(summary = "Обновление специального дня")
    public ResponseEntity<SpecialDayResponse> updateSpecialDay(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date,
                                                               @Valid @RequestBody SpecialDayCreateDto request){
        return ResponseEntity.ok(specialDayService.update(date,request));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @Operation(summary = "Список будущих специальных дней")
    public ResponseEntity<List<SpecialDayResponse>> listFutureDays(){
        return ResponseEntity.ok(specialDayService.getFutureDays());
    }

    @DeleteMapping("/{date}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_OWNER')")
    @Operation(summary = "Удаление специального дня")
    public ResponseEntity<Void> deleteSpecialDay(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        specialDayService.deleteByDate(date);
        return ResponseEntity.noContent().build();
    }



}
