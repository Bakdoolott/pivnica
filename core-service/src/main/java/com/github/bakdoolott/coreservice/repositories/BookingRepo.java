package com.github.bakdoolott.coreservice.repositories;

import com.github.bakdoolott.coreservice.models.Booking;
import com.github.bakdoolott.coreservice.models.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepo extends JpaRepository<Booking,Long> {
    @Query("""
            select distinct t.id from Booking b
            join b.tables t
            where t.id in :tableIds
              and b.enable = true
              and b.bookingStatus = :confirmed
              and b.dateTime < :to
              and b.endsAt > :from
            """)
    List<Long> findBusyTableIdsAmong(@Param("tableIds") List<Long> tableIds,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to,
                                     @Param("confirmed") BookingStatus confirmed);
}
