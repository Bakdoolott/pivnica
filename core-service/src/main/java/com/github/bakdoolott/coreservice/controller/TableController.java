package com.github.bakdoolott.coreservice.controller;

import com.github.bakdoolott.coreservice.models.dto.request.CreateTableRequest;
import com.github.bakdoolott.coreservice.models.dto.request.UpdateTableRequest;
import com.github.bakdoolott.coreservice.models.dto.response.TableResponse;
import com.github.bakdoolott.coreservice.services.TableService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/core/table")
@Tag(name = "Столы")
public class TableController {
    private final TableService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OWNER')")
    @Operation(
            summary = "Создание стола",
            description = "Столы могут создавать ADMIN и OWNER"
    )
    public ResponseEntity<List<TableResponse>> create(@RequestBody @Valid List<CreateTableRequest> request){
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PatchMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OWNER')")
    @Operation(
            summary = "Обновление стола списком",
            description = "Принимает список и обновляет всех"
    )
    public ResponseEntity<List<TableResponse>> update(@RequestBody @Valid List<UpdateTableRequest> request){
        return new ResponseEntity<>(service.update(request), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Нахождение одного стола по Id")
    public ResponseEntity<TableResponse> getById(@PathVariable Long id){
        return new ResponseEntity<>(service.getById(id), HttpStatus.OK);
    }

    @GetMapping("/get-hall-tables/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Нахождение столов одного зала",
            description = "Находит все столы по Id зала. Возвращает список"
    )
    public ResponseEntity<List<TableResponse>> getTablesByHallId(@PathVariable Long id){
        return new ResponseEntity<>(service.findTablesByHallId(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OWNER')")
    @Operation(summary = "Удаление стола по id")
    public ResponseEntity<Void> deleteById(@PathVariable Long id){
        service.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
