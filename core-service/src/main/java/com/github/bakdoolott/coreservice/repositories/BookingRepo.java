package com.github.bakdoolott.coreservice.repositories;

import com.github.bakdoolott.coreservice.models.Booking;
import com.github.bakdoolott.coreservice.models.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepo extends JpaRepository<Booking,Long> {
    @Query("""
            select distinct t.id from Booking b
            join b.tables t
            where t.id in :tableIds
              and b.enable = true
              and b.bookingStatus = :confirmed
              and b.bookingDate = :bookingDate
            """)
    List<Long> findBusyTableIdsForDate(@Param("tableIds") List<Long> tableIds,
                                     @Param("bookingDate") LocalDate bookingDate,
                                     @Param("confirmed") BookingStatus confirmed);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select distinct b from Booking b
            left join fetch b.tables
            where b.bookingDate = :bookingDate
            order by b.dateTime asc, b.id asc
        """)
    List<Booking> findAllForNight(@Param("bookingDate") LocalDate bookingDate);

    @Query("""
            select b from Booking b
            left join fetch b.tables
            where b.id = :id
              and b.userId = :userId
            """)
    Optional<Booking> findOwnById(@Param("id") Long id, @Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select b from Booking b
           where b.id = :id
           and b.userId = :userId
""")
    Optional<Booking> findOwnByIdForUpdate(@Param("id")Long id,
                                           @Param("userId") Long userId);

    @Query("""
          select count(b) from Booking b
          where b.userId = :userId
          and b.enable = true
          and b.bookingStatus = :confirmed
          and b.totalAmount = 0
          and b.bookingDate >= :today
""")
    Long countFreeActiveByUserId(@Param("userId") Long userId,
                               @Param("confirmed") BookingStatus confirmed,
                               @Param("today")LocalDate today);

    @Query("""
           select count(b) from Booking b
           where b.phoneNumber = :phone
           and b.enable = true
           and b.bookingStatus = :confirmed
           and b.totalAmount = 0
           and b.bookingDate >= :today
""")
    long countFreeActiveByPhone(@Param("phone") String phone,
                                @Param("confirmed") BookingStatus confirmed,
                                @Param("today")LocalDate today);

    @Query(value = "select pg_advisory_xact_lock(:userId)",nativeQuery = true)
    void lockFreeBookingUser(@Param("userId") Long userId);

    @Query(value = "select pg_advisory_xact_lock(:phoneId)", nativeQuery = true)
    void lockFreeBookingPhone(@Param("phoneId") Long phoneId);
}

