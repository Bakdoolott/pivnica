package com.github.bakdoolott.coreservice.repositories;

import com.github.bakdoolott.coreservice.models.BookingPrice;
import com.github.bakdoolott.coreservice.models.enums.TableType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingPriceRepo extends JpaRepository<BookingPrice,Long> {

    @Query("""
            select p from BookingPrice p
            where p.enable = true
              and p.startAt <= :at
              and (p.endAt is null or p.endAt > :at)
            """)
    List<BookingPrice> findAllActiveAt(@Param("at") LocalDateTime at);

    @Query("""
            select p from BookingPrice p
            where p.tableType = :tableType
              and p.enable = true
              and p.startAt <= :at
              and (p.endAt is null or p.endAt > :at)
            order by p.startAt desc
            limit 1
            """)
    Optional<BookingPrice> findActiveAt(@Param("tableType") TableType tableType,
                                        @Param("at") LocalDateTime at);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p from BookingPrice p
            where p.tableType = :tableType
              and p.enable = true
              and p.endAt is null
            """)
    Optional<BookingPrice> findOpenForUpdate(@Param("tableType") TableType tableType);

    @Query("""
            select p from BookingPrice p
            where p.tableType = :tableType
            order by p.startAt desc
            """)
    List<BookingPrice> findHistory(@Param("tableType") TableType tableType);
}