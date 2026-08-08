package com.github.bakdoolott.coreservice.controller;

import com.github.bakdoolott.coreservice.models.dto.request.CreateHallRequest;
import com.github.bakdoolott.coreservice.models.dto.request.UpdateHallRequest;
import com.github.bakdoolott.coreservice.models.dto.response.HallResponse;
import com.github.bakdoolott.coreservice.services.HallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/core/hall")
@RequiredArgsConstructor
@Tag(name = "Залы")
public class HallController {
    private final HallService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OWNER')")
    @Operation(summary = "Создание зала")
    public ResponseEntity<HallResponse> create(CreateHallRequest request){
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PatchMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OWNER')")
    @Operation(summary = "Обновление зала")
    public ResponseEntity<HallResponse> update(@Valid UpdateHallRequest request){
        return new ResponseEntity<>(service.update(request), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Нахождение всех залов",
            description = "Находит все залы, ничего не принимает, возвращает список"
    )
    public ResponseEntity<List<HallResponse>> getAll(){
        return new ResponseEntity<>(service.getAll(), HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OWNER')")
    @Operation(summary = "Удаление зала по Id")
    public ResponseEntity<Void> deleteHall(@PathVariable Long id){
        service.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}