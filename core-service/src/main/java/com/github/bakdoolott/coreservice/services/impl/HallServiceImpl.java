package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.exceptions.ConflictException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HallServiceImpl implements HallService {

    private final HallRepo repository;
    private final HallMapper mapper;

    @Override
    @Transactional
    public HallResponse create(CreateHallRequest request) {
        Optional<Hall> existing = repository.findByHallNumber(request.hallNumber());

        Hall hall;
        if (existing.isPresent()) {
            hall = existing.get();
            hall.setFloor(request.floor());
            if (request.hallStatus() != null) {
                hall.setHallStatus(request.hallStatus());
            }
            boolean wasDisabled = !hall.isEnable();
            hall.setEnable(true);

            log.info("Зал с айди:  '{}' уже существует (id={}{}). Обновился вместо создания",
                    request.hallNumber(), hall.getId(),
                    wasDisabled ? ", был неактивет стал активным" : "");
        } else {
            hall = Hall.builder()
                    .floor(request.floor())
                    .hallNumber(request.hallNumber())
                    .hallStatus(request.hallStatus() != null
                            ? request.hallStatus()
                            : HallStatus.ENABLE)
                    .enable(true)
                    .build();
            log.info("Cоздался зал с номером: '{}'", request.hallNumber());
        }

        return mapper.toResponse(repository.save(hall));
    }

    @Override
    @Transactional
    public HallResponse update(UpdateHallRequest request) {
        Hall hall = repository.findByIdAndEnableTrue(request.id())
                .orElseThrow(() -> new NotFoundException("Зал с ID " + request.id() + " не найден"));

        if (repository.existsByHallNumberAndIdNot(request.hallNumber(), request.id())) {
            throw new ConflictException("Зал с номером '" + request.hallNumber() + "' уже существует");
        }

        hall.setFloor(request.floor());
        hall.setHallNumber(request.hallNumber());
        if(request.hallStatus() != null){
            hall.setHallStatus(request.hallStatus());
        }

        return mapper.toResponse(repository.save(hall));
    }

    @Override
    public HallResponse getById(Long id) {

        return mapper.toResponse(findById(id));
    }

    @Override
    public HallDto findById(Long id) {
        return mapper.toDto(
                repository.findByIdAndEnableTrue(id)
                        .orElseThrow(() -> new NotFoundException("Зал с ID " + id + " не найден"))
        );
    }

    @Override
    public List<HallResponse> getAll() {

        return mapper.toResponseList(repository.findAllEnabled());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Hall hall = repository.findByIdAndEnableTrue(id)
                .orElseThrow(() -> new NotFoundException("Зал с ID " + id + " не найден"));
        hall.setEnable(false);
    }

    @Override
    public Boolean isExistById(Long id) {

        return repository.existsByIdAndEnableTrue(id);
    }
    
    private static HallStatus HallStatusDefault() {
        return HallStatus.ENABLE;
    }
}
