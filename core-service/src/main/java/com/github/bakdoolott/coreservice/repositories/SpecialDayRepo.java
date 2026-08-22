package com.github.bakdoolott.coreservice.repositories;

import com.github.bakdoolott.coreservice.models.SpecialDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialDayRepo extends JpaRepository<SpecialDay, Long> {

    Optional<SpecialDay> findByDateAndEnableTrue(LocalDate date);

    @Query("""
        select s from  SpecialDay s
        where s.date >= :today
        and s.enable = true
        order by s.date asc
""")
    List<SpecialDay> findAllFutureDays(@Param("today") LocalDate today);

    boolean existsByDateAndClosedTrueAndEnableTrue(LocalDate date);
}
