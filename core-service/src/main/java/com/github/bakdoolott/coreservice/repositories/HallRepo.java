package com.github.bakdoolott.coreservice.repositories;

import com.github.bakdoolott.coreservice.models.Hall;
import com.github.bakdoolott.coreservice.models.dto.HallDto;
import com.github.bakdoolott.coreservice.models.enums.HallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HallRepo extends JpaRepository<Hall,Long> {
    List<HallDto> findAllByEnableAndHallStatus(boolean enable, HallStatus hallStatus);
}
