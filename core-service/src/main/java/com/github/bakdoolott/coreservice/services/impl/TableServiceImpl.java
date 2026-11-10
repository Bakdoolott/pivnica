package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.exceptions.NotFoundException;
import com.github.bakdoolott.coreservice.mappers.HallMapper;
import com.github.bakdoolott.coreservice.mappers.TableMapper;
import com.github.bakdoolott.coreservice.models.Hall;
import com.github.bakdoolott.coreservice.models.Tables;
import com.github.bakdoolott.coreservice.models.dto.TablesDto;
import com.github.bakdoolott.coreservice.models.dto.request.CreateTableRequest;
import com.github.bakdoolott.coreservice.models.dto.request.UpdateTableRequest;
import com.github.bakdoolott.coreservice.models.dto.response.TableResponse;
import com.github.bakdoolott.coreservice.repositories.TableRepo;
import com.github.bakdoolott.coreservice.services.HallService;
import com.github.bakdoolott.coreservice.services.TableService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableServiceImpl implements TableService {

    private final TableRepo repository;
    private final HallService hallService;
    private final TableMapper mapper;
    private final HallMapper hallMapper;

    @Override
    @Transactional
    public List<TableResponse> create(List<CreateTableRequest> requests) {
        List<Tables> entities = new ArrayList<>(requests.size());

        for(CreateTableRequest request : requests){
            if(!hallService.isExistById(request.hallId())){
                throw new NotFoundException("Зал с ID " + request.hallId() + " не найден");
            }
            Optional<Tables> existing = repository.findByHallIdAndTableNumber(
                    request.hallId(),request.tableNumber());

                    Tables table;
                    if (existing.isPresent()) {
                        table = existing.get();
                        table.setX(request.x());
                        table.setY(request.y());
                        table.setTableType(request.tableType());
                        table.setTableState(request.tableState());
                        boolean wasDisabled = !table.isEnable();
                        table.setEnable(true);

                        log.info("Стол (айди зала={}, номер стола={}) уже существует (id={}{}). Обновился",
                                request.hallId(), request.tableNumber(), table.getId(),
                                wasDisabled ? ", был отключен - сейчас включен" : "");
                    } else {
                        table = Tables.builder()
                                .tableNumber(request.tableNumber())
                                .x(request.x())
                                .y(request.y())
                                .tableType(request.tableType())
                                .tableState(request.tableState())
                                .hall(Hall.builder().id(request.hallId()).build())
                                .enable(true)
                                .build();
                    }
                    entities.add(table);
                }
                return repository.saveAll(entities).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<TableResponse> update(List<UpdateTableRequest> requests) {
        Map<Long, Hall> hallCache = new HashMap<>();

        List<Tables> tables = requests.stream()
                .map(request -> {
                    Hall hall = hallCache.computeIfAbsent(request.hallId(),
                            id -> hallMapper.toEntity(hallService.findById(id)));

                    Tables table = repository.findByIdAndEnable(request.id(), true)
                            .orElseThrow(() -> new NotFoundException(
                                    "Столик с ID " + request.id() + " не найден"));

                    table.setTableNumber(request.tableNumber());
                    table.setX(request.x());
                    table.setY(request.y());
                    table.setTableType(request.tableType());
                    table.setTableState(request.tableState());
                    table.setHall(hall);

                    return table;
                })
                .toList();

        return tables.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public TableResponse getById(Long id) {
        return mapper.toResponse(findById(id));
    }

    @Override
    public TablesDto findById(Long id) {
        return mapper.toDto(
                repository.findByIdAndEnable(id, true)
                        .orElseThrow(() -> new NotFoundException("Столик с ID " + id + " не найден"))
        );
    }

    @Override
    public List<TableResponse> findTablesByHallId(Long id) {
        return mapper.toResponseList(
                repository.findByHallIdAndEnableTrueOrderByTableNumberAsc(id)
        );
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Tables table = repository.findByIdAndEnable(id, true)
                .orElseThrow(() -> new NotFoundException("Столик с ID " + id + " не найден"));

        table.setEnable(false);
    }
}