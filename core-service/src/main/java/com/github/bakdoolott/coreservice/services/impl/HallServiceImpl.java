package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.exceptions.NotFoundException;
import com.github.bakdoolott.coreservice.mappers.HallMapper;
import com.github.bakdoolott.coreservice.models.Hall;
import com.github.bakdoolott.coreservice.models.dto.HallDto;
import com.github.bakdoolott.coreservice.models.dto.request.CreateHallRequest;
import com.github.bakdoolott.coreservice.models.dto.request.UpdateHallRequest;
import com.github.bakdoolott.coreservice.models.dto.response.HallResponse;
import com.github.bakdoolott.coreservice.models.enums.HallStatus;
import com.github.bakdoolott.coreservice.repositories.HallRepo;
import com.github.bakdoolott.coreservice.services.HallService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HallServiceImpl implements HallService {
    private final HallRepo repository;
    private final HallMapper mapper;

    @Override
    @Transactional
    public HallResponse create(CreateHallRequest request) {
        Hall hall = Hall.builder()
                .floor(request.floor())
                .hallNumber(request.hallNumber())
                .hallStatus(request.hallStatus())
                .build();

        return mapper.toResponse(repository.save(hall));
    }

    @Override
    @Transactional
    public HallResponse update(UpdateHallRequest request) {
        Hall hall = repository.findByIdAndEnable(request.id(), true).orElseThrow(
                () -> new NotFoundException("Hall with id: " + request.id() + " not found"));
        hall.setFloor(request.floor());
        hall.setHallNumber(request.hallNumber());
        hall.setHallStatus(request.hallStatus());

        return mapper.toResponse(
                repository.save(hall)
        );
    }

    @Override
    public HallResponse getById(Long id) {
        return mapper.toResponse(
                findById(id)
        );
    }

    @Override
    public HallDto findById(Long id) {
        return mapper.toDto(
                repository.findById(id).orElseThrow(
                        () -> new NotFoundException("Entity with id: " + id + " not found")
                )
        );
    }

    @Override
    public List<HallResponse> getAll() {
        return mapper.toResponseList(
                repository.findAllByHallStatusAndEnable(HallStatus.ENABLE, true)
        );
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Hall hall = repository.findByIdAndEnable(id, true)
                .orElseThrow(() -> new NotFoundException("Entity with id: " + id + " not found"));
        hall.setEnable(false);
    }

    @Override
    public Boolean isExistById(Long id) {
        return repository.existsById(id);
    }
}
