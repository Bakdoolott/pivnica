package com.github.bakdoolott.coreservice.repositories;

import com.github.bakdoolott.coreservice.models.BookingPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BookingPriceRepo extends JpaRepository<BookingPrice,Long> {
    @Query("""
        select p from BookingPrice p
        where p.enable = true
          and p.startAt <= :now
          and (p.endAt is null or p.endAt > :now)
        order by p.startAt desc
        limit 1
        """)
    Optional<BookingPrice> findActiveAt(@Param("now") LocalDateTime now);

    Optional<BookingPrice> findFirstByEnableTrueAndEndAtIsNullOrderByStartAtDesc();
}

