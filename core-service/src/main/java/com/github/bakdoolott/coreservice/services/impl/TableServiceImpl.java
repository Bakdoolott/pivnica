package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.exceptions.NotFoundException;
import com.github.bakdoolott.coreservice.mappers.HallMapper;
import com.github.bakdoolott.coreservice.mappers.TableMapper;
import com.github.bakdoolott.coreservice.models.Hall;
import com.github.bakdoolott.coreservice.models.Tables;
import com.github.bakdoolott.coreservice.models.dto.request.CreateTableRequest;
import com.github.bakdoolott.coreservice.models.dto.request.UpdateTableRequest;
import com.github.bakdoolott.coreservice.models.dto.response.TableResponse;
import com.github.bakdoolott.coreservice.repositories.TableRepo;
import com.github.bakdoolott.coreservice.services.HallService;
import com.github.bakdoolott.coreservice.services.TableService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TableServiceImpl implements TableService {
    private final TableRepo repository;
    private final HallService hallService;
    private final TableMapper mapper;
    private final HallMapper hallMapper;

    @Override
    @Transactional
    public TableResponse create(CreateTableRequest request) {
        if(!hallService.isExistById(request.hallId())){
            throw new NotFoundException("Hall with id: " + request.hallId() + " not found");
        }

        Tables entity = Tables.builder()
                .tableNumber(request.tableNumber())
                .x(request.x())
                .y(request.y())
                .placeCount(request.placeCount())
                .tableState(request.tableState())
                .hall(Hall.builder().id(request.hallId()).build())
                .build();
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public List<TableResponse> update(List<UpdateTableRequest> requests) {

        List<Tables> tables = requests.stream()
                .map(request -> {

                    if (!hallService.isExistById(request.hallId())) {
                        throw new NotFoundException(
                                "Hall with id: " + request.hallId() + " not found"
                        );
                    }

                    Tables table = repository.findByIdAndEnable(request.id(), true)
                            .orElseThrow(() ->
                                    new NotFoundException(
                                            "Table with id: " + request.id() + " not found"
                                    )
                            );

                    table.setTableNumber(request.tableNumber());
                    table.setX(request.x());
                    table.setY(request.y());
                    table.setPlaceCount(request.placeCount());
                    table.setTableState(request.tableState());

                    table.setHall(
                            hallMapper.toEntity(
                                    hallService.findById(request.hallId())
                            )
                    );

                    return table;
                })
                .toList();

        return tables.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public TableResponse findById(Long id) {
        return mapper.toResponse(
                repository.findByIdAndEnable(id, true).orElseThrow(
                        () -> new NotFoundException("Table with id: " + id + " not found")
                )
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
        Tables table = repository.findByIdAndEnable(id, true).orElseThrow(
                () -> new NotFoundException("Table with id: " + id + " not found")
        );

        table.setEnable(false);
    }
}
