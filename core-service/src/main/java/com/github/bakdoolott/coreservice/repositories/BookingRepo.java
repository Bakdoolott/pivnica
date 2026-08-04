package com.github.bakdoolott.coreservice.repositories;

import com.github.bakdoolott.coreservice.models.Booking;
import com.github.bakdoolott.coreservice.models.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface BookingRepo extends JpaRepository<Booking,Long> {
    @Query("select b.tables.id from Booking b " +
            "where b.tables.hall.id = :hallId " +
            "  and b.enable = true " +
            "  and b.bookingStatus in :statuses " +
            "  and b.dateTime < :nightEnd " +
            "  and b.endsAt   > :nightStart " +
            "  and (b.bookingStatus <> com.github.bakdoolott.coreservice.models.enums.BookingStatus.PENDING " +
            "       or b.holdUntil > :now)")
    Set<Long> findBookedTableIds(@Param("hallId") Long hallId,
                                 @Param("nightStart") LocalDateTime nightStart,
                                 @Param("nightEnd") LocalDateTime nightEnd,
                                 @Param("statuses") Collection<BookingStatus> statuses,
                                 @Param("now") LocalDateTime now);
}
