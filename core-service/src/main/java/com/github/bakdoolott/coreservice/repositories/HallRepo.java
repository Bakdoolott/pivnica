package com.github.bakdoolott.coreservice.repositories;

import com.github.bakdoolott.coreservice.models.Hall;
import com.github.bakdoolott.coreservice.models.dto.HallDto;
import com.github.bakdoolott.coreservice.models.enums.HallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HallRepo extends JpaRepository<Hall,Long> {
    List<HallDto> findAllByEnableAndHallStatus(boolean enable, HallStatus hallStatus);
    Optional<Hall> findByIdAndEnable(Long id, boolean enable);
}
