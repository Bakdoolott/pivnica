package com.github.bakdoolott.coreservice.repositories;

import com.github.bakdoolott.coreservice.models.Hall;
import com.github.bakdoolott.coreservice.models.enums.HallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HallRepo extends JpaRepository<Hall, Long> {

    @Query("select h from Hall h where h.id = :id and h.enable = true")
    Optional<Hall> findByIdAndEnableTrue(@Param("id") Long id);

    boolean existsByIdAndEnableTrue(Long id);

    @Query("select h from Hall h where h.enable = true order by h.id")
    List<Hall> findAllEnabled();

    @Query("select h from Hall h where h.hallStatus = :status and h.enable = true")
    List<Hall> findAllByHallStatusAndEnableTrue(@Param("status") HallStatus status);

    boolean existsByHallNumber(String hallNumber);

    boolean existsByHallNumberAndIdNot(String hallNumber, Long id);

    Optional<Hall> findByHallNumber(String hallNumber);
}