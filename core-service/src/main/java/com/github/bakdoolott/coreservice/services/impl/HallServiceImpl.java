package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.exceptions.NotFoundException;
import com.github.bakdoolott.coreservice.mappers.HallMapper;
import com.github.bakdoolott.coreservice.models.Hall;
import com.github.bakdoolott.coreservice.models.dto.HallDto;
import com.github.bakdoolott.coreservice.models.dto.request.CreateHallRequest;
import com.github.bakdoolott.coreservice.models.dto.request.UpdateHallRequest;
import com.github.bakdoolott.coreservice.models.enums.HallStatus;
import com.github.bakdoolott.coreservice.repositories.HallRepo;
import com.github.bakdoolott.coreservice.services.HallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HallServiceImpl implements HallService {
    private final HallRepo repository;
    private final HallMapper mapper;

    @Override
    public HallDto create(CreateHallRequest request) {
        Hall hall = Hall.builder()
                .floor(request.floor())
                .hallNumber(request.hallNumber())
                .hallStatus(request.hallStatus())
                .build();

        return mapper.toDto(repository.save(hall));
    }

    @Override
    public HallDto update(UpdateHallRequest request) {
        Hall hall = repository.findById(request.id()).orElseThrow(
                () -> new NotFoundException("Entity with id: " + request.id() + " not found"));
        hall.setFloor(request.floor());
        hall.setHallNumber(request.hallNumber());
        hall.setHallStatus(request.hallStatus());

        return mapper.toDto(
                repository.save(hall)
        );
    }

    @Override
    public List<HallDto> getAll() {
        return repository.findAllByEnableAndHallStatus(true, HallStatus.ENABLE);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
