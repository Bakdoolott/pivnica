package com.github.bakdoolott.coreservice.repositories;

import com.github.bakdoolott.coreservice.models.Booking;
import com.github.bakdoolott.coreservice.models.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Set;

@Repository
public interface BookingRepo extends JpaRepository<Booking,Long> {
    @Query("""
            SELECT DISTINCT t.id
            FROM Booking b
            JOIN b.tables t
            WHERE t.hall.id = :hallId
              AND b.enable = true
              AND b.dateTime < :nightEnd
              AND b.endsAt > :nightStart
              AND (
                  b.bookingStatus = com.github.bakdoolott.coreservice.models.enums.BookingStatus.CONFIRMED
                  OR (
                      b.bookingStatus = com.github.bakdoolott.coreservice.models.enums.BookingStatus.PENDING
                      AND b.holdUntil > :now
                  )
              )
            """)
    Set<Long> findBookedTableIds(@Param("hallId") Long hallId,
                                 @Param("nightStart") LocalDateTime nightStart,
                                 @Param("nightEnd") LocalDateTime nightEnd,
                                 @Param("now") LocalDateTime now);
}
